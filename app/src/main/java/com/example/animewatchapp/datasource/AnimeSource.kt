package com.example.animewatchapp.datasource

import android.util.Log
import com.example.animewatchapp.model.Anime
import com.example.animewatchapp.model.AnimeDetails
import com.example.animewatchapp.utils.Utils.getJsoup
import com.example.animewatchapp.utils.Utils.postJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class AnimeSource {
    private val mainUrl = "https://yugenanime.tv"

    suspend fun latestAnime(page: Int): ArrayList<Anime> =
        withContext(Dispatchers.IO) {
            val animeList = ArrayList<Anime>()
            val doc = getJsoup("$mainUrl/latest/?page=$page")
            val allInfo = doc.getElementsByClass("ep-card")
            for (item in allInfo) {
                val itemImage = item.getElementsByTag("img").attr("data-src")
                val itemName = item.getElementsByClass("ep-title").text()
                val itemLink = item.getElementsByClass("ep-title").attr("href")
                animeList.add(
                    Anime(
                        animeName =  itemName,
                        animeImageURL =  itemImage,
                        animeLink = itemLink
                    )
                )
            }
            animeList
        }

    suspend fun trendingAnime(): ArrayList<Anime> =
        withContext(Dispatchers.IO) {
            val animeList = ArrayList<Anime>()
            val doc = getJsoup("$mainUrl/trending")
            val allInfo = doc.getElementsByClass("series-item")
            for (item in allInfo) {
                val itemImage = item.getElementsByTag("img").attr("src")
                val itemName = item.getElementsByClass("series-title").text()
                val itemLink = item.attr("href")
                animeList.add(
                    Anime(
                        animeName =  itemName,
                        animeImageURL =  itemImage,
                        animeLink = itemLink
                    )
                )
            }
            animeList
        }

    suspend fun searchAnime(searchedText: String): ArrayList<Anime> =
        withContext(Dispatchers.IO) {
            val animeList = ArrayList<Anime>()
            val searchUrl = "$mainUrl/discover/?q=${searchedText}"
            val doc = getJsoup(searchUrl)
            val allInfo = doc.getElementsByClass("anime-meta")
            for (item in allInfo) {
                val itemImage = item.getElementsByTag("img").attr("data-src")
                val itemName = item.getElementsByClass("anime-name").text()
                val itemLink = item.attr("href")
                animeList.add(
                    Anime(
                        animeName =  itemName,
                        animeImageURL =  itemImage,
                        animeLink = itemLink
                    )
                )
            }
            animeList
        }

    suspend fun animeDetails(contentLink: String): AnimeDetails =
        withContext(Dispatchers.IO) {
            val url = "$mainUrl${contentLink}watch/?sort=episode"
            val doc = getJsoup(url)
            val animeContent = doc.getElementsByClass("p-10-t")
            val animeCover =
                doc.getElementsByClass("page-cover-inner").first()!!.getElementsByTag("img")
                    .attr("src")
            val animeName = animeContent.first()!!.text()
            val animDesc = animeContent[1].text()

            val subsEpCount = doc.getElementsByClass("box p-10 p-15 m-15-b anime-metadetails")
                .select("div:nth-child(6)").select("span").text()
            val epList = (1..subsEpCount.toInt()).toList()

            AnimeDetails(
                animeName,
                animDesc,
                animeCover,
                epList
            )
        }

    suspend fun animeLink(
        animeUrl: String,
        animeEpisode: String
    ): String =
        withContext(Dispatchers.IO) {
            val watchLink = animeUrl.replace("anime", "watch")

            val animeEpUrl = "$mainUrl$watchLink$animeEpisode"

            var embedLink = getJsoup(animeEpUrl).getElementById("main-embed")!!.attr("src")
            if (!embedLink.contains("https:")) embedLink = "https:$embedLink"

            val mapOfHeaders = mutableMapOf(
                "X-Requested-With" to "XMLHttpRequest",
                "content-type" to "application/x-www-form-urlencoded; charset=UTF-8"
            )

            val apiRequest = "$mainUrl/api/embed/"
            val id = embedLink.split("/")
            val dataMap = mapOf("id" to id[id.size - 2], "ac" to "0")

            val linkDetails = postJson(apiRequest, mapOfHeaders, dataMap)!!.asJsonObject
            val link = linkDetails["hls"].asJsonArray.first().asString
            Log.d("LINK ANIME", link)
            link
        }
}