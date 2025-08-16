
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
    id("com.chaquo.python") version "16.1.0"
}

android {
    namespace = "com.example.estia"
    compileSdk = 36
    ndkVersion = "29.0.13599879"

    defaultConfig {
        applicationId = "com.example.estia"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }

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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

chaquopy {
    defaultConfig {
        version = "3.11"
        pip{
            install("ytmusicapi")
            install("yt-dlp")
        }

    }
}

val roomVersion = "2.6.1" // Use latest
kapt {
    correctErrorTypes = true
}

dependencies {

    // new pipe extractor
    //implementation("com.github.TeamNewPipe.NewPipeExtractor:NewPipeExtractor:v0.26.5")
    implementation("com.github.ZiaCheemaGit:NewPipeExtractor:final")

    // Media Transformer
    implementation("androidx.media3:media3-common:1.3.1")
    implementation("androidx.media3:media3-transformer:1.3.1")

    // Audio Tagging
    implementation("com.mpatric:mp3agic:0.9.1")
    implementation("org.jaudiotagger:jaudiotagger:2.0.1")

    // Required for minSdk < 33
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    // Retro fit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    // Life Cycle
    implementation("androidx.lifecycle:lifecycle-service:2.6.2")

    // ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")

    // headSet commands connector
    implementation("androidx.media3:media3-session:1.3.1")

    // Data Store
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // gson
    implementation ("com.google.code.gson:gson:2.10.1")

    // Room
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Coroutine support

    // get Dominant color
    implementation("io.coil-kt:coil:2.4.0")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.palette:palette-ktx:1.0.0")

    // jsoup
    implementation("org.jsoup:jsoup:1.16.1")


    // Media3 for notificatiopn
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")
    implementation("androidx.media:media:1.6.0")


    // Coil
    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation("androidx.compose.foundation:foundation:1.6.0")

    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    implementation("androidx.compose.ui:ui:")
    implementation ("androidx.compose.material:material")
    implementation ("androidx.compose.foundation:foundation")

    implementation("androidx.compose.foundation:foundation:1.5.0")

    implementation(platform("androidx.compose:compose-bom:2025.05.00"))

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.navigation:navigation-compose:2.9.0")

    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.activity:activity-ktx:1.8.2")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}