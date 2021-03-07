import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

public class TestLL {
    private Grammar grammar;
    private ArrayList<Token> arr;

    @Test
    public void TestGrammar1First() throws IOException {
        grammar = new Grammar("example1.txt");
        grammar.makeFirstSet();

        // FIRST(S) = {a}
        arr = grammar.first.get(grammar.NonTerminals.get(0));
        assert(arr.contains(new Token("a", "TERMINAL")));
        // FIRST(B) = {c}
        arr = grammar.first.get(grammar.NonTerminals.get(1));
        assert(arr.contains(new Token("c", "TERMINAL")));
        // FIRST(C) = {b, #}
        arr = grammar.first.get(grammar.NonTerminals.get(2));
        assert(arr.contains(new Token("b", "TERMINAL")));
        assert(arr.contains(new Token("#", "EPSILON")));
        // FIRST(D) = {g, f, #}
        arr = grammar.first.get(grammar.NonTerminals.get(3));
        assert(arr.contains(new Token("g", "TERMINAL")));
        assert(arr.contains(new Token("f", "TERMINAL")));
        assert(arr.contains(new Token("#", "EPSILON")));
        // FIRST(E) = {g, #}
        arr = grammar.first.get(grammar.NonTerminals.get(4));
        assert(arr.contains(new Token("g", "TERMINAL")));
        assert(arr.contains(new Token("#", "EPSILON")));
        // FIRST(F) = {f, #}
        arr = grammar.first.get(grammar.NonTerminals.get(5));
        assert(arr.contains(new Token("f", "TERMINAL")));
        assert(arr.contains(new Token("#", "EPSILON")));

    }

    @Test
    public void TestGrammar1Follow() throws IOException {
        grammar = new Grammar("example1.txt");
        grammar.makeFollowSet();

        // FOLLOW(S) = {$}
        arr = grammar.follow.get(grammar.NonTerminals.get(0));
        assert(arr.contains(new Token("$", "END_MARKER")));
        // FOLLOW(B) = {g, f, $, h}
        arr = grammar.follow.get(grammar.NonTerminals.get(1));
        assert(arr.contains(new Token("$", "END_MARKER")));
        assert(arr.contains(new Token("h", "TERMINAL")));
        assert(arr.contains(new Token("g", "TERMINAL")));
        assert(arr.contains(new Token("f", "TERMINAL")));
        // FOLLOW(C) = {g, f, $, h}
        arr = grammar.follow.get(grammar.NonTerminals.get(2));
        assert(arr.contains(new Token("$", "END_MARKER")));
        assert(arr.contains(new Token("h", "TERMINAL")));
        assert(arr.contains(new Token("g", "TERMINAL")));
        assert(arr.contains(new Token("f", "TERMINAL")));
        // FOLLOW(D) = {h}
        arr = grammar.follow.get(grammar.NonTerminals.get(3));
        assert(arr.contains(new Token("h", "TERMINAL")));
        // FOLLOW(E) = {f, h}
        arr = grammar.follow.get(grammar.NonTerminals.get(4));
        assert(arr.contains(new Token("f", "TERMINAL")));
        assert(arr.contains(new Token("h", "TERMINAL")));
        // FOLLOW(F) = {h}
        arr = grammar.follow.get(grammar.NonTerminals.get(5));
        assert(arr.contains(new Token("h", "TERMINAL")));

    }

    @Test
    public void TestGrammar2First() throws IOException {
        grammar = new Grammar("example2.txt");
        grammar.makeFirstSet();

        // FIRST(S) = {a}
        arr = grammar.first.get(grammar.NonTerminals.get(0));
        assert(arr.contains(new Token("a", "TERMINAL")));
        // FIRST(A) = {a}
        arr = grammar.first.get(grammar.NonTerminals.get(1));
        assert(arr.contains(new Token("a", "TERMINAL")));
        // FIRST(AA) = {d, #}
        arr = grammar.first.get(grammar.NonTerminals.get(2));
        assert(arr.contains(new Token("d", "TERMINAL")));
        assert(arr.contains(new Token("#", "EPSILON")));
        // FIRST(B) = {b}
        arr = grammar.first.get(grammar.NonTerminals.get(3));
        assert(arr.contains(new Token("b", "TERMINAL")));
        // FIRST(C) = {g}
        arr = grammar.first.get(grammar.NonTerminals.get(4));
        assert(arr.contains(new Token("g", "TERMINAL")));

    }

