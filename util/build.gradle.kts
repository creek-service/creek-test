plugins {
    `java-library`
}

val creekVersion : String by extra

dependencies {
    api("org.creek:creek-base-annotation:$creekVersion")

    testImplementation(project(":hamcrest"))
}