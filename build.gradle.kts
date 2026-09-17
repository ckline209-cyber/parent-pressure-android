plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}

val outOfOneDriveBuildRoot = file("C:/GradleBuilds/parent-pressure-android")
layout.buildDirectory.set(outOfOneDriveBuildRoot.resolve("root"))
subprojects {
    layout.buildDirectory.set(outOfOneDriveBuildRoot.resolve(name))
}
