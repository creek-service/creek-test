plugins {
    `java-library`
}

val creekBaseVersion : String by extra
val spotBugsVersion : String by extra

dependencies {
    api("org.creekservice:creek-base-annotation:$creekBaseVersion")
    api("com.github.spotbugs:spotbugs-annotations:$spotBugsVersion")

    testImplementation(project(":hamcrest"))
    testImplementation(project(":conformity"))
}