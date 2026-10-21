/*
 * BreweryX Bukkit-Plugin for an alternate brewing process
 * Copyright (C) 2024 The Brewery Team
 * GNU GPL version 3 or later; see LICENSE.txt.
 * Modified 2026-10-21 for the unofficial Paper 26.3 port; see PORT-NOTICE.md.
 */
import org.apache.tools.ant.filters.ReplaceTokens
plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}
group = "com.dre.brewery"
version = "3.7.1-paper26.3.1-beta.1"

val langVersion: Int = 25
val runTaskJavaVersion: Int = 25
val encoding: String = "UTF-8"

repositories {
    mavenCentral()
    maven("https://repo.jsinco.dev/releases") // UniversalScheduler
    maven("https://jitpack.io") // GriefPrevention, SlimeFun, PlaceholderAPI
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
    maven("https://maven.enginehub.org/repo/") // WorldEdit, WorldGuard
    maven("https://ci.ender.zone/plugin/repository/everything/") // LWC Extended
    maven("https://repo.minebench.de/") // ChestShop
    maven("https://repo.codemc.org/repository/maven-public/") // BlockLocker
    maven("https://nexus.phoenixdevt.fr/repository/maven-public/") // MythicLib (MMOItems)
    maven("https://repo.projectshard.dev/repository/releases/") // Shopkeepers
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://repo.glaremasters.me/repository/towny/") // Towny
    maven("https://repo.oraxen.com/releases") // Oraxen
    maven("https://storehouse.okaeri.eu/repository/maven-public/") // Okaeri Config
    maven("https://repo.papermc.io/repository/maven-public/") // PaperLib
    maven("https://repo.papermc.io/repository/maven-public/") // Movecraft
}

configurations.configureEach {
    exclude(group = "org.bukkit", module = "bukkit")
    exclude(group = "org.spigotmc", module = "spigot-api")
}

dependencyLocking { lockAllConfigurations() }

