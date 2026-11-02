import java.util.Properties

plugins{
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localSettings = Properties()
val localSettingsFile = rootProject.file("local.properties")
if (localSettingsFile.exists()){
    localSettingsFile.inputStream().use(localSettings::load)
}
val microsoftClientId = localSettings.getProperty("microsoft.clientId", "")
val microsoftSignatureHash = localSettings.getProperty("microsoft.signatureHash", "")

android{
    namespace = "cl.saba.operaciones"
    compileSdk = 36

    defaultConfig{
        applicationId = "cl.saba.operaciones"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
        buildConfigField("String", "MICROSOFT_CLIENT_ID", "\"$microsoftClientId\"")
        buildConfigField("String", "MICROSOFT_SIGNATURE_HASH", "\"$microsoftSignatureHash\"")
        buildConfigField("boolean", "PREVIEW_HOME", "false")
        manifestPlaceholders["microsoftSignatureHash"] = microsoftSignatureHash.ifBlank{ "unconfigured" }
    }

    buildTypes{
        create("preview"){
            initWith(getByName("debug"))
            applicationIdSuffix = ".preview"
            versionNameSuffix = "-preview"
            buildConfigField("boolean", "PREVIEW_HOME", "true")
            resValue("string", "app_name", "Saba · Vista previa")
            matchingFallbacks += "debug"
        }
    }

    compileOptions{
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures{
        buildConfig = true
        compose = true
    }
}

kotlin{
    compilerOptions{
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies{
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.icons)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.microsoft.msal)
}
