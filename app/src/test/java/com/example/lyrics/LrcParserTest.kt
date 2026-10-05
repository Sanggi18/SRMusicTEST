package com.example.lyrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {

    @Test
    fun parsesSingleTimestamp() {
        val lrc = "[00:12.34]Hello World"
        val parsed = LrcParser.parse(lrc)

        assertTrue(parsed is LyricsContent.Synced)
        val synced = parsed as LyricsContent.Synced
        assertEquals(1, synced.lines.size)
        assertEquals(12340L, synced.lines[0].startMs)
        assertEquals("Hello World", synced.lines[0].text)
    }

    @Test
    fun parsesMultipleTimestamps() {
        val lrc = "[00:10.00][00:20.00]Chorus line"
        val parsed = LrcParser.parse(lrc)

        assertTrue(parsed is LyricsContent.Synced)
        val synced = parsed as LyricsContent.Synced
        assertEquals(2, synced.lines.size)
        assertEquals(10000L, synced.lines[0].startMs)
        assertEquals(20000L, synced.lines[1].startMs)
        assertEquals("Chorus line", synced.lines[0].text)
        assertEquals("Chorus line", synced.lines[1].text)
    }

    @Test
    fun parsesTimestampWithoutFraction() {
        val lrc = "[01:05]One minute five seconds"
        val parsed = LrcParser.parse(lrc)

        assertTrue(parsed is LyricsContent.Synced)
        val synced = parsed as LyricsContent.Synced
        assertEquals(65000L, synced.lines[0].startMs)
    }

    @Test
    fun parsesOneDigitFraction() {
        val lrc = "[00:03.5]Half second"
        val parsed = LrcParser.parse(lrc)

        assertTrue(parsed is LyricsContent.Synced)
        val synced = parsed as LyricsContent.Synced
        assertEquals(3500L, synced.lines[0].startMs)
    }

    @Test
    fun appliesPositiveOffset() {
        val lrc = """
            [offset:+500]
            [00:02.00]With offset
        """.trimIndent()
        val parsed = LrcParser.parse(lrc)

        assertTrue(parsed is LyricsContent.Synced)
        val synced = parsed as LyricsContent.Synced
        assertEquals(2500L, synced.lines[0].startMs)
    }

    @Test
    fun appliesNegativeOffset() {
        val lrc = """
            [offset:-500]
            [00:02.00]Early line
        """.trimIndent()
        val parsed = LrcParser.parse(lrc)

        assertTrue(parsed is LyricsContent.Synced)
        val synced = parsed as LyricsContent.Synced
        assertEquals(1500L, synced.lines[0].startMs)
    }

    @Test
    fun parsesWordTiming() {
        val lrc = "[00:10.00]<00:10.20>Hello <00:10.60>world"
        val parsed = LrcParser.parse(lrc)

        assertTrue(parsed is LyricsContent.Synced)
        val synced = parsed as LyricsContent.Synced
        assertEquals(1, synced.lines.size)
        assertEquals("Hello world", synced.lines[0].text)
        assertEquals(2, synced.lines[0].words.size)
        assertEquals("Hello ", synced.lines[0].words[0].text)
        assertEquals(10200L, synced.lines[0].words[0].startMs)
        assertEquals("world", synced.lines[0].words[1].text)
        assertEquals(10600L, synced.lines[0].words[1].startMs)
    }

    @Test
    fun ignoresMetadataTags() {
        val lrc = """
            [ar:Artist Name]
            [ti:Track Title]
            [al:Album Name]
            [00:05.00]Actual lyrics
        """.trimIndent()
        val parsed = LrcParser.parse(lrc)

        assertTrue(parsed is LyricsContent.Synced)
        val synced = parsed as LyricsContent.Synced
        assertEquals(1, synced.lines.size)
        assertEquals("Actual lyrics", synced.lines[0].text)
    }

    @Test
    fun returnsPlainWhenNoTimestampExists() {
        val text = "Just some text\nWithout timestamps\nLike a poem"
        val parsed = LrcParser.parse(text)

        assertTrue(parsed is LyricsContent.Plain)
        val plain = parsed as LyricsContent.Plain
        assertEquals(text, plain.text)
    }

    @Test
    fun returnsNoneForEmptyLyrics() {
        assertEquals(LyricsContent.None, LrcParser.parse(""))
        assertEquals(LyricsContent.None, LrcParser.parse("   \n\r  "))
    }

    @Test
    fun sortsOutOfOrderLines() {
        val lrc = """
            [00:10.00]Second line
            [00:02.00]First line
        """.trimIndent()
        val parsed = LrcParser.parse(lrc)

        assertTrue(parsed is LyricsContent.Synced)
        val synced = parsed as LyricsContent.Synced
        assertEquals(2000L, synced.lines[0].startMs)
        assertEquals(10000L, synced.lines[1].startMs)
    }

    @Test
    fun ignoresMalformedLinesWithoutDestroyingValidLyrics() {
        val lrc = """
            [00:01.00]Valid line
            [invalid_tag]
            [9999:9999] Weird
            [00:05.00]Another valid line
        """.trimIndent()
        val parsed = LrcParser.parse(lrc)

        assertTrue(parsed is LyricsContent.Synced)
        val synced = parsed as LyricsContent.Synced
        assertEquals(2, synced.lines.size)
        assertEquals("Valid line", synced.lines[0].text)
        assertEquals("Another valid line", synced.lines[1].text)
    }

    // Binary search indexAt tests
    @Test
    fun returnsMinusOneBeforeFirstLine() {
        val synced = LyricsContent.Synced(
            listOf(
                LyricLine(1000L, "First"),
                LyricLine(2000L, "Second")
            )
        )
        assertEquals(-1, synced.indexAt(500L))
        assertEquals(-1, synced.indexAt(-100L))
    }

    @Test
    fun returnsFirstLineAtExactTimestamp() {
        val synced = LyricsContent.Synced(
            listOf(
                LyricLine(1000L, "First"),
                LyricLine(2000L, "Second")
            )
        )
        assertEquals(0, synced.indexAt(1000L))
    }

    @Test
    fun returnsCurrentLineBetweenTimestamps() {
        val synced = LyricsContent.Synced(
            listOf(
                LyricLine(1000L, "First"),
                LyricLine(2000L, "Second"),
                LyricLine(3000L, "Third")
            )
        )
        assertEquals(0, synced.indexAt(1500L))
        assertEquals(1, synced.indexAt(2000L))
        assertEquals(1, synced.indexAt(2999L))
        assertEquals(2, synced.indexAt(3000L))
    }

    @Test
    fun returnsLastLineAfterLastTimestamp() {
        val synced = LyricsContent.Synced(
            listOf(
                LyricLine(1000L, "First"),
                LyricLine(2000L, "Second")
            )
        )
        assertEquals(1, synced.indexAt(50000L))
    }

    @Test
    fun handlesEmptyLyrics() {
        val synced = LyricsContent.Synced(emptyList())
        assertEquals(-1, synced.indexAt(1000L))
    }
}
