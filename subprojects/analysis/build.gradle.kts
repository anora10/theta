plugins {
    id("java-common")
}

dependencies {
    compile(project(":theta-common"))
    compile(project(":theta-core"))
    compile(project(":theta-solver"))
    compile(project(":theta-expression-diagram"))
    testImplementation(project(":theta-solver-z3"))
}
