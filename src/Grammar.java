import org.w3c.dom.ls.LSOutput;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

import static java.lang.Math.abs;

class Grammar {
    ArrayList<Token> Terminals;
    ArrayList<Token> NonTerminals;

    private int countOfProduction;
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

    public static void main(String[] args) throws IOException {

/*
        Готовый тестовый пример для GOTO и для CLOSURE тоже

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

    //============================== LR ==============================

    void printGrammar(){
        System.out.println();
        for (Production production : Productions)
            System.out.println(production.toString());
    }

    void augmentGivenGrammar(){
        Token start = new Token(Productions.get(0).nonTerminal.data, "NONTERMINAL");
        start.data += "_st";
        Productions.add(0, new Production(start).add(Productions.get(0).nonTerminal));
        NonTerminals.add(0, start);
    }

    ArrayList<Production> GoTo(ArrayList<Production> I, Token X){
        ArrayList<Production> J = new ArrayList<>();
        for(Production pro : I){
            if(pro.definitions.contains(X)){
                Production temp = new Production(pro.nonTerminal);
                temp.definitions.addAll(pro.definitions);
                int indexOfDot = temp.definitions.indexOf(new Token("•", "DOT"));

                if(indexOfDot + 1 == temp.definitions.size())
                    continue;

                if( abs(indexOfDot - temp.definitions.indexOf(X) ) > 1)
                    continue;

                // если точка правее токена
                if(indexOfDot > temp.definitions.indexOf(X))
                    continue;

                temp.definitions.set(indexOfDot, temp.definitions.get(indexOfDot + 1));
                temp.definitions.set(indexOfDot + 1, new Token("•", "DOT"));

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
        if(I.definitions.indexOf(new Token("•", "DOT")) + 1 == I.definitions.size()){
            set.add(I);
            return set;
        }

        q.addFirst(I.get(I.definitions.indexOf(new Token("•", "DOT")) + 1));

        do {
            for(Production pro : Productions){
                if(pro.nonTerminal.data.equals(q.peekFirst().data)){
                    Production t = createItem(0, pro);
                    if(!set.contains(t)) {
                        set.add(t);
                        Token tok = t.definitions.get(t.definitions.indexOf(new Token("•", "DOT")) + 1);

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
        pro.definitions.addAll(p.definitions);
        pro.definitions.add(index, new Token("•", "DOT"));
        return pro;
    }

    void LRParser(ArrayList<Token> Input){

        ArrayList<Token> input = new ArrayList<>(Input);
        input.add(new Token("$", "END_MARKER"));

        ArrayList<Token> symbols = new ArrayList<>();
        symbolsForACTION = new ArrayList<>();
        symbols.add(new Token("$", "END_MARKER"));


        ArrayList<Production> I0 = closure(createItem(0, Productions.get(0)));

        ArrayList<Production> I_j;

        System.out.println(Input);

        Stack<Integer> stack = new Stack<>();
        stack.push(0);

        Token a = input.get(0);
        int pointer = 0;

        buildAllItems();

        for (int i = 1; i < 10; i++) {
            int s = stack.peek();

            System.out.println();
            System.out.println("СТРОКА   = " + i);
            System.out.println("Стек     = " + stack);
            System.out.println("Символы  = " + symbols);
            System.out.println("Вход     = " + input);
            System.out.println("s = " + s + " --- a = " + a.data);


            int action = ACTION1(s, a);
            if ( action == 1){
                symbols.add(input.get(pointer));
                symbolsForACTION.add(input.get(pointer));
                stack.push(t);
                prevToken = input.get(pointer);
                pointer++;
                a = input.get(pointer);
                //input.remove(0);
                System.out.println("Перенос в " + t);
            }
            else if (action == 2){
                stack.pop();
                System.out.println("Свертка по " + reduce.nonTerminal.data + " -> " + reduce.definitions);
                int count = reduce.definitions.size();
                for (int j = 0; j < count; j++){
                    symbols.remove(symbols.size() - 1);
                    symbolsForACTION.remove(symbolsForACTION.size() - 1);
                }
                symbols.add(reduce.nonTerminal);
                symbolsForACTION.add(reduce.nonTerminal);
                stack.push(getIndexFromGoTo(items.get(stack.peek()), reduce.nonTerminal));

            } else if(action == 3){
                System.out.println("SUCCESS");
                break;
            }


        }

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
            if(ind + 1 == pro.definitions.size())
                continue;
            Token t = pro.definitions.get(ind + 1);
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
                    if (ind + 1 == pro.definitions.size())
                        continue;
                    Token t = pro.definitions.get(ind + 1);
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



    // si  - перенос и размещение в стеке состояния i
    // r   - свёртка по продукции:
    Production reduce;
    // acc - принятие
    // Err - ошибка
    String ACTION(int i, Token a){

        // Очень сомнительное условие
        // UPD: страница 324 пункт в)     нужно переписать
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
        Token t = items.get(i).get(0).nonTerminal;

        ArrayList<Token> f = FollowLR.get(t);
        for (Token tok : f) {
            if (tok.data.equals(a.data)) {
                for(Production pro : items.get(i)) {
                    if (pro.getIndexOfDot() + 1 == pro.definitions.size()) {
                        if (pro.nonTerminal.equals(t)) {
                            Production wanted = new Production(pro);
                            wanted.definitions.remove(wanted.getIndexOfDot());
                            System.out.println("ACTION( " + i + ", " + a.data + ") = r" + Productions.indexOf(wanted));
                            return "r" + Productions.indexOf(wanted);
                        }
                    }
                }
            }

        }

        return "Err";
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
        // if t = start symbol of augmented grammar
        if(t.equals(NonTerminals.get(1))){
            result.add(new Token("$", "END_MARKER"));
        }

        for (Production pro : Productions){
            if(pro.definitions.contains(t)){
                if(FollowLR.get(pro.nonTerminal) != null)
                    result.addAll(FollowLR.get(pro.nonTerminal));
                int position = pro.definitions.lastIndexOf(t);
                if(position + 1 != pro.definitions.size()){
                    result.addAll(FollowLR(pro.definitions.get(position + 1), t));
                }
            }
        }
        return result;
    }






    int t;

    Token prevToken;
    ArrayList<Token> symbolsForACTION;
    // 1 = Shift  - перенос
    // 2 = Reduce - свёртка
    // 3 = Accept - принятие
    // 4 = Error  - ошибка
    int ACTION1(int i, Token a){
        int state = -1;
        if(i != -1)
            state = getIndexFromGoTo(items.get(i),a);
        if(state != -1){
            t = state;
            return 1;
        } else {

            ArrayList<Token> partToFind = new ArrayList<>(symbolsForACTION);
            //System.out.println(symbolsForACTION);
            for (int j = symbolsForACTION.size() - 1; j >= 0; j--) {
                for (Production pro : Productions) {
                    if (pro.definitions.equals(partToFind)) {
                        System.out.println(partToFind);
                        reduce = pro;
                        return 2;
                    }
                }
                partToFind.remove(0);
            }
        }
        if(a.equals(new Token("$", "END_MARKER")))
            return 3;

        return 4;
    }

    //============================== LR ==============================

    void makeSyntaxMatrix(){
        SyntaxMatrix = new HashMap<>();

        SyntaxMatrix.put(" ", new HashMap<>());
        for (Token t : Terminals) {
            SyntaxMatrix.get(" ").put(t.data, null);
        }
        for(Token nt : NonTerminals)
            SyntaxMatrix.put(nt.data, new HashMap<>());

        for(Production pro : Productions){
            for(Token tok : FirstNew(pro)){
                if(tok != null)
                    if(!tok.type.equals("EPSILON"))
                        SyntaxMatrix.get(pro.nonTerminal.data).put(tok.data, pro);
                    else{
                        for(Token t : follow.get(pro.nonTerminal)){ // ============================
                            if(t != null){
                                SyntaxMatrix.get(pro.nonTerminal.data).put(t.data, new Production(pro.nonTerminal, true));
                            }
                        }
                    }
            }
        }

        System.out.println("Matrix: ");
        for(Token nt : NonTerminals){
            for(Token t : Terminals)
                System.out.print(SyntaxMatrix.get(nt.data).get(t.data) + " ");
            System.out.println();
        }


    }

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

    void Parse(String str){
        //ArrayList<String> program = new ArrayList<>();

        // Пока что эта строка - "Лексер". Для разбора требуется один пробел между каждым токеном
        //String[] tempArr = str.split(" ");

        //
        ArrayList<String> program = new ArrayList<>();
        String token = "";
        for (int i = 0; i < str.length(); i++) {
            if(str.charAt(i) == ' ')
                continue;

            token += str.charAt(i);
            for(Token t : Terminals){
                if(t.data.equals(token)){
                    program.add(token);
                    token = "";
                }
            }
        }

        System.out.println("Получено _" + str + "_");
        System.out.println("Разобрано _" + program + "_");

        if(!true)
            return;

        /*
        for(String s : tempArr)
            if(!s.equals(" "))
                program.add(s.replaceAll("\n", ""));

         */


        int codePointer = 0;
        Stack<String> stack = new Stack<>();
        stack.push("$");
        stack.push(NonTerminals.get(0).data);

        while(true){
            //System.out.println();
            //if(codePointer < program.length)
            //    System.out.println(stack.peek() + ":::" + program[codePointer]);
            //if(TokenHasEpsProd(stack.peek())){
            //    stack.pop();
            //} else
            if(stack.peek().equals("$") && codePointer == program.size()){
                System.out.println("Успех");
                controller.LogConsole.appendText("Success\n");
                return;
            } else if(stack.peek().equals("#")){
                stack.pop();
            }
            else if(codePointer < program.size() && program.get(codePointer).equals(stack.peek())){
                System.out.println("Case 2");
                codePointer++;
                stack.pop();
            } else {
                if(codePointer == program.size()){
                    if(TokenHasEpsProd(stack.peek()))
                            stack.pop();
                    continue;
                }

                System.out.println(stack.peek() + "===" + program.get(codePointer));
                System.out.println(stack);
                Production p = SyntaxMatrix.get(stack.peek()).get(program.get(codePointer));

                System.out.println(stack.peek() + " " + program.get(codePointer) + " === " + p);

                if(p == null){
                    System.out.println("FAIL");
                    controller.LogConsole.appendText("FAIL\n");
                    return;
                }
                String[] temp = p.getProd().split(" ");
                stack.pop();
                for (int i = temp.length - 1; i > -1; i--) {
                    stack.push(temp[i]);
                }
                System.out.println(stack);
            }
        }


    }

