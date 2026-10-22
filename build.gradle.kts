plugins {
    id("net.fabricmc.fabric-loom") version "1.19.2"
    id("maven-publish")
}

group = "org.ryzen"
version = "1.0.0-26.3"

base {
    archivesName.set("ryzen")
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/java")
            exclude("**/*.java")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:26.3")
    // 26.1+ is non-obfuscated; official names, no intermediary layer
    modImplementation("net.fabricmc:fabric-loader:0.20.1")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.164.0+26.3")

    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    include("io.github.llamalad7:mixinextras-fabric:0.5.4")
    modImplementation("io.github.llamalad7:mixinextras-fabric:0.5.4")

    val nestedJars = fileTree("src/main/java/META-INF/jars") { include("*.jar") }
    modImplementation(nestedJars)

    implementation("com.github.weisj:jsvg:2.1.0")
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("io.netty:netty-codec-socks:4.2.15.Final")
    implementation("io.netty:netty-handler-proxy:4.2.15.Final")
}

loom {
    @Suppress("UnstableApiUsage")
    enableModProvidedJavadoc.set(false)

    mods {
        create("ryzen") {
            sourceSet(sourceSets.main.get())
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
