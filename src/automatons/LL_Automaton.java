package automatons;

import main.*;
import util.*;
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
            ArrayList<Token> e = removeDuplicates(LL_First(nonTerminal));
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
            try {
                e = Follow(nonTerminal);
            } catch (StackOverflowError stackOverflowError){
                // Для неоднозначной грамматики
                // Это ужасная вещь, но она нужна для редкого случая, когда два нетерминала взаимосвязаны через Follow
                // Follow(S) = ... + Follow(A)
                // Follow(A) = Follow(S)
                e = LL_First(nonTerminal);
                e.add(new Token("$","END_MARKER"));
                e.remove(new Token("#","EPSILON"));
            }
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

    public ArrayList<Token> LL_First(Token X){
        ArrayList<Token> temp = new ArrayList<>();
        temp.add(X);
        return LL_First(temp);
    }

    ArrayList<Token> LL_First(ArrayList<Token> X){
        ArrayList<Token> result = new ArrayList<>();
        if(X.get(0).isEpsilon()){
            result.add(X.get(0));
            return result;
        }
        if(g.Terminals.contains(X.get(0))){
            result.add(X.get(0));
            return result;
        }
        for(Production pro : g.Productions){
            if(pro.nonTerminal.data.equals(X.get(0).data)){
                boolean allProductionsHasEps = true;
                for(int i = 0; i < pro.size(); i++){
                    Token Yi = pro.get(i);
                    if(Yi.type.equals("TERMINAL")){
                        result.add(Yi);
                        allProductionsHasEps = false;
                        break;
                    }
                    ArrayList<Token> FirstYi = LL_First(Yi);
                    allProductionsHasEps = FirstYi.contains(new Token("#"));

                    if(Yi.type.equals("NONTERMINAL"))
                        for(Token t : LL_First(Yi))
                            if(t != null && !t.type.equals("EPSILON"))
                                result.add(t);

                    if (!Yi.type.equals("NONTERMINAL") || !FirstYi.contains(new Token("#")))
                        break;
                }
                if(allProductionsHasEps )
                    result.add(new Token("#", "EPSILON"));
            }
        }
        return result;
    }

    private ArrayList<Token> LL_First(Production pro){
        Token token = pro.nonTerminal;
        ArrayList<Token> result = new ArrayList<>();

        if(token.type.equals("TERMINAL"))
            result.add(token);
        else if(token.type.equals("EPSILON"))
            result.add(token);
        else {
            boolean allProductionsHasEps = true;
            for(int i = 0; i < pro.size(); i++){
                Token Yi = pro.get(i);
                if(Yi.type.equals("TERMINAL")){
                    result.add(Yi);
                    allProductionsHasEps = false;
                    break;
                }
                ArrayList<Token> FirstYi = LL_First(Yi);
                if(!FirstYi.contains(new Token("#")))
                    allProductionsHasEps = false;

                if(Yi.type.equals("NONTERMINAL"))
                    for(Token t : LL_First(Yi))
                        if(t != null && !t.type.equals("EPSILON"))
                            result.add(t);

                if (!Yi.type.equals("NONTERMINAL") || !FirstYi.contains(new Token("#")))
                    break;
            }
            if(allProductionsHasEps )
                result.add(new Token("#", "EPSILON"));
        }
        return result;
    }

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
            for(Token tok : LL_First(pro)){
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
