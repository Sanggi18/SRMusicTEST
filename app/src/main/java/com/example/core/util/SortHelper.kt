package com.example.core.util

object SortHelper {
    fun compareTitles(s1: String?, s2: String?): Int {
        val str1 = s1?.trim().orEmpty()
        val str2 = s2?.trim().orEmpty()
        return str1.compareTo(str2, ignoreCase = true)
    }

    fun compareArtists(a1: String?, a2: String?): Int {
        val str1 = a1?.trim().orEmpty()
        val str2 = a2?.trim().orEmpty()
        return str1.compareTo(str2, ignoreCase = true)
    }

    fun compareAlbums(a1: String?, a2: String?): Int {
        val str1 = a1?.trim().orEmpty()
        val str2 = a2?.trim().orEmpty()
        return str1.compareTo(str2, ignoreCase = true)
    }
}
