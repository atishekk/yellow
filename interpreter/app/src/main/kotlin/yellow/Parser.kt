package yellow

/*
  program -> (declaration)*
*/

import kotlin.collections.mutableListOf
import yellow.TokenType.*

class Parser(val tokens: List<Token>) {
  companion object {
    class ParserError : RuntimeException() {}

    private enum class FunctionType {
      FUNCTION,
      METHOD
    }
  }

  private var current = 0

  private var atEnd = false
    get() {
      return peek.type == EOF
    }

  private var previous: Token = tokens[0]
    get() {
      return tokens[current - 1]
    }

  private var peek: Token = tokens[0]
    get() {
      return tokens[current]
    }

  private val statements = mutableListOf<Stmt>()

  public fun parse(): List<Stmt> {
    while (!atEnd) {
      declaration()?.let { statements.add(it) }
    }

    return statements.toList()
  }

  // ----------------- Error Handling ---------------------

  // The parser goes into panic mode when an exception is throw
  // This method synchronises the parser at statement boundary
  private fun panicModeRecovery() {
    advance()
    while (!atEnd) {
      if (previous.type == SEMICOLON) return

      when (peek.type) {
        CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
        else -> advance()
      }
    }
  }

  // ------------------- Statements -----------------------

  // declaration -> classDeclaration | functionDeclaration | variableDeclaration
  private fun declaration(): Stmt? {
    try {
      if (match(CLASS)) return classDeclaration()
      if (match(FUN)) return function(FunctionType.FUNCTION)
      if (match(VAR)) return varDeclaration()
      return statement()
    } catch (err: ParserError) {
      panicModeRecovery()
      return null
    }
  }

  // classDeclaration -> class <IDENTIFIER> { methodDeclaration* }
  private fun classDeclaration(): Stmt {
    val name = consume(IDENTIFIER, "Expect class name.")
    consume(LEFT_BRACE, "Expect '{' before class body.")

    val methods = mutableListOf<Stmt.Function>()
    while (!check(RIGHT_BRACE) && !atEnd) {
      methods.add(function(FunctionType.METHOD))
    }
    consume(RIGHT_BRACE, "Expect '}' after class body.")
    return Stmt.Class(name, methods.toList())
  }

  // functionDeclaration -> fun <name : IDENTIFIER>(<parameters: IDENTIFIER>*) blockStatement
  // methodDeclaration -> <name: IDENTIFIER>(<parameters: IDENTIFIER>*) blockStatement
  private fun function(type: FunctionType): Stmt.Function {
    val name = consume(IDENTIFIER, "Expect " + type.toString() + "name.")
    consume(LEFT_PAREN, "Expect '(' after " + type.toString() + "name.")
    val parameters = mutableListOf<Token>()
    if (!check(RIGHT_PAREN)) {
      do {
        if (parameters.size >= 255) {
          error(peek, "Can't have more than 255 parameters.")
        }
        parameters.add(consume(IDENTIFIER, "Expect parameter name"))
      } while (match(COMMA))
    }
    consume(RIGHT_PAREN, "Expect ')' after " + type.toString() + " parameters")
    consume(LEFT_BRACE, "Expect '{' before " + type.toString() + " body.")
    val body = block()
    return Stmt.Function(name, parameters, body)
  }

  // blockStatement -> { declaration* }
  private fun block(): List<Stmt> {
    val statements = mutableListOf<Stmt>()
    while (!check(RIGHT_BRACE) && !atEnd) {
      declaration()?.let { statements.add(it) }
    }
    consume(RIGHT_BRACE, "Expected '}' after block")
    return statements
  }

  // varDeclaration -> var <name: IDENTIFIER> (= <initialiser: Expr>)? ;
  private fun varDeclaration(): Stmt {
    val name = consume(IDENTIFIER, "Expect variable name")
    val initialiser: Expr? = if (match(EQUAL)) expression() else null
    consume(SEMICOLON, "Expect ';' after variable declaration.")
    return Stmt.Var(name, initialiser)
  }

