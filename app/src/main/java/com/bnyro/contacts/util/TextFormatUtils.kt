package com.bnyro.contacts.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration

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

fun generateAnnotations(text: String): AnnotatedString {
    val keywords = mutableListOf<Pair<Format, String>>().apply {
        addKeywords(
            text,
            linkRegex,
            Format.LINK
        )
        addKeywords(
            text,
            emailRegex,
            Format.EMAIL
        )
        addKeywords(
            text,
            phoneRegex,
            Format.PHONE
        )
    }

    return buildAnnotatedString {
        append(text)
        keywords.forEach { kw ->
            val (format, keyword) = kw
            val indexOf = text.indexOf(keyword)
            addStyle(
                style = SpanStyle(
                    color = Color.Blue,
                    textDecoration = when (format) {
                        Format.LINK -> TextDecoration.Underline
                        else -> TextDecoration.None
                    }
                ),
                start = indexOf,
                end = indexOf + keyword.length
            )
            val link = when (format) {
                Format.LINK -> if (keyword.startsWith("http")) {
                    keyword
                } else {
                    "http://$keyword"
                }

                Format.PHONE ->
                    "tel:$keyword"

                Format.EMAIL -> "mailto:$keyword"
            }
            addStringAnnotation(
                tag = format.name,
                annotation = link,
                start = indexOf,
                end = indexOf + keyword.length
            )
        }
    }
}

private val linkRegex = Regex(
    "(?<!@)(?<!\\S)((https?://)?[a-zA-Z0-9\\-]{2,}(\\.[a-zA-Z0-9]{2,})+(/[^\\s/]+)*)"
)
private val emailRegex = Regex(
    "([A-Za-z0-9+_.-]+@(.+))"
)
private val phoneRegex = Regex(
    "((\\+\\d{1,3}(\\s)?)?((\\(\\d{3}\\))|(\\d{3}))[-\\s]?\\d{3}[-\\s]?\\d{4})"
)
