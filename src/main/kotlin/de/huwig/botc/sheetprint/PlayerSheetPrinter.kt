package de.huwig.botc.sheetprint

import de.huwig.botc.translations.L10n
import de.huwig.botc.translations.L10n.Companion.loadGettext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.io.PrintWriter
import java.net.URI
import java.util.*
import kotlin.math.ceil

@Suppress("EnumEntryName")
enum class RoleType {
    townsfolk,
    outsider,
    minion,
    demon,
    travellers,
    fabled,
}

@Serializable
data class Role(
    val id: String,
    val name: String,
    val roleType: RoleType,
    val print: String,
    val icon: String,
    val version: String,
    val isDisabled: Boolean,
)

@Serializable
data class ScriptRole(
    val id: String,
    val name: String? = null,
    val author: String? = null,
)

@Serializable
data class NightOrder(
    val firstNight: List<String>,
    val otherNight: List<String>,
)

@OptIn(ExperimentalSerializationApi::class)
class PlayerSheetPrinter(locale: Locale) {
    private val appL10n = loadGettext(locale, "app")

    companion object {
        private val roles = mutableMapOf<String, Role>()
        private val soa = mutableListOf<String>()
        private val firstNightOrder = mutableMapOf<String, Int>()
        private val otherNightOrder = mutableMapOf<String, Int>()
        private val characterEntry = PrintSheet.javaClass.getResourceAsStream("/html/character-entry.html")!!.readAllBytes().toString(Charsets.UTF_8)
        private val playerSheet = PrintSheet.javaClass.getResourceAsStream("/html/player-sheet.html")!!.readAllBytes().toString(Charsets.UTF_8)

        init {
            PrintSheet.javaClass.getResourceAsStream("/roles.json").use {
                Json.decodeFromStream<List<Role>>(it!!)
            }.forEach { role ->
                roles[role.id] = role
                soa.add(role.id)
            }

            PrintSheet.javaClass.getResourceAsStream("/nightsheet.json").use {
                val nightOrder = Json.decodeFromStream<NightOrder>(it!!)
                nightOrder.firstNight.forEachIndexed { index, s ->
                    firstNightOrder.put(s, index)
                }
                nightOrder.otherNight.forEachIndexed { index, s ->
                    otherNightOrder.put(s, index)
                }
            }
        }
    }

    private val l10n = L10n(locale)

