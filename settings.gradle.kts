pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
//        maven("https://maven.aliyun.com/repository/central")
//        maven("https://maven.aliyun.com/repository/google")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
//        maven("https://maven.aliyun.com/repository/google")
//        maven("https://maven.aliyun.com/repository/central")
        maven("https://jitpack.io")
    }
}


rootProject.name = "lmusic"
include(":app")
include(":ui")
include(":common")
include(":lmedia")
include(":lplayer")
include(":lplaylist")
include(":lhistory")
include(":lartist")
include(":lalbum")
include(":lextension")
include(":crash")
include(":extension-core")
include(":extension-ksp")
include(":component")
includeBuild("register")

val extFile = File(rootDir, "extension")
if (extFile.exists() && extFile.isDirectory) {
    include(":extension")
}