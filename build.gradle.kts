import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// the plugins {} block must be the first code of a build script.
plugins {
    alias(libs.plugins.kotlin.jvm)  //apply false
    alias(libs.plugins.spring.boot) // apply false
    alias(libs.plugins.spring.dependency.management)
//    alias(libs.plugins.sonarqube)
}

allprojects {
    group = "com.abc.us.accounting"
    version = "0.0.1-SNAPSHOT"
    description = "accounting project"

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()

        maven { url = uri("https://repo.maven.apache.org/maven2/") }
        maven { url = uri("https://repo.spring.io/milestone") }
        maven {
            url = uri("https://abc-us-712487249036.d.codeartifact.us-west-2.amazonaws.com/maven/abc-us-integration/")
            credentials {
                username = "aws"
                password = file("${System.getenv("HOME")}/.aws/codeartifact/abc-us").readText()
            }
        }
    }
    configurations {
        all {
            exclude(group = "commons-logging", module = "commons-logging")
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_21.toString()
        targetCompatibility = JavaVersion.VERSION_21.toString()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict","-Xjvm-default=all")
            jvmTarget = JavaVersion.VERSION_21.toString()
        }
    }

//    sonarqube {
//        properties {
//            properties["sonar.projectName"] = "ABC Accounting"
//            properties["sonar.projectDescription"] = "ABC Accounting System"
//        }
//    }
}

tasks.bootJar {
    enabled = false
}


tasks.bootBuildImage {
    enabled = false
}
