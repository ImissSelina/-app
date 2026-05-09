package com.mindeye.app.feature.destination.data

import com.mindeye.app.feature.destination.model.DestinationCandidate
import com.mindeye.app.feature.destination.model.DestinationParseResult

/**
 * 基础规则版目的地解析器实现
 * 后续可替换为地图 SDK 的 POI 检索
 */
class MockDestinationParser : DestinationParser {

    override fun parse(input: String): DestinationParseResult {
        val text = input.trim()

        if (text.isBlank()) {
            return DestinationParseResult.Failed("不好意思，我没听清，请再说一遍目的地好吗？")
        }

        // 简单纠错/修正句式识别（例如：“不是人民医院，是中心医院”）
        if (text.contains("不是") && text.contains("是")) {
            val corrected = text.substringAfterLast("是").trim()
            if (corrected.isNotBlank()) {
                return DestinationParseResult.Success(
                    DestinationCandidate(
                        poiName = corrected,
                        addressDetail = null
                    )
                )
            }
        }

        // 简单关键字匹配模拟
        val poiKeywords = listOf("医院", "地铁站", "火车站", "学校", "商场", "公园", "大厦", "中心")
        val matched = poiKeywords.firstOrNull { text.contains(it) }

        // 模拟歧义场景：如果只说“医院”
        if (text == "医院") {
            return DestinationParseResult.Ambiguous(
                query = text,
                options = listOf(
                    DestinationCandidate(poiName = "人民医院东门", addressDetail = "XX路1号"),
                    DestinationCandidate(poiName = "中心医院北院区", addressDetail = "YY路2号")
                )
            )
        }

        if (matched != null) {
            return DestinationParseResult.Success(
                DestinationCandidate(
                    poiName = text,
                    addressDetail = "模拟解析出的详细地址"
                )
            )
        }

        // 地址过短或不明确
        if (text.length <= 2) {
            return DestinationParseResult.NeedMoreInfo("你说的信息太少了，请补充城市、街道或具体地点名称。")
        }

        // 默认解析成功
        return DestinationParseResult.Success(
            DestinationCandidate(
                poiName = text,
                addressDetail = null
            )
        )
    }
}