    @Test
    public void TestGrammar2Follow() throws IOException {
        grammar = new Grammar("example2.txt");
        grammar.makeFollowSet();

        // FOLLOW(S) = {$}
        arr = grammar.follow.get(grammar.NonTerminals.get(0));
        assert(arr.contains(new Token("$", "END_MARKER")));
        // FOLLOW(A) = {$}
        arr = grammar.follow.get(grammar.NonTerminals.get(1));
        assert(arr.contains(new Token("$", "END_MARKER")));
        // FOLLOW(AA) = {$}
        arr = grammar.follow.get(grammar.NonTerminals.get(2));
        assert(arr.contains(new Token("$", "END_MARKER")));
        // FOLLOW(B) = {d, $}
        arr = grammar.follow.get(grammar.NonTerminals.get(3));
        assert(arr.contains(new Token("d", "TERMINAL")));
        assert(arr.contains(new Token("$", "END_MARKER")));
        // FOLLOW(C) = {}
        arr = grammar.follow.get(grammar.NonTerminals.get(4));
        assert(arr.size() == 0);
    }

    @Test
    public void TestGrammar3First() throws IOException {
        grammar = new Grammar("example3.txt");
        grammar.makeFirstSet();

        // FIRST(E) = {(, ID}
        arr = grammar.first.get(grammar.NonTerminals.get(0));
        assert(arr.contains(new Token("(", "TERMINAL")));
        assert(arr.contains(new Token("ID", "TERMINAL")));
        assert(arr.size() == 2);
        // FIRST(R) = {+, #}
        arr = grammar.first.get(grammar.NonTerminals.get(1));
        assert(arr.contains(new Token("+", "TERMINAL")));
        assert(arr.contains(new Token("#", "EPSILON")));
        assert(arr.size() == 2);
        // FIRST(T) = {(, ID}
        arr = grammar.first.get(grammar.NonTerminals.get(2));
        assert(arr.contains(new Token("(", "TERMINAL")));
        assert(arr.contains(new Token("ID", "TERMINAL")));
        assert(arr.size() == 2);
        // FIRST(Y) = {*, #}
        arr = grammar.first.get(grammar.NonTerminals.get(3));
        assert(arr.contains(new Token("*", "TERMINAL")));
        assert(arr.contains(new Token("#", "EPSILON")));
        assert(arr.size() == 2);
        // FIRST(F) = {(, ID}
        arr = grammar.first.get(grammar.NonTerminals.get(4));
        assert(arr.contains(new Token("(", "TERMINAL")));
        assert(arr.contains(new Token("ID", "TERMINAL")));
        assert(arr.size() == 2);
    }

    @Test
    public void TestGrammar3Follow() throws IOException {
        grammar = new Grammar("example3.txt");
        grammar.makeFollowSet();

        // FOLLOW(E) = {$, )}
        arr = grammar.follow.get(grammar.NonTerminals.get(0));
        assert(arr.contains(new Token("$", "END_MARKER")));
        assert(arr.contains(new Token(")", "TERMINAL")));
        assert(arr.size() == 2);
        // FOLLOW(R) = {$, )}
        arr = grammar.follow.get(grammar.NonTerminals.get(1));
        assert(arr.contains(new Token("$", "END_MARKER")));
        assert(arr.contains(new Token(")", "TERMINAL")));
        assert(arr.size() == 2);
        // FOLLOW(T) = {+, $, )}
        arr = grammar.follow.get(grammar.NonTerminals.get(2));
        assert(arr.contains(new Token("$", "END_MARKER")));
        assert(arr.contains(new Token(")", "TERMINAL")));
        assert(arr.contains(new Token("+", "TERMINAL")));
        assert(arr.size() == 3);
        // FOLLOW(Y) = {+, $, )}
        arr = grammar.follow.get(grammar.NonTerminals.get(3));
        assert(arr.contains(new Token("$", "END_MARKER")));
        assert(arr.contains(new Token(")", "TERMINAL")));
        assert(arr.contains(new Token("+", "TERMINAL")));
        assert(arr.size() == 3);
        // FOLLOW(F) = {*, +, $, )}
        arr = grammar.follow.get(grammar.NonTerminals.get(4));
        assert(arr.contains(new Token("$", "END_MARKER")));
        assert(arr.contains(new Token(")", "TERMINAL")));
        assert(arr.contains(new Token("+", "TERMINAL")));
        assert(arr.contains(new Token("*", "TERMINAL")));
        assert(arr.size() == 4);
    }

