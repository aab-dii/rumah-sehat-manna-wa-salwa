plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.android.rumahsehatmannawasalwa"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.android.rumahsehatmannawasalwa"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
//        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/api/\"")
//        buildConfigField("String", "BASE_URL", "\"http://192.168.110.24:8000/api/\"")
        buildConfigField("String", "BASE_URL", "\"http://192.168.1.6:8000/api/\"")
        buildConfigField("String", "ADMIN_WHATSAPP", "\"6285220264022\"")
//        buildConfigField("String", "BASE_URL", "\"http://192.168.110.195:8000/api/\"")
//        buildConfigField("String", "BASE_URL", "\"http://10.99.171.192:8000/api/\"")
//        buildConfigField("String", "BASE_URL", "\"http://10.10.202.138:8000/api/\"")
//        buildConfigField("String", "BASE_URL", "\"http://10.233.25.192:8000/api/\"")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.compose.material3:material3:1.3.0")
    // Pastikan juga compose foundation kamu update (opsional tapi disarankan)
    implementation("androidx.compose.foundation:foundation:1.7.0")
    //hilt
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.pusher:pusher-java-client:2.4.2")
    implementation("androidx.compose.material:material-icons-extended:1.7.6")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    // Navigasi antar layar
    implementation("androidx.navigation:navigation-compose:2.7.7")
    // ViewModel untuk Logic
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")

    // Retrofit & Gson (Network)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Tambahkan library Firebase Authentication
    // Biarkan tanpa angka, karena versinya sudah diatur oleh BoM (platform(...))
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-common-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")
//    implementation(libs.firebase.firestore.ktx)
//    implementation(libs.firebase.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.github.CanHub:Android-Image-Cropper:4.5.0")
}