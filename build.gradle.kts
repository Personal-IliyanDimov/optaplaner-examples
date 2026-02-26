plugins {
    java
    application
}

group = "com.example"
version = "1.0.0-SNAPSHOT"
description = "OptaPlanner Simple Example"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass.set("com.example.taskschedule.TaskScheduleApp")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.projectlombok:lombok:1.18.30")
    implementation("org.optaplanner:optaplanner-core:10.1.0")
    implementation("org.slf4j:slf4j-simple:2.0.13")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
