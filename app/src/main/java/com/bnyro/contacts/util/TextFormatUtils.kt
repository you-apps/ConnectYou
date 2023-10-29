package com.bnyro.contacts.util

enum class Format {
    LINK, EMAIL, PHONE
}

fun MutableList<Pair<Format, String>>.addKeywords(
    text: String,
    regex: Regex,
    format: Format
) {
    var results: MatchResult? = regex.find(text)
    while (results != null) {
        this.add(format to results.groupValues[1])
        results = results.next()
    }
}