    private boolean TokenHasEpsProd(String tok){
        boolean result = false;
        for(Production pro : Productions){
            if(pro.nonTerminal.data.equals(tok))
                result |= pro.hasEpsilonProduction();
        }
        return result;
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

        countOfProduction = rawTokens.size();
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
        countOfProduction = definitionIndex;
    }

    Grammar(String grammar, int i){

    }


    Token[] First(Token token){
        int setIndex = 0;
        Token[] set = new Token[30];
        //System.out.println();

        if(token.type.equals("TERMINAL"))
            set[setIndex] = token;
        else if(token.type.equals("EPSILON"))
            set[setIndex] = token;
        else {
            for(Production pro : Productions){
                if(pro.nonTerminal.equals(token)){
                    boolean allProductionsHasEps = true;
                    for(int i = 0; i < pro.size(); i++){
                        Token Yi = pro.get(i);
                        if(Yi.type.equals("TERMINAL")){
                            set[setIndex] = Yi;
                            setIndex++;
                            allProductionsHasEps = false; //
                            break;
                        }
                        Token[] FirstYi = First(Yi);

                        if(inTokenArray(FirstYi, new Token("#", "EPSILON")) == -1){
                            allProductionsHasEps = false;
                        }

                        if(Yi.type.equals("NONTERMINAL")){
                            for(Token t : First(Yi)){
                                if(t != null && !t.type.equals("EPSILON")){
                                    set[setIndex] = t;
                                    setIndex++;
                                }
                            }
                        }
                        if (!Yi.type.equals("NONTERMINAL") || inTokenArray(FirstYi, new Token("#", "EPSILON")) == -1)
                            break;

                    }
                    if(allProductionsHasEps ){
                        set[setIndex] = new Token("#", "EPSILON");
                        setIndex++;
                    }
                }
            }
        }
        return set;
    }

