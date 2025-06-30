plugins {
    `java-library`
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.spongepowered:configurate-yaml:4.2.0")
    compileOnly("org.jetbrains:annotations:24.1.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation("com.google.guava:guava:33.3.1-jre")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(platform("org.junit:junit-bom:5.13.2"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

group = "com.archyx"
version = "1.2.4-SNAPSHOT"
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

    javadoc {
        options {
            (this as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true)
        }
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
            if (project.properties.keys.containsAll(setOf("sonatypeUsername", "sonatypePassword"))) {
                maven {
                    name = "Snapshot"
                    url = uri("https://central.sonatype.com/repository/maven-snapshots/")

                    credentials {
                        username = project.property("sonatypeUsername").toString()
                        password = project.property("sonatypePassword").toString()
                    }
                }
            }
        }
    }

    signing {
        sign(publishing.publications.getByName("Polyglot"))
        isRequired = true
    }
}