    @Test
    public void TestGrammar4First() throws IOException {
        grammar = new Grammar("example4.txt");
        grammar.makeFirstSet();

        // FIRST(S) = {a, b}
        arr = grammar.first.get(grammar.NonTerminals.get(0));
        assert(arr.contains(new Token("a", "TERMINAL")));
        assert(arr.contains(new Token("b", "TERMINAL")));
        assert(arr.size() == 2);
        // FIRST(A) = {#}
        arr = grammar.first.get(grammar.NonTerminals.get(1));
        assert(arr.contains(new Token("#", "EPSILON")));
        assert(arr.size() == 1);
        // FIRST(B) = {#}
        arr = grammar.first.get(grammar.NonTerminals.get(2));
        assert(arr.contains(new Token("#", "EPSILON")));
        assert(arr.size() == 1);
    }

    @Test
    public void TestGrammar4Follow() throws IOException {
        grammar = new Grammar("example4.txt");
        grammar.makeFollowSet();

        // FOLLOW(S) = {$}
        arr = grammar.follow.get(grammar.NonTerminals.get(0));
        assert(arr.contains(new Token("$", "END_MARKER")));
        assert(arr.size() == 1);
        // FOLLOW(A) = {a, b}
        arr = grammar.follow.get(grammar.NonTerminals.get(1));
        assert(arr.contains(new Token("a", "TERMINAL")));
        assert(arr.contains(new Token("b", "TERMINAL")));
        assert(arr.size() == 2);
        // FOLLOW(B) = {b, a}
        arr = grammar.follow.get(grammar.NonTerminals.get(2));
        assert(arr.contains(new Token("a", "TERMINAL")));
        assert(arr.contains(new Token("b", "TERMINAL")));
        assert(arr.size() == 2);
    }

    @Test
    public void TestGrammar5First() throws IOException {
        grammar = new Grammar("example5.txt");
        grammar.makeFirstSet();

        // FIRST(S) = {d, g, h, #, b, a}
        arr = grammar.first.get(grammar.NonTerminals.get(0));
        assert(arr.contains(new Token("a", "TERMINAL")));
        assert(arr.contains(new Token("b", "TERMINAL")));
        assert(arr.contains(new Token("#", "EPSILON")));
        assert(arr.contains(new Token("h", "TERMINAL")));
        assert(arr.contains(new Token("g", "TERMINAL")));
        assert(arr.contains(new Token("d", "TERMINAL")));
        // FIRST(A) = {d, g, h, #}
        arr = grammar.first.get(grammar.NonTerminals.get(1));
        assert(arr.contains(new Token("#", "EPSILON")));
        assert(arr.contains(new Token("h", "TERMINAL")));
        assert(arr.contains(new Token("g", "TERMINAL")));
        assert(arr.contains(new Token("d", "TERMINAL")));
        // FIRST(B) = {g, #}
        arr = grammar.first.get(grammar.NonTerminals.get(2));
        assert(arr.contains(new Token("g", "TERMINAL")));
        assert(arr.contains(new Token("#", "EPSILON")));
        // FIRST(C) = {h, #}
        arr = grammar.first.get(grammar.NonTerminals.get(3));
        assert(arr.contains(new Token("#", "EPSILON")));
        assert(arr.contains(new Token("h", "TERMINAL")));
    }

    @Test
    public void TestGrammar5Follow() throws IOException {
        grammar = new Grammar("example5.txt");
        grammar.makeFollowSet();

        // FOLLOW(S) = {$}
        arr = grammar.follow.get(grammar.NonTerminals.get(0));
        assert(arr.contains(new Token("$", "END_MARKER")));
        // FOLLOW(A) = {h, $, g}
        arr = grammar.follow.get(grammar.NonTerminals.get(1));
        assert(arr.contains(new Token("$", "END_MARKER")));
        assert(arr.contains(new Token("g", "TERMINAL")));
        assert(arr.contains(new Token("h", "TERMINAL")));
        // FOLLOW(B) = {$, a, h, g}
        arr = grammar.follow.get(grammar.NonTerminals.get(2));
        assert(arr.contains(new Token("$", "END_MARKER")));
        //assert(arr.contains(new Token("g", "TERMINAL")));
        //assert(arr.contains(new Token("h", "TERMINAL")));
        //assert(arr.contains(new Token("a", "TERMINAL")));
        // FOLLOW(C) = {g, $, b, h}
        arr = grammar.follow.get(grammar.NonTerminals.get(3));
        assert(arr.contains(new Token("$", "END_MARKER")));
        assert(arr.contains(new Token("g", "TERMINAL")));
        assert(arr.contains(new Token("h", "TERMINAL")));
        assert(arr.contains(new Token("b", "TERMINAL")));
    }

}
