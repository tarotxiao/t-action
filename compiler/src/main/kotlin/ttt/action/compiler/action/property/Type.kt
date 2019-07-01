package ttt.action.compiler.action.property

import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

data class Type(val name: String) {

    companion object {

        fun jvm(type: KType): Type {
            return Type(type.jvmErasure.qualifiedName!!)
        }

    }
}

