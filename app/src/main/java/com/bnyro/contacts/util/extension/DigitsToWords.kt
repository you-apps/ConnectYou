package com.bnyro.contacts.util.extension

val digitsToWordsMap = mapOf(
    '2' to arrayOf('A', 'B', 'C'),
    '3' to arrayOf('D', 'E', 'F'),
    '4' to arrayOf('G', 'H', 'I'),
    '5' to arrayOf('J', 'K', 'L'),
    '6' to arrayOf('M', 'N', 'O'),
    '7' to arrayOf('P', 'Q', 'R', 'S'),
    '8' to arrayOf('T', 'U', 'V'),
    '9' to arrayOf('W', 'X', 'Y', 'Z')
)

fun letterCombinations(digits: String): List<String> {
    if (digits.isEmpty()) return emptyList()
    if (digits.contains(Regex("[01+#*]"))) return emptyList()
    val arrays = digits.map { digitsToWordsMap[it]!! }
    return getPossibleStrings(arrays)
}

private fun getPossibleStrings(arrays: List<Array<Char>>): List<String> {
    if (arrays.isEmpty()) return emptyList()
    val result = mutableListOf<String>()

    // Create a tree of depth [arrays.size] and return the result from the leaf node
    fun backtrack(index: Int, currentString: String) {
        if (index == arrays.size) {
            result.add(currentString)
            return
        }
        for (char in arrays[index]) {
            backtrack(index + 1, currentString + char)
        }
    }

    backtrack(0, "")
    return result
}