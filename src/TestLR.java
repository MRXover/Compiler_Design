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
        ArrayList<Production> C = new ArrayList<>();

        for(Production pro : grammar.Productions)
            expected.add(grammar.createItem(0, pro));

        C.add(expected.get(0));
        ArrayList<Production> result = grammar.closure(C);

        for(int i = 0; i < expected.size(); i++){
            //System.out.println(expected.get(i) + " == " + result.get(i));
            assert(expected.get(i).equals(result.get(i)));
        }

    }

}
