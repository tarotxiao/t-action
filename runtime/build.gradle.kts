group = "ttt.action"
version = "1.0-SNAPSHOT"

dependencies {
    compile(project(":action-core"))
    compile(project(":action-compiler"))
    compile(kotlin("reflect"))
}
