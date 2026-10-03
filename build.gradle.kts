plugins {
    id("fabric-loom") version "1.9.2"
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

dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings("net.fabricmc:yarn:1.21.1+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.9")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.116.6+1.21.1")

    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    
    include("io.github.llamalad7:mixinextras-fabric:0.5.4")
    modImplementation("io.github.llamalad7:mixinextras-fabric:0.5.4")

    implementation(fileTree("src/main/resources/META-INF/jars") { include("*.jar") })
    
    implementation("com.github.weisj:jsvg:2.1.0")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("io.netty:netty-codec-socks:4.1.112.Final")
    implementation("io.netty:netty-handler-proxy:4.1.112.Final")
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}
