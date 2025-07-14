plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.2.0"
    id("name.kropp.kotlinx-gettext") version "0.7.0"
    id("io.ktor.plugin") version "3.2.1"
    application
}

group = "de.huwig"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.miachm.sods:SODS:1.6.8")
    implementation("com.squareup.okio:okio-jvm:3.15.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.2.1")
    implementation("io.ktor:ktor-server-compression-jvm:3.2.1")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.2.1")
    implementation("io.ktor:ktor-server-netty-jvm:3.2.1")
    implementation("name.kropp.kotlinx-gettext:kotlinx-gettext:0.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("io.ktor:ktor-server-compression:3.2.1")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("de.huwig.botc.sheetprint.Main")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(22)
}

gettext {
    potFile.set(File("src/main/resources/po/app.pot"))
    keywords.set(listOf("tr", "trc:1c,2", "trn:1,2"))
}