plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.vibedev.visuolink"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vibedev.visuolink"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.4.69"

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
        buildConfig = true
    }
}

dependencies {
    implementation("org.json:json:20231013")
    implementation("androidx.lifecycle:lifecycle-service:2.8.4")
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")
    implementation("com.google.mlkit:face-detection:16.1.7")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.cardview)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.filament.android)
    implementation(libs.vision.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}