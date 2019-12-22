package minegame159.koa;

public class Lexer {
    private final String source;

    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Lexer(String source) {
        this.source = source;
    }

    public Token scanToken() {
        Token token = skipWhitespace();
        if (token != null) return token;
        start = current;

        if (isAtEnd()) return makeToken(Token.Type.Eof);

        char c = advance();
        if (isDigit(c) || (c == '-' && isDigit(peek()))) return number();
        else if (isAlpha(c)) return identifier();

        switch (c) {
            case '(': return makeToken(Token.Type.LeftParen);
            case ')': return makeToken(Token.Type.RightParen);
            case '{': return makeToken(Token.Type.LeftBrace);
            case '}': return makeToken(Token.Type.RightBrace);
            case '[': return makeToken(Token.Type.LeftBracket);
            case ']': return makeToken(Token.Type.RightBracket);
            case ',': return makeToken(Token.Type.Comma);
            case '.': return makeToken(Token.Type.Dot);
            case ';': return makeToken(Token.Type.Semicolon);
            case ':': return makeToken(Token.Type.Colon);
            case '+': if (match('=')) return makeToken(Token.Type.PlusEqual); else if (match('+')) return makeToken(Token.Type.PlusPlus); return makeToken(Token.Type.Plus);
            case '-': if (match('=')) return makeToken(Token.Type.MinusEqual); else if (match('-')) return makeToken(Token.Type.MinusMinus); return makeToken(Token.Type.Minus);
            case '*': if (match('=')) return makeToken(Token.Type.StarEqual); return makeToken(Token.Type.Star);
            case '/': if (match('=')) return makeToken(Token.Type.SlashEqual); return makeToken(Token.Type.Slash);
            case '%': if (match('=')) return makeToken(Token.Type.PercentageEqual); return makeToken(Token.Type.Percentage);
            case '=': if (match('=')) return makeToken(Token.Type.EqualEqual); return makeToken(Token.Type.Equal);
            case '!': if (match('=')) return makeToken(Token.Type.BangEqual); return makeToken(Token.Type.Bang);
            case '>': if (match('=')) return makeToken(Token.Type.GreaterEqual); return makeToken(Token.Type.Greater);
            case '<': if (match('=')) return makeToken(Token.Type.LessEqual); return makeToken(Token.Type.Less);
            case '"': return string();
            default:  return errorToken("Unexpected character.");
        }
    }

    private Token string() {
        while (peek() != '"') {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) return errorToken("Unterminated string.");

        advance();
        return makeToken(Token.Type.String);
    }

    private Token number() {
        while (isDigit(peek())) advance();

        if (peek() == '.') {
            advance();
            while (isDigit(peek())) advance();
        }

        return makeToken(Token.Type.Number);
    }

    private Token identifier() {
        while (isAlphaNumeric(peek())) advance();
        return makeToken(identifierType());
    }

    private Token.Type identifierType() {
        switch (source.charAt(start)) {
            case 'a': return checkKeyword(1, "nd", Token.Type.And);
            case 'b': return checkKeyword(1, "reak", Token.Type.Break);
            case 'c': return checkKeyword(1, "ontinue", Token.Type.Continue);
            case 'e': return checkKeyword(1, "lse", Token.Type.Else);
            case 'f':
                if (current - start > 1) {
                    switch (source.charAt(start + 1)) {
                        case 'a': return checkKeyword(2, "lse", Token.Type.False);
                        case 'o': return checkKeyword(2, "r", Token.Type.For);
                        case 'u': return checkKeyword(2, "nction", Token.Type.Function);
                    }
                }
            case 'i': return checkKeyword(1, "f", Token.Type.If);
            case 'n': return checkKeyword(1, "il", Token.Type.Nil);
            case 'o': return checkKeyword(1, "r", Token.Type.Or);
            case 'r': return checkKeyword(1, "eturn", Token.Type.Return);
            case 's': return checkKeyword(1, "elf", Token.Type.Self);
            case 't': return checkKeyword(1, "rue", Token.Type.True);
            case 'v': return checkKeyword(1, "ar", Token.Type.Var);
            case 'w': return checkKeyword(1, "hile", Token.Type.While);
            default:  return Token.Type.Identifier;
        }
    }

    private Token skipWhitespace() {
        while (true) {
            switch (peek()) {
                case ' ':
                case '\r':
                case '\t': advance(); break;
                case '\n': line++; advance(); break;
                case '/':
                    if (peekNext() == '/') {
                        advance();
                        advance();
                        while (!isAtEnd() && peek() != '\n') advance();
                    } else if (peekNext() == '*') {
                        advance();
                        advance();
                        while (!isAtEnd()) {
                            if (peek() == '*' && peekNext() == '/') break;
                            if (peek() == '\n') line++;
                            advance();
                        }
                        if (peek() == '*' && peekNext() == '/') {
                            advance();
                            advance();
                        } else return errorToken("Unterminated multi-line comment.");
                    }
                    break;
                default: return null;
            }
        }
    }

    // Helper methods

    private Token.Type checkKeyword(int start, String rest, Token.Type tokenType) {
        if (current - this.start == start + rest.length() && source.substring(this.start + start, this.start + start + rest.length()).equals(rest)) return tokenType;
        return Token.Type.Identifier;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAtEnd(int offset) {
        return current + offset >= source.length();
    }
    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    private char peekNext() {
        if (isAtEnd(1)) return '\0';
        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private Token makeToken(Token.Type tokenType) {
        return new Token(tokenType, source.substring(start, current), line);
    }
    private Token errorToken(String message) {
        return new Token(Token.Type.Error, message, line);
    }
}
