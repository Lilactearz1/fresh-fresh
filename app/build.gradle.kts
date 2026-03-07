plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.movix.transak_infield"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.movix.transak_infield"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.2"

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

	    // 👇 Important for LocalDate on older devices
	    isCoreLibraryDesugaringEnabled=true

    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
//		its default it allows you to use the jetpack to compose the ui
        compose = true
//        view binding allow you to connect input  ids directly to your code
        viewBinding =true

    }
    packaging {



        resources {
            excludes += setOf(
                "META-INF/io.netty.versions.properties",
                "META-INF/INDEX.LIST",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/DEPENDENCIES"
            )
        }
    }
}

dependencies {
	implementation("androidx.compose.material3:material3:1.3.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation ("androidx.coordinatorlayout:coordinatorlayout:1.3.0")
    implementation(libs.material)
    implementation(libs.androidx.navigation.runtime.android)
	implementation(libs.androidx.material3.android)
	implementation(libs.androidx.tv.material)
	implementation(libs.androidx.activity)
	implementation(libs.places)
	implementation(libs.androidx.media3.common.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.androidx.work.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
	testImplementation(libs.junit.jupiter)
	androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("com.itextpdf:itext7-core:7.2.5")
	implementation( "com.google.zxing:core:3.5.3")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
	coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.0.4")
	implementation ("com.android.volley:volley:1.2.1")

// Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
// OKHttp for custom server uploads
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("com.google.firebase:firebase-storage-ktx")

    //supabase to replace the firebase store and clouds

    implementation("io.github.jan-tennert.supabase:storage-kt:2.5.4")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.5.4")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.5.4")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.5.4")
    implementation("io.ktor:ktor-client-android:2.3.7")

    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Retrofit 3.0 core library
    implementation("com.squareup.retrofit2:retrofit:3.0.0")

    // Gson converter for JSON serialization/deserialization
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // OkHttp logging interceptor (optional, but highly recommended for debugging)
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.12")

//     for debug to view your database outside the code  (database debug)
//    debugImplementation("im.dino:dbinspector:3.4.1@aar")
}
configurations.all {
    exclude(group = "com.google.protobuf", module = "protobuf-java")
}