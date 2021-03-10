import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

import static java.lang.Math.abs;

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

    //HashSet<Production> closure;
    ArrayList<ArrayList<Production>> items;
    HashMap<Token, ArrayList<Token>> FollowLR;

    boolean isAugmented;

    public static void main(String[] args) throws IOException {

/*
        √отовый тестовый пример дл€ GOTO и дл€ CLOSURE тоже

        Grammar g = new Grammar("example10.txt");
        g.printGrammar();
        g.augmentGivenGrammar();
        g.printGrammar();

        ArrayList<Production> C = new ArrayList<Production>();
        Production p1 = g.createItem(1, g.Productions.get(0));
        Production p2 = g.createItem(1, g.Productions.get(1));
        C.add(p1);
        C.add(p2);

        System.out.println(p1);
        System.out.println(p2);

        g.GoTo(C, new Token("+", "TERMINAL"));
 */



    }

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
                int indexOfDot = temp.definition.indexOf(new Token("Х", "DOT"));

                if(indexOfDot + 1 == temp.definition.size())
                    continue;

                if( abs(indexOfDot - temp.definition.indexOf(X) ) > 1)
                    continue;

                // если точка правее токена
                if(indexOfDot > temp.definition.indexOf(X))
                    continue;

                temp.definition.set(indexOfDot, temp.definition.get(indexOfDot + 1));
                temp.definition.set(indexOfDot + 1, new Token("Х", "DOT"));

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
        //System.out.println(I.definitions.indexOf(new Token("Х", "DOT")));
        if(I.definition.indexOf(new Token("Х", "DOT")) + 1 == I.definition.size()){
            set.add(I);
            return set;
        }

        q.addFirst(I.get(I.definition.indexOf(new Token("Х", "DOT")) + 1));

        do {
            for(Production pro : Productions){
                if(pro.nonTerminal.data.equals(q.peekFirst().data)){
                    Production t = createItem(0, pro);
                    if(!set.contains(t)) {
                        set.add(t);
                        Token tok = t.definition.get(t.definition.indexOf(new Token("Х", "DOT")) + 1);

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
        pro.definition.add(index, new Token("Х", "DOT"));
        return pro;
    }

    public void SLRParser(ArrayList<Token> Input){
        Stage newWindow = new Stage();
        GridPane root = new GridPane();

        newWindow.setX(200);
        newWindow.setY(100);

        root.setPadding(new Insets(20));
        root.setHgap(25);
        root.setVgap(15);

        root.add(new Label("—трока"), 0, 0);
        root.add(new Label("—тек"), 1, 0);
        root.add(new Label("—имволы"), 2, 0);
        root.add(new Label("¬ход"), 3, 0);
        root.add(new Label("ƒействие"), 4, 0);

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
                stack.push(t);
                pointer++;
                a = input.get(pointer);
                System.out.println("ѕеренос в " + t);
                root.add(new Label("ѕеренос в " + t), 4, stringIndex);
            } else if (action.charAt(0) == 'r'){
                int prodNumber = Integer.parseInt(action.substring(1, action.length()));
                String shift = "—вертка по " + Productions.get(prodNumber).nonTerminal.data + " -> ";
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
                stack.push(getIndexFromGoTo(items.get(stack.peek()), Productions.get(prodNumber).nonTerminal));
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

        items = new ArrayList<>();
        // I0
        items.add(closure(p1));

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
            items.add(removeDuplicates(GoTo(items.get(0), tok)));
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
                    ArrayList<Production> X = removeDuplicates(GoTo(items.get(i), t));
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

    int getIndexFromGoTo(ArrayList<Production> I, Token X){
        ArrayList<Production> wanted = removeDuplicates(GoTo(I, X));
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).equals(wanted))
                return i;
        }
        return -1;
    }

    // si  - перенос и размещение в стеке состо€ни€ i
    // r   - свЄртка по продукции:
    // acc - прин€тие
    // Err - ошибка
    int t;
    String ACTION(int i, Token a){

        // ќчень сомнительное условие
        // UPD: страница 324 пункт в)     нужно переписать !!!=====================================
        if(a.data.equals("$") && i == 1){
            System.out.println("ACTION( " + i + ", " + a.data + ") = acc");
            return "acc";
        }

        int state = -1;
        if(i != -1)
            state = getIndexFromGoTo(items.get(i),a);
        if(state != -1){
            t = state;
            System.out.println("ACTION( " + i + ", " + a.data + ") = s" + t);
            return "s" + t;
        }

        // разве по первой продукции?...
        System.out.println("i=" + i + " a=" + a.data);
        if(i == -1)
            return "err";

        Token t = items.get(i).get(0).nonTerminal;

        ArrayList<Token> f = FollowLR.get(t);
        for (Token tok : f) {
            if (tok.data.equals(a.data)) {
                for(Production pro : items.get(i)) {
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
                // Ёто ужасна€ вещь, но она нужна дл€ редкого случа€, когда два нетерминала взаимосв€заны через Follow
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
            if(pro.nonTerminal.equals(X.get(0))){
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
                // ”бираем комментарии
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
        if(s.equals("Х"))
            return "DOT";
        return "TERMINAL";
    }


}
