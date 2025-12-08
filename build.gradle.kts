plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.5"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

// Configure Gradle IntelliJ Plugin 2.x
intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        // 从外部文件读取插件描述和更新记录
        description = providers.fileContents(layout.projectDirectory.file("includes/pluginDescription.html")).asText
        changeNotes = providers.fileContents(layout.projectDirectory.file("includes/pluginChanges.html")).asText

        ideaVersion {
            sinceBuild = providers.gradleProperty("platformSinceBuild")
            untilBuild = providers.gradleProperty("platformUntilBuild")
        }
    }

    pluginVerification {
        ides {
            ide("IC", "2022.3")
            ide("IC", "2023.1")
            ide("IC", "2023.2")
            ide("IC", "2023.3")
            ide("IC", "2024.1")
            ide("IC", "2024.2")
            ide("IC", "2024.3")
            ide("IC", "2025.1")
            ide("IC", "2025.2")

            ide("IU", "2022.3")
            ide("IU", "2023.1")
            ide("IU", "2023.2")
            ide("IU", "2023.3")
            ide("IU", "2024.1")
            ide("IU", "2024.2")
            ide("IU", "2024.3")
            ide("IU", "2025.1")
            ide("IU", "2025.2")

            ide("WS", "2022.3")
            ide("WS", "2023.1")
            ide("WS", "2023.2")
            ide("WS", "2023.3")
            ide("WS", "2024.1")
            ide("WS", "2024.2")
            ide("WS", "2024.3")
            ide("WS", "2025.1")
            ide("WS", "2025.2")

            ide("PS", "2022.3")
            ide("PS", "2023.1")
            ide("PS", "2023.2")
            ide("PS", "2023.3")
            ide("PS", "2024.1")
            ide("PS", "2024.2")
            ide("PS", "2024.3")
            ide("PS", "2025.1")
            ide("PS", "2025.2")

            ide("PY", "2022.3")
            ide("PY", "2023.1")
            ide("PY", "2023.2")
            ide("PY", "2023.3")
            ide("PY", "2024.1")
            ide("PY", "2024.2")
            ide("PY", "2024.3")
            ide("PY", "2025.1")
            ide("PY", "2025.2")

            ide("GO", "2022.3")
            ide("GO", "2023.1")
            ide("GO", "2023.2")
            ide("GO", "2023.3")
            ide("GO", "2024.1")
            ide("GO", "2024.2")
            ide("GO", "2024.3")
            ide("GO", "2025.1")
            ide("GO", "2025.2")

            ide("RD", "2022.3")
            ide("RD", "2023.1")
            ide("RD", "2023.2")
            ide("RD", "2023.3")
            ide("RD", "2024.1")
            ide("RD", "2024.2")
            ide("RD", "2024.3")
            ide("RD", "2025.1")
            ide("RD", "2025.2")

            ide("CL", "2022.3")
            ide("CL", "2023.1")
            ide("CL", "2023.2")
            ide("CL", "2023.3")
            ide("CL", "2024.1")
            ide("CL", "2024.2")
            ide("CL", "2024.3")
            ide("CL", "2025.1")
            ide("CL", "2025.2")

            ide("RM", "2022.3")
            ide("RM", "2023.1")
            ide("RM", "2023.2")
            ide("RM", "2023.3")
            ide("RM", "2024.1")
            ide("RM", "2024.2")
            ide("RM", "2024.3")
            ide("RM", "2025.1")
            ide("RM", "2025.2")

            ide("DB", "2022.3")
            ide("DB", "2023.1")
            ide("DB", "2023.2")
            ide("DB", "2023.3")
            ide("DB", "2024.1")
            ide("DB", "2024.2")
            ide("DB", "2024.3")
            ide("DB", "2025.1")
            ide("DB", "2025.2")

            ide("RR", "2023.1")
            ide("RR", "2023.2")
            ide("RR", "2023.3")
            ide("RR", "2024.1")
            ide("RR", "2024.2")
            ide("RR", "2024.3")
            ide("RR", "2025.1")
            ide("RR", "2025.2")
        }
    }
}

dependencies {
    // IntelliJ Platform
    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

        // Plugin development utilities
        instrumentationTools()

        // Marketplace ZIP Signer for plugin signing
        zipSigner()

        // Plugin verifier for validation
        pluginVerifier()

        // Test framework
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")

    // 主要依赖
    implementation("net.coobird:thumbnailator:0.4.20")
    compileOnly("org.slf4j:slf4j-simple:2.0.13")

    // 测试依赖
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.junit.platform:junit-platform-suite:1.9.2")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.9.2") // 兼容 JUnit 4
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.2.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.assertj:assertj-swing-junit:3.9.2")
    testImplementation("com.aliyun.oss:aliyun-sdk-oss:3.17.4")

    testCompileOnly("org.projectlombok:lombok:1.18.26")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.26")
}

tasks {
    val javaVersion = providers.gradleProperty("javaVersion").get()

    withType<JavaCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    buildPlugin {
        doLast {
            copy {
                from("build/distributions")
                include("${rootProject.name}-${project.version}.zip")
                into("/Users/dong4j/Downloads/mik")
            }
        }
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
        channels = providers.gradleProperty("publishChannels").map { listOf(it) }
    }

    test {
        useJUnitPlatform()
    }
}

