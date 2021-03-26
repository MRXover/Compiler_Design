package test;

import automatons.LR_Automaton;
import main.Grammar;
import org.junit.Test;
import util.ItemLR;
import util.Token;

import java.io.IOException;
import java.util.ArrayList;

import static util.SupportFunctions.removeDuplicates;

public class TestLR {
    private Grammar grammar;

    @Test
    public void testCLOSURE1() throws IOException {
        LR_Automaton LR = new LR_Automaton(new Grammar("example11.txt"));
        LR.g.printGrammar();
        LR.g.augmentGivenGrammar();

        ItemLR item = new ItemLR(0, LR.g.Productions.get(0));
        item.setTerminal(new Token("$"));

        ArrayList<ItemLR> result = LR.CLOSURE(item);
        ArrayList<ItemLR> expected = new ArrayList<>();
        // S_st -> [(•,DOT), (S,NONTERMINAL)], $
        expected.add(new ItemLR(item));
        //S -> [(•,DOT), (C,NONTERMINAL), (C,NONTERMINAL)], $
        expected.add(new ItemLR(0, LR.g.Productions.get(1), new Token("$")));
        //C -> [(•,DOT), (c,TERMINAL), (C,NONTERMINAL)], c
        expected.add(new ItemLR(0, LR.g.Productions.get(2), new Token("c", "TERMINAL")));
        //C -> [(•,DOT), (c,TERMINAL), (C,NONTERMINAL)], d
        expected.add(new ItemLR(0, LR.g.Productions.get(2), new Token("d", "TERMINAL")));
        //C -> [(•,DOT), (d,TERMINAL)], c
        expected.add(new ItemLR(0, LR.g.Productions.get(3), new Token("c", "TERMINAL")));
        //C -> [(•,DOT), (d,TERMINAL)], d
        expected.add(new ItemLR(0, LR.g.Productions.get(3), new Token("d", "TERMINAL")));

        /*
        S_st -> [(•,DOT), (S,NONTERMINAL)], $
        S -> [(•,DOT), (C,NONTERMINAL), (C,NONTERMINAL)], $
        C -> [(•,DOT), (c,TERMINAL), (C,NONTERMINAL)], c
        C -> [(•,DOT), (c,TERMINAL), (C,NONTERMINAL)], d
        C -> [(•,DOT), (d,TERMINAL)], c
        C -> [(•,DOT), (d,TERMINAL)], d
         */
        assert(expected.size() == result.size());
        for(ItemLR it : expected)
            assert(result.contains(it));
    }

    @Test
    public void testGOTO1() throws IOException {
        LR_Automaton LR = new LR_Automaton(new Grammar("example11.txt"));
        LR.g.augmentGivenGrammar();
        ItemLR item = new ItemLR(0, LR.g.Productions.get(0), new Token("$"));

        ItemLR expected_I1 = new ItemLR(1, LR.g.Productions.get(0), new Token("$"));

        ArrayList<ItemLR> expected_I2 = new ArrayList<>();
        //S -> [(C,NONTERMINAL), (•,DOT), (C,NONTERMINAL)], $
        expected_I2.add(new ItemLR(1, LR.g.Productions.get(1), new Token("$")));
        //C -> [(•,DOT), (c,TERMINAL), (C,NONTERMINAL)], $
        expected_I2.add(new ItemLR(0, LR.g.Productions.get(2), new Token("$")));
        //C -> [(•,DOT), (d,TERMINAL)], $
        expected_I2.add(new ItemLR(0, LR.g.Productions.get(3), new Token("$")));

        ArrayList<ItemLR> expected_I3 = new ArrayList<>();
        //C -> [(c,TERMINAL), (•,DOT), (C,NONTERMINAL)], c
        expected_I3.add(new ItemLR(1, LR.g.Productions.get(2), new Token("c","TERMINAL")));
        //C -> [(•,DOT), (c,TERMINAL), (C,NONTERMINAL)], c
        expected_I3.add(new ItemLR(0, LR.g.Productions.get(2), new Token("c","TERMINAL")));
        //C -> [(•,DOT), (d,TERMINAL)], c
        expected_I3.add(new ItemLR(0, LR.g.Productions.get(3), new Token("c","TERMINAL")));
        //C -> [(c,TERMINAL), (•,DOT), (C,NONTERMINAL)], d
        expected_I3.add(new ItemLR(1, LR.g.Productions.get(2), new Token("d","TERMINAL")));
        //C -> [(•,DOT), (c,TERMINAL), (C,NONTERMINAL)], d
        expected_I3.add(new ItemLR(0, LR.g.Productions.get(2), new Token("d","TERMINAL")));
        //C -> [(•,DOT), (d,TERMINAL)], d
        expected_I3.add(new ItemLR(0, LR.g.Productions.get(3), new Token("d","TERMINAL")));

        ArrayList<ItemLR> expected_I4 = new ArrayList<>();
        //C -> [(d,TERMINAL), (•,DOT)], c
        expected_I4.add(new ItemLR(1, LR.g.Productions.get(3), new Token("c","TERMINAL")));
        //C -> [(d,TERMINAL), (•,DOT)], d
        expected_I4.add(new ItemLR(1, LR.g.Productions.get(3), new Token("d","TERMINAL")));

        ArrayList<ItemLR> I0 = LR.CLOSURE(item);
        ArrayList<ItemLR> I1 = LR.GOTO(I0, new Token("S", "NONTERMINAL"));
        ArrayList<ItemLR> I2 = LR.GOTO(I0, new Token("C", "NONTERMINAL"));
        ArrayList<ItemLR> I3 = LR.GOTO(I0, new Token("c", "TERMINAL"));
        ArrayList<ItemLR> I4 = LR.GOTO(I0, new Token("d", "TERMINAL"));

        assert(I1.size() == 1);
        assert(I1.get(0).equals(expected_I1));

        for(ItemLR it : expected_I2)
            assert(I2.contains(it));

        for(ItemLR it : expected_I3)
            assert(I3.contains(it));

        for(ItemLR it : expected_I4)
            assert(I4.contains(it));
    }


