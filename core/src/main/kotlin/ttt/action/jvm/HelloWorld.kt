package ttt.action.jvm

import java.io.PrintStream

class HelloWorld : JvmAction {

    fun hello(out: PrintStream = System.out, @Named("name") name: String = "世界") {
        out.println("你好！$name！")
    }
}