    Token[] Follow(Token token){
        //System.out.println();
        //System.out.println("Обработка : " + token.data);
        String debug = "";
        Token[] set = new Token[100];
        int setIndex = 0;
        if (token.equals(NonTerminals.get(0))){
            set[setIndex] = new Token("$", "END_MARKER");
            setIndex++;
        }
        for (Production pro : Productions) {
            if(!pro.definitions.contains(token))
                continue;
            if(token.data.equals(debug)){
                System.out.println(pro + " has token " + token.data);
            }
            int i = pro.getTokenIndex(token) + 1;
            if(token.data.equals(debug))
                System.out.println(pro + " " + i + " pro.size=" + pro.size());

            // Rule-2
            if(i == pro.size()){
                if(token.data.equals(debug)){
                    System.out.println("Rule-2");
                }
                if (pro.nonTerminal.equals(NonTerminals.get(0))){
                    Token[] follow = Follow(pro.nonTerminal);
                    for (Token t : follow) {
                        if (t != null) {
                            set[setIndex] = t;
                            setIndex++;
                        }
                    }
                    set[setIndex] = new Token("$", "END_MARKER");
                    setIndex++;
                    //return set;
                } else {
                    if(pro.nonTerminal.equals(token))
                        continue;

                    Token[] Follow = Follow(pro.nonTerminal);
                    for (Token t : Follow) {
                        if (t != null) {
                            set[setIndex] = t;
                            setIndex++;
                        }
                    }
                    if(token.data.equals(debug))
                        System.out.println("Follow(" + pro.nonTerminal.data + ")=" + Arrays.toString(Follow));
                }
                //return set;
                //continue;
            }
            else
                for(; i < pro.size(); i++){
                    // Rule 3
                    Token[] First = First(pro.get(i));
/*
                System.out.println(pro.get(i));
                System.out.println(first.get(pro.get(i)));
                System.out.println(first.get(new Token("B", "NONTERMINAL")));
                System.out.println(first.size());
                Token[] First = (Token[]) first.get(pro.get(i)).toArray();
 */
                    // Rule 3-2
                    if(inTokenArray(First, new Token("#", "EPSILON")) !=-1 ){
                        if(token.data.equals(debug)){
                            System.out.println("Rule-3-2");
                        }

                        // Это на example5 не влияет
                        if(pro.get(i).equals(token))
                            continue;


                        for (Token value : First) {
                            //System.out.println(j + "=" + First[j]);
                            if (value == null || value.type.equals("EPSILON"))
                                continue;
                            set[setIndex] = value;
                            setIndex++;
                        }
                        Token[] Follow = Follow(pro.nonTerminal);
                        for (Token t: Follow) {
                            if(t != null){
                                set[setIndex] = t;
                                setIndex++;
                            }
                        }
                        if(token.data.equals(debug)){
                            System.out.println("Follow(" + pro.nonTerminal.data + ") = " + Arrays.toString(Follow));
                            System.out.println("First(" + pro.get(i).data + ") = " + Arrays.toString(First));
                            System.out.println("Set = " + Arrays.toString(set));
                        }
                    }
                    // Rule-3-1
                    else {
                        if(token.data.equals(debug))
                            System.out.println("Rule-3-1");
                        for (Token t: First) {
                            if(t != null){
                                set[setIndex] = t;
                                setIndex++;
                            }
                        }
                    }
                }
        }
        return set;
    }

