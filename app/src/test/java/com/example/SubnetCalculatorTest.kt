package com.example

import com.example.util.SubnetCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubnetCalculatorTest {

    @Test
    fun testIPv4Details() {
        val details = SubnetCalculator.calculate("192.168.1.50", 24)
        
        assertEquals("192.168.1.0", details.networkAddress)
        assertEquals("192.168.1.255", details.broadcastAddress)
        assertEquals("192.168.1.1", details.firstUsable)
        assertEquals("192.168.1.254", details.lastUsable)
        assertEquals("254", details.totalUsable)
        assertEquals("255.255.255.0", details.subnetMask)
        assertEquals("0.0.0.255", details.wildcardMask)
        assertFalse(details.isIpv6)
    }

    @Test
    fun testIPv4Split() {
        val splits = SubnetCalculator.splitSubnetIpv4("192.168.1.0", 24, 4)
        assertEquals(4, splits.size)
        assertEquals("192.168.1.0", splits[0].first)
        assertEquals(26, splits[0].second)
        assertEquals("192.168.1.64", splits[1].first)
        assertEquals("192.168.1.128", splits[2].first)
        assertEquals("192.168.1.192", splits[3].first)
    }
}
