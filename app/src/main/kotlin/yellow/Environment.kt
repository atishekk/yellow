package yellow

class Environment(val enclosing: Environment? = null) {
  val values = mutableMapOf<String, Any?>()

  fun define(name: String, value: Any?) {
    values.put(name, value)
  }

  fun assign(name: Token, value: Any?) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value)
      return
    }
    enclosing?.let { enclosing.assign(name, value) }

    throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
  }

  fun assignAt(distance: Int, name: Token, value: Any?) {
    ancestor(distance)?.let { it.values[name.lexeme] = value }
  }

  fun getAt(distance: Int, name: String): Any? {
    return ancestor(distance)?.let { it.values[name] }
  }

  fun get(name: Token): Any? {
    if (values.containsKey(name.lexeme)) {
      return values[name.lexeme]
    }
    enclosing?.let {
      return it.get(name)
    }
    throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
  }

  private fun ancestor(distance: Int): Environment? {
    var environment: Environment? = this
    for (i in 1..distance) {
      environment = environment?.enclosing
    }
    return environment
  }
}
