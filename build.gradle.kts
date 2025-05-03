plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.spongepowered:configurate-yaml:4.1.2")
    compileOnly("org.jetbrains:annotations:24.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
}

group = "com.archyx"
version = "1.2.2"
description = "Polyglot"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks {
    test {
        useJUnitPlatform()
    }
}