    void makeFirstSet() {
        FirstSet = new ArrayList<>(NonTerminals.size());
        first = new HashMap<>(NonTerminals.size());
        for (Token nonTerminal : NonTerminals) {
            ArrayList<Token> e = new ArrayList<>(Arrays.asList(First(nonTerminal)));
            ArrayList<Token> ee = removeDuplicates(e);
            ee.remove(null);
            FirstSet.add(ee);
            first.put(nonTerminal, ee);
        }
    }

    void makeFollowSet() {
        FollowSet = new ArrayList<>(NonTerminals.size());
        follow = new HashMap<>(NonTerminals.size());
        for (Token nonTerminal : NonTerminals) {
            ArrayList<Token> e;
            try {
                e = new ArrayList<>(Arrays.asList(Follow(nonTerminal)));
            } catch (StackOverflowError stackOverflowError){
                e = new ArrayList<>(Arrays.asList(First(nonTerminal)));
                e.add(new Token("$","END_MARKER"));
                e.remove(new Token("#","EPSILON"));
            }
            ArrayList<Token> ee = removeDuplicates(e);
            ee.remove(null);
            //System.out.println(ee);
            FollowSet.add(ee);
            follow.put(nonTerminal, ee);
        }
    }

    void printFirstSet(){
        for (Token nonTerminal : NonTerminals) {
            String firstString = "FIRST(" + nonTerminal.data + ") = {";
            for (Token token: first.get(nonTerminal))
                if (token != null)
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
                if (token != null)
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

    // Токен содерится в массиве
    private int inTokenArray(Token[] tokenArray, Token wantedToken) {
        int index = 0;
        for (Token arrayToken : tokenArray) {
            if (arrayToken != null) {
                if (wantedToken.data.equals(arrayToken.data)) {
                    return index;
                }
            }
            index ++;
        }
        return -1;
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

    private Token[] FirstNew(Production pro){
        Token token = pro.nonTerminal;
        int setIndex = 0;
        Token[] set = new Token[30];
        //System.out.println();

        if(token.type.equals("TERMINAL"))
            set[setIndex] = token;
        else if(token.type.equals("EPSILON"))
            set[setIndex] = token;
        else {
            boolean allProductionsHasEps = true;
            for(int i = 0; i < pro.size(); i++){
                Token Yi = pro.get(i);
                //System.out.println(pro);
                if(Yi.type.equals("TERMINAL")){
                    set[setIndex] = Yi;
                    setIndex++;
                    allProductionsHasEps = false; //
                    break;
                }
                Token[] FirstYi = First(Yi);
                if(inTokenArray(FirstYi, new Token("#", "EPSILON")) == -1){
                    allProductionsHasEps = false;
                }
                if(Yi.type.equals("NONTERMINAL")){
                    for(Token t : First(Yi)){
                        if(t != null && !t.type.equals("EPSILON")){
                            set[setIndex] = t;
                            setIndex++;
                        }
                    }
                }
                if (!Yi.type.equals("NONTERMINAL") || inTokenArray(FirstYi, new Token("#", "EPSILON")) == -1)
                    break;
            }
            if(allProductionsHasEps ){
                set[setIndex] = new Token("#", "EPSILON");
                setIndex++;
            }
        }
        /*
        for(Production pro : Productions){
            if(pro.nonTerminal.equals(token)){
                if(pro.hasToken(new Token("#", "EPSILON")))
                    set[setIndex] = new Token("#", "EPSILON");
            }
        }
         */
        //System.out.println("final set for " + token.data + " is " + Arrays.toString(set));
        return set;
    }

}
