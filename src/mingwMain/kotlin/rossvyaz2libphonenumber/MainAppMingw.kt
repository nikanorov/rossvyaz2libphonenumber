/*
 * Copyright 2019 Andrey Nikanorov and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package rossvyaz2libphonenumber

import kotlinx.cinterop.*
import platform.posix.*

const val outputFileName = "7.txt"
const val PROCESS_PREFIXES = 0
const val PROCESS_OPERATORS = 1
const val COUNTRY_CODE = "7"

var prefixBegin = arrayListOf<Long>()
var prefixEnd = arrayListOf<Long>()
var prefixOperator = arrayListOf<String>()
var prefixDefCode = arrayListOf<String>()

var operatorsList = mutableSetOf<String>()

val operatorReplaceMap = HashMap<String, String>()

fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Usage: rossvyaz2libphonenumber export <file.csv> <operator_replace.csv>")
        println("Usage: rossvyaz2libphonenumber operators_list <file.csv>")
        return
    }

    val fileName = args[1]

    when (args[0]) {
        "export" -> {
            processRossvyazFile(fileName, PROCESS_PREFIXES)
            if (args.size > 2 && args[2].isNotBlank())
                loadReplaces(args[2])
            writeOutput()
            println("Success! Entries after processing: ${prefixBegin.size}.")

        }
        "operators_list" -> {
            processRossvyazFile(fileName, PROCESS_OPERATORS)
            println(operatorsList)
        }
        else -> {
            println("Unknown option.")
        }

    }

}

fun processRossvyazFile(fileName: String, option: Int) {
    val file = fopen(fileName, "r")
    if (file == null) {
        perror("Cannot open input file $fileName")
        return
    }

    try {
        memScoped {
            val bufferLength = 64 * 1024
            val buffer = allocArray<ByteVar>(bufferLength)

            var nextLine = removeUTF8BOM(fgets(buffer, bufferLength, file)?.toKString())

            while (nextLine != null && nextLine.isNotEmpty()) {

                val lineData = processLine(nextLine, ';')
                when (option) {
                    PROCESS_PREFIXES -> processPrefixes(lineData)
                    PROCESS_OPERATORS -> operatorsList.add(lineData[4])
                }

                nextLine = fgets(buffer, bufferLength, file)?.toKString()
            }
        }
    } finally {
        fclose(file)
    }

}


private fun removeUTF8BOM(s: String?): String? {
    if (s != null && s.startsWith("\uFEFF")) {
        return s.substring(1)
    }
    return s
}

fun processPrefixes(lineData: List<String>) {
    if (prefixOperator.isNotEmpty() && prefixOperator[prefixOperator.lastIndex] == lineData[4]
        && prefixDefCode[prefixDefCode.lastIndex] == lineData[0]
        && (prefixEnd[prefixEnd.lastIndex] + 1).equals((COUNTRY_CODE + lineData[0] + lineData[1]).toLong())
    ) {

        prefixEnd[prefixEnd.lastIndex] = (COUNTRY_CODE + lineData[0] + lineData[2]).toLong()

    } else {
        prefixBegin.add((COUNTRY_CODE + lineData[0] + lineData[1]).toLong())
        prefixEnd.add((COUNTRY_CODE + lineData[0] + lineData[2]).toLong())
        prefixDefCode.add(lineData[0])
        prefixOperator.add(lineData[4])
    }

}


fun writeOutput() {
    val file_wr = fopen(outputFileName, "wt") ?: throw Error("Cannot write to file '$outputFileName'")

    try {

        //remove ending zeros, but leave first 4 chars.
        val replaceRegex = "(?<=.{4})(0+\$)".toRegex()

        for (i in prefixBegin.indices) {
            val operator = operatorReplaceMap.getOrElse(prefixOperator[i], {
                prefixOperator[i]
            })

            val prefix = prefixBegin[i].toString().replace(replaceRegex, "")

            if (prefix.length < 10)
                fputs("$prefix|$operator\n", file_wr)
            else
                println("Entry skipped, too long: $prefix | $operator")
        }

    } finally {
        fclose(file_wr)
    }

}

fun loadReplaces(fileName: String) {
    val file = fopen(fileName, "r")
    if (file != null) {
        try {
            memScoped {
                val bufferLength = 64 * 1024
                val buffer = allocArray<ByteVar>(bufferLength)

                var nextLine = fgets(buffer, bufferLength, file)?.toKString()

                while (nextLine != null && nextLine.isNotEmpty()) {
                    val lineData = processLine(nextLine, ';')
                    if (!lineData.isNullOrEmpty())
                        operatorReplaceMap[lineData[0]] = lineData[1].trim()

                    nextLine = fgets(buffer, bufferLength, file)?.toKString()
                }
            }
        } finally {
            fclose(file)
        }
    } else {
        perror("Cannot read replacements file '$fileName'. ")
    }

}

fun processLine(line: String, separator: Char): List<String> {
    val result = mutableListOf<String>()
    val builder = StringBuilder()
    var quotes = 0
    for (ch in line) {
        when {
            ch == '\"' -> {
                quotes++
                builder.append(ch)
            }
            (ch == '\n') || (ch == '\r') -> {
            }
            (ch == separator) && (quotes % 2 == 0) -> {
                result.add(builder.toString())
                builder.setLength(0)
            }
            else -> builder.append(ch)
        }
    }
    return result
}
