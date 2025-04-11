package com.abk.utils

import android.graphics.Bitmap

object PrintUtil {
    const val MAX_BIT_WIDTH: Int = 384

    fun getBitmapData(bm: Bitmap): ByteArray {
        var w = bm.getWidth()
        val h = bm.getHeight()
        if (w > MAX_BIT_WIDTH) w = MAX_BIT_WIDTH
        val bitW = getPaddingBitWidth(w)
        val pitch = bitW / 8
        val bits = ByteArray(pitch * h)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val color = bm.getPixel(x, y)
                if ((color and 0xFF) < 128) {
                    bits[y * pitch + x / 8] = (bits[y * pitch + x / 8].toInt() or (0x80 shr (x % 8))).toByte()
                }
            }
        }
        return bits
    }

    fun getEmptyData(width: Int, height: Int): ByteArray? {
        var w = width
        if (w > MAX_BIT_WIDTH) w = MAX_BIT_WIDTH
        val bitW = getPaddingBitWidth(w)
        val pitch = bitW / 8
        return ByteArray(pitch * height)
    }

    fun getPaddingBitWidth(width: Int): Int {
        return ((width + 7) / 8) * 8
    }
}
