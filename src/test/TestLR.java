package test;

import automatons.LR_Automaton;
import automatons.SLR_Automaton;
import main.Grammar;
import org.junit.Test;
import stuff.ItemLR;
import stuff.Production;

import java.io.IOException;
import java.util.ArrayList;

import static stuff.SupportFunctions.createItem;

public class TestLR {
    private Grammar grammar;

    @Test
    public void testCLOSURE() throws IOException {
        grammar = new Grammar("example11.txt");
        grammar.augmentGivenGrammar();

        ArrayList<ItemLR> expected = new ArrayList<>();

        expected.add(new ItemLR());

        LR_Automaton LR = new LR_Automaton(grammar);

        ArrayList<ItemLR> result = LR.CLOSURE(expected.get(0));

        for(int i = 0; i < expected.size(); i++){
            //System.out.println(expected.get(i) + " == " + result.get(i));
            assert(expected.get(i).equals(result.get(i)));
        }
    }
}
