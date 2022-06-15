package yellow

import yellow.TokenType.*

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {

  // global environment
  val globals = Environment()

  // Resolved environment location of the local variables (Static Analysis)
  private val locals = mutableMapOf<Expr, Int>()

  // current environment in scope
  private var environment = globals

  // Define <native functions> here
  init {
    globals.define("__print__", __print__)
    globals.define("__println__", __println__)
    globals.define("__input__", __input__)

    globals.define("__list__", __list__)
    globals.define("__list__append__", __list__append__)
    globals.define("__list__get__", __list__get__)
    globals.define("__list__set__", __list__set__)
    globals.define("__list__delete__", __list__delete__)
    globals.define("__list__len__", __list__len__)
  }

  fun interpret(statements: List<Stmt>) {
    try {
      statements.forEach { execute(it) }
    } catch (err: RuntimeError) {
      Yellow.runtimError(err)
    }
  }

  // using expr instead of lexeme as keys to the table
  // because there can be multiple expressions
  // in different scopes that reference the same variable using the same lexeme
  fun resolve(expr: Expr, distance: Int) {
    locals.put(expr, distance)
  }

  //            Stmt

  override fun visitBlockStmt(stmt: Stmt.Block) {
    executeBlock(stmt.statements, Environment(environment))
  }

  // executes a block:
  // - change the current environment with the new provided env
  // - Call execute on the statements
  // - restore the previous env
  // using try and finally to ensure that the env is restored in case of a exception
  fun executeBlock(statements: List<Stmt>, environment: Environment) {
    val previous = this.environment
    try {
      this.environment = environment
      statements.forEach { execute(it) }
    } finally {
      this.environment = previous
    }
  }

  // Create a class definition:
  // - Define the class name in the env
  // - Create method objects and create the class object
  // - Assign the class name the class object
  override fun visitClassStmt(stmt: Stmt.Class) {
    var superclass: Any? = null
    stmt.superclass?.let {
      superclass = evaluate(stmt.superclass)
      if (superclass !is YellowClass) {
        throw RuntimeError(stmt.superclass.name, "Superclass must be a class")
      }
    }

    environment.define(stmt.name.lexeme, null)

    stmt.superclass?.let {
      environment = Environment(environment)
      environment.define("super", superclass)
    }

    val methods = mutableMapOf<String, YellowFunction>()
    stmt.methods.forEach { method ->
      val function = YellowFunction(method, environment, method.name.lexeme.equals("init"))
      methods[method.name.lexeme] = function
    }
    val cls = YellowClass(stmt.name.lexeme, methods, null)
    stmt.superclass?.let { environment = environment.enclosing!! }
    environment.assign(stmt.name, cls)
  }

  override fun visitExpressionStmt(stmt: Stmt.Expression) {
    evaluate(stmt.expression)
  }

  // Define a function:
  // - create a function object
  // - bind the name to the object in the current env
  override fun visitFunctionStmt(stmt: Stmt.Function) {
    val function = YellowFunction(stmt, environment, false)
    environment.define(stmt.name.lexeme, function)
  }

  override fun visitIfStmt(stmt: Stmt.If) {
    if (truthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch)
    } else {
      stmt.elseBranch?.let { execute(stmt.elseBranch) }
    }
  }

  override fun visitImportStmt(stmt: Stmt.Import) {
    if (stmt.file !is Expr.Literal && (stmt.file as Expr.Literal).value !is String) {
      throw RuntimeError(stmt.token, "File name should be a string literal")
    }
    try {
      val userDir = System.getProperty("user.home")
      val file = ((stmt.file).value as String)
      if (file.trim().startsWith("std:")) {
        val filename = file.substring(4)
        filename.trim()
        Yellow.runFile("$userDir/.yellow/std/$filename.yellow")
      } else {
        throw RuntimeError(stmt.token, "Invalid import")
      }
    } catch (err: java.nio.file.NoSuchFileException) {
      throw RuntimeError(stmt.token, "Can't find the import")
    }
  }

  override fun visitPrintStmt(stmt: Stmt.Print) {
    val value = evaluate(stmt.expression)
    println(stringify(value))
  }

  override fun visitReturnStmt(stmt: Stmt.Return) {
    stmt.value?.let { throw Return(evaluate(stmt.value)) }
  }

  // Define a variable:
  // - Evaluate the initialiser if not null
  // - Define the variable and bind the value
  override fun visitVarStmt(stmt: Stmt.Var) {
    val value = if (stmt.value != null) evaluate(stmt.value) else null
    environment.define(stmt.name.lexeme, value)
  }

  override fun visitWhileStmt(stmt: Stmt.While) {
    while (truthy(evaluate(stmt.condition))) execute(stmt.body)
  }

  //                Expr
  override fun visitAssignExpr(expr: Expr.Assign): Any? {
    val value = evaluate(expr.value)
    val distance = locals[expr]
    if (distance != null) environment.assignAt(distance, expr.name, value)
    else globals.assign(expr.name, value)
    return value
  }

  // Binary Operators
  override fun visitBinaryExpr(expr: Expr.Binary): Any? {
    val left = evaluate(expr.left)
    val right = evaluate(expr.right)

    when (expr.operator.type) {
      GREATER -> {
        checkNumberOperands(expr.operator, left, right)
        return (left as Double) > (right as Double)
      }
      GREATER_EQUAL -> {
        checkNumberOperands(expr.operator, left, right)
        return (left as Double) >= (right as Double)
      }
      LESS -> {
        checkNumberOperands(expr.operator, left, right)
        return (left as Double) < (right as Double)
      }
      LESS_EQUAL -> {
        checkNumberOperands(expr.operator, left, right)
        return (left as Double) <= (right as Double)
      }
      MINUS -> {
        checkNumberOperands(expr.operator, left, right)
        return (left as Double) - (right as Double)
      }
      SLASH -> {
        checkNumberOperands(expr.operator, left, right)
        return (left as Double) / (right as Double)
      }
      STAR -> {
        checkNumberOperands(expr.operator, left, right)
        return (left as Double) * (right as Double)
      }
      BANG_EQUAL -> {
        return !equality(left, right)
      }
      EQUAL_EQUAL -> {
        return equality(left, right)
      }
      PLUS -> {
        if (left is Double && right is Double) return left + right
        if (left is String && right is String) return left + right

        throw RuntimeError(expr.operator, "Operands must be two numbers or two strings")
      }
      else -> {
        return null
      }
    }
  }

  override fun visitCallExpr(expr: Expr.Call): Any? {
    val callee = evaluate(expr.callee)

    val arguments = mutableListOf<Any?>()
    for (argument in expr.args) {
      arguments.add(evaluate(argument))
    }
    if (callee is YellowCallable) {
      val function = callee
      if (arguments.size != function.arity()) {
        throw RuntimeError(
            expr.paren,
            "Expected ${function.arity()} arguments but got ${arguments.size}"
        )
      }
      try {
        return function.call(this, arguments)
      } catch (err: NativeFunctionError) {
        throw RuntimeError(expr.paren, err.message!!)
      }
    } else {
      throw RuntimeError(expr.paren, "can only call functions and classes.")
    }
  }

  override fun visitGetExpr(expr: Expr.Get): Any? {
    val obj = evaluate(expr.obj)
    if (obj is YellowInstance) {
      return obj.get(expr.name)
    }
    throw RuntimeError(expr.name, "Only instances have properties")
  }

  override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
    return evaluate(expr.expr)
  }

  override fun visitLiteralExpr(expr: Expr.Literal): Any? {
    return expr.value
  }

  override fun visitLogicalExpr(expr: Expr.Logical): Any? {
    val left = evaluate(expr.left)

    if (expr.operator.type == OR) {
      if (truthy(left)) return left
    } else {
      if (!truthy(left)) return left
    }
    return evaluate(expr.right)
  }

  override fun visitSetExpr(expr: Expr.Set): Any? {
    val obj = evaluate(expr.obj)

    if (obj is YellowInstance) {
      val value = evaluate(expr.value)
      obj.set(expr.name, value)
      return value
    } else {
      throw RuntimeError(expr.name, "Only instances have fields")
    }
  }

  override fun visitSuperExpr(expr: Expr.Super): Any? {
    val distance = locals[expr]!!
    val superclass = environment.getAt(distance, "super") as YellowClass
    val obj = environment.getAt(distance - 1, "this") as YellowInstance
    val method = superclass.findMethod(expr.method.lexeme)
    method?.let {
      return method.bind(obj)
    }
    throw RuntimeError(expr.method, "Undefined property '${expr.method.lexeme}'.")
  }

  override fun visitThisExpr(expr: Expr.This): Any? {
    return lookupVariable(expr.keyword, expr)
  }

  override fun visitUnaryExpr(expr: Expr.Unary): Any? {
    val right = evaluate(expr.right)
    when (expr.operator.type) {
      BANG -> return !truthy(right)
      MINUS -> {
        checkNumberOperands(expr.operator, right)
        return -(right as Double)
      }
      else -> return null
    }
  }

  override fun visitVariableExpr(expr: Expr.Variable): Any? {
    return lookupVariable(expr.name, expr)
  }

  // Helpers
  // Helper to call the Interpreter visitor on statement nodes
  private fun execute(stmt: Stmt) {
    stmt.accept(this)
  }

  // Helper to call the Interpreter visitor on expressions
  private fun evaluate(expr: Expr): Any? {
    return expr.accept(this)
  }

  //
  private fun truthy(obj: Any?): Boolean {
    return when (obj) {
      null -> false
      is Boolean -> obj
      else -> true
    }
  }

  private fun equality(a: Any?, b: Any?): Boolean {
    if (a == null && b == null) return true
    if (a == null) return false
    return a.equals(b)
  }

  private fun checkNumberOperands(op: Token, vararg operands: Any?) {
    operands.forEach { if (it !is Double) throw RuntimeError(op, "operand(s) must be a number.") }
  }

  private fun lookupVariable(name: Token, expr: Expr): Any? {
    val distance = locals[expr]
    if (distance != null) {
      return environment.getAt(distance, name.lexeme)
    } else {
      return globals.get(name)
    }
  }

  private fun stringify(obj: Any?): String {
    if (obj == null) return "nil"

    if (obj is Double) {
      var text = obj.toString()
      if (text.endsWith(".0")) text = text.substring(0, text.length - 2)
      return text
    }
    return obj.toString()
  }
}

class RuntimeError(val token: Token, message: String) : RuntimeException(message) {}
