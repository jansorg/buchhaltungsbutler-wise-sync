val ktorVersion: String by project

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/transferwise")
    }
}

application {
    mainClass.set("dev.ja.sync.Main")
}

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-java:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-resources:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    // 3rd-party YAML support for kotlinx-serialization, https://github.com/him188/yamlkt
    implementation("net.mamoe.yamlkt:yamlkt:0.13.0")

    // for Transferwise digital-signatures copy
    implementation("org.bouncycastle:bcprov-jdk18on:1.76")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.76")

    implementation("org.apache.logging.log4j:log4j-core:2.21.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.21.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}
