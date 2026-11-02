buildscript{
    repositories{
        google()
    }
    dependencies{
        classpath("com.android.tools:r8:9.1.57")
    }
}

plugins{
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
