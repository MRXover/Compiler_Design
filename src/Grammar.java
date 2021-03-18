import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

import static java.lang.Math.*;

class Grammar {

    Token startSymbol;
    ArrayList<Token> Terminals;
    ArrayList<Token> NonTerminals;

    ArrayList<Production> Productions;

    ArrayList<ArrayList<Token>> FirstSet;
    ArrayList<ArrayList<Token>> FollowSet;
    HashMap<Token, ArrayList<Token>> first;
    HashMap<Token, ArrayList<Token>> follow;

    private Controller controller;
    HashMap<String, HashMap<String, Production>> SyntaxMatrix;

    ArrayList<ArrayList<Production>> SLR_items;
    ArrayList<ArrayList<ItemLR>> LR_items;
    HashMap<Integer, ArrayList<ItemLR>> LALR_items;
    HashMap<Token, ArrayList<Token>> FollowLR;



    boolean isAugmented;

    public static void main(String[] args) throws IOException {
        Grammar g = new Grammar("example11.txt");
        g.augmentGivenGrammar();
        g.printGrammar();
        ItemLR i = new ItemLR(0, g.Productions.get(0));
        i.setTerminal(new Token("$"));
        System.out.println(i);

        //System.out.println(g.LR_CLOSURE(i));
        g.LR_buildAllItems();
        g.LALR_ResizeItems();

        System.out.println("=======================================================================");
        System.out.println(g.LALR_getIndexFromGoTo(g.LALR_items.get(36), new Token("c", "TERMINAL")));

        //ArrayList<ItemLR> I7 = removeDuplicates(g.LR_GOTO(I6, new Token("d","TERMINAL")));
        //System.out.println("I7 = " + I7);

        //g.LR_buildAllItems();


        //ArrayList<Token> tok = new ArrayList<>();
        //tok.add(new Token("C"));
        //tok.add(new Token("$"));
        //System.out.println(tok);
        //System.out.println(g.LR_First(tok));
    }

    //============================= LALR =============================

