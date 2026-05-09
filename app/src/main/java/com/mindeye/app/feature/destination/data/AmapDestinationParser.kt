package com.mindeye.app.feature.destination.data

import android.content.Context
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItem
import com.amap.api.services.core.PoiResult
import com.amap.api.services.core.PoiSearch
import com.mindeye.app.feature.destination.model.DestinationCandidate
import com.mindeye.app.feature.destination.model.DestinationParseResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking

/**
 * 高德地图 POI 搜索目的地解析实现
 */
class AmapDestinationParser(
    private val context: Context
) : DestinationParser {

    override fun parse(input: String): DestinationParseResult {
        return try {
            runBlockingSearch(input)
        } catch (e: Exception) {
            DestinationParseResult.Failed("目的地解析失败：${e.message ?: "未知错误"}")
        }
    }

    private fun runBlockingSearch(input: String): DestinationParseResult {
        val deferred = CompletableDeferred<DestinationParseResult>()

        val query = PoiSearch.Query(input, "", "")
        query.pageSize = 10
        query.setCityLimit(false)

        val poiSearch = PoiSearch(context, query)

        poiSearch.setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {
            override fun onPoiSearched(result: PoiResult?, rCode: Int) {
                if (!deferred.isActive) return

                try {
                    if (rCode != 1000 || result == null || result.pois.isNullOrEmpty()) {
                        deferred.complete(
                            DestinationParseResult.Failed("没有找到匹配的目的地，请换个说法试试")
                        )
                        return
                    }

                    val pois = result.pois
                    val candidates = pois.map { it.toCandidate() }

                    when {
                        candidates.size == 1 -> {
                            deferred.complete(DestinationParseResult.Success(candidates.first()))
                        }

                        candidates.isNotEmpty() -> {
                            deferred.complete(
                                DestinationParseResult.Ambiguous(
                                    query = input,
                                    options = candidates.take(5)
                                )
                            )
                        }

                        else -> {
                            deferred.complete(
                                DestinationParseResult.Failed("没有找到匹配的目的地，请换个说法试试")
                            )
                        }
                    }
                } catch (e: Exception) {
                    deferred.complete(
                        DestinationParseResult.Failed("解析结果处理失败：${e.message ?: "未知错误"}")
                    )
                }
            }

            override fun onPoiItemSearched(p0: PoiItem?, p1: Int) {}
        })

        poiSearch.searchPOIAsyn()

        // 阻塞等待结果（为了兼容现有接口，实际建议改为 suspend）
        return runBlocking {
            try {
                // 设置 10 秒超时
                kotlinx.coroutines.withTimeout(10_000L) {
                    deferred.await()
                }
            } catch (e: Exception) {
                DestinationParseResult.Failed("POI 搜索超时，请稍后重试")
            }
        }
    }

    private fun PoiItem.toCandidate(): DestinationCandidate {
        val point: LatLonPoint? = latLonPoint
        return DestinationCandidate(
            city = cityName,
            district = adName,
            poiName = title,
            addressDetail = snippet,
            latitude = point?.latitude,
            longitude = point?.longitude
        )
    }
}
