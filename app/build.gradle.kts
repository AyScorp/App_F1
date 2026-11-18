plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.f1widget2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.f1widget2"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // ... reste de la configuration
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // AJOUTEZ ICI les nouvelles dépendances
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
