package automatons;

import main.*;
import util.*;

import java.io.IOException;
import java.util.*;

import static util.SupportFunctions.*;

public class LL_Automaton extends Automaton {

    Grammar g;
    Controller controller;
    public ArrayList<ArrayList<Token>> FirstSet;
    public ArrayList<ArrayList<Token>> FollowSet;
    public HashMap<Token, ArrayList<Token>> first;
    public HashMap<Token, ArrayList<Token>> follow;
    public HashMap<String, HashMap<String, Production>> SyntaxMatrix;

    public LL_Automaton(Grammar g, Controller controller){
        this.g = g;
        this.controller = controller;
    }

    public void makeFirstSet() {
        FirstSet = new ArrayList<>(g.NonTerminals.size());
        first = new HashMap<>(g.NonTerminals.size());
        for (Token nonTerminal : g.NonTerminals) {
            ArrayList<Token> e = removeDuplicates(First(nonTerminal,null));
            e.remove(null);
            FirstSet.add(e);
            first.put(nonTerminal, e);
        }
    }

    public void makeFollowSet() {
        FollowSet = new ArrayList<>(g.NonTerminals.size());
        follow = new HashMap<>(g.NonTerminals.size());
        for (Token nonTerminal : g.NonTerminals) {
            ArrayList<Token> e;
            //try {
                e = Follow(nonTerminal, null);
            /*} catch (StackOverflowError stackOverflowError){
                // Для неоднозначной грамматики
                // Это ужасная вещь, но она нужна для редкого случая, когда два нетерминала взаимосвязаны через Follow
                // Follow(S) = ... + Follow(A)
                // Follow(A) = Follow(S)
                System.out.println("111");
                e = First(nonTerminal, null);
                e.add(new Token("$","END_MARKER"));
                e.remove(new Token("#","EPSILON"));
            }*/
            ArrayList<Token> ee = removeDuplicates(e);
            ee.remove(null);
            FollowSet.add(ee);
            follow.put(nonTerminal, ee);
        }
    }

    public void printFirstSet(){
        for (Token nonTerminal : g.NonTerminals) {
            String firstString = "FIRST(" + nonTerminal.data + ") = {";
            for (Token token: first.get(nonTerminal))
                firstString += token.data + ", ";
            firstString += "}";
            firstString = firstString.replace(", }", "}");
            System.out.println(firstString);
            if (controller != null)
                controller.LogConsole.appendText(firstString + "\n");
        }
        if (controller != null)
            controller.LogConsole.appendText("\n");
    }

    public void printFollowSet(){
        for (Token nonTerminal : g.NonTerminals) {
            String followString = "FOLLOW(" + nonTerminal.data + ") = {";
            for (Token token : follow.get(nonTerminal))
                followString += token.data + ", ";
            followString += "}";
            followString = followString.replace(", }", "}");
            System.out.println(followString);
            if (controller != null)
                controller.LogConsole.appendText(followString + "\n");
        }
        if (controller != null)
            controller.LogConsole.appendText("\n");
    }

    ArrayList<Token> First(Token X, Token prev){
        //System.out.println(); System.out.println("Внешний First( " + X.data + " )");
        ArrayList<Token> result = new ArrayList<>();
        if(X.type.equals("TERMINAL")){
            result.add(X);
            return result;
        }
        if(X.type.equals("EPSILON")){
            result.add(X);
            return result;
        }

        for(Production pro : g.Productions)
            if(pro.nonTerminal.equals(X)){
                //System.out.println("pro = " + pro);
                result.addAll(First(X, pro.definition, prev, true));
            }
        return result;
    }


    private ArrayList<Token> First(Token current, ArrayList<Token> X, Token prev, boolean deleteNextEps){
        ArrayList<Token> result = new ArrayList<>();
        //System.out.println("Внутренний First( " + X + " )");
        //System.out.println("получил prev = " + prev);
        if(X.isEmpty())
            return result;
        if(X.get(0).type.equals("TERMINAL")){
            result.add(X.get(0));
            return result;
        }
        if(X.get(0).type.equals("EPSILON")){
            result.add(X.get(0));
            return result;
        }
        // X : Y1 Y2 Y3
        if (prev != null && prev.equals(X.get(0)))
            return result;
        //System.out.println("prev = " + X.get(0));
        ArrayList<Token> FirstY1 = First(X.get(0), current);
        if (FirstY1.contains(new Token("#"))){
            result.addAll(FirstY1);
            if(deleteNextEps)
                result.remove(new Token("#"));
            ArrayList<Token> subList = new ArrayList<>(X);
            subList.remove(0);
            result.addAll(First(current, subList, prev, false));
        } else
            result.addAll(FirstY1);
        return result;
    }




/*
    ArrayList<Token> Follow(Token X, Token prev){
        System.out.println();
        System.out.println("Внешний FOLLOW( " + X.data + " )");
        ArrayList<Token> result = new ArrayList<>();
        if(X.equals(g.startSymbol))
            result.add(new Token("$"));
        for(Production pro : g.Productions){
            // Rule-2
            System.out.println("prod = " + pro);
            if(pro.nonTerminal.equals(X) && ){
                result.addAll(Follow(pro.get(pro.size() - 1), X));
            }

            // Rule-3
            if(pro.definition.contains(X)){
                System.out.println("pro = " + pro);
                ArrayList<Token> subList = new ArrayList<>();
                for(int i = pro.getTokenIndex(X) + 1; i < pro.size(); i++)
                    subList.add(pro.get(i));

                result.addAll(Follow(X, subList, prev));
            }
        }
        return result;
    }


    private ArrayList<Token> Follow(Token current, ArrayList<Token> X, Token prev){
        ArrayList<Token> result = new ArrayList<>();
        System.out.println("Внутренний FOLLOW( " + X + " )");
        System.out.println("получил prev = " + prev);
        if(X.isEmpty())
            return result;



        return result;
    }

 */

