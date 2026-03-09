package com.fontchanger

enum class FontStyle(
    val displayName: String,
    val preview: String
) {
    BOLD("Bold", "\uD835\uDC00\uD835\uDC01\uD835\uDC02"),
    ITALIC("Italic", "\uD835\uDC34\uD835\uDC35\uD835\uDC36"),
    BOLD_ITALIC("Bold Italic", "\uD835\uDC68\uD835\uDC69\uD835\uDC6A"),
    SANS_SERIF("Sans-Serif", "\uD835\uDDA0\uD835\uDDA1\uD835\uDDA2"),
    SANS_SERIF_BOLD("Sans-Serif Bold", "\uD835\uDDD4\uD835\uDDD5\uD835\uDDD6"),
    SANS_SERIF_ITALIC("Sans-Serif Italic", "\uD835\uDE08\uD835\uDE09\uD835\uDE0A"),
    SANS_SERIF_BOLD_ITALIC("Sans-Serif Bold Italic", "\uD835\uDE3C\uD835\uDE3D\uD835\uDE3E"),
    SCRIPT("Script", "\uD835\uDC9C\u212C\uD835\uDC9E"),
    BOLD_SCRIPT("Bold Script", "\uD835\uDCD0\uD835\uDCD1\uD835\uDCD2"),
    FRAKTUR("Fraktur", "\uD835\uDD04\uD835\uDD05\u212D"),
    BOLD_FRAKTUR("Bold Fraktur", "\uD835\uDD6C\uD835\uDD6D\uD835\uDD6E"),
    DOUBLE_STRUCK("Double-Struck", "\uD835\uDD38\uD835\uDD39\u2102"),
    MONOSPACE("Monospace", "\uD835\uDE70\uD835\uDE71\uD835\uDE72"),
    CIRCLED("Circled", "\u24B6\u24B7\u24B8"),
    SQUARED("Squared", "\uD83C\uDD30\uD83C\uDD31\uD83C\uDD32"),
    SMALL_CAPS("Small Caps", "\u1D00\u0299\u1D04"),
    UPSIDE_DOWN("Upside Down", "\u0250q\u0254");
}
