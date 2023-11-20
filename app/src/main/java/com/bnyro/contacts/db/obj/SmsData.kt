package com.bnyro.contacts.db.obj

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bnyro.contacts.util.Format
import com.bnyro.contacts.util.addKeywords

@Entity(tableName = "localSms")
data class SmsData(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo val address: String = "",
    @ColumnInfo val body: String = "",
    @ColumnInfo val timestamp: Long = 0,
    @ColumnInfo val threadId: Long = 0,
    @ColumnInfo val type: Int = 0,
    @ColumnInfo(defaultValue = "NULL") var simNumber: Int? = null
) {
    val formatted: AnnotatedString
        get() {
            val text = body
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

    companion object {
        private val linkRegex = Regex(
            "(?<!@)(?<!\\S)((https?://)?[a-zA-Z0-9\\-]{2,}(\\.[a-zA-Z0-9]{2,})+)"
        )
        private val emailRegex = Regex(
            "([A-Za-z0-9+_.-]+@(.+))"
        )
        private val phoneRegex = Regex(
            "((\\+\\d{1,3}(\\s)?)?((\\(\\d{3}\\))|(\\d{3}))[-\\s]?\\d{3}[-\\s]?\\d{4})"
        )
    }
}
