package de.huwig.botc.sheetprint

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.PrintWriter
import java.util.*

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val playerSheetPrinter = PlayerSheetPrinter(Locale.GERMANY)
        embeddedServer(
            factory = Netty,
            host = System.getenv("HOST") ?: "0.0.0.0",
            port = System.getenv("PORT")?.toInt() ?: 8080,
            module = {
                install(Compression) { gzip() }
                install(ContentNegotiation) { json() }
                routing {
                    staticResources(remotePath = "/", basePackage = "html")
                    route("/CGI-BIN/SHEETPRT.EXE") {
                        printSheet(playerSheetPrinter)
                    }
                }
            }
        ).start(wait = true)
    }
}

private fun Route.printSheet(playerSheetPrinter: PlayerSheetPrinter) {
    post {
        try {
            println("POST")
            val parameters = call.receiveParameters()

            println("Printing...")
            val scriptName = parameters["scriptName"]!!
            val author = parameters["author"]!!
            val scriptJson = parameters["scriptJson"]!!

            println("...$scriptName by $author (${scriptJson.length})")

            call.respondTextWriter(contentType = ContentType.Text.Html) {
                playerSheetPrinter.printPlayerSheet(
                    scriptName = scriptName,
                    author = author,
                    scriptJson = scriptJson,
                    out = PrintWriter(this),
                )
            }

            println("...printed")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}