    @Test
    public void testGOTO2() throws IOException {
        LR_Automaton LR = new LR_Automaton(new Grammar("example11.txt"));
        LR.g.augmentGivenGrammar();
        ItemLR item = new ItemLR(0, LR.g.Productions.get(0), new Token("$"));

        ArrayList<ItemLR> I0 = LR.CLOSURE(item);
        ArrayList<ItemLR> I2 = LR.GOTO(I0, new Token("C", "NONTERMINAL"));
        ArrayList<ItemLR> I5 = LR.GOTO(I2, new Token("C", "NONTERMINAL"));
        ArrayList<ItemLR> I6 = LR.GOTO(I2, new Token("c", "TERMINAL"));
        ArrayList<ItemLR> I6_c = LR.GOTO(I6, new Token("c", "TERMINAL"));
        ArrayList<ItemLR> I7_fromI2 = LR.GOTO(I2, new Token("d", "TERMINAL"));
        ArrayList<ItemLR> I7_fromI6 = LR.GOTO(I6, new Token("d", "TERMINAL"));
        ArrayList<ItemLR> I9 = LR.GOTO(I6, new Token("C", "NONTERMINAL"));

        ArrayList<ItemLR> expected_I5 = new ArrayList<>();
        //S -> [(C,NONTERMINAL), (C,NONTERMINAL), (•,DOT)], $
        expected_I5.add(new ItemLR(2, LR.g.Productions.get(1), new Token("$")));

        ArrayList<ItemLR> expected_I6 = new ArrayList<>();
        //C -> [(c,TERMINAL), (•,DOT), (C,NONTERMINAL)], $
        expected_I6.add(new ItemLR(1, LR.g.Productions.get(2), new Token("$")));
        //C -> [(•,DOT), (c,TERMINAL), (C,NONTERMINAL)], $
        expected_I6.add(new ItemLR(0, LR.g.Productions.get(2), new Token("$")));
        //C -> [(•,DOT), (d,TERMINAL)], $
        expected_I6.add(new ItemLR(0, LR.g.Productions.get(3), new Token("$")));

        ArrayList<ItemLR> expected_I7_fromI2 = new ArrayList<>();
        //C -> [(d,TERMINAL), (•,DOT)], $
        expected_I7_fromI2.add(new ItemLR(1, LR.g.Productions.get(3), new Token("$")));

        ArrayList<ItemLR> expected_I7_fromI6 = new ArrayList<>();
        //C -> [(d,TERMINAL), (•,DOT)], $
        expected_I7_fromI6.add(new ItemLR(1, LR.g.Productions.get(3), new Token("$")));

        ArrayList<ItemLR> expected_I9 = new ArrayList<>();
        //C -> [(c,TERMINAL), (C,NONTERMINAL), (•,DOT)], $
        expected_I9.add(new ItemLR(2, LR.g.Productions.get(2), new Token("$")));

        for(ItemLR it : expected_I5)
            assert(I5.contains(it));

        for(ItemLR it : expected_I6)
            assert(I6.contains(it));

        assert(I6.equals(I6_c));

        for(ItemLR it : expected_I7_fromI2)
            assert(I7_fromI2.contains(it));

        for(ItemLR it : expected_I7_fromI6)
            assert(I7_fromI6.contains(it));

        for(ItemLR it : expected_I9)
            assert(I9.contains(it));
    }

    @Test
    public void testGOTO3() throws IOException {
        LR_Automaton LR = new LR_Automaton(new Grammar("example11.txt"));
        LR.g.augmentGivenGrammar();
        ItemLR item = new ItemLR(0, LR.g.Productions.get(0), new Token("$"));

        ArrayList<ItemLR> I0 = LR.CLOSURE(item);
        ArrayList<ItemLR> I3 = LR.GOTO(I0, new Token("c", "TERMINAL"));
        ArrayList<ItemLR> I3_c = LR.GOTO(I3, new Token("c", "TERMINAL"));
        ArrayList<ItemLR> I4 = LR.GOTO(I3, new Token("d", "TERMINAL"));
        ArrayList<ItemLR> I8 = LR.GOTO(I3, new Token("C", "NONTERMINAL"));

        ArrayList<ItemLR> expected_I4 = new ArrayList<>();
        //C -> [(d,TERMINAL), (•,DOT)], c
        expected_I4.add(new ItemLR(1, LR.g.Productions.get(3), new Token("c", "TERMINAL")));
        //C -> [(d,TERMINAL), (•,DOT)], d
        expected_I4.add(new ItemLR(1, LR.g.Productions.get(3), new Token("d", "TERMINAL")));

        ArrayList<ItemLR> expected_I8 = new ArrayList<>();
        //C -> [(c,TERMINAL), (C,NONTERMINAL), (•,DOT)], c
        expected_I8.add(new ItemLR(2, LR.g.Productions.get(2), new Token("c", "TERMINAL")));
        //C -> [(c,TERMINAL), (C,NONTERMINAL), (•,DOT)], d
        expected_I8.add(new ItemLR(2, LR.g.Productions.get(2), new Token("d", "TERMINAL")));

        assert(I3.equals(I3_c));

        for(ItemLR it : expected_I4)
            assert(I4.contains(it));

        for(ItemLR it : expected_I8)
            assert(I8.contains(it));
    }


}
