plugins {
    id("fabric-loom") version "1.13.6"
    id("maven-publish")
}

group = "org.ryzen"
version = "1.0.0"

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
    minecraft("com.mojang:minecraft:1.21.11")
    mappings("net.fabricmc:intermediary:1.21.11:v2")

    modImplementation("net.fabricmc:fabric-loader:0.19.3")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.141.6+1.21.11")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    // Это нужно, чтобы MixinExtras встроился в мод
    include("io.github.llamalad7:mixinextras-fabric:0.5.4")
    modImplementation("io.github.llamalad7:mixinextras-fabric:0.5.4")

    // ВАЖНО: используем include(files(...)) для всех вложенных модов из META-INF/jars.
    // Это заставляет Loom физически упаковать их в финальный JAR в папку META-INF/jars 
    // и корректно обработать секцию "jars" в fabric.mod.json.
    val nestedJars = fileTree("src/main/java/META-INF/jars") { include("*.jar") }
    nestedJars.files.forEach { jarFile ->
        include(files(jarFile))
        modImplementation(files(jarFile))
    }

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
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
