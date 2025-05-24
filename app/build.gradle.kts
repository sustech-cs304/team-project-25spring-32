plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.pa"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.pa"
        minSdk = 24
        targetSdk = 34
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
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    //noinspection UseTomlInstead
    implementation("com.arthenica:ffmpeg-kit-full-gpl:6.0-2.LTS")
    implementation ("com.google.android.exoplayer:exoplayer-core:2.19.1")
    implementation ("com.google.android.exoplayer:exoplayer-ui:2.19.1")
    // 如果需要 dash, hls 等格式，添加对应的模块
    implementation ("com.google.android.exoplayer:exoplayer-dash:2.19.1")
    implementation ("com.google.android.exoplayer:exoplayer-hls:2.19.1")
    implementation ("androidx.annotation:annotation:1.9.1")
    implementation(libs.glide)
    implementation (libs.flexbox)
    implementation(libs.room.common)
    annotationProcessor(libs.compiler)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation (libs.gson)
    implementation (libs.tensorflow.lite)
    implementation (libs.tensorflow.lite.gpu)  // 可选（GPU加速）
    implementation (libs.tensorflow.lite.support)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // 网络请求
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // 协程
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.adapter.rxjava3)
}