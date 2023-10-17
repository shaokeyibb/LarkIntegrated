val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val lark_oapi_version: String by project

plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.5"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"
}

group = "cn.org.xaufeca"
version = "0.0.1"

application {
    mainClass.set("cn.org.xaufeca.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-tomcat")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.larksuite.oapi:oapi-sdk:$lark_oapi_version")
    implementation("com.larksuite.oapi:oapi-sdk-servlet-ext:1.0.0-rc3"){
        exclude("com.larksuite.oapi:oapi-sdk")
    }
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
