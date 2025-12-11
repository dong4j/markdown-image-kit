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
            create("IC", "2022.3")
            create("IC", "2023.1")
            create("IC", "2023.2")
            create("IC", "2023.3")
            create("IC", "2024.1")
            create("IC", "2024.2")
            create("IC", "2024.3")
            create("IC", "2025.1")
            create("IC", "2025.2")

            create("IU", "2022.3")
            create("IU", "2023.1")
            create("IU", "2023.2")
            create("IU", "2023.3")
            create("IU", "2024.1")
            create("IU", "2024.2")
            create("IU", "2024.3")
            create("IU", "2025.1")
            create("IU", "2025.2")

            create("WS", "2022.3")
            create("WS", "2023.1")
            create("WS", "2023.2")
            create("WS", "2023.3")
            create("WS", "2024.1")
            create("WS", "2024.2")
            create("WS", "2024.3")
            create("WS", "2025.1")
            create("WS", "2025.2")

            create("PS", "2022.3")
            create("PS", "2023.1")
            create("PS", "2023.2")
            create("PS", "2023.3")
            create("PS", "2024.1")
            create("PS", "2024.2")
            create("PS", "2024.3")
            create("PS", "2025.1")
            create("PS", "2025.2")

            create("PY", "2022.3")
            create("PY", "2023.1")
            create("PY", "2023.2")
            create("PY", "2023.3")
            create("PY", "2024.1")
            create("PY", "2024.2")
            create("PY", "2024.3")
            create("PY", "2025.1")
            create("PY", "2025.2")

            create("GO", "2022.3")
            create("GO", "2023.1")
            create("GO", "2023.2")
            create("GO", "2023.3")
            create("GO", "2024.1")
            create("GO", "2024.2")
            create("GO", "2024.3")
            create("GO", "2025.1")
            create("GO", "2025.2")

            create("RD", "2022.3")
            create("RD", "2023.1")
            create("RD", "2023.2")
            create("RD", "2023.3")
            create("RD", "2024.1")
            create("RD", "2024.2")
            create("RD", "2024.3")
            create("RD", "2025.1")
            create("RD", "2025.2")

            create("CL", "2022.3")
            create("CL", "2023.1")
            create("CL", "2023.2")
            create("CL", "2023.3")
            create("CL", "2024.1")
            create("CL", "2024.2")
            create("CL", "2024.3")
            create("CL", "2025.1")
            create("CL", "2025.2")

            create("RM", "2022.3")
            create("RM", "2023.1")
            create("RM", "2023.2")
            create("RM", "2023.3")
            create("RM", "2024.1")
            create("RM", "2024.2")
            create("RM", "2024.3")
            create("RM", "2025.1")
            create("RM", "2025.2")

            create("DB", "2022.3")
            create("DB", "2023.1")
            create("DB", "2023.2")
            create("DB", "2023.3")
            create("DB", "2024.1")
            create("DB", "2024.2")
            create("DB", "2024.3")
            create("DB", "2025.1")
            create("DB", "2025.2")

            create("RR", "2023.1")
            create("RR", "2023.2")
            create("RR", "2023.3")
            create("RR", "2024.1")
            create("RR", "2024.2")
            create("RR", "2024.3")
            create("RR", "2025.1")
            create("RR", "2025.2")
        }
    }
}

dependencies {
    // IntelliJ Platform
    intellijPlatform {
        create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

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

