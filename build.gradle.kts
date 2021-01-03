plugins {
    id("com.android.application") version "4.1.1" apply false
    kotlin("android") version "1.4.21" apply false
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

val clean by tasks.registering(Delete::class) {
    delete(rootProject.buildDir)
}
