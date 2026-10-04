package com.pemmob.mbkmybacaankomik.data.remote

import okhttp3.Dns
import java.net.InetAddress

/**
 * Custom OkHttp DNS Resolver untuk MangaDex.
 *
 * Di beberapa jaringan (khususnya ISP di Indonesia), domain `api.mangadex.org`
 * dan `uploads.mangadex.org` kerap mengalami DNS poisoning / dialihkan ke aduankonten.id.
 * Resolver ini memetakan domain MangaDex langsung ke IP resmi MangaDex (dxlb.mangadex.org: 45.129.229.1, 45.129.229.2)
 * dan meneruskan domain lainnya ke System DNS.
 */
class MangaDexDns : Dns {

    private val mangaDexIps = listOf(
        InetAddress.getByName("45.129.229.1"),
        InetAddress.getByName("45.129.229.2")
    )

    override fun lookup(hostname: String): List<InetAddress> {
        return when (hostname) {
            "api.mangadex.org", "uploads.mangadex.org" -> mangaDexIps
            else -> {
                try {
                    val systemAddresses = Dns.SYSTEM.lookup(hostname)
                    // Jika ISP membajak domain mangadex.network ke IP lokal/blokir
                    if (hostname.endsWith("mangadex.org") && systemAddresses.any { isPrivateOrBlockedIp(it) }) {
                        mangaDexIps
                    } else {
                        systemAddresses
                    }
                } catch (e: Exception) {
                    if (hostname.endsWith("mangadex.org")) {
                        mangaDexIps
                    } else {
                        throw e
                    }
                }
            }
        }
    }

    private fun isPrivateOrBlockedIp(address: InetAddress): Boolean {
        val host = address.hostAddress ?: return false
        return host.startsWith("10.") ||
                host.startsWith("192.168.") ||
                host.startsWith("36.86.") // IP umum ISP block landing page
    }
}
