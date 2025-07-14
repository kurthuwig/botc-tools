package de.huwig.botc.sheetprint

import kotlinx.serialization.ExperimentalSerializationApi
import name.kropp.kotlinx.gettext.Locale
import java.io.FileWriter
import java.io.PrintWriter

@OptIn(ExperimentalSerializationApi::class)
object PrintSheet {
    @JvmStatic
    fun main(args: Array<String>) {
        val playerSheetPrinter = PlayerSheetPrinter(Locale.GERMANY)

        data class Parameters(val name: String, val author: String, val filename: String)

        listOf(
            Parameters("Catfishing (v11.1.1)", "Emily", "Catfishing"),
            Parameters("Hide and Seek (6.1.0)", "Narninian & Zaba", "Hide and Seek"),
            Parameters("Veiled Threats (v1.1.0)", "Ian (UrbanMinotaur)", "Veiled Threats"),
            Parameters("No Roles Barred (1.0.1)", "Andrew Pichot", "No Roles Barred"),
            Parameters("Boozling (9.0.0)", "Lau", "Boozling"),
            Parameters("The Ballad of Seat 7 (v6.0.0)", "TrashWarlock", "The Ballad of Seat 7"),

            Parameters("Trouble Brewing (1.0.0)", "The Pandemonium Institute", "Trouble Brewing"),
            Parameters("Sects and Violets (1.0.0)", "The Pandemonium Institute", "Sects and Violets"),
            Parameters("Bad Moon Rising (1.0.0)", "The Pandemonium Institute", "Bad Moon Rising"),

            Parameters("Carousel", "The Pandemonium Institute", "Carousel"),

            Parameters("Anonymous Dishonesty", "Emerald, Fran, Kohav, & Theo", "Anonymous Dishonesty"),
            Parameters("Contempt", "Milk", "Contempt"),
            Parameters("Devout Theists", "Emerald", "Devout Theists"),
            Parameters("Harold Holt's Revenge", "Theo", "Harold Holt's Revenge"),
            Parameters("Insanity & Intuition", "Sam", "Insanity and Intuition"),
            Parameters("Irrational Behaviour", "Josh", "Irrational Behaviour"),
            Parameters("The Midnight Oasis", "Lachlan", "The Midnight Oasis"),
            Parameters("Monkey Do Math", "Jams, Josh, Kerry, & Zak", "Monkey Do Math"),
            Parameters("The Ones You Least Expect", "Taylor & Viva La Sam", "The Ones You Least Expect"),
            Parameters("Punchy", "Lachlan", "Punchy"),
            Parameters("Quick Maths", "Fran", "Quick Maths"),
            Parameters("Revenge of the Martian Vampires", "Rope", "Revenge of the Martian Vampires"),
            Parameters("Whose Cult Is It Anyway?", "Aero", "Whose Cult Is It Anyway"),
        ).forEach { params ->
            println(params)
            val scriptJson = PrintSheet.javaClass.getResourceAsStream("/scripts/${params.filename}.json").use {
                String(it!!.readAllBytes(), Charsets.UTF_8)
            }

            PrintWriter(FileWriter("${params.filename}.html")).use { out ->
                playerSheetPrinter.printPlayerSheet(params.name, params.author, scriptJson, out)
            }
        }
    }
}
