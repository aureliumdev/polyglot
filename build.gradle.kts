plugins {
    java
    `maven-publish`
}

repositories {
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://jitpack.io")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("net.kyori:adventure-text-minimessage:4.11.0")
    implementation("net.kyori:adventure-api:4.13.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.13.1")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    compileOnly("org.spigotmc:spigot-api:1.19.2-R0.1-SNAPSHOT")
}

group = "com.archyx"
version = "1.0.4"
description = "Polyglot"
java.sourceCompatibility = JavaVersion.VERSION_17

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
