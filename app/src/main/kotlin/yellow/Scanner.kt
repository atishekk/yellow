package yellow

import yellow.TokenType.*

class Scanner(private val source: String) {
  private val tokens = mutableListOf<Token>()
  private var start = 0
  private var current = 0
  private var line = 1

  private var atEnd = false
    get() {
      return source.length <= current
    }

  private var peek: Char = source[0]
    get() {
      return if (atEnd) 0.toChar() else source[current]
    }

  private var lookahead: Char = source[0]
    get() {
      return if (current + 1 >= source.length) 0.toChar() else source[current + 1]
    }

  companion object {
    val keywords =
        mapOf(
            "and" to AND,
            "class" to CLASS,
            "else" to ELSE,
            "false" to FALSE,
            "for" to FOR,
            "fun" to FUN,
            "if" to IF,
            "nil" to NIL,
            "or" to OR,
            "print" to PRINT,
            "return" to RETURN,
            "super" to SUPER,
            "this" to THIS,
            "true" to TRUE,
            "var" to VAR,
            "while" to WHILE,
        )
  }

  // ============ Public methods ==================

  public fun scan(): List<Token> {
    while (!atEnd) {
      start = current
      val tkn = scanToken()
      tkn?.let { tokens.add(tkn) }
    }
    tokens.add(Token(EOF, "", null, line))
    return tokens.toList()
  }

  // ============ Private methods ==================

  private fun scanToken(): Token? {
    val c = advance()
    return when (c) {
      '(' -> token(LEFT_PAREN)
      ')' -> token(RIGHT_PAREN)
      '{' -> token(LEFT_BRACE)
      '}' -> token(RIGHT_BRACE)
      ',' -> token(COMMA)
      '.' -> token(DOT)
      '-' -> token(MINUS)
      '+' -> token(PLUS)
      ';' -> token(SEMICOLON)
      '*' -> token(STAR)
      '!' -> {
        return if (match('=')) token(BANG_EQUAL) else token(BANG)
      }
      '=' -> {
        return if (match('=')) token(EQUAL_EQUAL) else token(EQUAL)
      }
      '<' -> {
        return if (match('=')) token(LESS_EQUAL) else token(LESS)
      }
      '>' -> {
        return if (match('=')) token(GREATER_EQUAL) else token(GREATER)
      }
      '/' -> {
        if (match('/')) {
          while (peek != '\n' && !atEnd) advance()
          return null
        } else {
          return token(SLASH)
        }
      }
      ' ', '\r', '\t' -> null
      '\n' -> {
        line++
        return null
      }
      '"' -> string()
      else -> {
        if (digit(c)) {
          return number()
        } else if (alphabet(c)) {
          return identifier()
        } else {
          Yellow.error(line, "unexpected character.")
          return null
        }
      }
    }
  }

  private fun identifier(): Token {
    while (alphanumeric(peek)) advance()

    val text = source.substring(start, current)
    var type = keywords[text]
    if (type == null) type = IDENTIFIER
    return token(type)
  }

  private fun number(): Token {
    while (digit(peek)) advance()

    if (peek == '.' && digit(lookahead)) {
      advance()
      while (digit(peek)) advance()
    }
    val value = source.substring(start, current).toDouble()
    return token(NUMBER, value)
  }

  private fun string(): Token? {
    while (peek != '"' && !atEnd) {
      if (peek == '\n') line++
      advance()
    }
    if (atEnd) {
      Yellow.error(line, "unterminated string.")
      return null
    }

    // Consume the ending '"'
    advance()
    var value = source.substring(start + 1, current - 1)
    return token(STRING, value)
  }

  private fun alphabet(c: Char): Boolean {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'
  }

  private fun digit(c: Char): Boolean {
    return (c >= '0' && c <= '9')
  }

  private fun alphanumeric(c: Char): Boolean {
    return alphabet(c) || digit(c)
  }

  private fun advance(): Char {
    return source[current++]
  }

  private fun match(expected: Char): Boolean {
    if (atEnd) return false
    if (source[current] != expected) return false
    current++
    return true
  }

  private fun token(type: TokenType): Token {
    return token(type, null)
  }

  private fun token(type: TokenType, literal: Any?): Token {
    val lexeme = source.substring(start, current)
    return Token(type, lexeme, literal, line)
  }
}
