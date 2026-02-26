plugins {
    java
    application
}

group = "com.example"
version = "1.0.0-SNAPSHOT"
description = "OptaPlanner Simple Example"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("com.example.taskschedule.TaskScheduleApp")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.optaplanner:optaplanner-core:10.1.0")
    implementation("org.slf4j:slf4j-simple:2.0.13")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

