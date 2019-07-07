package ttt.action.runtime.interpreter.property

import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

/**
 * 类型
 *
 * 类型的本质，是用于定义某个数据体的结构。
 * 类型的目的，是用来在不同的Action之间作为某种特殊的标的传输数据。
 * 区别于名称，类型往往是带有一定的共通性的，作为一种非个性化的连接存在。
 * 因此，对于在Action间作为标记传输的类型而言，一般代表了两类操作：
 * 1. 类型的兼容性，用于判断输入和输出是否能够正常连接
 * 2. 类型的转换，用于将不同的类型通过一些固定的胶水代码进行连接
 *
 * 因此，类型的从设计上，需要尽可能多的记录下用于做以上两种操作的信息。
 * 从组合的层面上来讲，又需要类型所携带的信息尽可能精简。
 * 因此，使用一个唯一的字符串用来表示类型，同时将类型的操作外置化。
 * 这意味着，不同的类型操作器需要自己对类型进行理解，并提取出其需要的信息。
 *
 */
data class Type(val name: String) {

    companion object {

        /**
         * Kotlin的类型系统，目前需要针对
         */
        fun jvm(type: KType): Type {
            return Type(type.jvmErasure.qualifiedName!!)
        }

    }
}
