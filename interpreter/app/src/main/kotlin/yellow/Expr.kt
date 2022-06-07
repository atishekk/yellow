package yellow

abstract class Expr {
  interface Visitor<T> {
    fun visitBinaryExpr(expr: Binary): T
    fun visitAssignExpr(expr: Assign): T
    fun visitCallExpr(expr: Call): T
    fun visitGetExpr(expr: Get): T
    fun visitGroupingExpr(expr: Grouping): T
    fun visitLiteralExpr(expr: Literal): T
    fun visitLogicalExpr(expr: Logical): T
    fun visitSetExpr(expr: Set): T
    fun visitSuperExpr(expr: Super): T
    fun visitThisExpr(expr: This): T
    fun visitUnaryExpr(expr: Unary): T
    fun visitVariableExpr(expr: Variable): T
  }

  // Assignment expression: <name: Token> = <value: Expr>
  class Assign(
      val name: Token,
      val value: Expr,
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitAssignExpr(this)
    }
  }

  // Binary Expression: <left:Expr> <op:Token> <right:Expr>
  class Binary(
      val left: Expr,
      val operator: Token,
      val right: Expr,
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitBinaryExpr(this)
    }
  }

  // Function call expression: <callee: Expr>(<args: List<Expr>)
  class Call(
      val callee: Expr,
      val paren: Token, // ending right parenthesis - For error reporting
      val args: List<Expr>,
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitCallExpr(this)
    }
  }

  // Object property getter: <Object: Expr>.<name:Token>
  class Get(
      val obj: Expr,
      val name: Token,
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitGetExpr(this)
    }
  }

  // Parenthesis (<expr: Expr>)
  class Grouping(
      val expr: Expr,
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitGroupingExpr(this)
    }
  }

  // Number, string, boolean literal (nil as well)
  class Literal(
      val value: Any?,
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitLiteralExpr(this)
    }
  }

  // Logical operator: and/or (short-circuited)
  class Logical(
      val left: Expr,
      val operator: Token,
      val right: Expr,
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitLogicalExpr(this)
    }
  }

  // Object property setter: <Object:Expr>.<name: Token> = <value: Expr>
  class Set(
      val obj: Expr,
      val name: Token,
      val value: Expr,
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitSetExpr(this)
    }
  }

  // Super class method access: super.<method:IDENTIFIER>
  class Super(
      val keyword: Token,
      val method: Token,
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitSuperExpr(this)
    }
  }

  // this keyword for referencing instance in methods
  class This(
      val keyword: Token,
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitThisExpr(this)
    }
  }

  // Unary operator <op: Token> <value: Expr>
  class Unary(
      val operator: Token,
      val right: Expr,
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitUnaryExpr(this)
    }
  }

  // Accessing variable bindings
  class Variable(
      val name: Token,
  ) : Expr() {
    override fun <T> accept(visitor: Visitor<T>): T {
      return visitor.visitVariableExpr(this)
    }
  }

  abstract fun <T> accept(visitor: Visitor<T>): T
}
