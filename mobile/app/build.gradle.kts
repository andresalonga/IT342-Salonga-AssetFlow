plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "edu.cit.salonga.assetflow"
    compileSdk = 34

    defaultConfig {
        applicationId = "edu.cit.salonga.assetflow"
        minSdk = 33
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val googleWebClientId = System.getenv("GOOGLE_OAUTH_WEB_CLIENT_ID")
            ?: System.getenv("GOOGLE_OAUTH_CLIENT_ID")
            ?: project.findProperty("GOOGLE_OAUTH_WEB_CLIENT_ID")?.toString()
            ?: project.findProperty("GOOGLE_OAUTH_CLIENT_ID")?.toString()
            ?: "22999592860-r41k8gqk2kgkkl4p8h0k4s978d9qdsen.apps.googleusercontent.com"
        buildConfigField("String", "DEFAULT_WEB_CLIENT_ID", "\"$googleWebClientId\"")
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
        compose = false
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.material)
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // Retrofit & OkHttp (for API calls)
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Glide (for Image Loading)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // Gson (for JSON parsing)
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Coroutines (for async operations)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}