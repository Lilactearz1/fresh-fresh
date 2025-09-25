plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.movix.transak_infield"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.movix.transak_infield"
        minSdk = 24
        targetSdk = 35
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








//     for debug to view your database outside the code  (database debug)
//    debugImplementation("im.dino:dbinspector:3.4.1@aar")
}