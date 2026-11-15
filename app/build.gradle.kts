plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

// Di GitHub Actions nomor build dipakai sebagai versionCode supaya APK baru bisa
// dipasang menimpa versi lama. Saat build lokal nilainya 1.
val ciBuildNumber: Int = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1

android {
    namespace = "com.samatani.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.samatani.app"
        minSdk = 26
        targetSdk = 35
        versionCode = ciBuildNumber
        versionName = "0.1.$ciBuildNumber"
    }

    signingConfigs {
        // Kunci DEMO agar APK debug & release bisa langsung di-install dan di-update.
        // Ganti dengan kunci rahasia Anda sendiri sebelum rilis ke Play Store.
        create("demo") {
            storeFile = file("samatani-demo.keystore")
            storePassword = "samatani"
            keyAlias = "samatani"
            keyPassword = "samatani"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("demo")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("demo")
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
        buildConfig = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
