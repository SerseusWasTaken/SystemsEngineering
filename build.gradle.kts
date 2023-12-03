plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3")

    implementation("org.apache.activemq:activemq-broker:6.0.0")
    // https://mvnrepository.com/artifact/org.apache.activemq/activemq-jaas
    implementation("org.apache.activemq:activemq-jaas:6.0.0")
    // https://mvnrepository.com/artifact/org.apache.activemq/activemq-kahadb-store
    implementation("org.apache.activemq:activemq-kahadb-store:6.0.0")


    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")


    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.4.14")



    runtimeOnly("io.insert-koin:koin-core:3.5.0")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.mockk:mockk-agent:1.13.8")
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(19)
}

application {
    mainClass.set("MainKt")
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "19" }
