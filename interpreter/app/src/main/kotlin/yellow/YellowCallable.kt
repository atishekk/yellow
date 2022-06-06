package yellow

interface YellowCallable {
  fun arity(): Int

  fun call(interpreter: Interpreter, args: List<Any?>): Any?
}
