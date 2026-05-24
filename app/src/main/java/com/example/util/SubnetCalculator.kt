package com.example.util

import java.math.BigInteger
import java.net.InetAddress
import kotlin.math.pow

object SubnetCalculator {

    data class SubnetDetails(
        val networkAddress: String,
        val broadcastAddress: String,
        val firstUsable: String,
        val lastUsable: String,
        val totalUsable: String,
        val subnetMask: String,
        val wildcardMask: String,
        val cidr: Int,
        val isIpv6: Boolean
    )

    fun calculate(ipInput: String, prefixLength: Int): SubnetDetails {
        val addressStr = ipInput.trim()
        val isV6 = addressStr.contains(":")

        if (isV6) {
            return calculateIpv6(addressStr, prefixLength)
        } else {
            return calculateIpv4(addressStr, prefixLength)
        }
    }

    private fun calculateIpv4(ip: String, cidr: Int): SubnetDetails {
        val ipv4Int = ipToLong(ip)
        val mask = if (cidr == 0) 0L else (0xFFFFFFFFL shl (32 - cidr)) and 0xFFFFFFFFL
        val wildcard = mask.inv() and 0xFFFFFFFFL

        val network = ipv4Int and mask
        val broadcast = network or wildcard

        val first = if (cidr >= 31) network else network + 1
        val last = if (cidr >= 31) broadcast else broadcast - 1
        val total = if (cidr >= 31) 0 else (broadcast - network - 1).coerceAtLeast(0)

        return SubnetDetails(
            networkAddress = longToIp(network),
            broadcastAddress = longToIp(broadcast),
            firstUsable = longToIp(first),
            lastUsable = longToIp(last),
            totalUsable = total.toString(),
            subnetMask = longToIp(mask),
            wildcardMask = longToIp(wildcard),
            cidr = cidr,
            isIpv6 = false
        )
    }

    private fun calculateIpv6(ip: String, prefix: Int): SubnetDetails {
        val bytes = InetAddress.getByName(ip).address
        val ipBig = BigInteger(1, bytes)

        // Create mask
        var maskBig = BigInteger.ZERO
        for (i in 0 until 128) {
            if (i < prefix) {
                maskBig = maskBig.or(BigInteger.ONE.shiftLeft(127 - i))
            }
        }

        val networkBig = ipBig.and(maskBig)
        val wildcardBig = maskBig.xor(BigInteger.ONE.shiftLeft(128).minus(BigInteger.ONE))
        val broadcastBig = networkBig.or(wildcardBig)

        val firstBig = networkBig.add(BigInteger.ONE)
        val lastBig = broadcastBig.subtract(BigInteger.ONE)
        val totalBig = if (prefix >= 127) BigInteger.ZERO else broadcastBig.subtract(networkBig).subtract(BigInteger.ONE)

        return SubnetDetails(
            networkAddress = formatIpv6Big(networkBig),
            broadcastAddress = formatIpv6Big(broadcastBig),
            firstUsable = formatIpv6Big(firstBig),
            lastUsable = formatIpv6Big(lastBig),
            totalUsable = totalBig.toString(),
            subnetMask = formatIpv6Big(maskBig),
            wildcardMask = formatIpv6Big(wildcardBig),
            cidr = prefix,
            isIpv6 = true
        )
    }

    fun splitSubnetIpv4(ip: String, cidr: Int, subnetsCount: Int): List<Pair<String, Int>> {
        val ipv4Int = ipToLong(ip)
        val mask = if (cidr == 0) 0L else (0xFFFFFFFFL shl (32 - cidr)) and 0xFFFFFFFFL
        val baseNetwork = ipv4Int and mask

        val additionalBits = when {
            subnetsCount <= 1 -> 0
            subnetsCount <= 2 -> 1
            subnetsCount <= 4 -> 2
            subnetsCount <= 8 -> 3
            subnetsCount <= 16 -> 4
            subnetsCount <= 32 -> 5
            subnetsCount <= 64 -> 6
            else -> 8
        }

        val newCidr = (cidr + additionalBits).coerceAtMost(32)
        val size = 2.0.pow(32 - newCidr).toLong()

        val list = mutableListOf<Pair<String, Int>>()
        for (i in 0 until subnetsCount) {
            val netAddr = baseNetwork + i * size
            if (netAddr <= 0xFFFFFFFFL) {
                list.add(Pair(longToIp(netAddr), newCidr))
            }
        }
        return list
    }

    private fun ipToLong(ip: String): Long {
        val parts = ip.split(".").map { it.toLong() }
        if (parts.size != 4) return 0L
        return (parts[0] shl 24) or (parts[1] shl 16) or (parts[2] shl 8) or parts[3]
    }

    private fun longToIp(longIp: Long): String {
        return "${(longIp ushr 24) and 0xFF}.${(longIp ushr 16) and 0xFF}.${(longIp ushr 8) and 0xFF}.${longIp and 0xFF}"
    }

    private fun formatIpv6Big(big: BigInteger): String {
        val bytes = big.toByteArray()
        val fullBytes = ByteArray(16)
        val offset = 16 - bytes.size
        if (offset >= 0) {
            System.arraycopy(bytes, 0, fullBytes, offset, bytes.size)
        } else {
            System.arraycopy(bytes, -offset, fullBytes, 0, 16)
        }
        return InetAddress.getByAddress(fullBytes).hostAddress ?: "::"
    }
}
