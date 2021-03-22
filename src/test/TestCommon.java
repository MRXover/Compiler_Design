package test;

import main.Grammar;
import org.junit.Test;
import stuff.Token;

import java.io.IOException;
import java.util.ArrayList;


public class TestCommon {
    private Grammar grammar;

    @Test
    public void testLexer() throws IOException {
        grammar = new Grammar("example10.txt");
        String input = "ID + ID";

        ArrayList<Token> result = grammar.Lexer(input);
        ArrayList<Token> expected = new ArrayList<>();

        expected.add(new Token("ID","TERMINAL"));
        expected.add(new Token("+","TERMINAL"));
        expected.add(new Token("ID","TERMINAL"));

        for (int i = 0; i < expected.size(); i++)
            assert(expected.get(i).equals(result.get(i)));

    }
}
