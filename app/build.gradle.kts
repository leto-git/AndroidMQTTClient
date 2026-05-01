plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // KSP
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.leto.mqttelement"
    compileSdk = 36

    packaging {
        resources {
            // Prevent errors from duplicate files from Eclipse Paho libraries mqtt v3-1 and mqtt v5
            pickFirsts.add("bundle.properties")

            excludes.add("META-INF/DEPENDENCIES")
            excludes.add("META-INF/LICENSE")
            excludes.add("META-INF/notice.txt")
        }
    }

    defaultConfig {
        applicationId = "com.leto.mqttelement"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.material.icons.extended)

    // Eclipse Paho library for MQTT (3.1.1 and 5.0)
    implementation(libs.org.eclipse.paho.client.mqttv3)
    implementation(libs.org.eclipse.paho.mqttv5.client)

    // Room for persistent storage
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    // KSP support for Room
    ksp(libs.androidx.room.compiler)
    // SQLCipher for encrypted storage
    implementation(libs.net.zetetic.sqlcipher)
    // Sqlite ktx
    implementation(libs.androidx.sqlite)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}