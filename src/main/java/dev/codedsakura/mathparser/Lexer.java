package dev.codedsakura.mathparser;

import java.util.ArrayList;
import java.util.List;

class Lexer {
    private static final String WHITESPACE = " \t\n";
    private static final String QUOTES = "\"'";
    private static final char ESCAPE = '\\';

    private static final String NUMBERS = "0123456789._";
    private static final String NUM_EXPONENTS = "eE";
    private static final String NUM_EXP_NEGATIVE = "-";
    private static final String NUM_BASE_PREFIX = "0";
    private static final char NUM_HEX_INDICATOR = 'x';
    private static final char NUM_OCT_INDICATOR = 'o';
    private static final char NUM_BIN_INDICATOR = 'b';
    private static final String NUM_HEX = "0123456789ABCDEFabcdef_";
    private static final String NUM_OCT = "01234567_";
    private static final String NUM_BIN = "01_";

    private static final String PARENTHESIS = "()[]{}";

    private static final String[] EXPRESSION_SPLIT_END = {":"};


    private final List<String> tokens = new ArrayList<>();

    Lexer(String data) {
        tokenize(data);
    }

    private void addExpression(StringBuilder current) {
        String expr = current.toString();
        for (String end : EXPRESSION_SPLIT_END) {
            if (expr.length() <= end.length())
                continue;

            if (expr.endsWith(end)) {
                tokens.add(expr.substring(0, expr.length() - end.length()));
                tokens.add(end);
                return;
            }
        }
        tokens.add(expr);
    }

    private void tokenize(String data) {
        var current = new StringBuilder();
        var state = State.NONE;

        char[] charArray = data.toCharArray();
        for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
            char c = charArray[i];

            switch (state) {
                case NONE -> {
                    if (WHITESPACE.indexOf(c) >= 0) {
                        break;
                    }

                    current = new StringBuilder();
                    current.append(c);
                    if (QUOTES.indexOf(c) >= 0) {
                        state = State.STRING;
                    } else if (NUMBERS.indexOf(c) >= 0) {
                        state = State.NUMBER;
                    } else if (PARENTHESIS.indexOf(c) >= 0) {
                        tokens.add(current.toString());
                    } else {
                        state = State.EXPRESSION;
                    }
                }
                case NUMBER -> {
                    if (current.toString().equals(NUM_BASE_PREFIX)) {
                        state = switch (c) {
                            case NUM_HEX_INDICATOR -> State.NUMBER_HEX;
                            case NUM_OCT_INDICATOR -> State.NUMBER_OCT;
                            case NUM_BIN_INDICATOR -> State.NUMBER_BIN;
                            default -> state;
                        };
                        if (state != State.NUMBER) {
                            current.append(c);
                            break;
                        }
                    }

                    if (NUMBERS.indexOf(c) >= 0) {
                        current.append(c);
                    } else if (NUM_EXPONENTS.indexOf(c) >= 0) {
                        for (char expChar : NUM_EXPONENTS.toCharArray()) {
                            if (current.indexOf(Character.toString(expChar)) >= 0) {
                                throw new SyntaxError("Invalid or unexpected token");
                            }
                        }

                        current.append(c);
                        state = State.NUMBER_EXP;
                    } else if (WHITESPACE.indexOf(c) >= 0) {
                        tokens.add(current.toString());
                        state = State.NONE;
                    } else {
                        tokens.add(current.toString());
                        state = State.NONE;
                        i--;
                    }
                }
                case NUMBER_EXP -> {
                    if (NUMBERS.indexOf(c) >= 0 || NUM_EXP_NEGATIVE.indexOf(c) >= 0) {
                        current.append(c);
                        state = State.NUMBER;
                    } else {
                        throw new SyntaxError("Invalid or unexpected token");
                    }
                }
                case NUMBER_HEX, NUMBER_OCT, NUMBER_BIN -> {
                    String numStr = switch (state) {
                        case NUMBER_HEX -> NUM_HEX;
                        case NUMBER_OCT -> NUM_OCT;
                        case NUMBER_BIN -> NUM_BIN;
                        // unreachable
                        default -> throw new SyntaxError("Invalid or unexpected token");
                    };
                    if (numStr.indexOf(c) >= 0) {
                        current.append(c);
                    } else if (WHITESPACE.indexOf(c) >= 0) {
                        tokens.add(current.toString());
                        state = State.NONE;
                    } else {
                        tokens.add(current.toString());
                        state = State.NONE;
                        i--;
                    }
                }
                case EXPRESSION -> {
                    if (WHITESPACE.indexOf(c) >= 0) {
                        addExpression(current);
                        state = State.NONE;
                    } else if (PARENTHESIS.indexOf(c) >= 0) {
                        addExpression(current);
                        state = State.NONE;
                        tokens.add(Character.toString(c));
                    } else if (NUMBERS.indexOf(c) >= 0) {
                        addExpression(current);
                        state = State.NONE;
                        i--;
                    } else {
                        current.append(c);
                    }
                }
                case STRING -> {
                    current.append(c);
                    if (c == current.charAt(0)) {
                        var escaped = false;
                        // .length() is 1 more than the index of last char, and last char is known (a quote)
                        var index = current.length() - 2;
                        while (index >= 0 && current.charAt(index) == ESCAPE) {
                            escaped = !escaped;
                            index--;
                        }
                        if (escaped) break;

                        tokens.add(current.toString());
                        state = State.NONE;
                    }
                }
            }
        }

        switch (state) {
            case NUMBER, NUMBER_HEX, NUMBER_OCT, NUMBER_BIN, EXPRESSION -> tokens.add(current.toString());
            case NONE -> {
            }

            default -> throw new SyntaxError("Invalid or unexpected token");
        }
    }

    public List<String> getTokens() {
        return tokens;
    }

    private enum State {
        NONE,
        NUMBER, NUMBER_EXP, NUMBER_HEX, NUMBER_OCT, NUMBER_BIN,
        EXPRESSION,
        STRING
    }

    public static class SyntaxError extends RuntimeException {
        SyntaxError(String message) {
            super(message);
        }
    }
}
