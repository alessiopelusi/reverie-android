import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // navigation
    // Kotlin serialization plugin for type safe routes and navigation arguments
    kotlin("plugin.serialization") version "2.2.0"

    // KSP
    id("com.google.devtools.ksp")

    // Hilt
    id("com.google.dagger.hilt.android")

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")

    // parcelable
    id("kotlin-parcelize")
}

android {
    namespace = "com.mirage.reverie"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mirage.reverie"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "$applicationId.HiltTestRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug") // usa la chiave debug anche per release
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                // Default file with automatically generated optimization rules.
                getDefaultProguardFile("proguard-android-optimize.txt"),

            )
            ndk {
                //noinspection ChromeOsAbiSupport
                abiFilters += listOf("arm64-v8a")
            }
        }
        debug {
            isDebuggable = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // navigation
    // Jetpack Compose integration
    implementation(libs.androidx.navigation.compose)

    // Views/Fragments integration
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Feature module support for Fragments
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    // Testing Navigation
    androidTestImplementation(libs.androidx.navigation.testing)

    // JSON serialization library, works with the Kotlin serialization plugin
    implementation(libs.kotlinx.serialization.json)

    // bottom navigation
    implementation(libs.androidx.material)

    // extended icons
    implementation(libs.androidx.material.icons.extended)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))

    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Use this library and TextFlow() if you use material3
    implementation(libs.combo.breaker.material3)
    implementation(libs.pathway)

    // coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // runtime livedata
    implementation(libs.androidx.runtime.livedata)

    // supabase
    implementation(libs.supabase.storage.kt)
    implementation(libs.ktor.client.okhttp)

    // google accompanist permissions
    implementation(libs.accompanist.permissions)

    // libphoneutil
    implementation(libs.libphonenumber)

    // Compose UI Testing
    implementation(libs.androidx.ui.test.junit4.android)
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.test.manifest)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.android.compiler)
    //testAnnotationProcessor(libs.hilt.android.compiler)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
    //androidTestAnnotationProcessor(libs.hilt.android.compiler)

    // MockK for mocking ViewModels and dependencies
    testImplementation(libs.mockk)

    // Turbine for collecting Flows in tests
    testImplementation(libs.turbine)

    // Coroutine Testing
    testImplementation(libs.kotlinx.coroutines.test)

    //mockito
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
}