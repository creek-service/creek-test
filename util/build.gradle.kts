plugins {
    `java-library`
}

val spotBugsVersion : String by extra

dependencies {
    api("com.github.spotbugs:spotbugs-annotations:$spotBugsVersion")

    testImplementation(project(":hamcrest"))
    testImplementation(project(":conformity"))
}