
// app/src/main/java/ir/nivatheme/app/core/util/Format.kt
package ir.nivatheme.app.core.util

private val faDigits = mapOf(
    '0' to '۰', '1' to '۱', '2' to '۲', '3' to '۳', '4' to '۴',
    '5' to '۵', '6' to '۶', '7' to '۷', '8' to '۸', '9' to '۹'
)

fun String.toFa(): String = map { faDigits[it] ?: it }.joinToString("")

fun Long.toFaNumber(): String =
    java.text.DecimalFormat("#,###").format(this).toFa()

fun Long.toman(): String = toFaNumber() + " تومان"

fun Int.toFa(): String = toString().toFa()

fun String.isValidIranMobile(): Boolean = Regex("^09\\d{9}$").matches(trim())
