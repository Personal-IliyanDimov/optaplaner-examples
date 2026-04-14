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
    mainClass.set("com.example.expertschedule.TaskScheduleApp")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    implementation("org.projectlombok:lombok:1.18.30")
    implementation("org.optaplanner:optaplanner-core:10.1.0")
    implementation("org.optaplanner:optaplanner-core-impl:10.1.0")
    implementation("org.optaplanner:optaplanner-persistence-common:10.1.0")
    implementation("org.optaplanner:optaplanner-benchmark:10.1.0")
    implementation("ch.qos.logback:logback-classic:1.5.16")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("benchmark") {
    group = "application"
    description = "Runs OptaPlanner expert benchmark; prints report folder under local/benchmark/"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.imd.expertschedule.ExpertBenchmarkApp")
    workingDir = layout.projectDirectory.asFile
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
