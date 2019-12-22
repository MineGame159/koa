package minegame159.koa;

public class Token {
    public enum Type {
        LeftParen, RightParen, LeftBrace, RightBrace, LeftBracket, RightBracket,
        Comma, Dot, Semicolon, Colon,

        Plus, Minus, Star, Slash, Percentage,
        Equal, EqualEqual, Bang, BangEqual,
        Greater, GreaterEqual, Less, LessEqual,

        PlusEqual, MinusEqual, StarEqual, SlashEqual, PercentageEqual,
        PlusPlus, MinusMinus,

        Identifier,
        String, Number, Nil, True, False,

        If, Else, And, Or,
        While, For, Break, Continue,

        Function, Self, Var, Return,

        Error,
        Eof
    }

    public final Type type;
    public final String lexeme;
    public final int line;

    public Token(Type type, String lexeme, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    @Override
    public String toString() {
        if (lexeme == null) return type + ", " + line;
        return type + ", '" + lexeme + "', " + line;
    }
}
