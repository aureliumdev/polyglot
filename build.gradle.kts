plugins {
    `java-library`
    `maven-publish`
    signing
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
version = "1.2.3"
description = "Polyglot"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withJavadocJar()
    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
    }
}

if (project.properties.keys.containsAll(setOf("developerId", "developerUsername", "developerEmail", "developerUrl"))) {
    publishing {
        publications.create<MavenPublication>("Polyglot") {
            groupId = "dev.aurelium"
            artifactId = "polyglot"
            version = project.version.toString()

            pom {
                name.set("Polyglot")
                description.set("Message localization library for Bukkit plugins")
                url.set("https://github.com/aureliumdev/polyglot")
                licenses {
                    license {
                        name.set("The GNU General Public License, Version 3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                    }
                }
                developers {
                    developer {
                        id.set(project.property("developerId").toString())
                        name.set(project.property("developerUsername").toString())
                        email.set(project.property("developerEmail").toString())
                        url.set(project.property("developerUrl").toString())
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/aureliumdev/polyglot.git")
                    developerConnection.set("scm:git:git://github.com/aureliumdev/polyglot.git")
                    url.set("https://github.com/aureliumdev/polyglot/tree/master")
                }
            }

            from(components["java"])
        }

        repositories {
            maven {
                name = "StagingDeploy"
                url = uri(layout.buildDirectory.dir("staging-deploy"))
            }
        }
    }

    signing {
        sign(publishing.publications.getByName("Polyglot"))
        isRequired = true
    }
}
