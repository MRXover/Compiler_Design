package test;

import automatons.SLR_Automaton;
import main.Grammar;
import util.Production;
import org.junit.Test;
import util.*;
import static util.SupportFunctions.*;

import java.io.IOException;
import java.util.ArrayList;

public class TestSLR {
    private Grammar grammar;

    @Test
    public void testClosure() throws IOException {
        grammar = new Grammar("example10.txt");
        grammar.augmentGivenGrammar();

        ArrayList<Production> expected = new ArrayList<>();

        for(Production pro : grammar.Productions)
            expected.add(createItem(0, pro));

        SLR_Automaton SLR = new SLR_Automaton(grammar);

        ArrayList<Production> result = SLR.CLOSURE(expected.get(0));

        for(int i = 0; i < expected.size(); i++){
            //System.out.println(expected.get(i) + " == " + result.get(i));
            assert(expected.get(i).equals(result.get(i)));
        }
    }

    @Test
    public void testGoTo() throws IOException {
        grammar = new Grammar("example10.txt");
        grammar.augmentGivenGrammar();
        SLR_Automaton SLR = new SLR_Automaton(grammar);

        ArrayList<Production> C = new ArrayList<>();
        ArrayList<Production> expected = new ArrayList<>();

        Production p1 = createItem(1, grammar.Productions.get(0));
        Production p2 = createItem(1, grammar.Productions.get(1));
        C.add(p1);
        C.add(p2);

        expected.add(createItem(2, grammar.Productions.get(1)));
        for (int i = 3; i < grammar.Productions.size(); i++)
            expected.add(createItem(0, grammar.Productions.get(i)));

        ArrayList<Production> result = SLR.GOTO(C, new Token("+", "TERMINAL"));

        for(int i = 0; i < expected.size(); i++){
            //System.out.println(expected.get(i) + " == " + result.get(i));
            assert(expected.get(i).equals(result.get(i)));
        }
    }


    @Test
    public void testGoTo1() throws IOException {
        grammar = new Grammar("example10.txt");
        grammar.augmentGivenGrammar();
        SLR_Automaton SLR = new SLR_Automaton(grammar);

        ArrayList<Production> C = new ArrayList<>();
        Production p1 = createItem(0, grammar.Productions.get(0));
        Production p2 = createItem(1, grammar.Productions.get(1));
        //C.add(p1);
        //C.add(p2);
        C = SLR.CLOSURE(p1);

        System.out.println("P1 = " + p1);
        System.out.println("P2 = " + p2);

        ArrayList<Production> result = SLR.GOTO(C, new Token("T", "NONTERMINAL"));
        System.out.println(result);
    }




}
