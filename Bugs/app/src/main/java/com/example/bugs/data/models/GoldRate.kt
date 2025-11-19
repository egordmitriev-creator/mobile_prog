package com.example.bugs.data.models

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "ValCurs", strict = false)
data class ValCurs @JvmOverloads constructor(
    @field:ElementList(inline = true, required = false)
    var valutes: List<Valute>? = null
)

@Root(name = "Valute", strict = false)
data class Valute @JvmOverloads constructor(
    @field:Element(name = "ID", required = false)
    var id: String? = null,

    @field:Element(name = "NumCode", required = false)
    var numCode: String? = null,

    @field:Element(name = "CharCode", required = false)
    var charCode: String? = null,

    @field:Element(name = "Nominal", required = false)
    var nominal: Int = 0,

    @field:Element(name = "Name", required = false)
    var name: String? = null,

    @field:Element(name = "Value", required = false)
    var value: String? = null
) {
    fun getValueAsFloat(): Float {
        return value?.replace(",", ".")?.toFloatOrNull() ?: 0f
    }
}