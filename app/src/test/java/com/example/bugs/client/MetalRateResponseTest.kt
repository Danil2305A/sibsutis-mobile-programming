package com.example.bugs.client

import com.example.bugs.client.dto.MetalRateResponse
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MetalRateResponseTest {

    private val mapper = XmlMapper().registerKotlinModule()

    @Test
    fun attributesVsElements() {
        val xml = """
            <Metall>
              <Record Date="01/01/2025" Code="1">
                <Buy>5000,00</Buy>
                <Sell>5100,00</Sell>
              </Record>
            </Metall>
        """.trimIndent()

        val r = mapper.readValue(xml, MetalRateResponse::class.java).records.single()
        assertEquals("1", r.code)
        assertEquals("01/01/2025", r.date)
        assertEquals("5000,00", r.buy)
        assertEquals("5100,00", r.sell)
    }

    @Test
    fun multipleRecords() {
        val xml = """
            <Metall>
              <Record Date="01/01/2025" Code="1"><Buy>1</Buy><Sell>2</Sell></Record>
              <Record Date="02/01/2025" Code="2"><Buy>3</Buy><Sell>4</Sell></Record>
            </Metall>
        """.trimIndent()
        assertEquals(2, mapper.readValue(xml, MetalRateResponse::class.java).records.size)
    }

    @Test
    fun emptyMetallProducesEmptyList() {
        assertEquals(0, mapper.readValue("<Metall/>", MetalRateResponse::class.java).records.size)
    }

    @Test
    fun missingBuySellBecomeNull() {
        val xml = """<Metall><Record Date="x" Code="1"/></Metall>"""
        val r = mapper.readValue(xml, MetalRateResponse::class.java).records.single()
        assertNull(r.buy)
        assertNull(r.sell)
    }

    @Test
    fun unknownAttributesIgnored() {
        val xml = """<Metall><Record Code="1" Foo="bar"><Buy>1</Buy></Record></Metall>"""
        assertEquals("1", mapper.readValue(xml, MetalRateResponse::class.java).records.single().buy)
    }
}