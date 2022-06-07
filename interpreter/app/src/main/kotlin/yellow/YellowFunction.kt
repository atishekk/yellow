package yellow

class Return(val value: Any?) : RuntimeException(null, null, false, false) {}

class YellowFunction(
    private val function: Stmt.Function,
    private val closure: Environment,
    private val initialiser: Boolean
) : YellowCallable {

  fun bind(instance: YellowInstance): YellowFunction {
    val environment = Environment(closure)
    environment.define("this", instance)
    return YellowFunction(function, environment, initialiser)
  }

  override fun arity(): Int {
    return function.params.size
  }

  override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
    // create the function env
    val environment = Environment(closure)
    // define function args in the env
    function.params.zip(args).forEach { (param, arg) -> environment.define(param.lexeme, arg) }

    // Execute the value and capture the return
    // return the value
    try {
      interpreter.executeBlock(function.body, environment)
    } catch (returnVal: Return) {
      if (initialiser) return closure.getAt(0, "this")
      return returnVal.value
    }

    if (initialiser) return closure.getAt(0, "this")
    // No value returned
    return null
  }

  override fun toString(): String {
    return "<fn ${function.name.lexeme}>"
  }
}
