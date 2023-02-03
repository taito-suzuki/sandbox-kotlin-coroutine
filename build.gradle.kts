import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    // Coroutineを使用するために必要となるライブラリ
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    // https://mvnrepository.com/artifact/software.amazon.awssdk/s3
    // implementation("software.amazon.awssdk:s3:2.17.172")
    implementation("software.amazon.awssdk:s3:2.19.28")

    implementation("aws.sdk.kotlin:s3:0.20.0-beta")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set(project.properties.getOrDefault("main", "") as String)
}