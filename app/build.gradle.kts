import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.example.androiddemo"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.androiddemo"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "MINIMAX_API_KEY", "\"${project.findProperty("MINIMAX_API_KEY") ?: ""}\"")
        val nvidiaApiKey = localProperties.getProperty("NVIDIA_API_KEY")
            ?: project.findProperty("NVIDIA_API_KEY")?.toString()
            ?: ""
        buildConfigField("String", "NVIDIA_API_KEY", "\"$nvidiaApiKey\"")
    }

    signingConfigs {
        create("debugCustom") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debugCustom")
        }
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation("androidx.window:window:1.2.0")
    implementation(files("libs/AMap_Location_V11.1.001_20260402.jar"))
    implementation(files("libs/SparkChain.aar"))

    // OkHttp for WebSocket and SSE
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-sse:4.12.0")

    // CameraX
    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // OpenCV for document edge detection
    implementation("org.opencv:opencv:4.12.0")

    // Coroutines for image processing
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // ML Kit Document Scanner
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0")

    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // ZXing for QR code generation
    implementation("com.google.zxing:core:3.5.3")

    // Guava for ListenableFuture
    implementation("com.google.guava:guava:32.1.3-android")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
