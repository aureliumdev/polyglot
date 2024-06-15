plugins {
    java
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    compileOnly("org.jetbrains:annotations:24.1.0")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
}

group = "com.archyx"
version = "1.2.0"
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

tasks {
    test {
        useJUnitPlatform()
    }
}
