package automatons;

import main.Grammar;
import util.ItemLR;
import util.Production;
import util.Token;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;

import static util.SupportFunctions.createDOT;
import static util.SupportFunctions.removeDuplicates;

public class LR_Automaton extends Automaton {


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

                J.add(temp);
                J.addAll(CLOSURE(temp));
            }
        }
        return removeDuplicates(J);
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


    public static void main(String[] args) throws IOException {
        LR_Automaton LR = new LR_Automaton(new Grammar("example11.txt"));
        LR.g.printGrammar();
        LR.g.augmentGivenGrammar();

        ItemLR item = new ItemLR(0, LR.g.Productions.get(0), new Token("$"));

        ArrayList<ItemLR> I0 = LR.CLOSURE(item);
        System.out.println();

        ArrayList<ItemLR> I2 = LR.GOTO(I0, new Token("C", "NONTERMINAL"));
        ArrayList<ItemLR> I5 = LR.GOTO(I2, new Token("c", "TERMINAL"));

        for(ItemLR it : I5)
            System.out.println(it);


    }

    public ArrayList<ItemLR> CLOSURE(ItemLR I){
        boolean debug = false;
        ArrayList<ItemLR> result = new ArrayList<>();
        ArrayDeque<ItemLR> stack = new ArrayDeque<>();
        if(I.nonTerminal.equals(g.startSymbol))
            result.add(I);
        stack.addFirst(I);
        int i = 1;
        do {
            ItemLR current = stack.pollFirst();
            if(debug) {
                System.out.println();
                System.out.println("STEP " + i);
                System.out.println("Current = " + current);
                System.out.println("stack = " + stack);
            }
            if(current.getIndexOfDot() + 1 == current.size())
                return result;
            Token tokenAfterDot = current.get(current.getIndexOfDot() + 1);
            if(debug) System.out.println("B = " + tokenAfterDot.data);
            for (Production pro : g.Productions){
                if(pro.nonTerminal.equals(tokenAfterDot)){
                    if(debug) System.out.println("Pro = " + pro);
                    ArrayList<Token> tokensForFIRST =
                            new ArrayList<>(current.definition.subList(current.getIndexOfDot() + 2, current.size()));
                    tokensForFIRST.add(current.terminal);
                    for(Token tok : LR_First(tokensForFIRST)){
                        ItemLR newItem = new ItemLR(0,pro, tok);
                        if(!result.contains(newItem)){
                            result.add(newItem);
                            stack.addFirst(newItem);
                            if(debug) System.out.println("Добавлено " + newItem);
                        }
                    }

                }
            }
            i++;
        } while ( !stack.isEmpty());
        return result;
    }



}
