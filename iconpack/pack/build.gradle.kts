plugins {
    id("com.android.application")
}

android {
    namespace = "app.deliverance.iconpack"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "app.deliverance.iconpack"
        minSdk = 26
        targetSdk = 36
        versionCode = System.getenv("VERSION_CODE_OVERRIDE")?.toIntOrNull() ?: 1
        versionName = "0.1.0"
    }

    // One plate shape per flavor, installable side by side: heater shield, round seal, or bare emblem.
    flavorDimensions += "plate"
    productFlavors {
        create("heater") {
            dimension = "plate"
            applicationIdSuffix = ".heater"
        }
        create("seal") {
            dimension = "plate"
            applicationIdSuffix = ".seal"
        }
        create("bare") {
            dimension = "plate"
            applicationIdSuffix = ".bare"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}
