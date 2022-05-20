plugins {
    `java-library`
}

val creekVersion : String by extra
val spotBugsVersion : String by extra

dependencies {
    api("org.creek:creek-base-annotation:$creekVersion")
    api("com.github.spotbugs:spotbugs-annotations:$spotBugsVersion")

    testImplementation(project(":hamcrest"))
    testImplementation(project(":conformity"))
}