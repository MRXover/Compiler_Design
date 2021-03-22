package automatons;

import main.Grammar;
import stuff.ItemLR;
import stuff.Production;
import stuff.Token;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

import static stuff.SupportFunctions.createDOT;
import static stuff.SupportFunctions.removeDuplicates;

public class LR_Automaton extends Automaton {


    public static void main(String[] args) throws IOException {
        LR_Automaton LR = new LR_Automaton(new Grammar("example11.txt"));
        LR.g.printGrammar();
        LR.g.augmentGivenGrammar();
        LR.buildAllItems();

        System.out.println("•".equals("•"));
    }


    public Grammar g;
    public ArrayList<ArrayList<ItemLR>> items;

    public LR_Automaton(Grammar grammar){
        g = grammar;
    }

    // si  - перенос и размещение в стеке состояния i
    // rj   - свёртка по продукции:
    // acc - принятие
    // err - ошибка
    public String ACTION(int i, Token a){
        ItemLR st = new ItemLR(1, g.Productions.get(0));
        st.setTerminal(new Token("$"));

        if(a.data.equals("$") && items.get(i).contains(st))
            return "acc";

        boolean shift = false;
        for(ItemLR item : items.get(i)){
            int dotPos = item.getIndexOfDot();
            if(dotPos + 1 != item.size() && item.get(dotPos + 1).data.equals(a.data)) {
                shift = true;
                break;
            }
        }
        if(shift){
            //System.out.println("ACTION( " + i + ", " + a.data + ") = s" + LR_getIndexFromGoTo(LR_items.get(i),a));
            return "s" + getIndexFromGOTO(items.get(i),a);
        }

        for(ItemLR item : items.get(i)){
            if(!item.terminal.data.equals(a.data))
                continue;
            if(item.nonTerminal.data.equals(g.startSymbol.data))
                continue;
            if(item.getIndexOfDot() + 1 == item.size()) {
                Production wanted = new Production(item);
                wanted.definition.remove(wanted.getIndexOfDot());
                //System.out.println("ACTION( " + i + ", " + a.data + ") = r" + Productions.indexOf(wanted));
                return "r" + g.Productions.indexOf(wanted);
            }
        }
        return "err";
    }

    public void buildAllItems(){
        int index = 0;
        int oldIndex = 0;
        items = new ArrayList<>();

        ItemLR p1 = new ItemLR(0, g.Productions.get(0));
        p1.setTerminal(new Token("$"));
        // I0
        items.add(CLOSURE(p1));
        System.out.println(CLOSURE(p1));

        ArrayList<Token> tokensToCheck = new ArrayList<>();

        for(ItemLR item : items.get(0)){
            int ind = item.getIndexOfDot();
            if(ind + 1 == item.definition.size())
                continue;
            Token t = item.definition.get(ind + 1);
            if(!tokensToCheck.contains(t))
                tokensToCheck.add(t);
        }
        // 1st Iteration
        for(Token tok : tokensToCheck){
            items.add(removeDuplicates(GOTO(items.get(0), tok)));
            index++;
        }
        oldIndex = index;
        int left = 1;

        tokensToCheck.clear();

        do{
            for (int i = left; i < oldIndex + 1; i++) {
                for (ItemLR item : items.get(i)) {
                    int ind = item.getIndexOfDot();
                    if (ind + 1 == item.definition.size())
                        continue;
                    Token t = item.definition.get(ind + 1);
                    if (!tokensToCheck.contains(t))
                        tokensToCheck.add(t);
                }
                for (Token t : tokensToCheck) {
                    ArrayList<ItemLR> X = removeDuplicates(GOTO(items.get(i), t));
                    if(!LR_containsItem(X)) {
                        items.add(X);
                        index++;
                    }
                }
                tokensToCheck.clear();
            }
            left = oldIndex;
            oldIndex = index;
        } while( left != index );

        int i = 0;
        for(ArrayList<ItemLR> list : items){
            System.out.println();
            System.out.println("I" + i + " =");
            for(ItemLR item : list)
                System.out.println(item);
            i++;
        }

    }

    boolean LR_containsItem(ArrayList<ItemLR> items){
        boolean currentIsEqual = true;
        for (ArrayList<ItemLR> lr_item : this.items) {
            if (items.size() != lr_item.size())
                continue;
            int j = 0;
            for (ItemLR item : items) {
                if (!item.equals(lr_item.get(j))) {
                    currentIsEqual = false;
                    break;
                }
                j++;
            }
            if (currentIsEqual)
                return true;
            currentIsEqual = true;
        }

        return false;
    }

