plugins {
    id("java-common")
}

dependencies {
    compile(project(":theta-common"))
    compile(project(":theta-core"))
    compile(project(":theta-solver"))
    compile(project(":theta-solver-z3"))
    implementation(Deps.z3)
    compile("com.koloboke:koloboke-api-jdk8:1.0.0")
    runtime("com.koloboke:koloboke-impl-jdk8:1.0.0")
}