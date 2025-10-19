import java.util.Properties

plugins {
    alias(libs.plugins.android.application)

}

android {
    namespace = "com.shuttleroid.vehicle"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.shuttleroid.vehicle"
        minSdk = 28
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
        debug {
            // applicationIdSuffix(".mk3") // 동시 설치 원하면 주석 해제
        }
    }

    buildFeatures{
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // local.properties -> BuildConfig(BASE_URL)
    val localProps = Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) load(f.inputStream())
    }
    val baseUrl = localProps.getProperty("api.baseUrl") ?: "https://host.imagine.io.kr"
    defaultConfig {
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
    }
}

dependencies {
    val room = "2.7.2"
    implementation("androidx.room:room-runtime:$room")
    annotationProcessor("androidx.room:room-compiler:$room")

    implementation("com.google.code.gson:gson:2.10.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime:2.8.6")

    // Fused Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
