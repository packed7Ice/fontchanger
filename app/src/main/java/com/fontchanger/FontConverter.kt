package com.fontchanger

object FontConverter {

    fun convert(input: String, style: FontStyle): String {
        if (style == FontStyle.UPSIDE_DOWN) {
            return convertUpsideDown(input)
        }
        return buildString {
            for (char in input) {
                append(convertChar(char, style))
            }
        }
    }

    private fun convertChar(char: Char, style: FontStyle): String {
        return when (style) {
            FontStyle.SMALL_CAPS -> convertSmallCaps(char)
            FontStyle.UPSIDE_DOWN -> convertUpsideDownChar(char)
            else -> {
                val mapping = offsetMappings[style] ?: return char.toString()
                when {
                    char in 'A'..'Z' -> {
                        val index = char - 'A'
                        val cp = mapping.upperExceptions[index] ?: (mapping.upperStart + index)
                        cp.toUnicodeString()
                    }
                    char in 'a'..'z' -> {
                        val index = char - 'a'
                        val cp = mapping.lowerExceptions[index] ?: (mapping.lowerStart + index)
                        cp.toUnicodeString()
                    }
                    else -> char.toString()
                }
            }
        }
    }

    private fun Int.toUnicodeString(): String = String(Character.toChars(this))

    private fun convertSmallCaps(char: Char): String {
        return when {
            char in 'A'..'Z' -> char.toString()
            char in 'a'..'z' -> {
                smallCapsLower[char - 'a']?.toUnicodeString() ?: char.toString()
            }
            else -> char.toString()
        }
    }

    private fun convertUpsideDown(input: String): String {
        return buildString {
            for (char in input.reversed()) {
                append(convertUpsideDownChar(char))
            }
        }
    }

    private fun convertUpsideDownChar(char: Char): String {
        return when (char) {
            'M' -> "W"; 'W' -> "M"
            'b' -> "q"; 'd' -> "p"; 'n' -> "u"; 'p' -> "d"; 'q' -> "b"; 'u' -> "n"
            in 'A'..'Z' -> upsideDownUpper[char - 'A']?.toUnicodeString() ?: char.toString()
            in 'a'..'z' -> upsideDownLower[char - 'a']?.toUnicodeString() ?: char.toString()
            else -> char.toString()
        }
    }

    private data class OffsetMapping(
        val upperStart: Int,
        val lowerStart: Int,
        val upperExceptions: Map<Int, Int> = emptyMap(),
        val lowerExceptions: Map<Int, Int> = emptyMap(),
    )

    private val offsetMappings = mapOf(
        FontStyle.BOLD to OffsetMapping(0x1D400, 0x1D41A),
        FontStyle.ITALIC to OffsetMapping(
            0x1D434, 0x1D44E,
            lowerExceptions = mapOf(7 to 0x210E)
        ),
        FontStyle.BOLD_ITALIC to OffsetMapping(0x1D468, 0x1D482),
        FontStyle.SANS_SERIF to OffsetMapping(0x1D5A0, 0x1D5BA),
        FontStyle.SANS_SERIF_BOLD to OffsetMapping(0x1D5D4, 0x1D5EE),
        FontStyle.SANS_SERIF_ITALIC to OffsetMapping(0x1D608, 0x1D622),
        FontStyle.SANS_SERIF_BOLD_ITALIC to OffsetMapping(0x1D63C, 0x1D656),
        FontStyle.SCRIPT to OffsetMapping(
            0x1D49C, 0x1D4B6,
            upperExceptions = mapOf(
                1 to 0x212C, 4 to 0x2130, 5 to 0x2131,
                7 to 0x210B, 8 to 0x2110, 11 to 0x2112,
                12 to 0x2133, 17 to 0x211B
            ),
            lowerExceptions = mapOf(4 to 0x212F, 6 to 0x210A, 14 to 0x2134)
        ),
        FontStyle.BOLD_SCRIPT to OffsetMapping(0x1D4D0, 0x1D4EA),
        FontStyle.FRAKTUR to OffsetMapping(
            0x1D504, 0x1D51E,
            upperExceptions = mapOf(
                2 to 0x212D, 7 to 0x210C, 8 to 0x2111,
                17 to 0x211C, 25 to 0x2128
            )
        ),
        FontStyle.BOLD_FRAKTUR to OffsetMapping(0x1D56C, 0x1D586),
        FontStyle.DOUBLE_STRUCK to OffsetMapping(
            0x1D538, 0x1D552,
            upperExceptions = mapOf(
                2 to 0x2102, 7 to 0x210D, 13 to 0x2115,
                15 to 0x2119, 16 to 0x211A, 17 to 0x211D, 25 to 0x2124
            )
        ),
        FontStyle.MONOSPACE to OffsetMapping(0x1D670, 0x1D68A),
        FontStyle.CIRCLED to OffsetMapping(0x24B6, 0x24D0),
        FontStyle.SQUARED to OffsetMapping(0x1F130, 0x1F130),
    )

    private val smallCapsLower = mapOf(
        0 to 0x1D00, 1 to 0x0299, 2 to 0x1D04, 3 to 0x1D05,
        4 to 0x1D07, 5 to 0xA730, 6 to 0x0262, 7 to 0x029C,
        8 to 0x026A, 9 to 0x1D0A, 10 to 0x1D0B, 11 to 0x029F,
        12 to 0x1D0D, 13 to 0x0274, 14 to 0x1D0F, 15 to 0x1D18,
        16 to 0x01EB, 17 to 0x0280, 18 to 0xA731, 19 to 0x1D1B,
        20 to 0x1D1C, 21 to 0x1D20, 22 to 0x1D21,
        24 to 0x028F, 25 to 0x1D22
    )

    private val upsideDownUpper = mapOf(
        0 to 0x2200, 1 to 0x15FA, 2 to 0x0186, 3 to 0x15E1,
        4 to 0x018E, 5 to 0x2132, 6 to 0x2141,
        9 to 0x017F, 11 to 0x02E5,
        15 to 0x0500, 17 to 0x1D1A, 19 to 0x22A5,
        20 to 0x2229, 21 to 0x039B, 24 to 0x2144
    )

    private val upsideDownLower = mapOf(
        0 to 0x0250, 2 to 0x0254, 4 to 0x01DD, 5 to 0x025F,
        6 to 0x0183, 7 to 0x0265, 8 to 0x1D09, 9 to 0x027E,
        10 to 0x029E, 12 to 0x026F, 17 to 0x0279,
        19 to 0x0287, 21 to 0x028C, 22 to 0x028D, 24 to 0x028E
    )
}
