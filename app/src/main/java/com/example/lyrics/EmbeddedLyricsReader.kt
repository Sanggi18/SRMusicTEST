package com.example.lyrics

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.id3.AbstractID3v2Frame
import org.jaudiotagger.tag.id3.AbstractID3v2Tag
import org.jaudiotagger.tag.id3.framebody.FrameBodySYLT
import java.io.File

class EmbeddedLyricsReader {

    companion object {
        private const val SYLT_TIMESTAMP_MILLISECONDS = 0x02
    }

    fun read(songId: Long, dataPath: String): LyricsDocument? {
        val file = File(dataPath)
        if (!file.isFile || !file.canRead()) return null

        return try {
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag ?: return null

            // 1. Native synchronized ID3 lyrics first when available.
            readSylt(songId, tag)?.let { return it }

            // 2. Generic lyrics text. It may contain LRC markup.
            val rawLyrics = try {
                tag.getFirst(FieldKey.LYRICS)
            } catch (_: Exception) {
                ""
            }

            if (rawLyrics.isNullOrBlank()) return null

            val parsed = LrcParser.parse(rawLyrics)
            when (parsed) {
                is LyricsContent.Synced -> LyricsDocument(
                    songId = songId,
                    source = LyricsSource.EMBEDDED_LYRICS,
                    content = parsed
                )

                is LyricsContent.Plain -> LyricsDocument(
                    songId = songId,
                    source = LyricsSource.EMBEDDED_LYRICS,
                    content = parsed
                )

                LyricsContent.None -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun readSylt(songId: Long, tag: org.jaudiotagger.tag.Tag): LyricsDocument? {
        val id3Tag = tag as? AbstractID3v2Tag ?: return null
        val frameObject = try {
            id3Tag.getFrame("SYLT")
        } catch (_: Exception) {
            null
        } ?: return null

        val frames = when (frameObject) {
            is AbstractID3v2Frame -> listOf(frameObject)
            is List<*> -> frameObject.filterIsInstance<AbstractID3v2Frame>()
            else -> emptyList()
        }

        for (frame in frames) {
            val body = frame.body as? FrameBodySYLT ?: continue
            if (body.contentType != 1) continue
            if (body.timeStampFormat != SYLT_TIMESTAMP_MILLISECONDS) continue

            val rawBytes = body.lyrics ?: continue
            if (rawBytes.isEmpty()) continue

            return try {
                val charset = when (body.textEncoding.toInt()) {
                    1 -> java.nio.charset.StandardCharsets.UTF_16
                    2 -> java.nio.charset.StandardCharsets.UTF_16BE
                    3 -> java.nio.charset.StandardCharsets.UTF_8
                    else -> java.nio.charset.StandardCharsets.ISO_8859_1
                }
                val isWide = body.textEncoding.toInt() == 1 || body.textEncoding.toInt() == 2

                val lines = mutableListOf<LyricLine>()
                var offset = 0
                val length = rawBytes.size
                var currentLineText = StringBuilder()
                var currentLineStartMs: Long? = null
                var currentWords = mutableListOf<LyricWord>()

                while (offset < length) {
                    val strStart = offset
                    if (isWide) {
                        while (offset + 1 < length && !(rawBytes[offset] == 0.toByte() && rawBytes[offset + 1] == 0.toByte())) {
                            offset += 2
                        }
                    } else {
                        while (offset < length && rawBytes[offset] != 0.toByte()) {
                            offset++
                        }
                    }

                    val wordText = if (offset > strStart) {
                        try {
                            String(rawBytes, strStart, offset - strStart, charset)
                        } catch (_: Exception) {
                            String(rawBytes, strStart, offset - strStart, java.nio.charset.StandardCharsets.ISO_8859_1)
                        }
                    } else ""

                    if (isWide) {
                        if (offset + 1 < length && rawBytes[offset] == 0.toByte() && rawBytes[offset + 1] == 0.toByte()) {
                            offset += 2
                        }
                    } else {
                        if (offset < length && rawBytes[offset] == 0.toByte()) {
                            offset++
                        }
                    }

                    if (offset + 4 <= length) {
                        val b0 = rawBytes[offset].toLong() and 0xFF
                        val b1 = rawBytes[offset + 1].toLong() and 0xFF
                        val b2 = rawBytes[offset + 2].toLong() and 0xFF
                        val b3 = rawBytes[offset + 3].toLong() and 0xFF
                        val timestamp = (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
                        offset += 4

                        if (currentLineStartMs == null) {
                            currentLineStartMs = timestamp
                        }

                        if (wordText.contains('\n')) {
                            val parts = wordText.split('\n')
                            for ((idx, part) in parts.withIndex()) {
                                if (part.isNotEmpty()) {
                                    currentLineText.append(part)
                                    currentWords += LyricWord(timestamp, part)
                                }
                                if (idx < parts.lastIndex) {
                                    val t = currentLineText.toString().trim()
                                    if (t.isNotBlank()) {
                                        lines += LyricLine(currentLineStartMs ?: timestamp, t, currentWords.toList())
                                    }
                                    currentLineText = StringBuilder()
                                    currentWords = mutableListOf()
                                    currentLineStartMs = null
                                }
                            }
                        } else if (wordText.isNotBlank()) {
                            currentLineText.append(wordText)
                            currentWords += LyricWord(timestamp, wordText)
                        }
                    } else {
                        break
                    }
                }

                val finalText = currentLineText.toString().trim()
                if (finalText.isNotBlank()) {
                    lines += LyricLine(currentLineStartMs ?: 0L, finalText, currentWords.toList())
                }

                val normalized = lines
                    .sortedBy { it.startMs }
                    .filter { it.text.isNotBlank() }

                if (normalized.isNotEmpty()) {
                    LyricsDocument(
                        songId = songId,
                        source = LyricsSource.EMBEDDED_SYLT,
                        content = LyricsContent.Synced(normalized)
                    )
                } else null
            } catch (_: Exception) {
                null
            }
        }

        return null
    }
}