  // statement -> forStatement | ifStatement | printStatement | returnStatement
  //              | whileStatement
  //              | expressionStatement
  //              | blockStatement
  private fun statement(): Stmt {
    if (match(FOR)) return forStatement()
    if (match(IF)) return ifStatement()
    if (match(PRINT)) return printStatement()
    if (match(RETURN)) return returnStatement()
    if (match(WHILE)) return whileStatement()
    if (match(LEFT_BRACE)) return Stmt.Block(block())
    return expressionStatement()
  }

  // TODO: Range based for loops
  // Currently implemented as a desugared while loop
  // forStatement -> for(<initialiser: Stmt> <condition: Expr>; <update: Expr>) <body: Stmt>
  private fun forStatement(): Stmt {
    consume(LEFT_PAREN, "Expect '(' after 'for'")
    val initialiser: Stmt? =
        if (match(SEMICOLON)) null else if (match(VAR)) varDeclaration() else expressionStatement()

    val condition: Expr = if (!check(SEMICOLON)) expression() else Expr.Literal(true)
    consume(SEMICOLON, "Expect ';' after loop condition.")

    val update: Expr? = if (!check(RIGHT_PAREN)) expression() else null
    consume(RIGHT_PAREN, "Expect ')' after for clauses")

    var body: Stmt = statement()

    update?.let { body = Stmt.Block(listOf(body, Stmt.Expression(update))) }

    body = Stmt.While(condition, body)

    initialiser?.let { body = Stmt.Block(listOf(initialiser, body)) }

    return body
  }

  // else if?? / elif ??
  // ifStatement -> if(<condition: Expr>) <thenBranch:Stmt> (else <elseBranch: Stmt>)?
  private fun ifStatement(): Stmt {
    consume(LEFT_PAREN, "Expect '(' after if.")
    val condition = expression()
    consume(RIGHT_PAREN, "Expect ')' after if condition")

    val thenBranch = statement()
    val elseBranch: Stmt? = if (match(ELSE)) statement() else null

    return Stmt.If(condition, thenBranch, elseBranch)
  }

  // printStatement -> print <expr: Expr> ;
  private fun printStatement(): Stmt {
    val expr = expression()
    consume(SEMICOLON, "Expect ';' after value.")
    return Stmt.Print(expr)
  }

  // returnStatement -> return (<expr: Expr>)? ;
  private fun returnStatement(): Stmt {
    val keyword = previous
    val expr: Expr? = if (!check(SEMICOLON)) expression() else null
    consume(SEMICOLON, "Expect ';' after return value.")
    return Stmt.Return(keyword, expr)
  }

  // whileStatement -> while(<condition: Expr>) <body: Stmt>
  private fun whileStatement(): Stmt {
    consume(LEFT_PAREN, "Expect '(' after while")
    val condition = expression()
    consume(RIGHT_PAREN, "Expect ')' after while condition")
    val body = statement()

    return Stmt.While(condition, body)
  }

  // expressionStatement -> <expr: Expr> ;
  private fun expressionStatement(): Stmt {
    val expr = expression()
    consume(SEMICOLON, "Expect ';' after expression")
    return Stmt.Expression(expr)
  }

  // expression -> assignment
  private fun expression(): Expr {
    return assignment()
  }

  // assignment -> (call .)? IDENTIFIER = assignment | or
  private fun assignment(): Expr {
    val expr = or()

    if (match(EQUAL)) {
      val equals = previous
      val value = assignment()
      // check the target is a valid l-value reference
      if (expr is Expr.Variable) {
        val name = expr.name
        return Expr.Assign(name, value)
      } else if (expr is Expr.Get) {
        val get = expr
        return Expr.Set(get.obj, get.name, value)
      }
      error(equals, "Invalid assignment target")
    }

    return expr
  }

