package automatons;

import main.Controller;
import main.Grammar;
import util.*;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import static util.SupportFunctions.*;

public class SLR_Automaton extends Automaton {

    private Grammar g;
    private Controller controller;
    public HashMap<Token, ArrayList<Token>> FollowLR;
    public ArrayList<ArrayList<Production>> items;
    public HashMap<Integer, HashMap<Token, String>> actionTable;

    public SLR_Automaton(Grammar grammar, Controller controller){
        g = grammar;
        this.controller = controller;
    }

    public ArrayList<Production> GOTO(ArrayList<Production> I, Token X){
        ArrayList<Production> J = new ArrayList<>();
        for (Production pro : I) {
            if (pro.definition.contains(X)) {
                Production temp = new Production(pro.nonTerminal);
                temp.definition.addAll(pro.definition);
                int indexOfDot = temp.definition.indexOf(new Token("•", "DOT"));
                if (indexOfDot + 1 == temp.definition.size())
                    continue;
                if (!temp.definition.get(indexOfDot + 1).equals(X))
                    continue;
                temp.definition.set(indexOfDot, temp.definition.get(indexOfDot + 1));
                temp.definition.set(indexOfDot + 1, new Token("•", "DOT"));
                J.addAll(CLOSURE(temp));
            }
        }
        return J;
    }

    public ArrayList<Production> CLOSURE(Production I){
        HashMap<String, Boolean> added = new HashMap<>();
        for (Token tok : g.NonTerminals)
            added.put(tok.data, false);

        ArrayList<Production> set = new ArrayList<>();
        set.add(I);
        ArrayDeque<Token> q = new ArrayDeque<>();
        //System.out.println(I);
        //System.out.println(I.definitions.indexOf(new Token("•", "DOT")));
        if(I.getIndexOfDot() + 1 == I.definition.size()){
            set.add(I);
            return set;
        }

        q.addFirst(I.get(I.definition.indexOf(new Token("•", "DOT")) + 1));

        do {
            for(Production pro : g.Productions){
                if(pro.nonTerminal.data.equals(q.peekFirst().data)){
                    Production t = createItem(0, pro);
                    if(!set.contains(t)) {
                        set.add(t);
                        Token tok = t.definition.get(t.definition.indexOf(new Token("•", "DOT")) + 1);

                        if(!tok.type.equals("TERMINAL") && !added.get(tok.data) ){
                            q.addLast(tok);
                        }
                    }
                }
            }
            added.replace(q.peekFirst().data, true);
            q.pollFirst();

            // чистка
            q.removeIf(t -> added.get(t.data));

        } while (!q.isEmpty());

        return set;
    }

    public void buildAllItems(){
        int index = 0;
        int oldIndex = 0;
        Production p1 = createItem(0, g.Productions.get(0));

        items = new ArrayList<>();
        // I0
        items.add(CLOSURE(p1));

        ArrayList<Token> tokensToCheck = new ArrayList<>();

        for(Production pro : items.get(0)){
            int ind = pro.getIndexOfDot();
            if(ind + 1 == pro.definition.size())
                continue;
            Token t = pro.definition.get(ind + 1);
            if(!tokensToCheck.contains(t))
                tokensToCheck.add(t);
        }
        // 1st Iteration
        for(Token tok : tokensToCheck){
            items.add(removeDuplicates(GOTO(items.get(0), tok)));
            index++;
            //System.out.println(index + " " +tok.data + " = " + removeDuplicates(GoTo(items.get(0), tok)));
        }
        oldIndex = index;
        int left = 1;
        tokensToCheck.clear();
        do{
            for (int i = left; i < oldIndex + 1; i++) {
                for (Production pro : items.get(i)) {
                    int ind = pro.getIndexOfDot();
                    if (ind + 1 == pro.definition.size())
                        continue;
                    Token t = pro.definition.get(ind + 1);
                    if (!tokensToCheck.contains(t))
                        tokensToCheck.add(t);
                }
                for (Token t : tokensToCheck) {
                    ArrayList<Production> X = removeDuplicates(GOTO(items.get(i), t));
                    if (!items.contains(X)) {
                        items.add(X);
                        index++;
                    }
                }
                tokensToCheck.clear();
            }
            left = oldIndex;
            oldIndex = index;
        } while( left != index);
        //for (int i = 0; i < items.size(); i++) System.out.println(i + " = " + items.get(i));
    }