dependencies {
    // Spigot
    compileOnly("io.papermc.paper:paper-api:26.3.build.169-beta") {
        exclude("com.google.code.gson", "gson") // Implemented manually
    }
    // Paper Lib, performance improvements on Paper-based servers and async teleporting on Folia
    implementation("io.papermc:paperlib:1.0.8")

    // Database source implementation
    implementation("com.zaxxer:HikariCP:7.0.2") {
        exclude("org.slf4j", "slf4j-api")
    }
    // Implemented manually mainly due to older server versions implementing versions of GSON
    // which don't support records.
    implementation("com.google.code.gson:gson:2.11.0")
    // For proper scheduling between Bukkit-Folia like servers, https://github.com/Anon8281/UniversalScheduler
    implementation("com.github.Anon8281:UniversalScheduler:0.1.3-dev")
    // Nice annotations, I prefer these to Lombok's, https://www.jetbrains.com/help/idea/annotating-source-code.html
    compileOnly("org.jetbrains:annotations:26.0.1")
    // MongoDB & log4j to suppress MongoDB's logger
    implementation("org.mongodb:mongodb-driver-sync:5.3.0-beta0")
    compileOnly("org.apache.logging.log4j:log4j-core:2.23.1")
    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    // Okaeri configuration
    implementation("eu.okaeri:okaeri-configs-yaml-snakeyaml:5.0.5") {
        exclude("org.yaml", "snakeyaml")
    }
    constraints {
        implementation("org.yaml:snakeyaml") {
            version {
                require("2.3")
                reject("1.33")
            }
        }
    }

    // Plugin Compatability
    compileOnly("com.sk89q:worldguard:6.1") // https://dev.bukkit.org/projects/worldedit/files
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0-SNAPSHOT") // https://dev.bukkit.org/projects/worldedit/files
    compileOnly("com.sk89q.worldedit:worldedit-core:7.3.0-SNAPSHOT") // https://dev.bukkit.org/projects/worldguard/files
    compileOnly("com.griefcraft:lwc:2.3.2") { // https://www.spigotmc.org/resources/lwc-extended.69551/history
        exclude("com.google")
    }
    compileOnly("com.github.TechFortress:GriefPrevention:16.18") // https://www.spigotmc.org/resources/griefprevention.1884/history
    compileOnly(files("lib/LogBlock.jar")) // https://www.spigotmc.org/resources/logblock.67333/history | https://www.iani.de/jenkins/job/LogBlock/123/
    compileOnly("com.github.Slimefun:Slimefun4:RC-35") // https://github.com/Slimefun/Slimefun4/releases
    compileOnly("io.lumine:MythicLib-dist:1.6-SNAPSHOT") // https://www.spigotmc.org/resources/mythiclib.90306/history
    compileOnly("com.acrobot.chestshop:chestshop:3.12.2") // https://github.com/ChestShop-authors/ChestShop-3/releases
    compileOnly("com.palmergames.bukkit.towny:towny:0.100.3.0") // https://www.spigotmc.org/resources/towny-advanced.72694/history
    compileOnly("com.github.Angeschossen:LandsAPI:7.11.10") // https://www.spigotmc.org/resources/lands.53313/history
    compileOnly("com.nisovin.shopkeepers:ShopkeepersAPI:2.18.0") // https://www.spigotmc.org/resources/shopkeepers.80756/history
    compileOnly("nl.rutgerkok:blocklocker:1.10.4") // https://www.spigotmc.org/resources/blocklocker.3268/history
    compileOnly("me.clip:placeholderapi:2.11.5") // https://www.spigotmc.org/resources/placeholderapi.6245/history
    compileOnly("io.th0rgal:oraxen:1.163.0") // https://www.spigotmc.org/resources/%E2%98%84%EF%B8%8F-oraxen-custom-items-blocks-emotes-furniture-resourcepack-and-gui-1-18-1-21-3.72448/
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.1") // https://www.spigotmc.org/resources/%E2%9C%A8itemsadder%E2%AD%90emotes-mobs-items-armors-hud-gui-emojis-blocks-wings-hats-liquids.73355/updates
    compileOnly(files("lib/Movecraft.jar")) // API only, never included in the runtime JAR.


    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.papermc.paper:paper-api:26.3.build.169-beta")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}




java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
}
tasks.withType<JavaCompile>().configureEach { options.encoding = "UTF-8" }
tasks.processResources {
    filteringCharset = "UTF-8"
    filter<ReplaceTokens>(mapOf("tokens" to mapOf("version" to project.version.toString()), "beginToken" to "\${", "endToken" to "}"))
    from("LICENSE.txt")
}
val stageDependencyNotices by tasks.registering {
    val output = layout.buildDirectory.dir("dependency-notices")
    inputs.files(configurations.runtimeClasspath)
    outputs.dir(output)
    doLast {
        configurations.runtimeClasspath.get().files.forEach { dependency ->
            copy {
                from(zipTree(dependency))
                include("META-INF/LICENSE*", "META-INF/NOTICE*", "LICENSE*", "NOTICE*", "COPYING*")
                into(output.get().dir(dependency.nameWithoutExtension))
            }
        }
    }
}
tasks.shadowJar {
    val pack = "com.dre.brewery.depend"
    relocate("com.google.gson", "$pack.google.gson")
    relocate("com.google.errorprone", "$pack.google.errorprone")
    relocate("com.github.Anon8281.universalScheduler", "$pack.universalScheduler")
    relocate("eu.okaeri", "$pack.okaeri")
    relocate("com.mongodb", "$pack.mongodb")
    relocate("org.bson", "$pack.bson")
    relocate("io.papermc.lib", "$pack.paperlib")
    relocate("com.zaxxer.hikari", "$pack.hikari")
    archiveClassifier.set("")
    from("third-party-licenses") { into("META-INF/third-party-licenses") }
    from("PORT-NOTICE.md")
    dependsOn(stageDependencyNotices)
    from(layout.buildDirectory.dir("dependency-notices")) { into("META-INF/third-party-licenses/bundled") }
}
tasks.jar { enabled = false }
tasks.build { dependsOn(tasks.shadowJar) }
tasks.test { useJUnitPlatform() }
