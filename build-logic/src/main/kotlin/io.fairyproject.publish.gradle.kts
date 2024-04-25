import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `maven-publish`
}

publishing {
    publications {
        if (!plugins.hasPlugin("org.gradle.java-gradle-plugin")) {
            create<MavenPublication>("maven") {
                val java = components.findByName("java")
                if (java != null)
                    from(java)
                else {
                    from(components["javaPlatform"])
                }

                pom {
                    name = "Fairy " + project.name
                    description = project.description

                    url = "https://github.com/FairyProject/fairy"
                    organization {
                        name = "Fairy Project"
                        url = "https://github.com/FairyProject/fairy"
                    }
                    licenses {
                        license {
                            name = "MIT License"
                            url = "https://www.apache.org/licenses/LICENSE-2.0"
                            distribution = "repo"
                        }
                    }
                    scm {
                        url = "https://github.com/FairyProject/fairy"
                        connection = "scm:git:git://github.com/FairyProject/fairy"
                        developerConnection = "scm:git:git://github.com/FairyProject/fairy"
                    }
                    developers {
                        developer {
                            id = "leegod"
                            name = "LeeGod"
                            email = "leegod@imanity.dev"
                        }
                    }
                    issueManagement {
                        system = "GitHub"
                        url = "https://github.com/FairyProject/fairy/issues"
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "Production"
            url = uri("https://repo.imanity.dev/imanity-libraries/")
            credentials {
                username = findProperty("imanityLibrariesUsername").toString()
                password = findProperty("imanityLibrariesPassword").toString()
            }
        }
    }
}