plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
}

android {
    namespace = "com.bnyro.contacts"
    compileSdk = 34

    androidResources {
        generateLocaleConfig = true
    }

    defaultConfig {
        applicationId = "com.bnyro.contacts"
        minSdk = 23
        targetSdk = 33
        versionCode = 28
        versionName = "9.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val compose_version: String by rootProject.extra
    // Core & Lifecycle
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Compose & UI
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.6.7")
    implementation("com.github.nanihadesuka:LazyColumnScrollbar:1.9.0")
    implementation("androidx.navigation:navigation-compose:2.8.0-alpha08")

    // VCard
    implementation("com.googlecode.ez-vcard:ez-vcard:0.11.3")
    implementation("net.lingala.zip4j:zip4j:2.11.5")

    // Image parsing
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Phone number formatting
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.35")

    // Biometrics
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")

    // Markdown support for notes
    implementation("com.halilibo.compose-richtext:richtext-ui-material3:0.17.0")
    implementation("com.halilibo.compose-richtext:richtext-commonmark:0.17.0")

    // Room database
    val roomVersion = "2.6.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
