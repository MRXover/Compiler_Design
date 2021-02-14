import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

public class TestLR {
    private Grammar grammar;

    @Test
    public void testClosure() throws IOException {
        grammar = new Grammar("example10.txt");
        grammar.augmentGivenGrammar();

        ArrayList<Production> expected = new ArrayList<>();

        for(Production pro : grammar.Productions)
            expected.add(grammar.createItem(0, pro));

        ArrayList<Production> result = grammar.closure(expected.get(0));

        for(int i = 0; i < expected.size(); i++){
            //System.out.println(expected.get(i) + " == " + result.get(i));
            assert(expected.get(i).equals(result.get(i)));
        }
    }

    @Test
    public void testGoTo() throws IOException {
        grammar = new Grammar("example10.txt");
        grammar.augmentGivenGrammar();

        ArrayList<Production> C = new ArrayList<>();
        ArrayList<Production> expected = new ArrayList<>();

        Production p1 = grammar.createItem(1, grammar.Productions.get(0));
        Production p2 = grammar.createItem(1, grammar.Productions.get(1));
        C.add(p1);
        C.add(p2);

        expected.add(grammar.createItem(2, grammar.Productions.get(1)));
        for (int i = 3; i < grammar.Productions.size(); i++)
            expected.add(grammar.createItem(0, grammar.Productions.get(i)));

        ArrayList<Production> result = grammar.GoTo(C, new Token("+", "TERMINAL"));

        for(int i = 0; i < expected.size(); i++){
            //System.out.println(expected.get(i) + " == " + result.get(i));
            assert(expected.get(i).equals(result.get(i)));
        }
    }


    @Test
    public void testGoTo1() throws IOException {
        grammar = new Grammar("example10.txt");
        grammar.augmentGivenGrammar();

        ArrayList<Production> C = new ArrayList<>();
        Production p1 = grammar.createItem(0, grammar.Productions.get(0));
        Production p2 = grammar.createItem(1, grammar.Productions.get(1));
        //C.add(p1);
        //C.add(p2);
        C = grammar.closure(p1);

        System.out.println("P1 = " + p1);
        System.out.println("P2 = " + p2);

        ArrayList<Production> result = grammar.GoTo(C, new Token("T", "NONTERMINAL"));
        System.out.println(result);
    }



    @Test
    public void testLRParser() throws IOException {
        grammar = new Grammar("example10.txt");
        grammar.augmentGivenGrammar();

        ArrayList<Token> input = grammar.Lexer("ID * ID");

        grammar.LRParser(input);

    }

    @Test
    public void testBuildAllItems() throws IOException {
        grammar = new Grammar("example10.txt");
        grammar.augmentGivenGrammar();

        grammar.buildAllItems();
    }

    @Test
    public void testACTION() throws IOException {
        grammar = new Grammar("example10.txt");
        grammar.augmentGivenGrammar();



    }


}
