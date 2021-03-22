package automatons;

import stuff.*;
import java.util.*;
import static stuff.SupportFunctions.*;

public class LALR_Automaton extends Automaton {

    LR_Automaton LR;
    public HashMap<Integer, ArrayList<ItemLR>> items;

    public LALR_Automaton(LR_Automaton LR){
        this.LR = LR;
    }

    public int getIndexFromGOTO(ArrayList<ItemLR> I, Token X){
        int LR_goto = LR.getIndexFromGOTO(I, X);
        if(LR_goto == -1){
            ArrayList<ItemLR> wanted = removeDuplicates(LR.GOTO(I, X));
            for(Map.Entry<Integer, ArrayList<ItemLR>> pair : items.entrySet()){
                boolean isEqual = true;
                for(ItemLR item : pair.getValue())
                    if (!wanted.contains(item)) {
                        isEqual = false;
                        break;
                    }
                if(isEqual)
                    return pair.getKey();
            }
        }
        if(items.get(LR_goto) != null)
            return LR_goto;
        else {
            String s1 = "" + LR_goto;
            for(Map.Entry<Integer, ArrayList<ItemLR>> pair : items.entrySet()){
                String s2 = "" + pair.getKey();
                if(s2.startsWith(s1) || s2.endsWith(s1))
                    return pair.getKey();
            }
        }
        return -1;
    }

    public void ResizeItems(){
        ArrayList<Integer> indexes = new ArrayList<>();
        items = new HashMap<>();

        for (int i = 0; i < LR.items.size(); i++) {
            for (int j = i + 1; j < LR.items.size(); j++) {
                if (LALR_itemsAreEqual(LR.items.get(i), LR.items.get(j))) {
                    indexes.add(i);
                    indexes.add(j);
                    items.put(Integer.valueOf(i + "" + j), union(LR.items.get(i),LR.items.get(j)));
                    break;
                }
            }
        }
        for (int i = 0; i < LR.items.size(); i++)
            if(!indexes.contains(i))
                items.put(i, LR.items.get(i));

        //for(Map.Entry<Integer, ArrayList<ItemLR>> pair : LALR_items.entrySet())
        //    System.out.println(pair.getKey() + " " + pair.getValue());
    }



    // O(n*log(n))
    private boolean LALR_itemsAreEqual(ArrayList<ItemLR> list1, ArrayList<ItemLR> list2){
        HashMap<Production, Boolean> temp = new HashMap<>();
        if(list1.size() < list2.size()) {
            for (ItemLR item : list1)
                temp.put(new Production(item), false);

            for(ItemLR item : list2){
                Production t = new Production(item);
                for(ItemLR it : list1){
                    if(new Production(item).equals(new Production(it))){
                        temp.replace(t, true);
                        break;
                    }
                }
            }
        }else{
            for(ItemLR item : list2)
                temp.put(new Production(item), false);

            for(ItemLR item : list1){
                Production t = new Production(item);
                for(ItemLR it : list2){
                    if(new Production(item).equals(new Production(it))){
                        temp.replace(t, true);
                        break;
                    }
                }
            }
        }

        for(Map.Entry<Production, Boolean> entry : temp.entrySet()) {
            if(!entry.getValue())
                return false;
        }
        return true;
    }

    // si  - перенос и размещение в стеке состояния i
    // rj   - свёртка по продукции:
    // acc - принятие
    // err - ошибка
    public String ACTION(int i, Token a){
        ItemLR st = new ItemLR(1, LR.g.Productions.get(0));
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
            if(item.nonTerminal.data.equals(LR.g.startSymbol.data))
                continue;
            if(item.getIndexOfDot() + 1 == item.size()) {
                Production wanted = new Production(item);
                wanted.definition.remove(wanted.getIndexOfDot());
                //System.out.println("ACTION( " + i + ", " + a.data + ") = r" + Productions.indexOf(wanted));
                return "r" + LR.g.Productions.indexOf(wanted);
            }
        }
        return "err";
    }
}
