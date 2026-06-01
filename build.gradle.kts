plugins {
    id("maven-publish")
    id("java-library")
}

val getGitCommit = providers.exec {
    commandLine("git", "rev-parse", "--short=7", "HEAD")
}.standardOutput.asText.map { it.trim() }

version = getGitCommit.get() + "-SNAPSHOT"
group = "eu.cafestube.data"

repositories {
    mavenCentral()
    maven {
        name = "cafestubeRepository"
        url = uri("https://repo.cafestube.net/repository/maven/")
        credentials(PasswordCredentials::class)
    }
    maven {
        url = uri("https://libraries.minecraft.net")
    }
}

dependencies {
    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("net.kyori:adventure-nbt:5.1.0")


    // https://mvnrepository.com/artifact/net.kyori/adventure-api
    api("net.kyori:adventure-api:5.1.0")
    api("com.google.code.gson:gson:2.10.1")
    // https://mvnrepository.com/artifact/it.unimi.dsi/fastutil
    api("it.unimi.dsi:fastutil:8.5.12")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    api("org.slf4j:slf4j-api:2.0.9")
    // https://mvnrepository.com/artifact/com.google.guava/guava
    api("com.google.guava:guava:32.1.3-jre")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    api("org.apache.commons:commons-lang3:3.20.0")
    api("com.mojang:datafixerupper:6.0.8")
    // https://mvnrepository.com/artifact/net.kyori/adventure-text-serializer-gson
    api("net.kyori:adventure-text-serializer-gson:5.1.0")
    api("com.mojang:brigadier:1.2.9")
    // https://mvnrepository.com/artifact/net.kyori/adventure-nbt
    compileOnly("net.kyori:adventure-nbt:5.1.0")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib-common
    api("org.jetbrains.kotlin:kotlin-stdlib-common:1.9.21")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

// make build reproducible
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
    inputs.property("archivesName", "externaldataconverter")

    from("LICENSE") {
        rename { "${it}_externaldataconverter" }
    }
}

// configure the maven publication
publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "externaldataconverter"
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "cafestubeRepository"
            credentials(PasswordCredentials::class)
            url = uri("https://repo.cafestube.net/repository/maven-snapshots/")
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
