import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
id("net.fabricmc.fabric-loom") version "${providers.gradleProperty("loom_version").get()}"
`maven-publish`
id("org.jetbrains.kotlin.jvm") version "2.3.21"
}

version = providers.gradleProperty("mod_version").get()
group = providers.maven_group.get()

base {
archivesName.set(providers.gradleProperty("archives_base_name").get())
}

loom {
splitEnvironmentSourceSets()

mods {
register("malioptrenderv2") {
sourceSet(sourceSets.main.get())
sourceSet(sourceSets.getByName("client"))
}
}

// ADICIONADO PARA CORRIGIR O CRASH DO REFMAP
mixin {
defaultRefmapName.set("malioptrenderv2.refmap.json")
}
}

fabricApi {
configureDataGeneration {
client = true
}
}

dependencies {
minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
mappings(loom.officialMojangMappings())
modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")
modImplementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
modImplementation("net.fabricmc:fabric-language-kotlin:${providers.gradleProperty("fabric_kotlin_version").get()}")
}

tasks.processResources {
inputs.property("version", project.version)
filesMatching("fabric.mod.json") {
expand("version" to project.version)
}
}

tasks.withType<JavaCompile>().configureEach {
options.release.set(21)
}

kotlin {
compilerOptions {
jvmTarget.set(JvmTarget.JVM_21)
}
}

java {
withSourcesJar()
sourceCompatibility = JavaVersion.VERSION_21
targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
val projectName = project.name
inputs.property("projectName", projectName)
from("LICENSE") {
rename { "${it}_$projectName" }
}
}

publishing {
publications {
register<MavenPublication>("mavenJava") {
from(components["java"])
}
}
}