    public int getIndexFromGOTO(ArrayList<ItemLR> I, Token X){
        ArrayList<ItemLR> wanted = removeDuplicates(GOTO(I, X));
        for(int i = 0; i < items.size(); i++)
            if(items.get(i).equals(wanted))
                return i;
        return -1;
    }

    public ArrayList<ItemLR> GOTO(ArrayList<ItemLR> I, Token X){
        ArrayList<ItemLR> J = new ArrayList<>();
        for(ItemLR item : I){
            if(item.definition.contains(X)){
                ItemLR temp = new ItemLR(item);
                int indexOfDot = temp.getIndexOfDot();

                if(indexOfDot + 1 == temp.definition.size())
                    continue;

                if( Math.abs(indexOfDot - temp.definition.indexOf(X) ) > 1)
                    continue;

                // если точка правее токена
                if(indexOfDot > temp.definition.lastIndexOf(X))
                    continue;

                temp.definition.set(indexOfDot, temp.definition.get(indexOfDot + 1));
                temp.definition.set(indexOfDot + 1, createDOT());
                J.addAll(CLOSURE(temp));
            }
        }
        return J;
    }

    ArrayList<Token> LR_First(ArrayList<Token> input){
        ArrayList<Token> result = new ArrayList<>();

        for (int i = 0; i < input.size(); i++){
            if(input.get(i).data.equals("#")){
                ArrayList<Token> temp = new ArrayList<>();
                for(int j = i + 1; j < input.size(); j++)
                    temp.add(input.get(j));
                result = LR_First(temp);
                break;
            } else{
                if (g.Terminals.contains(input.get(i)) || input.get(i).data.equals("$")){
                    result.add(input.get(i));
                    return result;
                } else {
                    ArrayList<Token> tokensFromNonterminal = new LL_Automaton(g,null).LL_First(input.get(i));
                    if(tokensFromNonterminal.contains(new Token("#"))){
                        result.addAll(tokensFromNonterminal);
                        result.remove(new Token("#"));
                        ArrayList<Token> temp = new ArrayList<>();
                        for(int j = i + 1; j < input.size(); j++)
                            temp.add(input.get(j));
                        result.addAll(LR_First(temp));
                        break;
                    } else {
                        result.addAll(tokensFromNonterminal);
                        return result;
                    }
                }
            }
        }

        return result;
    }

    public ArrayList<ItemLR> CLOSURE(ItemLR I){
        boolean debug = true;
        HashMap<String, Boolean> added = new HashMap<>();
        for (Token tok : g.NonTerminals)
            added.put(tok.data, false);

        ArrayList<ItemLR> set = new ArrayList<>();
        //set.add(I);
        ArrayDeque<Token> q = new ArrayDeque<>();
        if(I.getIndexOfDot() + 1 == I.definition.size()){
            //System.out.println("set = " + set);
            set.add(I);
            return set;
        }
        q.addFirst(I.get(I.getIndexOfDot() + 1));

        /*
        System.out.println("-------------------");
        System.out.println(I);
        System.out.println(I.getIndexOfDot() + 1);
        System.out.println("-------------------");

         */

        int step = 1;
        ArrayList<Token> rightTokens = new ArrayList<>();
        rightTokens.add(I.terminal);
        ArrayList<Token> first = new ArrayList<>();

        do {
            if(debug){
                System.out.println();
                System.out.println("STEP " + step);
                System.out.println(q);
                System.out.println(added);
            }

            for(Production pro : g.Productions){
                if(pro.nonTerminal.data.equals(q.peekFirst().data)){
                    if(debug) System.out.println("pro = " + pro);

                    for(Token tok : rightTokens){
                        ItemLR temp = new ItemLR(0,pro);
                        temp.setTerminal(tok);
                        if(debug) System.out.println("Добавлено " + temp);
                        if(debug) System.out.println(pro);
                        //if(NonTerminals.contains(temp.definition.get(1)) && added.get(temp.definition.get(1).data))
                        q.addLast(temp.definition.get(1));
                        if(debug) System.out.println("q + " + temp.definition.get(1));
                        set.add(temp);

                        for(int i = temp.getIndexOfDot() + 2; i < temp.definition.size(); i++) {
                            first.add(temp.definition.get(i));
                            System.out.println("F = " + temp.definition.get(i));
                        }
                    }

                }
            }
            rightTokens.clear();
            rightTokens.addAll(LR_First(first));
            System.out.println("First = " + LR_First(first) + " " + first);

            added.replace(q.peekFirst().data, true);
            q.pollFirst();

            step++;
        } while (!q.isEmpty() );
        return set;
    }


}
