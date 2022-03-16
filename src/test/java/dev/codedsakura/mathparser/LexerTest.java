package dev.codedsakura.mathparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LexerTest {

    @Test
    void tokenizeTestSimple() {
        Assertions.assertArrayEquals(
                new String[]{"1", "+", "1"},
                new Lexer("1 + 1").getTokens().toArray(new String[0])
        );
    }

    @Test
    void tokenizeTestFloats() {
        assertArrayEquals(
                new String[]{"1.5", "==", "15e-1"},
                new Lexer("1.5 == 15e-1").getTokens().toArray(new String[0])
        );
    }

    @Test
    void tokenizeTestFunction() {
        assertArrayEquals(
                new String[]{"sin", "(", "pi", ")", ">>>", "1"},
                new Lexer("sin(pi) >>> 1").getTokens().toArray(new String[0])
        );
    }

    @Test
    void tokenizeTestNegatives() {
        assertArrayEquals(
                new String[]{"-", "77", "==", "-", "(", "11", "*", "7", ")"},
                new Lexer("-77 == -( 11*7 )").getTokens().toArray(new String[0])
        );
    }

    @Test
    void tokenizeTestStrings() {
        assertArrayEquals(
                new String[]{"\"1+1>0\"", "+", "'\"\\''"},
                new Lexer("\"1+1>0\" + '\"\\''").getTokens().toArray(new String[0])
        );
    }

    @Test
    void tokenizeTestAtan2() {
        assertArrayEquals(
                new String[]{"atan", "2", "(", "2", ",", "5", ")"},
                new Lexer("atan2(2, 5)").getTokens().toArray(new String[0])
        );
    }

    @Test
    void tokenizeTestArray() {
        assertArrayEquals(
                new String[]{"[", "'qwe'", ",", "4", ",", ".12", ",", "]", "[", "0", "]"},
                new Lexer("['qwe', 4, .12, ][0]").getTokens().toArray(new String[0])
        );
    }

    @Test
    void tokenizeTestObject() {
        assertArrayEquals(
                new String[]{"{", "key", ":", "0xC0FFEE", ",", "...", "{", "[", "12", "]", ":", "[", "]", "}", ",", "''", ":", "-", "0", "}"},
                new Lexer("{key: 0xC0FFEE, ...{[12]: []}, '': -0}").getTokens().toArray(new String[0])
        );
    }

    @Test
    void tokenizeTestNumbers() {
        assertArrayEquals(
                new String[]{"[", "123_456", ",", "123_456e-123_123", ",", "0b1010_1000", ",", "0o761_253", ",", "0xC0F_FEE", "]"},
                new Lexer("[ 123_456, 123_456e-123_123, 0b1010_1000, 0o761_253, 0xC0F_FEE ]").getTokens().toArray(new String[0])
        );
    }

    @Test
    void tokenizeTestExceptions() {
        assertThrows(Lexer.SyntaxError.class, () -> new Lexer("1e2e3"));
        assertThrows(Lexer.SyntaxError.class, () -> new Lexer("1egg"));
        assertThrows(Lexer.SyntaxError.class, () -> new Lexer("'unclosed"));
    }
}
