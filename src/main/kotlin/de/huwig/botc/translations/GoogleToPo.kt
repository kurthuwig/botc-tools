package de.huwig.botc.translations

import com.github.miachm.sods.SpreadSheet
import java.io.File
import java.io.PrintWriter

object GoogleToPo {
    @JvmStatic
    fun main(args: Array<String>) {
        print("Reading spreadsheet...")
        val spread = SpreadSheet(File("/home/kurt/Projekte/Spiele/Blood on the Clocktower/translations/Community BOTC Translations.ods"))
        println("done")
        val en_GB = spread.sheets[1].dataRange.values
        val msgIds = mutableMapOf<String, Array<Any?>>()
        en_GB.forEachIndexed { rowNo, row ->
            if (rowNo == 0) {
                return@forEachIndexed
            }
            msgIds[(row[0] ?: return@forEachIndexed) as String] = row
        }
        spread.sheets.forEachIndexed { sheetNo, sheet ->
            if (sheetNo == 0 || sheetNo == 2) {
                return@forEachIndexed
            }

            val language = sheet.name
            println("language: $language")
            val directory = File("/home/kurt/24h/botc/${language}/LC_MESSAGES")
            directory.mkdirs()
            PrintWriter(File(directory, "characters.po")).use { out ->
                val l10n = sheet.dataRange.values
                out.println("""msgid ""
msgstr ""
"Project-Id-Version: \n"
"POT-Creation-Date: \n"
"PO-Revision-Date: \n"
"Last-Translator: \n"
"Language-Team: \n"
"Language: $language\n"
"MIME-Version: 1.0\n"
"Content-Type: text/plain; charset=UTF-8\n"
"Content-Transfer-Encoding: 8bit\n"
"X-Generator: GoogleToPo 1.0\n"
""")
                l10n.forEachIndexed { rowNo, l18nRow ->
                    if (rowNo == 0) {
                        return@forEachIndexed
                    }
                    val id = (l18nRow[0] ?: return@forEachIndexed) as String
                    val enRow = msgIds[id]
                    if (null == enRow) {
                        println("No English text for $id")
                        return@forEachIndexed
                    }
                    for (colNo in 1 .. 7) {
                        val enText = escapeForCSource((enRow[colNo] as String?) ?: continue)
                        val translation = escapeForCSource((l18nRow[colNo] as String?) ?: continue)
                        out.println()
                        if (sheetNo != 1) {
                            out.println("msgctxt \"$id ${en_GB[0][colNo] as String}\"")
                            out.println("msgid \"$enText\"")
                            out.println("msgstr \"$translation\"")
                        } else {
                            out.println("msgid \"$id ${en_GB[0][colNo] as String}\"")
                            out.println("msgstr \"$enText\"")
                        }
                    }
                }
            }
        }
    }

    fun escapeForCSource(input: String): String {
        val builder = StringBuilder()
        for (char in input.trim()) {
            when (char) {
                '\\' -> builder.append("\\\\") // Escape backslash first!
                '"' -> builder.append("\\\"") // Escape double quotes
                '\n' -> builder.append("\\n") // Newline
                '\t' -> builder.append("\\t") // Tab
                else -> {
                    builder.append(char)
                }
            }
        }
        return builder.toString()
    }
}