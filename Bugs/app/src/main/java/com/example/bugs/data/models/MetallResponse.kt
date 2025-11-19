package com.example.bugs.data.models

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import java.text.SimpleDateFormat
import java.util.*

@Root(name = "Metall", strict = false)
data class MetallResponse @JvmOverloads constructor(
    @field:ElementList(inline = true, required = false)
    var records: List<MetalRecord>? = null
)

@Root(name = "Record", strict = false)
data class MetalRecord @JvmOverloads constructor(
    @field:Element(name = "Buy", required = false)
    var buy: String? = null,

    @field:Element(name = "Sell", required = false)
    var sell: String? = null,

    @field:Element(name = "Date", required = false)
    var date: String? = null
) {
    // Получить цену продажи как число (в рублях за грамм)
    fun getSellPrice(): Float {
        return sell?.replace(",", ".")?.toFloatOrNull() ?: 0f
    }

    // Конвертировать в рубли за унцию (1 унция = 31.1035 грамм)
    fun getPricePerOunce(): Float {
        return getSellPrice() * 31.1035f
    }
}