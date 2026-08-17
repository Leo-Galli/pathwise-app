plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "it.leogalli.pathwise"
    // 36: richiesto da androidx.health.connect:connect-client:1.1.0
    compileSdk = 36

    defaultConfig {
        applicationId = "it.leogalli.pathwise"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0-beta"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Chiave Google Maps. Inietta via file locale (non committato):
        //   echo "MAPS_API_KEY=AIza..." >> local.properties
        // oppure da variabile d'ambiente. Senza chiave l'app gira ma la mappa è vuota.
        val mapsKey = providers.gradleProperty("MAPS_API_KEY")
            .orElse(providers.environmentVariable("MAPS_API_KEY"))
            .getOrElse("YOUR_MAPS_API_KEY")
        manifestPlaceholders["MAPS_API_KEY"] = mapsKey
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // ── Core AndroidX ─────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // ── Jetpack Compose (BOM) ────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // ── DI: Hilt ─────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // ── Persistenza: Room ────────────────────────────────────
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ── Storage: DataStore Preferences ───────────────────────
    implementation(libs.androidx.datastore.preferences)

    // ── Coroutines ───────────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)
    // Bridge Tasks (Google Play Services) → Coroutine: lastLocation.await()
    implementation(libs.kotlinx.coroutines.play.services)

    // ── Google Maps SDK + Maps Compose ───────────────────────
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // ── Health Connect (sincronizzazione fitness) ────────────
    implementation(libs.androidx.health.connect.client)

    // ── Debug / Preview ──────────────────────────────────────
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ── Test (parità motore) ─────────────────────────────────
    testImplementation("junit:junit:4.13.2")
}
