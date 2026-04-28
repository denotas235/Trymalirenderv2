plugins {
    id("net.fabricmc.fabric-loom") version "1.7-SNAPSHOT"
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
}

version = "1.0.0"
group = "com.malioptrender2v"

loom {
    splitEnvironmentSourceSets()
    mods {
        register("malioptrenderv2") {
            sourceSet(sourceSets.main.get())
            sourceSet(sourceSets.getByName("client"))
        }
    }
    mixin {
        defaultRefmapName.set("malioptrenderv2.refmap.json")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.16.7")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.106.1+1.21.1")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.12.1+kotlin.2.0.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