    int LALR_getIndexFromGoTo(ArrayList<ItemLR> I, Token X){
        int LR_goto = LR_getIndexFromGoTo(I, X);
        if(LR_goto == -1){
            ArrayList<ItemLR> wanted = removeDuplicates(LR_GOTO(I, X));
            for(Map.Entry<Integer, ArrayList<ItemLR>> pair : LALR_items.entrySet()){
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
        if(LALR_items.get(LR_goto) != null)
            return LR_goto;
        else {
            String s1 = "" + LR_goto;
            for(Map.Entry<Integer, ArrayList<ItemLR>> pair : LALR_items.entrySet()){
                String s2 = "" + pair.getKey();
                if(s2.startsWith(s1) || s2.endsWith(s1))
                    return pair.getKey();
            }
        }
        return -1;
    }

    void LALR_ResizeItems(){
        ArrayList<Integer> indexes = new ArrayList<>();
        LALR_items = new HashMap<>();

        for (int i = 0; i < LR_items.size(); i++) {
            for (int j = i + 1; j < LR_items.size(); j++) {
                if (LALR_itemsAreEqual(LR_items.get(i), LR_items.get(j))) {
                    indexes.add(i);
                    indexes.add(j);
                    LALR_items.put(Integer.valueOf(i + "" + j), union(LR_items.get(i),LR_items.get(j)));
                    break;
                }
            }
        }
        for (int i = 0; i < LR_items.size(); i++)
            if(!indexes.contains(i))
                LALR_items.put(i, LR_items.get(i));

        //for(Map.Entry<Integer, ArrayList<ItemLR>> pair : LALR_items.entrySet())
        //    System.out.println(pair.getKey() + " " + pair.getValue());
    }

    public <T> ArrayList<T> union(ArrayList<T> list1, ArrayList<T> list2) {
        Set<T> set = new HashSet<T>();
        set.addAll(list1);
        set.addAll(list2);
        return new ArrayList<T>(set);
    }

    // O(n*log(n))
    boolean LALR_itemsAreEqual(ArrayList<ItemLR> list1, ArrayList<ItemLR> list2){
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
    String LALR_ACTION(int i, Token a){
        ItemLR st = new ItemLR(1, Productions.get(0));
        st.setTerminal(new Token("$"));

        if(a.data.equals("$") && LALR_items.get(i).contains(st))
            return "acc";

        boolean shift = false;
        for(ItemLR item : LALR_items.get(i)){
            int dotPos = item.getIndexOfDot();
            if(dotPos + 1 != item.size() && item.get(dotPos + 1).data.equals(a.data)) {
                shift = true;
                break;
            }
        }
        if(shift){
            //System.out.println("ACTION( " + i + ", " + a.data + ") = s" + LR_getIndexFromGoTo(LR_items.get(i),a));
            return "s" + LALR_getIndexFromGoTo(LALR_items.get(i),a);
        }

        for(ItemLR item : LALR_items.get(i)){
            if(!item.terminal.data.equals(a.data))
                continue;
            if(item.nonTerminal.data.equals(startSymbol.data))
                continue;
            if(item.getIndexOfDot() + 1 == item.size()) {
                Production wanted = new Production(item);
                wanted.definition.remove(wanted.getIndexOfDot());
                //System.out.println("ACTION( " + i + ", " + a.data + ") = r" + Productions.indexOf(wanted));
                return "r" + Productions.indexOf(wanted);
            }
        }
        return "err";
    }

    public void LALR_Parser(ArrayList<Token> Input){
        Stage newWindow = new Stage();
        GridPane root = new GridPane();

        newWindow.setX(200);
        newWindow.setY(100);

        root.setPadding(new Insets(20));
        root.setHgap(25);
        root.setVgap(15);

        root.add(new Label("Строка"), 0, 0);
        root.add(new Label("Стек"), 1, 0);
        root.add(new Label("Символы"), 2, 0);
        root.add(new Label("Вход"), 3, 0);
        root.add(new Label("Действие"), 4, 0);

        ArrayList<Token> input = new ArrayList<>(Input);
        input.add(new Token("$", "END_MARKER"));

        ArrayList<Token> symbols = new ArrayList<>();
        symbols.add(new Token("$", "END_MARKER"));

        System.out.println(Input);

        Stack<Integer> stack = new Stack<>();
        stack.push(0);

        Token a = input.get(0);
        int pointer = 0;
        int stringIndex = 1;

        while (true) {
            int s = stack.peek();

            root.add(new Label("(" + stringIndex + ")"), 0, stringIndex);
            root.add(new Label(String.valueOf(stack)), 1, stringIndex);

            String symbolsString = "";
            for(Token t : symbols)
                symbolsString += t.data;

            root.add(new Label(symbolsString), 2, stringIndex);

            String inputString = "";
            for(Token t : input.subList(pointer, input.size()))
                inputString += t.data;
            root.add(new Label(inputString), 3, stringIndex);

            String action = LALR_ACTION(s, a);
            if ( action.charAt(0) == 's'){
                symbols.add(input.get(pointer));
                stack.push(Integer.valueOf(action.substring(1)));
                pointer++;
                a = input.get(pointer);
                System.out.println("Перенос в " + Integer.valueOf(action.substring(1)));
                root.add(new Label("Перенос в " + Integer.valueOf(action.substring(1))), 4, stringIndex);
            } else if (action.charAt(0) == 'r'){
                int prodNumber = Integer.parseInt(action.substring(1, action.length()));
                String shift = "Свертка по " + Productions.get(prodNumber).nonTerminal.data + " -> ";
                for (Token t : Productions.get(prodNumber).definition){
                    shift += t.data;
                    stack.pop();
                }
                System.out.println(shift);
                root.add(new Label(shift), 4, stringIndex);
                int count = Productions.get(prodNumber).definition.size();
                for (int j = 0; j < count; j++){
                    symbols.remove(symbols.size() - 1);
                }
                symbols.add(Productions.get(prodNumber).nonTerminal);
                stack.push(LALR_getIndexFromGoTo(LALR_items.get(stack.peek()), Productions.get(prodNumber).nonTerminal));
            } else if(action.charAt(0) == 'a'){
                System.out.println("SUCCESS");
                root.add(new Label("SUCCESS"), 4, stringIndex);
                break;
            } else if(action.charAt(0) == 'e'){
                System.out.println("ERROR");
                root.add(new Label("ERROR"), 4, stringIndex);
                break;
            }
            stringIndex++;

        }

        ScrollPane scrollPane = new ScrollPane(root);

        Scene scene = new Scene(scrollPane, root.getMaxWidth(), root.getMaxHeight());
        newWindow.setTitle("LR Parse table");
        newWindow.setScene(scene);

        newWindow.show();
    }

    ///============================ LALR =============================


    //============================== LR ==============================


    // si  - перенос и размещение в стеке состояния i
    // rj   - свёртка по продукции:
    // acc - принятие
    // err - ошибка
    String LR_ACTION(int i, Token a){
        ItemLR st = new ItemLR(1, Productions.get(0));
        st.setTerminal(new Token("$"));

        if(a.data.equals("$") && LR_items.get(i).contains(st))
            return "acc";

        boolean shift = false;
        for(ItemLR item : LR_items.get(i)){
            int dotPos = item.getIndexOfDot();
            if(dotPos + 1 != item.size() && item.get(dotPos + 1).data.equals(a.data)) {
                shift = true;
                break;
            }
        }
        if(shift){
            //System.out.println("ACTION( " + i + ", " + a.data + ") = s" + LR_getIndexFromGoTo(LR_items.get(i),a));
            return "s" + LR_getIndexFromGoTo(LR_items.get(i),a);
        }

        for(ItemLR item : LR_items.get(i)){
            if(!item.terminal.data.equals(a.data))
                continue;
            if(item.nonTerminal.data.equals(startSymbol.data))
                continue;
            if(item.getIndexOfDot() + 1 == item.size()) {
                Production wanted = new Production(item);
                wanted.definition.remove(wanted.getIndexOfDot());
                //System.out.println("ACTION( " + i + ", " + a.data + ") = r" + Productions.indexOf(wanted));
                return "r" + Productions.indexOf(wanted);
            }
        }

        return "err";
    }

    void LR_buildAllItems(){
        int index = 0;
        int oldIndex = 0;
        LR_items = new ArrayList<>();

        ItemLR p1 = new ItemLR(0, Productions.get(0));
        p1.setTerminal(new Token("$"));
        // I0
        LR_items.add(LR_CLOSURE(p1));

        ArrayList<Token> tokensToCheck = new ArrayList<>();

        for(ItemLR item : LR_items.get(0)){
            int ind = item.getIndexOfDot();
            if(ind + 1 == item.definition.size())
                continue;
            Token t = item.definition.get(ind + 1);
            if(!tokensToCheck.contains(t))
                tokensToCheck.add(t);
        }
        // 1st Iteration
        for(Token tok : tokensToCheck){
            LR_items.add(removeDuplicates(LR_GOTO(LR_items.get(0), tok)));
            index++;
        }
        oldIndex = index;
        int left = 1;

        tokensToCheck.clear();

        do{
            for (int i = left; i < oldIndex + 1; i++) {
                for (ItemLR item : LR_items.get(i)) {
                    int ind = item.getIndexOfDot();
                    if (ind + 1 == item.definition.size())
                        continue;
                    Token t = item.definition.get(ind + 1);
                    if (!tokensToCheck.contains(t))
                        tokensToCheck.add(t);
                }
                for (Token t : tokensToCheck) {
                    ArrayList<ItemLR> X = removeDuplicates(LR_GOTO(LR_items.get(i), t));
                    if(!LR_containsItem(X)) {
                        LR_items.add(X);
                        index++;
                    }
                }
                tokensToCheck.clear();
            }
            left = oldIndex;
            oldIndex = index;
        } while( left != index );
        /*
        int i = 0;
        for(ArrayList<ItemLR> list : LR_items){
            System.out.println();
            System.out.println("I" + i + " =");
            for(ItemLR item : list)
                System.out.println(item);
            i++;
        }
         */
    }

    boolean LR_containsItem(ArrayList<ItemLR> items){
        boolean currentIsEqual = true;
        for (ArrayList<ItemLR> lr_item : LR_items) {
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

    int LR_getIndexFromGoTo(ArrayList<ItemLR> I, Token X){
        ArrayList<ItemLR> wanted = removeDuplicates(LR_GOTO(I, X));
        for(int i = 0; i < LR_items.size(); i++){
            if(LR_items.get(i).equals(wanted))
                return i;
        }
        return -1;
    }

    ArrayList<ItemLR> LR_GOTO(ArrayList<ItemLR> I, Token X){
        ArrayList<ItemLR> J = new ArrayList<>();
        for(ItemLR item : I){
            if(item.definition.contains(X)){
                ItemLR temp = new ItemLR(item);

                int indexOfDot = temp.definition.indexOf(new Token("•", "DOT"));

                if(indexOfDot + 1 == temp.definition.size()){
                    continue;
                }

                if( abs(indexOfDot - temp.definition.indexOf(X) ) > 1){
                    continue;
                }

                // если точка правее токена
                if(indexOfDot > temp.definition.lastIndexOf(X))
                    continue;

                temp.definition.set(indexOfDot, temp.definition.get(indexOfDot + 1));
                temp.definition.set(indexOfDot + 1, new Token("•", "DOT"));

                J.addAll(LR_CLOSURE(temp));
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
                if (Terminals.contains(input.get(i)) || input.get(i).data.equals("$")){
                    result.add(input.get(i));
                    return result;
                } else {
                    ArrayList<Token> tokensFromNonterminal = LL_First(input.get(i));
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

    ArrayList<ItemLR> LR_CLOSURE(ItemLR I){
        boolean debug = false;
        HashMap<String, Boolean> added = new HashMap<>();
        for (Token tok : NonTerminals)
            added.put(tok.data, false);

        ArrayList<ItemLR> set = new ArrayList<>();
        set.add(I);
        ArrayDeque<Token> q = new ArrayDeque<>();
        if(I.definition.indexOf(new Token("•", "DOT")) + 1 == I.definition.size()){
            set.add(I);
            return set;
        }

        q.addFirst(I.get(I.definition.indexOf(new Token("•", "DOT")) + 1));

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

            for(Production pro : Productions){
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

                        for(int i = temp.definition.indexOf(new Token("•", "DOT")) + 2; i < temp.definition.size(); i++)
                            first.add(temp.definition.get(i));

                    }

                }
            }
            rightTokens.clear();
            rightTokens.addAll(LR_First(first));

            added.replace(q.peekFirst().data, true);
            q.pollFirst();

            step++;
        } while (!q.isEmpty() );
        return set;
    }

    public void LR_Parser(ArrayList<Token> Input){
        Stage newWindow = new Stage();
        GridPane root = new GridPane();

        newWindow.setX(200);
        newWindow.setY(100);

        root.setPadding(new Insets(20));
        root.setHgap(25);
        root.setVgap(15);

        root.add(new Label("Строка"), 0, 0);
        root.add(new Label("Стек"), 1, 0);
        root.add(new Label("Символы"), 2, 0);
        root.add(new Label("Вход"), 3, 0);
        root.add(new Label("Действие"), 4, 0);

        ArrayList<Token> input = new ArrayList<>(Input);
        input.add(new Token("$", "END_MARKER"));

        ArrayList<Token> symbols = new ArrayList<>();
        symbols.add(new Token("$", "END_MARKER"));

        System.out.println(Input);

        Stack<Integer> stack = new Stack<>();
        stack.push(0);

        Token a = input.get(0);
        int pointer = 0;
        int stringIndex = 1;

        while (true) {
            int s = stack.peek();

            root.add(new Label("(" + stringIndex + ")"), 0, stringIndex);
            root.add(new Label(String.valueOf(stack)), 1, stringIndex);

            String symbolsString = "";
            for(Token t : symbols)
                symbolsString += t.data;

            root.add(new Label(symbolsString), 2, stringIndex);

            String inputString = "";
            for(Token t : input.subList(pointer, input.size()))
                inputString += t.data;
            root.add(new Label(inputString), 3, stringIndex);

            String action = LR_ACTION(s, a);
            if ( action.charAt(0) == 's'){
                symbols.add(input.get(pointer));
                stack.push(Integer.valueOf(action.substring(1)));
                pointer++;
                a = input.get(pointer);
                System.out.println("Перенос в " + Integer.valueOf(action.substring(1)));
                root.add(new Label("Перенос в " + Integer.valueOf(action.substring(1))), 4, stringIndex);
            } else if (action.charAt(0) == 'r'){
                int prodNumber = Integer.parseInt(action.substring(1, action.length()));
                String shift = "Свертка по " + Productions.get(prodNumber).nonTerminal.data + " -> ";
                for (Token t : Productions.get(prodNumber).definition){
                    shift += t.data;
                    stack.pop();
                }
                System.out.println(shift);
                root.add(new Label(shift), 4, stringIndex);
                int count = Productions.get(prodNumber).definition.size();
                for (int j = 0; j < count; j++){
                    symbols.remove(symbols.size() - 1);
                }
                symbols.add(Productions.get(prodNumber).nonTerminal);
                stack.push(LR_getIndexFromGoTo(LR_items.get(stack.peek()), Productions.get(prodNumber).nonTerminal));
            } else if(action.charAt(0) == 'a'){
                System.out.println("SUCCESS");
                root.add(new Label("SUCCESS"), 4, stringIndex);
                break;
            } else if(action.charAt(0) == 'e'){
                System.out.println("ERROR");
                root.add(new Label("ERROR"), 4, stringIndex);
                break;
            }
            stringIndex++;

        }

        ScrollPane scrollPane = new ScrollPane(root);

        Scene scene = new Scene(scrollPane, root.getMaxWidth(), root.getMaxHeight());
        newWindow.setTitle("LR Parse table");
        newWindow.setScene(scene);

        newWindow.show();
    }

    ///============================= LR ==============================




    //============================== SLR ==============================

    void printGrammar(){
        System.out.println();
        for (Production production : Productions)
            System.out.println(production.toString());
    }

    void augmentGivenGrammar(){
        Token start = new Token(Productions.get(0).nonTerminal.data, "NONTERMINAL");
        start.data += "_st";
        startSymbol = start;
        Productions.add(0, new Production(start).add(Productions.get(0).nonTerminal));
        NonTerminals.add(0, start);
        isAugmented = true;
    }

    ArrayList<Production> GoTo(ArrayList<Production> I, Token X){
        ArrayList<Production> J = new ArrayList<>();
        for(Production pro : I){
            if(pro.definition.contains(X)){
                Production temp = new Production(pro.nonTerminal);
                temp.definition.addAll(pro.definition);
                int indexOfDot = temp.definition.indexOf(new Token("•", "DOT"));

                if(indexOfDot + 1 == temp.definition.size())
                    continue;

                if( abs(indexOfDot - temp.definition.indexOf(X) ) > 1)
                    continue;

                // если точка правее токена
                if(indexOfDot > temp.definition.indexOf(X))
                    continue;

                temp.definition.set(indexOfDot, temp.definition.get(indexOfDot + 1));
                temp.definition.set(indexOfDot + 1, new Token("•", "DOT"));

                J.addAll(closure(temp));
            }
        }
        return J;
    }

    ArrayList<Production> closure(Production I){
        HashMap<String, Boolean> added = new HashMap<>();
        for (Token tok : NonTerminals)
            added.put(tok.data, false);

        ArrayList<Production> set = new ArrayList<>();
        set.add(I);
        ArrayDeque<Token> q = new ArrayDeque<>();
        //System.out.println(I);
        //System.out.println(I.definitions.indexOf(new Token("•", "DOT")));
        if(I.definition.indexOf(new Token("•", "DOT")) + 1 == I.definition.size()){
            set.add(I);
            return set;
        }

        q.addFirst(I.get(I.definition.indexOf(new Token("•", "DOT")) + 1));

        do {
            for(Production pro : Productions){
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

    Production createItem(int index, Production p){
        Production pro = new Production(p.nonTerminal);
        pro.definition.addAll(p.definition);
        pro.definition.add(index, new Token("•", "DOT"));
        return pro;
    }

    public void SLR_Parser(ArrayList<Token> Input){
        Stage newWindow = new Stage();
        GridPane root = new GridPane();

        newWindow.setX(200);
        newWindow.setY(100);

        root.setPadding(new Insets(20));
        root.setHgap(25);
        root.setVgap(15);

        root.add(new Label("Строка"), 0, 0);
        root.add(new Label("Стек"), 1, 0);
        root.add(new Label("Символы"), 2, 0);
        root.add(new Label("Вход"), 3, 0);
        root.add(new Label("Действие"), 4, 0);

        ArrayList<Token> input = new ArrayList<>(Input);
        input.add(new Token("$", "END_MARKER"));

        ArrayList<Token> symbols = new ArrayList<>();
        symbols.add(new Token("$", "END_MARKER"));

        System.out.println(Input);

        Stack<Integer> stack = new Stack<>();
        stack.push(0);

        Token a = input.get(0);
        int pointer = 0;
        int stringIndex = 1;

        while (true) {
            int s = stack.peek();

            root.add(new Label("(" + stringIndex + ")"), 0, stringIndex);
            root.add(new Label(String.valueOf(stack)), 1, stringIndex);

            String symbolsString = "";
            for(Token t : symbols)
                symbolsString += t.data;

            root.add(new Label(symbolsString), 2, stringIndex);

            String inputString = "";
            for(Token t : input.subList(pointer, input.size()))
                inputString += t.data;
            root.add(new Label(inputString), 3, stringIndex);

            String action = ACTION(s, a);
            if ( action.charAt(0) == 's'){
                symbols.add(input.get(pointer));
                stack.push(Integer.valueOf(action.substring(1)));
                pointer++;
                a = input.get(pointer);
                System.out.println("Перенос в " + Integer.valueOf(action.substring(1)));
                root.add(new Label("Перенос в " + Integer.valueOf(action.substring(1))), 4, stringIndex);
            } else if (action.charAt(0) == 'r'){
                int prodNumber = Integer.parseInt(action.substring(1, action.length()));
                String shift = "Свертка по " + Productions.get(prodNumber).nonTerminal.data + " -> ";
                for (Token t : Productions.get(prodNumber).definition){
                    shift += t.data;
                    stack.pop();
                }
                System.out.println(shift);
                root.add(new Label(shift), 4, stringIndex);
                int count = Productions.get(prodNumber).definition.size();
                for (int j = 0; j < count; j++){
                    symbols.remove(symbols.size() - 1);
                }
                symbols.add(Productions.get(prodNumber).nonTerminal);
                stack.push(getIndexFromGoTo(SLR_items.get(stack.peek()), Productions.get(prodNumber).nonTerminal));
            } else if(action.charAt(0) == 'a'){
                System.out.println("SUCCESS");
                root.add(new Label("SUCCESS"), 4, stringIndex);
                break;
            } else if(action.charAt(0) == 'e'){
                System.out.println("ERROR");
                root.add(new Label("ERROR"), 4, stringIndex);
                break;
            }
            stringIndex++;

        }

        ScrollPane scrollPane = new ScrollPane(root);

        Scene scene = new Scene(scrollPane, root.getMaxWidth(), root.getMaxHeight());
        newWindow.setTitle("LR Parse table");
        newWindow.setScene(scene);

        newWindow.show();
    }

    void buildAllItems(){
        int index = 0;
        int oldIndex = 0;
        Production p1 = createItem(0, Productions.get(0));

        SLR_items = new ArrayList<>();
        // I0
        SLR_items.add(closure(p1));

        ArrayList<Token> tokensToCheck = new ArrayList<>();

        for(Production pro : SLR_items.get(0)){
            int ind = pro.getIndexOfDot();
            if(ind + 1 == pro.definition.size())
                continue;
            Token t = pro.definition.get(ind + 1);
            if(!tokensToCheck.contains(t))
                tokensToCheck.add(t);
        }

        // 1st Iteration
        for(Token tok : tokensToCheck){
            SLR_items.add(removeDuplicates(GoTo(SLR_items.get(0), tok)));
            index++;
            //System.out.println(index + " " +tok.data + " = " + removeDuplicates(GoTo(items.get(0), tok)));
        }
        oldIndex = index;
        int left = 1;

        tokensToCheck.clear();

        do{
            for (int i = left; i < oldIndex + 1; i++) {
                for (Production pro : SLR_items.get(i)) {
                    int ind = pro.getIndexOfDot();
                    if (ind + 1 == pro.definition.size())
                        continue;
                    Token t = pro.definition.get(ind + 1);
                    if (!tokensToCheck.contains(t))
                        tokensToCheck.add(t);
                }
                for (Token t : tokensToCheck) {
                    ArrayList<Production> X = removeDuplicates(GoTo(SLR_items.get(i), t));
                    if (!SLR_items.contains(X)) {
                        SLR_items.add(X);
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

    int getIndexFromGoTo(ArrayList<Production> I, Token X){
        ArrayList<Production> wanted = removeDuplicates(GoTo(I, X));
        for(int i = 0; i < SLR_items.size(); i++){
            if(SLR_items.get(i).equals(wanted))
                return i;
        }
        return -1;
    }

    // si  - перенос и размещение в стеке состояния i
    // rj   - свёртка по продукции:
    // acc - принятие
    // Err - ошибка
    String ACTION(int i, Token a){

        // Очень сомнительное условие
        // UPD: страница 324 пункт в)     нужно переписать !!!=====================================
        if(a.data.equals("$") && i == 1){
            System.out.println("ACTION( " + i + ", " + a.data + ") = acc");
            return "acc";
        }

        int state = -1;
        if(i != -1)
            state = getIndexFromGoTo(SLR_items.get(i),a);
        if(state != -1){
            System.out.println("ACTION( " + i + ", " + a.data + ") = s" + state);
            return "s" + state;
        }

        // разве по первой продукции?...
        System.out.println("i=" + i + " a=" + a.data);
        if(i == -1)
            return "err";

        Token t = SLR_items.get(i).get(0).nonTerminal;

        ArrayList<Token> f = FollowLR.get(t);
        for (Token tok : f) {
            if (tok.data.equals(a.data)) {
                for(Production pro : SLR_items.get(i)) {
                    if (pro.getIndexOfDot() + 1 == pro.definition.size()) {
                        if (pro.nonTerminal.equals(t)) {
                            Production wanted = new Production(pro);
                            wanted.definition.remove(wanted.getIndexOfDot());
                            System.out.println("ACTION( " + i + ", " + a.data + ") = r" + Productions.indexOf(wanted));
                            return "r" + Productions.indexOf(wanted);
                        }
                    }
                }
            }

        }

        return "err";
    }

    void buildFollowLR(){
        FollowLR = new HashMap<>();
        for(Token t : NonTerminals){
            FollowLR.put(t, removeDuplicates(FollowLR(t, null)));
        }
    }

    ArrayList<Token> FollowLR(Token t, Token prev){
        System.out.println("t= " + t + " prev = " + prev);
        if(t.equals(prev))
            return new ArrayList<>();

        ArrayList<Token> result = new ArrayList<Token>();
        if(t.type.equals("TERMINAL")){
            result.add(t);
            return result;
        }
        if(t.equals(startSymbol)){
            result.add(new Token("$", "END_MARKER"));
        }

        for (Production pro : Productions){
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


    ///============================= SLR ==============================

    //============================== LL ==============================

    void makeFirstSet() {
        FirstSet = new ArrayList<>(NonTerminals.size());
        first = new HashMap<>(NonTerminals.size());
        for (Token nonTerminal : NonTerminals) {
            ArrayList<Token> e = removeDuplicates(LL_First(nonTerminal));
            e.remove(null);
            FirstSet.add(e);
            first.put(nonTerminal, e);
        }
    }

    void makeFollowSet() {
        FollowSet = new ArrayList<>(NonTerminals.size());
        follow = new HashMap<>(NonTerminals.size());
        for (Token nonTerminal : NonTerminals) {
            ArrayList<Token> e;
            try {
                e = Follow(nonTerminal);
            } catch (StackOverflowError stackOverflowError){
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

    void printFirstSet(){
        for (Token nonTerminal : NonTerminals) {
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

    void printFollowSet(){
        for (Token nonTerminal : NonTerminals) {
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

    ArrayList<Token> LL_First(Token X){
        ArrayList<Token> temp = new ArrayList<Token>();
        temp.add(X);
        return LL_First(temp);
    }

    ArrayList<Token> LL_First(ArrayList<Token> X){
        ArrayList<Token> result = new ArrayList<>();
        if(X.get(0).isEpsilon()){
            result.add(X.get(0));
            return result;
        }
        if(Terminals.contains(X.get(0))){
            result.add(X.get(0));
            return result;
        }
        for(Production pro : Productions){
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
        if (token.equals(startSymbol))
            result.add(new Token("$", "END_MARKER"));

        for (Production pro : Productions) {
            if(!pro.definition.contains(token))
                continue;
            int i = pro.getTokenIndex(token) + 1;
            if(i == pro.size()){
                if (pro.nonTerminal.equals(startSymbol)){
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
    
    void LL_Parse(ArrayList<Token> program){
        int codePointer = 0;
        Stack<Token> stack = new Stack<>();
        stack.push(new Token("$"));
        stack.push(startSymbol);

        while(true){
            if(stack.peek().equals(new Token("$")) && codePointer == program.size()){
                controller.LogConsole.appendText("Success\n");
                return;
            } else if(stack.peek().equals(new Token("#"))){
                stack.pop();
            } else if(codePointer < program.size() && program.get(codePointer).equals(stack.peek())){
                codePointer++;
                stack.pop();
            } else {
                if(codePointer == program.size()){
                    if(TokenHasEpsProd(stack.peek()))
                        stack.pop();
                    continue;
                }
                Production p = SyntaxMatrix.get(stack.peek().data).get(program.get(codePointer).data);
                if(p == null){
                    controller.LogConsole.appendText("FAIL\n");
                    return;
                }
                ArrayList<Token> temp = p.definition;
                stack.pop();
                for (int i = temp.size() - 1; i > -1; i--)
                    stack.push(temp.get(i));
            }
        }
    }

    private boolean TokenHasEpsProd(Token tok){
        boolean result = false;
        for(Production pro : Productions){
            if(pro.nonTerminal.data.equals(tok.data))
                result |= pro.hasEpsilonProduction();
        }
        return result;
    }

    void makeSyntaxMatrix(){
        SyntaxMatrix = new HashMap<>();

        SyntaxMatrix.put(" ", new HashMap<>());
        for (Token t : Terminals)
            SyntaxMatrix.get(" ").put(t.data, null);

        for(Token nt : NonTerminals)
            SyntaxMatrix.put(nt.data, new HashMap<>());

        for(Production pro : Productions){
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

    ///============================= LL ==============================




    boolean isLeftRecursive(){
        for(Production pro : Productions)
            if(pro.nonTerminal.data.equals(pro.get(0).data))
                return true;
        return false;
    }


    ArrayList<Token> Lexer(String input){
        ArrayList<Token> program = new ArrayList<>();
        String tok = "";
        for (int i = 0; i < input.length(); i++) {
            if(input.charAt(i) == ' ')
                continue;

            tok += input.charAt(i);
            for(Token t : Terminals){
                if(t.data.equals(tok)){
                    program.add(t);
                    tok = "";
                }
            }
        }
        return program;
    }


    Grammar(Controller controller, String fileName) throws IOException{
        this(fileName);
        this.controller = controller;
        String str = "";
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine())
                str += scanner.nextLine() + "\n";
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        controller.GrammarArea.setText("");
        controller.GrammarArea.appendText(str + "\n");
    }

    Grammar(String fileName) throws IOException {
        ArrayList<String> rawTokens = new ArrayList<>();
        File fileObject = new File(fileName);
        BufferedReader in = new BufferedReader(new FileReader(fileObject));
        String s;
        while ( (s = in.readLine()) != null) {
            Scanner lineScanner = new Scanner(s).useDelimiter("\\s");
            while (lineScanner.hasNextLine()) {
                String line = lineScanner.nextLine();
                // Убираем комментарии
                if(line.contains("//"))
                    line = (String) line.subSequence(0, line.indexOf("//"));
                rawTokens.add(line);
            }
        }
        in.close();

        int countOfProduction = rawTokens.size();
        Productions = new ArrayList<>(countOfProduction);
        String temp;
        for (int i = 0; i < countOfProduction; i++) {
            temp = (String) rawTokens.get(i).subSequence(0, rawTokens.get(i).indexOf(":"));
            Productions.add( new Production(new Token(temp, "NONTERMINAL")));
        }

        ArrayList<Token> TempNonTerminals = new ArrayList<>();
        for (int i = 0; i < countOfProduction; i++)
            TempNonTerminals.add(Productions.get(i).nonTerminal);

        NonTerminals = removeDuplicates(TempNonTerminals);

        String[] temp1;
        int definitionIndex = 0;
        Terminals = new ArrayList<>();
        for (int i = 0; i < countOfProduction; i++) {
            temp1 = rawTokens.get(i).split(" ");
            for (int j = 1; j < temp1.length; j++) {
                String value = temp1[j].replace(" ", "");
                if(value.equals(":"))
                    continue;
                if(value.equals("|"))
                    Productions.add(++definitionIndex, new Production(NonTerminals.get(i)));
                else{
                    Productions.get(definitionIndex).add(new Token(value, type(value)));
                    if(type(value).equals("TERMINAL"))
                        Terminals.add(new Token(value, "TERMINAL"));
                }
            }
            definitionIndex++;
        }
        Terminals.add(new Token("$", "END_MARKER"));
        startSymbol = NonTerminals.get(0);
    }

    Grammar(String grammar, int i){

    }



    private static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {
        ArrayList<T> newList = new ArrayList<T>();
        for (T element : list)
            if (!newList.contains(element))
                newList.add(element);
        return newList;
    }

    private String type(String str){
        String s = str.replace(" ", "");
        for (Token nonTerminal : NonTerminals) {
            if (nonTerminal.data.equals(s))
                return "NONTERMINAL";
        }
        if(s.equals("|"))
            return "PIPE";
        if(s.equals("#"))
            return "EPSILON";
        if(s.equals("•"))
            return "DOT";
        return "TERMINAL";
    }


}