    fun printPlayerSheet(scriptName: String, author: String, scriptJson: String, out: PrintWriter) {
        val json = Json { ignoreUnknownKeys = true }
        val jsonArray = json.parseToJsonElement(scriptJson).jsonArray
        val roleNames = mutableListOf<String>()
        jsonArray.forEach {
            when (it) {
                is JsonObject -> {
                    try {
                        val scriptRole = json.decodeFromJsonElement(ScriptRole.serializer(), it)
                        if ("_meta" != scriptRole.id) {
                            roleNames.add(scriptRole.id)
                        }
                    } catch (_: Exception) {
                        println("Warnung: Unbekanntes Objekt im Array: $it")
                    }
                }
                is JsonPrimitive ->
                    it.contentOrNull?.let { name ->
                        roleNames.add(name)
                    } ?: println("Unknown role: $it")
                else -> println("Unknown role: $it")
            }
        }

        val scriptRoles = roleNames
            .map {
                it
                    .replace("'", "")
                    .replace(" ", "")
                    .replace("_", "")
            }
            .map { roles[it] ?: run {
                println("Unknown role: $it")
                return
            } }
            .sortedBy { soa.indexOf(it.id) }

        val firstNight = scriptRoles
            .filter { it.roleType != RoleType.travellers && it.roleType != RoleType.fabled }
            .filter { null != firstNightOrder[it.id] }.sortedBy { firstNightOrder[it.id] }
        val otherNight = scriptRoles
            .filter { it.roleType != RoleType.travellers && it.roleType != RoleType.fabled }
            .filter { null != otherNightOrder[it.id] }.sortedBy { otherNightOrder[it.id] }

        val townsfolk = scriptRoles.filter { it.roleType == RoleType.townsfolk }
        val outsiders = scriptRoles.filter { it.roleType == RoleType.outsider }
        val minions = scriptRoles.filter { it.roleType == RoleType.minion }
        val demons = scriptRoles.filter { it.roleType == RoleType.demon }
        val fabled = scriptRoles.filter { it.roleType == RoleType.fabled }
        val travellers = scriptRoles.filter { it.roleType == RoleType.travellers }

        out.println(
            playerSheet
                .replace("###SCRIPT_NAME###", scriptName)
                .replace("###AUTHOR###", appL10n.tr("by {{author}}", Pair("author", author)))
                .replace("###TOWNSFOLK_ROWS###", ceil(townsfolk.size / 2.0).toInt().toString())
                .replace("###OUTSIDER_ROWS###", ceil(outsiders.size / 2.0).toInt().toString())
                .replace("###MINION_ROWS###", ceil(minions.size / 2.0).toInt().toString())
                .replace("###DEMON_ROWS###", ceil(demons.size / 2.0).toInt().toString())
                .replace("###SECTION_TOWNSFOLK###", appL10n.trc("section", "Townsfolk"))
                .replace("###TOWNSFOLK###", sectionHtml(townsfolk, firstNight, otherNight))
                .replace("###SECTION_OUTSIDERS###", appL10n.trc("section", "Outsiders"))
                .replace("###OUTSIDERS###", sectionHtml(outsiders, firstNight, otherNight))
                .replace("###SECTION_MINIONS###", appL10n.trc("section", "Minions"))
                .replace("###MINIONS###", sectionHtml(minions, firstNight, otherNight))
                .replace("###SECTION_DEMONS###", appL10n.trc("section", "Demons"))
                .replace("###DEMONS###", sectionHtml(demons, firstNight, otherNight))
                .replace("###SECTION_FABLED###", appL10n.trc("section", "Fabled"))
                .replace("###FABLED###", sectionHtml(fabled, firstNight, otherNight))
                .replace("###SECTION_TRAVELLERS###", appL10n.trc("section", "Travellers"))
                .replace("###TRAVELLERS###", sectionHtml(travellers, firstNight, otherNight))
                .replace("[", "<span class=\"setup-modification\">[")
                .replace("]", "]</span>")
                .replace("###FIRST_NIGHT_EXPLANATION###", appL10n.tr("First night order"))
                .replace("###NOT_FIRST_NIGHT_EXPLANATION###", appL10n.tr("* not in the first night"))
                .replace("###OTHER_NIGHT_EXPLANATION###", appL10n.tr("Other night order"))
        )
        out.flush()
    }

    private fun sectionHtml(
        sectionRoles: Iterable<Role>,
        firstNight: List<Role>,
        otherNight: List<Role>,
    ): String {
        val result = StringBuilder()
        sectionRoles.forEach { role ->
            result.append(
                characterEntry
                    .replace("###CHARACTER_NAME###", l10n.l18n(role.id, L10n.Type.NAME))
                    .replace(
                        "###CHARACTER_ABILITY###",
                        l10n.l18n(role.id, L10n.Type.ABILITY)
                            .replace("\\\"", "\"")
                    )
                    .replace(
                        "###CHARACTER_ICON###",
                        URI.create(
                            "https://script.bloodontheclocktower.com/" + role.icon.substring(2)
                                .replace(" ", "%20")
                        ).toString()
                    )
                    .replace("###FIRST_NIGHT_ORDER###", firstNight.indexOf(role).let {
                        if (it == -1) "" else (it + 1).toString()
                    })
                    .replace("###OTHER_NIGHT_ORDER###", otherNight.indexOf(role).let {
                        if (it == -1) "" else (it + 1).toString()
                    })
            )
        }
        return result.toString()
    }
}