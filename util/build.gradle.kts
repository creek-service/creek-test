plugins {
    `java-library`
}

val creekVersion : String by extra

dependencies {
    api("org.creekservice:creek-base-annotation:$creekVersion")

    testImplementation(project(":hamcrest"))
    testImplementation(project(":conformity"))
}