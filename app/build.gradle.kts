plugins {
    alias(libs.plugins.android.application)
    id("org.sonarqube") version "4.4.1.3373"
}
//sonarqube plugin configuration, 应当在依赖安装之后解析
sonarqube {
    properties {
        property("sonar.projectKey", "com.example.pa")
        property("sonar.projectName", "PA")
        property("sonar.projectVersion", "1.0")
        property("sonar.sources", "src/main/java")
        property("sonar.tests", "src/test/java")
        property("sonar.language", "java")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.java.binaries", "build/intermediates/javac/debug")
        property("sonar.coverage.jacoco.xmlReportPaths", "${buildDir}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
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
        debug {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
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
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-common:1.3.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.3.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.1")
    implementation("androidx.media3:media3-exoplayer-smoothstreaming:1.3.1")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation(libs.glide)
    implementation(libs.flexbox)
    implementation(libs.room.common)
    annotationProcessor(libs.compiler)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.gson)
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.gpu)  // 可选（GPU加速）
    implementation(libs.tensorflow.lite.support)
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

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.3")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")

    //测试相关
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    // Mockito for mocking

    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.robolectric:robolectric:4.10.3")

    //这两个有冲突不能同时使用，同时下面那个inline安卓环境有点不支持
    androidTestImplementation("org.mockito:mockito-android:5.3.1")
    //androidTestImplementation ("org.mockito:mockito-inline:5.2.0")

    // Architecture Components testing
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")


    // Fragment testing
    debugImplementation("androidx.fragment:fragment-testing:1.6.2")

    // AndroidX 测试依赖
    androidTestImplementation ("androidx.test:runner:1.6.1")
    androidTestImplementation ("androidx.test.ext:junit:1.2.1")

}