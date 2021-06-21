package com.lalilu.common

class Mathf {
    companion object {
        fun <T : Number> clamp(min: T, max: T, num: T): T {
            if (num.toDouble() < min.toDouble()) return min
            if (num.toDouble() > max.toDouble()) return max
            return num
        }

        fun <T : Number> clampInLoop(min: T, max: T, num: T): T {
            if (num.toDouble() < min.toDouble()) return max
            if (num.toDouble() > max.toDouble()) return min
            return num
        }

        fun clampInLoop(min: Int, max: Int, num: Int, offset: Int): Int {
            if (num + offset < min) return max + (num + offset) + 1
            if (num + offset > max) return min + (num + offset - max) - 1
            return num + offset
        }
    }
}