    public int getIndexFromGOTO(ArrayList<Production> I, Token X){
        ArrayList<Production> wanted = removeDuplicates(GOTO(I, X));
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).equals(wanted))
                return i;
        }
        return -1;
    }

    @Override
    public void makeActionTable(){
        actionTable = new HashMap<>();
        boolean e1 = controller.errorRecoveryLR.isSelected();

        for (int i = 0; i < items.size(); i++) {
            actionTable.put(i, new HashMap<>());
            for (Token t : g.Terminals) {
                String result = ACTION(i, t);
                /*if(result.equals("err")) {
                    actionTable.get(i).put(t, " ");
                }else*/
                    actionTable.get(i).put(t, result);
            }
            for (Token t : g.NonTerminals){
                actionTable.get(i).put(t, ACTION(i, t));
            }
            Token a = new Token("$", "END_MARKER");
            actionTable.get(i).put(a, ACTION(i, a));
        }

        if(e1){
            Token plus = new Token("+", "TERMINAL");
            Token mult = new Token("*", "TERMINAL");
            Token id = new Token("id", "TERMINAL");
            Token closeBracket = new Token(")", "TERMINAL");
            Token endMarker = new Token("$", "END_MARKER");
            actionTable.get(0).replace(plus, "e1");
            actionTable.get(0).replace(mult, "e1");
            actionTable.get(0).replace(endMarker, "e1");
            actionTable.get(0).replace(closeBracket, "e2");

            actionTable.get(1).replace(closeBracket, "e2");
            actionTable.get(1).replace(mult, "s6");
            actionTable.get(1).replace(id, "e3");

            actionTable.get(3).replace(id, "s5");

            actionTable.get(4).replace(plus, "e1");
            actionTable.get(4).replace(mult, "e1");
            actionTable.get(4).replace(closeBracket, "e1");

            actionTable.get(5).replace(id, "e3");

            actionTable.get(6).replace(closeBracket, "e2");
            actionTable.get(6).replace(endMarker, "e1");
            actionTable.get(6).replace(plus, "e1");
            actionTable.get(6).replace(mult, "e1");

            actionTable.get(7).replace(closeBracket, "e2");
        }

    }

    // si  - перенос и размещение в стеке состояния i
    // rj  - свёртка по продукции:
    // acc - принятие
    // err - ошибка
    public String ACTION(int i, Token a){
        Production p1 = createItem(1, g.Productions.get(0));
        if(a.data.equals("$") && i != -1 && items.get(i).contains(p1)){
            System.out.println("ACTION( " + i + ", " + a.data + ") = acc");
            return "acc";
        }

        int state = -1;
        if(i != -1)
            state = getIndexFromGOTO(items.get(i),a);
        if(state != -1){
            System.out.println("ACTION( " + i + ", " + a.data + ") = s" + state);
            return "s" + state;
        }

        System.out.println("i=" + i + " a=" + a.data);
        if(i == -1)
            return "err";

        System.out.println(items.get(i).get(0));
        Token t = items.get(i).get(0).nonTerminal;
        ArrayList<Token> f = FollowLR.get(t);
        System.out.println("Follow = " + f);
        System.out.println(t);
        for (Token tok : f) {
            if (tok.data.equals(a.data)) {
                for(Production pro : items.get(i)) {
                    if (pro.getIndexOfDot() + 1 == pro.definition.size()) {
                        if (pro.nonTerminal.equals(t)) {
                            Production wanted = new Production(pro);
                            wanted.definition.remove(wanted.getIndexOfDot());
                            System.out.println("ACTION( " + i + ", " + a.data + ") = r" + g.Productions.indexOf(wanted));
                            return "r" + g.Productions.indexOf(wanted);
                        }
                    }
                }
            }

        }
        return "err";
    }

    public void buildFollow(){
        FollowLR = new HashMap<>();
        for(Token t : g.NonTerminals){
            FollowLR.put(t, removeDuplicates(FollowLR(t, null)));
        }
    }
    public static void main(String[] args) throws IOException {
        SLR_Automaton a = new SLR_Automaton(new Grammar("example14.txt"), null);
        /*a.buildAllItems();
        a.buildFollow();
        System.out.println(a.ACTION(3, new Token("id", "TERMINAL")));*/
        System.out.println(a.FollowLR(new Token("E", "NONTERMINAL")));

    }

    private ArrayList<Token> FollowLR(Token t){
        ArrayList<Token> result = new ArrayList<>();

        boolean setIsUpdated = true;

        for (Production pro : g.Productions){
            if(!pro.definition.contains(t))
                continue;

            for(int i = 0; i < pro.definition.size() - 1; i++){
                if(pro.definition.get(i).equals(t)){
                    if(pro.definition.get(i + 1).type.equals("TERMINAL"))
                        result.add(pro.definition.get(i + 1));
                    //else
                        // здесь скорее всего живёт баг
                        //result.addAll(LL.First(pro.definition.get(i+1), null));
                }
            }
        }
        if(g.startSymbol.equals(t))
            result.add(new Token("$", "END_MARKER"));
        return result;
    }




    private ArrayList<Token> FollowLR(Token t, Token prev){
        //System.out.println("t= " + t + " prev = " + prev);
        if(t.equals(prev))
            return new ArrayList<>();

        ArrayList<Token> result = new ArrayList<Token>();
        if(t.type.equals("TERMINAL")){
            result.add(t);
            return result;
        }
        if(t.equals(g.startSymbol))
            result.add(new Token("$", "END_MARKER"));

        for (Production pro : g.Productions){
            if(pro.definition.contains(t)){
                if(FollowLR.get(pro.nonTerminal) != null)
                    result.addAll(FollowLR.get(pro.nonTerminal));
                int position = pro.definition.lastIndexOf(t);
                if(position + 1 != pro.definition.size()){
                    result.addAll(FollowLR(pro.definition.get(position + 1), t));
                }
            }
        }
        return result;
    }

}
