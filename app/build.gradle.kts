plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdkVersion(30)
    buildToolsVersion = "30.0.2"
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=kotlin.ExperimentalStdlibApi"
        )
    }

    buildFeatures {
        viewBinding = true
    }

    lintOptions {
        isCheckDependencies = true
        isCheckGeneratedSources = true
        disable("GradleDependency")
    }

    splits {
        abi {
            isEnable = true
            isUniversalApk = true
        }
    }
}

dependencies {
    // Kotlin / Java
    implementation(kotlin("stdlib-jdk7"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.9")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.0.10")

    // Android Architecture Components
    implementation("androidx.activity:activity-ktx:1.2.0-alpha08")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.0")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0")
    implementation("androidx.drawerlayout:drawerlayout:1.1.0")
    implementation("androidx.core:core-ktx:1.3.1")
    implementation("androidx.fragment:fragment-ktx:1.3.0-alpha08")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.recyclerview:recyclerview:1.2.0-alpha05")

    // CameraX
    val cameraVersion = "1.0.0-beta08"
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-core:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-view:1.0.0-alpha15")

    // EventBus
    implementation("org.greenrobot:eventbus:3.2.0")
    kapt("org.greenrobot:eventbus-annotation-processor:3.2.0")

    // Firebase
    implementation("com.google.firebase:firebase-analytics-ktx:17.5.0")
    implementation("com.google.firebase:firebase-auth-ktx:19.3.2")
    implementation("com.google.firebase:firebase-core:17.5.0")
    implementation("com.google.firebase:firebase-crashlytics-ktx:17.2.1")
    implementation("com.google.firebase:firebase-firestore-ktx:21.5.0")
    implementation("com.google.firebase:firebase-iid:20.2.4")
    implementation("com.google.firebase:firebase-messaging:20.2.4")
    implementation("com.google.firebase:firebase-ml-vision:24.0.3")
    implementation("com.google.firebase:firebase-ml-vision-image-label-model:20.0.1")
    implementation("com.google.firebase:firebase-perf:19.0.8")
    implementation("com.google.firebase:firebase-storage-ktx:19.1.1")

    // Firebase UI
    val firebaseUiVersion = "6.3.0"
    implementation("com.firebaseui:firebase-ui-firestore:$firebaseUiVersion")
    implementation("com.firebaseui:firebase-ui-storage:$firebaseUiVersion")

    // Glide
    val glideVersion = "4.11.0"
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    kapt("com.github.bumptech.glide:compiler:$glideVersion")

    // Guava
    implementation("com.google.guava:guava:29.0-jre")

    // Hilt
    val hiltVersion = "1.0.0-alpha02"
    val hiltAndroidVersion = "2.28.3-alpha"
    implementation("androidx.hilt:hilt-lifecycle-viewmodel:$hiltVersion")
    implementation("com.google.dagger:hilt-android:$hiltAndroidVersion")
    implementation("com.google.dagger:hilt-android:$hiltAndroidVersion")
    kapt("androidx.hilt:hilt-compiler:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltAndroidVersion")

    // Lifecycle
    val lifecycleVersion = "2.3.0-alpha07"
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")

    // Material Components
    implementation("com.google.android.material:material:1.3.0-alpha02")

    // Navigation Component
    val navigationVersion = "2.3.0"
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")

    // Unit Testing
    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")

}

kapt {
    correctErrorTypes = true
    arguments {
        arg("eventBusIndex", "com.vt.shoppet.EventBusIndex")
    }
}