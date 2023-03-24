/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.nio.charset.StandardCharsets

plugins {
    id("java-library")
    id("maven-publish")
}

group = "io.github.honhimw"
version = "0.0.1-SNAPSHOT"
description = "Support for reading and writing Excel-Hyperlink via Jackson abstractions."

val title = "Jackson dataformat: HyperLink"
val jacksonVersion = "2.14.2"
val poiVersion = "5.2.3"
val snapshots = version.toString().endsWith("SNAPSHOT")

repositories {
    mavenCentral()
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("org.apache.poi:poi-ooxml:$poiVersion")
    implementation("org.slf4j:slf4j-api:2.0.6")
}

dependencies {
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey:0.5.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("commons-beanutils:commons-beanutils:1.9.4")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.3.5")
    testRuntimeOnly("org.apache.logging.log4j:log4j-to-slf4j:2.19.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

dependencies {
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    compileOnly("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")
    testCompileOnly("org.projectlombok:lombok:1.18.24")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set(title)
                description.set(project.description)
//                url.set("https://github.com/scndry/jackson-dataformat-spreadsheet")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        name.set("Ryan S. Yang")
                        email.set("scndryan@gmail.com")
                        url.set("https://scndry.github.io")
                    }
                    developer {
                        name.set("honhimw")
                        email.set("honhimw@outlook.com")
                    }
                }
//                scm {
//                    connection.set("scm:git:git://github.com/scndry/jackson-dataformat-spreadsheet.git")
//                    developerConnection.set("scm:git:ssh://github.com/scndry/jackson-dataformat-spreadsheet.git")
//                    url.set("https://github.com/scndry/jackson-dataformat-spreadsheet")
//                }
            }
        }
    }
    repositories {
        maven {
            if ("repository" in properties) {
                name = properties["repository"] as String
                url = uri(properties[if (snapshots) "${name}Snapshots" else "${name}Releases"] as String)
                credentials(PasswordCredentials::class)
            } else {
                url = uri(layout.buildDirectory.dir(if (snapshots) "publications/snapshots" else "publications/releases"))
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = StandardCharsets.UTF_8.name()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Javadoc> {
    val opts = options as CoreJavadocOptions
    opts.addBooleanOption("Xdoclint:-missing", true)
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Implementation-Title" to title,
            "Implementation-Version" to archiveVersion,
            "Implementation-Vendor-Id" to project.group,
            "Specification-Title" to "jackson-databind",
            "Specification-Version" to jacksonVersion,
            "Specification-Vendor" to "FasterXML",
        )
    }
}
