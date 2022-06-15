package yellow

import java.util.Stack

class StaticAnalysis(val interpreter: Interpreter) : Expr.Visitor<Unit>, Stmt.Visitor<Unit> {

  companion object {

    private enum class ClassScope {
      NONE,
      CLASS,
      SUBCLASS
    }

    private enum class FunctionScope {
      NONE,
      METHOD,
      FUNCTION,
      INITIALISER
    }
  }

  private var curClassScope: ClassScope = ClassScope.NONE
  private var curFunctionScope: FunctionScope = FunctionScope.NONE

  private val scopes = Stack<MutableMap<String, Boolean>>()

  // Resolves the scope information, calls the accept function with the StaticAnalysis visitor
  fun resolve(statements: List<Stmt>) {
    for (statement in statements) {
      resolve(statement)
    }
  }

  fun resolve(statement: Stmt) {
    statement.accept(this)
  }

  fun resolve(expression: Expr) {
    expression.accept(this)
  }

  // ------------- Visitor Impl -------------

  //                    Stmt

  override fun visitBlockStmt(stmt: Stmt.Block): Unit {
    beginScope()
    resolve(stmt.statements)
    endScope()
  }

  // Initialises the class scope and add this and the methods to the scope
  override fun visitClassStmt(stmt: Stmt.Class) {
    var enclosingClassScope = curClassScope
    curClassScope = ClassScope.CLASS
    declare(stmt.name)
    define(stmt.name)

    if (stmt.superclass != null && stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
      Yellow.error(stmt.superclass.name, "A class can't inherit from itself")
    }

    stmt.superclass?.let { it ->
      curClassScope = ClassScope.SUBCLASS
      resolve(it)
      beginScope()
      scopes.peek().put("super", true)
    }

    beginScope()
    scopes.peek()["this"] = true
    for (method in stmt.methods) {
      resolveFunction(method, FunctionScope.METHOD)
    }
    endScope()
    stmt.superclass?.let { endScope() }
    curClassScope = enclosingClassScope
  }

  override fun visitExpressionStmt(stmt: Stmt.Expression) {
    resolve(stmt.expression)
  }

  override fun visitFunctionStmt(stmt: Stmt.Function) {
    declare(stmt.name)
    define(stmt.name)
    resolveFunction(stmt, FunctionScope.FUNCTION)
  }

  override fun visitIfStmt(stmt: Stmt.If) {
    resolve(stmt.condition)
    resolve(stmt.thenBranch)
    stmt.elseBranch?.let { resolve(it) }
  }

  override fun visitImportStmt(stmt: Stmt.Import) {
    resolve(stmt.file)
  }

  override fun visitPrintStmt(stmt: Stmt.Print) {
    resolve(stmt.expression)
  }

  // check if the return statement is not is a function and object initialisers cant return
  // value, empty return is allowed tho
  override fun visitReturnStmt(stmt: Stmt.Return) {
    if (curFunctionScope == FunctionScope.NONE) {
      Yellow.error(stmt.keyword, "Can't return from top-level code")
    }
    stmt.value?.let {
      if (curFunctionScope == FunctionScope.INITIALISER) {
        Yellow.error(stmt.keyword, "Can't return a value from an initialiser")
      }
      resolve(stmt.value)
    }
  }

  override fun visitVarStmt(stmt: Stmt.Var) {
    declare(stmt.name)
    stmt.value?.let { resolve(stmt.value) }
    define(stmt.name)
  }

  override fun visitWhileStmt(stmt: Stmt.While) {
    resolve(stmt.condition)
    resolve(stmt.body)
  }

  //                EXPR

  override fun visitAssignExpr(expr: Expr.Assign) {
    resolve(expr.value)
    resolveLocal(expr, expr.name)
  }

  override fun visitBinaryExpr(expr: Expr.Binary) {
    resolve(expr.left)
    resolve(expr.right)
  }

  override fun visitCallExpr(expr: Expr.Call) {
    resolve(expr.callee)

    for (argument in expr.args) {
      resolve(argument)
    }
  }

  override fun visitGetExpr(expr: Expr.Get) {
    resolve(expr.obj)
  }

  override fun visitGroupingExpr(expr: Expr.Grouping) {
    resolve(expr.expr)
  }

  override fun visitLiteralExpr(expr: Expr.Literal) {}

  override fun visitLogicalExpr(expr: Expr.Logical) {
    resolve(expr.left)
    resolve(expr.right)
  }

  override fun visitSetExpr(expr: Expr.Set) {
    resolve(expr.value)
    resolve(expr.obj)
  }

  override fun visitSuperExpr(expr: Expr.Super) {
    if (curClassScope == ClassScope.NONE) {
      Yellow.error(expr.keyword, "Can't use 'super' outside of a class")
    } else if (curClassScope != ClassScope.SUBCLASS) {
      Yellow.error(expr.keyword, "Can't use 'super' in a class with no superclass.")
    }
    resolveLocal(expr, expr.keyword)
  }

  override fun visitThisExpr(expr: Expr.This) {
    if (curClassScope == ClassScope.NONE) {
      Yellow.error(expr.keyword, "Can't use 'this' outside of a class")
      return
    }
    resolveLocal(expr, expr.keyword)
  }

  override fun visitUnaryExpr(expr: Expr.Unary) {
    resolve(expr.right)
  }

  override fun visitVariableExpr(expr: Expr.Variable) {
    if (!scopes.empty() && scopes.peek().get(expr.name.lexeme) == false) {
      Yellow.error(expr.name, "Can't read local variable in its own initialiser")
    }
    resolveLocal(expr, expr.name)
  }

  // ------------- Helpers -------------------

  // Adds a new scope
  private fun beginScope() {
    scopes.push(mutableMapOf<String, Boolean>())
  }

  // Removes the current scope
  private fun endScope() {
    scopes.pop()
  }

  // defines a new name binding
  private fun define(name: Token) {
    if (scopes.empty()) return
    scopes.peek()[name.lexeme] = true
  }

  // declares a name binding as ready for use, re-declaration is an error
  //  <exception: (global namespace)>
  private fun declare(name: Token) {
    // global scope
    if (scopes.empty()) return

    val scope = scopes.peek()

    if (scope.containsKey(name.lexeme)) {
      Yellow.error(name, "Already a variable with this name in this scope.")
    } else {
      scope[name.lexeme] = false
    }
  }

  // declares a function in a new scope along with the parameters
  private fun resolveFunction(function: Stmt.Function, scope: FunctionScope) {
    var enclosingFunctionScope = curFunctionScope
    curFunctionScope = scope
    beginScope()
    for (param in function.params) {
      declare(param)
      define(param)
    }
    resolve(function.body)
    endScope()
    curFunctionScope = enclosingFunctionScope
  }

  // resolve the environment location for the local name
  private fun resolveLocal(expr: Expr, name: Token) {
    for (i in scopes.size - 1 downTo 0) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(expr, scopes.size - 1 - i)
        return
      }
    }
  }
}