  // or -> and ('or' and)*
  private fun or(): Expr {
    var expr = and()

    while (match(OR)) {
      val operator = previous
      val right = and()
      expr = Expr.Logical(expr, operator, right)
    }
    return expr
  }

  // and -> equality ('and' equality)*
  private fun and(): Expr {
    var expr = equality()

    while (match(OR)) {
      val operator = previous
      val right = equality()
      expr = Expr.Logical(expr, operator, right)
    }
    return expr
  }

  // equality -> comparison (!= | == comparison)*
  private fun equality(): Expr {
    var expr = comparison()
    while (match(BANG_EQUAL, EQUAL_EQUAL)) {
      val operator = previous
      val right = comparison()
      expr = Expr.Binary(expr, operator, right)
    }

    return expr
  }

  // comparison -> term (> | >= | < | <= term)*
  private fun comparison(): Expr {
    var expr = term()

    while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      val operator = previous
      val right = term()
      expr = Expr.Binary(expr, operator, right)
    }

    return expr
  }

  // term -> factor (- | + factor)*
  private fun term(): Expr {
    var expr = factor()
    while (match(MINUS, PLUS)) {
      val operator = previous
      val right = factor()
      expr = Expr.Binary(expr, operator, right)
    }
    return expr
  }

  // factor -> unary (* | / unary)*
  private fun factor(): Expr {
    var expr = unary()
    while (match(STAR, SLASH)) {
      val operator = previous
      val right = unary()
      expr = Expr.Binary(expr, operator, right)
    }
    return expr
  }

  // unary -> (! | -) unary | call
  private fun unary(): Expr {
    if (match(BANG, MINUS)) {
      val operator = previous
      val right = unary()
      return Expr.Unary(operator, right)
    }
    return call()
  }

  private fun callHelper(callee: Expr): Expr {
    val arguments = mutableListOf<Expr>()
    if (!check(RIGHT_PAREN)) {
      do {
        if (arguments.size >= 255) {
          error(peek, "Can't have more than 255 arguments")
        }
        arguments.add(expression())
      } while (match(COMMA))
    }

    val paren = consume(RIGHT_PAREN, "Expect ')' after arguments.")
    return Expr.Call(callee, paren, arguments)
  }

  // call -> primary ( "(" arguments? ")" | "." IDENTIFIER )*
  private fun call(): Expr {
    var expr = primary()
    while (true) {
      if (match(LEFT_PAREN)) {
        expr = callHelper(expr)
      } else if (match(DOT)) {
        val name = consume(IDENTIFIER, "Expect property name after '.'.")
        expr = Expr.Get(expr, name)
      } else {
        break
      }
    }
    return expr
  }

  private fun primary(): Expr {
    if (match(FALSE)) return Expr.Literal(false)
    if (match(TRUE)) return Expr.Literal(true)
    if (match(NIL)) return Expr.Literal(null)
    if (match(THIS)) return Expr.This(previous)
    if (match(NUMBER, STRING)) return Expr.Literal(previous.literal)

    if (match(IDENTIFIER)) return Expr.Variable(previous)

    if (match(LEFT_PAREN)) {
      val expr = expression()
      consume(RIGHT_PAREN, "Expect ')' after expression")
      return Expr.Grouping(expr)
    }

    throw error(peek, "Expect expression")
  }

  private fun consume(type: TokenType, msg: String): Token {
    if (check(type)) return advance()
    throw error(peek, msg)
  }

  private fun match(vararg types: TokenType): Boolean {
    for (type in types) {
      if (check(type)) {
        advance()
        return true
      }
    }
    return false
  }

  private fun advance(): Token {
    if (!atEnd) current++
    return previous
  }

  private fun check(type: TokenType): Boolean {
    if (atEnd) return false
    return peek.type == type
  }
  private fun error(tkn: Token, msg: String): ParserError {
    Yellow.error(tkn, msg)
    return ParserError()
  }
}
