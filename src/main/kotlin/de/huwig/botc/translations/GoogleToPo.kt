package de.huwig.botc.translations

import com.github.miachm.sods.SpreadSheet
import java.io.File
import java.io.PrintWriter
import kotlin.system.exitProcess

object GoogleToPo {
    @JvmStatic
    fun main(args: Array<String>) {
        if (1 != args.size) {
            println("Usage: java -jar google-to-po.jar <working directory>")
            exitProcess(1)
        }
        convertCharacters(args[0])
        convertJinxes(args[0])
    }

    private fun convertCharacters(workingDirectory: String) {
        print("Reading characters...")
        val spread = SpreadSheet(File(workingDirectory, "Community BOTC Translations.ods"))
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
            if (sheetNo == 0) {
                return@forEachIndexed
            }

            val language = sheet.name.substring(0, sheet.name.indexOf('_'))
            println("language: $language")
            val directory = File(workingDirectory, "botc/${sheet.name}/LC_MESSAGES")
            directory.mkdirs()
            PrintWriter(File(directory, "characters.po")).use { out ->
                val l10n = sheet.dataRange.values
                out.print("""msgid ""
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
                        var translation = escapeForCSource((l18nRow[colNo] as String?) ?: "")
                        if ("en" != language && enText == translation) translation = ""
                        out.println()
                        out.println("msgctxt \"$id ${en_GB[0][colNo] as String}\"")
                        out.println("msgid \"$enText\"")
                        out.println("msgstr \"$translation\"")
                    }
                }
            }
        }
    }

    private fun convertJinxes(workingDirectory: String) {
        print("Reading jinxes...")
        val spread = SpreadSheet(File(workingDirectory, "Jinxes 2.ods"))
        println("done")
        val en_GB = spread.sheets[1].dataRange.values
        val msgIds = mutableMapOf<String, Array<Any?>>()
        en_GB.forEachIndexed { rowNo, row ->
            if (rowNo == 0) {
                return@forEachIndexed
            }
            msgIds["${row[0] ?: return@forEachIndexed} ${row[1] ?: return@forEachIndexed}"] = row
        }
        spread.sheets.forEachIndexed { sheetNo, sheet ->
            if (sheetNo == 0) {
                return@forEachIndexed
            }

            val language = sheet.name.substring(0, sheet.name.indexOf('_'))
            println("language: $language")
            val directory = File(workingDirectory, "botc/${sheet.name}/LC_MESSAGES")
            directory.mkdirs()
            PrintWriter(File(directory, "jinxes.po")).use { out ->
                val l10n = sheet.dataRange.values
                out.print("""msgid ""
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
                    val id = "${l18nRow[0] ?: return@forEachIndexed} ${l18nRow[1] ?: return@forEachIndexed}"
                    val enRow = msgIds[id]
                    if (null == enRow) {
                        println("No English text for $id")
                        return@forEachIndexed
                    }
                    val enText = escapeForCSource((enRow[2] as String?) ?: return@forEachIndexed)
                    var translation = escapeForCSource((l18nRow[2] as String?) ?: "")
                    if ("en" != language && enText == translation) translation = ""
                    out.println()
                    out.println("msgctxt \"${l18nRow[0] as String?} ${l18nRow[1] as String}\"")
                    out.println("msgid \"$enText\"")
                    out.println("msgstr \"$translation\"")
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