    public static void main(String[] args) throws IOException {
        LL_Automaton LL = new LL_Automaton(new Grammar("example12.txt"),null);
        System.out.println(LL.Follow(new Token("A", "NONTERMINAL"), null));
        System.out.println("===");
        System.out.println(LL.First(new Token("A", "NONTERMINAL"), null));

    }

    ArrayList<Token> Follow(Token X, Token prev){
        ArrayList<Token> result = new ArrayList<>();
        if(prev != null && prev.equals(X))
            return result;
        // Rule-1
        if (X.equals(g.startSymbol))
            result.add(new Token("$", "END_MARKER"));
        for (int j = 0; j < g.Productions.size(); j++) {
            Production pro = g.Productions.get(j);
            if(pro.definition.contains(X)){
                boolean productionIsNotOver = true;
                // Rule-2
                if(pro.getTokenIndex(X) + 1 == pro.size()){
                    result.addAll(Follow(pro.nonTerminal, X));
                    continue;
                }
                int pointer = pro.getTokenIndex(X);
                do {
                    ArrayList<Token> subList = new ArrayList<>();
                    for (int i = pointer + 1; i < pro.size(); i++)
                        subList.add(pro.get(i));
                    ArrayList<Token> FirstB = new ArrayList<>();
                    for (Token t : subList){
                        ArrayList<Token> tempFirst = First(t, null);
                        FirstB.addAll(tempFirst);
                        if(!tempFirst.contains(new Token("#")))
                            break;
                    }
                    if (!FirstB.contains(new Token("#"))) {
                        //System.out.println("Rule-3.1");
                        result.addAll(FirstB);
                    } else {
                        //System.out.println("Rule-3.2");
                        result.addAll(FirstB);
                        result.remove(new Token("#"));
                        result.addAll(Follow(pro.nonTerminal, X));
                        break;
                    }
                    if(!subList.contains(X))
                        productionIsNotOver = false;
                    else
                        pointer = pointer + subList.indexOf(X) + 1;
                } while (productionIsNotOver);
            }
        }
        return removeDuplicates(result);
    }


/*
    ArrayList<Token> Follow(Token token){
        ArrayList<Token> result = new ArrayList<>();
        if (token.equals(g.startSymbol))
            result.add(new Token("$", "END_MARKER"));

        for (Production pro : g.Productions) {
            if(!pro.definition.contains(token))
                continue;
            int i = pro.getTokenIndex(token) + 1;
            if(i == pro.size()){
                if (pro.nonTerminal.equals(g.startSymbol)){
                    result.addAll(Follow(pro.nonTerminal));
                    result.add(new Token("$", "END_MARKER"));
                } else {
                    if(pro.nonTerminal.equals(token))
                        continue;
                    result.addAll(Follow(pro.nonTerminal));
                }
            }
            else
                for(; i < pro.size(); i++){
                    ArrayList<Token> First = LL_First(pro.get(i));
                    if(First.contains(new Token("#")) ){
                        if(pro.get(i).equals(token))
                            continue;
                        for (Token value : First) {
                            if (value.type.equals("EPSILON"))
                                continue;
                            result.add(value);
                        }
                        result.addAll(Follow(pro.nonTerminal));
                    }
                    else
                        result.addAll(First);
                }
        }
        return result;
    }

 */

    public boolean TokenHasEpsProd(Token tok){
        boolean result = false;
        for(Production pro : g.Productions){
            if(pro.nonTerminal.data.equals(tok.data))
                result |= pro.hasEpsilonProduction();
        }
        return result;
    }

    public void makeSyntaxMatrix(){
        SyntaxMatrix = new HashMap<>();

        SyntaxMatrix.put(" ", new HashMap<>());
        for (Token t : g.Terminals)
            SyntaxMatrix.get(" ").put(t.data, null);

        for(Token nt : g.NonTerminals)
            SyntaxMatrix.put(nt.data, new HashMap<>());

        for(Production pro : g.Productions){
            for(Token tok : First(pro.nonTerminal, pro.definition, null, true)){
                if(!tok.type.equals("EPSILON"))
                    SyntaxMatrix.get(pro.nonTerminal.data).put(tok.data, pro);
                else{
                    for(Token t : follow.get(pro.nonTerminal)){
                        if(t != null){
                            SyntaxMatrix.get(pro.nonTerminal.data)
                                    .put(t.data, new Production(pro.nonTerminal, true));
                        }
                    }
                }
            }
        }
    }

    public Controller getController(){
        return controller;
    }

}
