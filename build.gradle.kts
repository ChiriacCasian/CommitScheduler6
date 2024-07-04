plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.15.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.CommitScheduler"
version = "2.0"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2.5")
    type.set("IC") // Target IDE Platform

    //plugins.set(listOf("git4idea", "com.intellij.modules.vcs", "org.jetbrains.plugins.terminal"))
    plugins.set(listOf("git4idea"))
}


dependencies {
    implementation("org.knowm.xchart:xchart:3.8.0")
    implementation("org.apache.avalon.framework:avalon-framework-api:4.3.1")
    implementation("org.apache.avalon.framework:avalon-framework-impl:4.3.1")
    implementation("org.apache.avalon.logkit:avalon-logkit:2.2.1")

    implementation ("javax.jms:javax.jms-api:2.0.1")
    implementation ("javax.mail:javax.mail-api:1.6.2")
    implementation ("log4j:log4j:1.2.17")
    implementation ("com.sun.mail:javax.mail:1.6.2")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    shadowJar {
        archiveClassifier.set("")
    }
    patchPluginXml {
        sinceBuild.set("222")
        untilBuild.set("232.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
    runPluginVerifier {/// verify these versions for intelij compatibility
        ideVersions.set(listOf("IU-242.16677.21"))
    }
}
tasks.build{dependsOn(tasks.shadowJar) }