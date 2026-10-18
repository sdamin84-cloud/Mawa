package com.example

import com.example.mawa.util.BengaliFordiParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BengaliFordiParserTest {

    @Test
    fun testFishWithPrice() {
        val result = BengaliFordiParser.parse("মাছ ২০০")
        assertEquals("মাছ", result.productName)
        assertEquals(200.0, result.estimatedRate, 0.001)
        assertEquals(1.0, result.quantity, 0.001)
    }

    @Test
    fun testOnionWithTwoKg() {
        val result = BengaliFordiParser.parse("পিয়াজ দুই কেজি")
        assertEquals("পিয়াজ", result.productName)
        assertEquals(2.0, result.quantity, 0.001)
        assertEquals("কেজি", result.unit)
    }

    @Test
    fun testRiceWithFiveKg() {
        val result = BengaliFordiParser.parse("৫ কেজি চাল")
        assertEquals("চাল", result.productName)
        assertEquals(5.0, result.quantity, 0.001)
        assertEquals("কেজি", result.unit)
    }

    @Test
    fun testEggsWithTwelvePieces() {
        val result = BengaliFordiParser.parse("১২টি ডিম")
        assertEquals("ডিম", result.productName)
        assertEquals(12.0, result.quantity, 0.001)
        assertEquals("টি", result.unit)
    }

    @Test
    fun testSoybeanOilWithTwoLiters() {
        val result = BengaliFordiParser.parse("সয়াবিন তেল ২ লিটার")
        assertEquals("সয়াবিন তেল", result.productName)
        assertEquals(2.0, result.quantity, 0.001)
        assertEquals("লিটার", result.unit)
    }

    @Test
    fun testPotatoWithFiftyTaka() {
        val result = BengaliFordiParser.parse("আলু ৫০ টাকার")
        assertEquals("আলু", result.productName)
        assertEquals(50.0, result.estimatedRate, 0.001)
        assertEquals(1.0, result.quantity, 0.001)
    }
}
