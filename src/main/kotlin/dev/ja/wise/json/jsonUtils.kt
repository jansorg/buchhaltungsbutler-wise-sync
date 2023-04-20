package dev.ja.wise.json

import kotlinx.datetime.Instant

fun Instant.asWiseFormat(): String {
    return this.toString()
}