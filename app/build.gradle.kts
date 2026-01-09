plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.tabula"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tabula"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.8.7")

    implementation("com.google.android.material:material:1.11.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
