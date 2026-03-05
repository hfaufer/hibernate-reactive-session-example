plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation(libs.hibernate.core)
    implementation(libs.hibernate.validator)
    implementation(libs.hibernate.reactive)

    runtimeOnly(libs.expressly)
    runtimeOnly(libs.vertx.pg.client)

    // JPA metamodel generation for criteria queries. (Those classes with the _ suffix.)
    annotationProcessor(libs.hibernate.jpamodelgen)
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.12.1")
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    // Define the main class for the application.
    mainClass = "org.hibernate.reactive.example.session.CompletionStageMain"
}
