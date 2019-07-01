package ttt.action.runtime.interpreter

import org.junit.Test
import ttt.action.compound.CompoundAction
import ttt.action.jvm.HelloWorld
import ttt.action.jvm.JvmAction
import ttt.action.jvm.Named
import java.io.PrintStream

class RunnerTest {

    @Test
    fun compoundHlloWorld() {
        CompoundAction(
            listOf(
                object : JvmAction {
                    @Named("name")
                    fun name(): String {
                        return "ttt"
                    }
                },
                object : JvmAction {
                    fun output(): PrintStream {
                        return System.err
                    }
                },
                HelloWorld()
            )
        ).toRunner().runMain()
    }


    @Test
    fun helloWorld() {
        HelloWorld().toRunner().runMain()
    }
}