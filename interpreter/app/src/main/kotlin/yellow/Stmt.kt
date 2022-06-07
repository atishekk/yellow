package yellow

abstract class Stmt {
  interface Visitor<T> {
    fun visitBlockStmt(stmt: Block): T
    fun visitClassStmt(stmt: Class): T
    fun visitFunctionStmt(stmt: Function): T
    fun visitExpressionStmt(stmt: Expression): T
    fun visitIfStmt(stmt: If): T
    fun visitPrintStmt(stmt: Print): T
    fun visitReturnStmt(stmt: Return): T
    fun visitVarStmt(stmt: Var): T
    fun visitWhileStmt(stmt: While): T
  }

  // Block statements: {<block: List<Stmt>}
  class Block(
      val statements: List<Stmt>,
  ) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitBlockStmt(this)
    }
  }

  // Class node
  class Class(
      val name: Token,
      val methods: List<Stmt.Function>,
      val superclass: Expr.Variable?,
  ) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitClassStmt(this)
    }
  }

  // Expression statements - expression + ';'
  class Expression(
      val expression: Expr,
  ) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitExpressionStmt(this)
    }
  }

  // Function Declaration
  class Function(
      val name: Token,
      val params: List<Token>,
      val body: List<Stmt>,
  ) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitFunctionStmt(this)
    }
  }

  // if(<condition: Expr>) <thenBranch:Stmt> else <elseBranch:Stmt>
  class If(
      val condition: Expr,
      val thenBranch: Stmt,
      val elseBranch: Stmt?,
  ) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitIfStmt(this)
    }
  }

  // Debug print stmt
  class Print(
      val expression: Expr,
  ) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitPrintStmt(this)
    }
  }

  class Return(
      val keyword: Token,
      val value: Expr?,
  ) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitReturnStmt(this)
    }
  }

  class Var(
      val name: Token,
      val value: Expr?,
  ) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitVarStmt(this)
    }
  }

  class While(
      val condition: Expr,
      val body: Stmt,
  ) : Stmt() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitWhileStmt(this)
    }
  }

  abstract fun <T> accept(visitor: Visitor<T>): T
}
