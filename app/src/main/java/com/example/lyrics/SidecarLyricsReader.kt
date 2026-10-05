package com.example.lyrics

import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

class SidecarLyricsReader {

    companion object {
        private const val MAX_SIDECAR_BYTES = 1_048_576L
    }

    fun findSidecar(audioFile: File): File? {
        val parent = audioFile.parentFile ?: return null
        val base = audioFile.nameWithoutExtension

        val lrc = File(parent, "$base.lrc")
        if (lrc.isFile && lrc.canRead()) return lrc

        val txt = File(parent, "$base.txt")
        if (txt.isFile && txt.canRead()) return txt

        return null
    }

    fun read(songId: Long, audioPath: String): LyricsDocument? {
        val audioFile = File(audioPath)
        val sidecar = findSidecar(audioFile) ?: return null

        if (sidecar.length() > MAX_SIDECAR_BYTES) return null

        return try {
            val content = decode(sidecar.readBytes())
            val parsed = LrcParser.parse(content)

            when (parsed) {
                is LyricsContent.Synced -> LyricsDocument(
                    songId = songId,
                    source = if (sidecar.extension.equals("lrc", true)) {
                        LyricsSource.SIDECAR_LRC
                    } else {
                        LyricsSource.SIDECAR_TEXT
                    },
                    content = parsed
                )

                is LyricsContent.Plain -> LyricsDocument(
                    songId = songId,
                    source = LyricsSource.SIDECAR_TEXT,
                    content = parsed
                )

                LyricsContent.None -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun decode(bytes: ByteArray): String {
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) {
            return bytes.copyOfRange(3, bytes.size).toString(StandardCharsets.UTF_8)
        }

        if (bytes.size >= 2 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xFE.toByte()
        ) {
            return bytes.copyOfRange(2, bytes.size).toString(Charsets.UTF_16LE)
        }

        if (bytes.size >= 2 &&
            bytes[0] == 0xFE.toByte() &&
            bytes[1] == 0xFF.toByte()
        ) {
            return bytes.copyOfRange(2, bytes.size).toString(Charsets.UTF_16BE)
        }

        return try {
            val decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)

            decoder.decode(ByteBuffer.wrap(bytes)).toString()
        } catch (_: CharacterCodingException) {
            Charset.forName("windows-1252").decode(ByteBuffer.wrap(bytes)).toString()
        }
    }
}
