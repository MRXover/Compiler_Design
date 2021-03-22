package stuff;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SupportFunctions {



    public static Production createItem(int index, Production p){
        Production pro = new Production(p.nonTerminal);
        pro.definition.addAll(p.definition);
        pro.definition.add(index, new Token("•", "DOT"));
        return pro;
    }

    public static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {
        ArrayList<T> newList = new ArrayList<T>();
        for (T element : list)
            if (!newList.contains(element))
                newList.add(element);
        return newList;
    }

    public static  <T> ArrayList<T> union(ArrayList<T> list1, ArrayList<T> list2) {
        Set<T> set = new HashSet<T>();
        set.addAll(list1);
        set.addAll(list2);
        return new ArrayList<T>(set);
    }

    public static Token createDOT(){
        return new Token("•", "DOT");
    }

}
