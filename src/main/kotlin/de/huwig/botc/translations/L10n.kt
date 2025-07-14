package de.huwig.botc.translations

import name.kropp.kotlinx.gettext.Gettext
import name.kropp.kotlinx.gettext.Locale
import okio.source
import kotlin.jvm.java

class L10n(locale: Locale) {
    enum class Type(val value: String) {
        NAME("name"),
        ABILITY("ability"),
        FLAVOR("flavor"),
        OTHER_NIGHT_REMINDER("otherNightReminder"),
        REMINDERS("reminders"),
    }

    companion object {
        private val mapping = loadGettext(Locale.of("en", "GB"), "mapping")

        fun loadGettext(locale: Locale, name: String): Gettext {
            val poStream = (
                    L10n::class.java.getResourceAsStream(
                        "/po/${locale.language}_${locale.country}/LC_MESSAGES/${name}.po"
                    ) ?: L10n::class.java.getResourceAsStream(
                        "/po/${locale.language}/LC_MESSAGES/${name}.po"
                    )!!
                    ).source()
            return Gettext.load(locale, poStream)
        }
    }

    private val characterL10n = loadGettext(locale, "characters")

    fun l18n(id: String, type: Type): String {
        val context = id + " " + type.value
        return characterL10n.trc(
            context = context,
            text = mapping.tr(context).replace("\"", "\\\""),
        )
    }
}
