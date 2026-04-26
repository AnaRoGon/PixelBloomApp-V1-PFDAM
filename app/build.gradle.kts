plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "dam.pfdam.pixelbloom"
    compileSdk = 36

    defaultConfig {
        applicationId = "dam.pfdam.pixelbloom"
        minSdk = 29
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

    //Para el mockeo de datos en los tests
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Dependencia para Firebase
    implementation(platform(libs.firebase.bom))
    // Dependencia para Authentication
    implementation(libs.firebase.auth)
    // Dependencia para la splashScreen
    implementation(libs.androidx.core.splashscreen)
    //Dependencia para la conexión con Firestore
    implementation(libs.firebase.firestore)
    // Dependencia para Storage
    implementation(libs.firebase.storage)
    // Dependencia para picasso
    implementation(libs.picasso)

    //Dependencias para la navegación entre fragmentos
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //Dependencia para tomar los datos de un json para el mockeo y pruebas
    implementation(libs.gson)
    //Dependencia de Retrofit
    implementation(libs.retrofit)
    //PAra que retrofit entienda el formato JSON
    implementation(libs.converter.gson)

    //Dependencia de taptargetview para la guía interactiva.
    implementation(libs.taptargetview)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    // Dependencias para testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Implementación de la librería de MockK y Core para el testeo
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.core.testing)
}