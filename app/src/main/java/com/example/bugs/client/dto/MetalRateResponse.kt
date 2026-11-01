package com.example.bugs.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "Metall")
data class MetalRateResponse(
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "Record")
    var records: List<MetalRateRecord> = emptyList()
)

data class MetalRateRecord(
    @JacksonXmlProperty(localName = "Buy")
    var buy: String? = null,

    @JacksonXmlProperty(localName = "Sell")
    var sell: String? = null,

    @JacksonXmlProperty(isAttribute = true, localName = "Code")
    var code: String? = null,

    @JacksonXmlProperty(isAttribute = true, localName = "Date")
    var date: String? = null
)