package ttt.action.jvm

import ttt.action.Action

/**
 * 基础的Action类。
 *
 * 通常只包含一个方法，或者只包含一个用[Main]修饰的方法。
 * 该方法会用来生成一个[ttt.action.context.Context]。
 */
interface JvmAction : Action {

}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Main(val canBeOverride: Boolean)

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Named(val name: String = "")