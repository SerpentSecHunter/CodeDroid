plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.chaquo.python") version "15.0.1"
}

android {
    namespace  = "com.example.codedroid"
    compileSdk = 35

    defaultConfig {
        applicationId             = "com.example.codedroid"
        minSdk                    = 26
        targetSdk                 = 35
        versionCode               = 6
        versionName               = "2.3.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Chaquopy — Python runtime
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES", "META-INF/LICENSE",
                "META-INF/NOTICE", "META-INF/*.kotlin_module",
                "META-INF/AL2.0", "META-INF/LGPL2.1"
            )
        }
    }

    chaquopy {
        defaultConfig {
            version = "3.11"
            buildPython("py", "-3.11")
            pip {
                install("requests")
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.datastore)
    implementation(libs.commons.net)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines)

    // HTTP
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Encrypted prefs
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Media
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // WebView
    implementation(libs.webkit)

    // New dependencies for v2.2
    implementation("com.airbnb.android:lottie-compose:6.4.0")
    
    implementation("androidx.documentfile:documentfile:1.0.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
