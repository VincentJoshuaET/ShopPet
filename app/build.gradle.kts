plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.google-services")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdkVersion(30)
    buildToolsVersion = "30.0.1"
    ndkVersion = "21.3.6528147"

    defaultConfig {
        applicationId = "com.vt.shoppet"
        minSdkVersion(23)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        coreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        viewBinding = true
    }

    lintOptions {
        isCheckDependencies = true
        isCheckGeneratedSources = true
    }

    splits {
        abi {
            isEnable = true
            isUniversalApk = true
        }
    }

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Kotlin / Java
    implementation(kotlin("stdlib-jdk7", "1.3.72"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.7")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.0.9")

    // Android Architecture Components
    implementation("androidx.activity:activity-ktx:1.2.0-alpha06")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.core:core-ktx:1.3.0")
    implementation("androidx.fragment:fragment-ktx:1.3.0-alpha06")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.recyclerview:recyclerview:1.2.0-alpha04")

    // CameraX
    val cameraVersion = "1.0.0-beta06"
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-core:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-view:1.0.0-alpha13")

    // Firebase
    implementation("com.google.firebase:firebase-auth-ktx:19.3.2")
    implementation("com.google.firebase:firebase-core:17.4.4")
    implementation("com.google.firebase:firebase-firestore-ktx:21.5.0")
    implementation("com.google.firebase:firebase-messaging:20.2.3")
    implementation("com.google.firebase:firebase-storage-ktx:19.1.1")

    // Firebase-UI
    val firebaseUiVersion = "6.2.1"
    implementation("com.firebaseui:firebase-ui-firestore:$firebaseUiVersion")
    implementation("com.firebaseui:firebase-ui-storage:$firebaseUiVersion")

    // Glide
    val glideVersion = "4.11.0"
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    kapt("com.github.bumptech.glide:compiler:$glideVersion")

    // Guava
    implementation("com.google.guava:guava:29.0-jre")

    // Hilt
    val hiltVersion = "1.0.0-alpha01"
    val hiltAndroidVersion = "2.28.1-alpha"
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:$hiltVersion")
    implementation("com.google.dagger:hilt-android:$hiltAndroidVersion")
    implementation("com.google.dagger:hilt-android:$hiltAndroidVersion")
    kapt("androidx.hilt:hilt-compiler:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltAndroidVersion")

    // Lifecycle
    val lifecycleVersion = "2.2.0"
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")

    // Material Components
    implementation("com.google.android.material:material:1.3.0-alpha01")

    // ML Kit
    implementation("com.google.mlkit:image-labeling:16.1.0")

    // Navigation Component
    val navigationVersion = "2.3.0"
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")

    // Unit Testing
    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")

}