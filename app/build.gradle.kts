plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.13"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class", "**/R\$*.class", "**/BuildConfig.*",
        "**/Manifest*.*", "**/*Test*.*", "**/databinding/**",
        // Exclude Activity/View classes (require device/emulator for Espresso tests)
        "**/view/**",
        // Exclude repository classes (thin Firebase wrappers, require Firestore emulator)
        "**/repository/**",
        // Exclude RecyclerView adapters (UI-only, tested via Espresso)
        "**/*Adapter*.*",
        // Exclude MainActivity (UI entry point, tested via Espresso)
        "**/MainActivity.*"
    )

    val debugTree = fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug/compileDebugJavaWithJavac/classes") {
        exclude(fileFilter)
    }

    classDirectories.setFrom(debugTree)
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(fileTree(layout.buildDirectory) {
        include("jacoco/testDebugUnitTest.exec")
    })
}

android {
    namespace = "com.example.ticketreservationapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.ticketreservationapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.all { test ->
            test.useJUnitPlatform()
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.7.0")

    // Firebase BOM manages all Firebase library versions
    implementation(platform("com.google.firebase:firebase-bom:34.9.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.7")
}