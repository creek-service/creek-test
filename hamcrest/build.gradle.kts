plugins {
    `java-library`
}

var hamcrestVersion: String by extra
var spotBugsVersion: String by extra

dependencies {
    api("org.hamcrest:hamcrest-core:$hamcrestVersion")

    testImplementation("com.github.spotbugs:spotbugs-annotations:$spotBugsVersion")
}