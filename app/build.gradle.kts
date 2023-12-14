plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    signingConfigs {
        create("release") {
            keyAlias = "key0"
            keyPassword = "123456"
            storeFile = file("keystore.jks")
            storePassword = "123456"
        }
    }
    namespace = "com.msl.myphotopicker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.msl.myphotopicker"
        minSdk = 24
        targetSdk = 33
        versionCode = 6
        versionName = "6.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
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
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("com.google.android.gms:play-services-ads:22.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("io.github.otpless-tech:otpless-android-sdk:2.1.3")

    implementation("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")

    implementation("com.android.billingclient:billing:6.1.0")
    implementation("com.android.billingclient:billing-ktx:6.1.0")

    implementation("com.facebook.android:audience-network-sdk:6.+")
    implementation("com.facebook.android:facebook-android-sdk:16.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}