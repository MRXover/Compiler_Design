import java.io.*;
import java.util.*;

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


    public static void main(String[] args) throws IOException {
        Grammar g = new Grammar("example10.txt");
        g.printGrammar();
        g.augmentGivenGrammar();
        g.printGrammar();

        HashSet<Production> C = new HashSet<>();
        Production start = g.createItem(0, g.Productions.get(0));
        C.add(start);
        System.out.println();
        System.out.println(start);
        System.out.println();
        System.out.println(g.closure(C));


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

    HashSet<Production> closure(HashSet<Production> I){
        LinkedHashSet<Production> J = new LinkedHashSet<Production>(I);
        Boolean added[] = new Boolean[Productions.size()];
        boolean nothingToAdd = true;

        do {
            //каждый пункт из J
            Iterator iterator = J.iterator();
            while (iterator.hasNext())  {
                // A -> a Х B b
                Production pro = (Production) iterator.next();
                Token B = pro.definitions.get(pro.definitions.indexOf(new Token("Х", "DOT")) + 1);

                for (Production A : Productions) {
                    if (A.nonTerminal.equals(B)) {

                        J.add(createItem(0, A));
                        if(A.definitions.indexOf(new Token("Х", "DOT")) != A.definitions.size()){
                            nothingToAdd = false;
                        }
                    }
                }
            }
        } while (!nothingToAdd);
        


        return J;
    }


    Production createItem(int index, Production p){
        Production pro = new Production(p.nonTerminal);
        pro.definitions.addAll(p.definitions);
        pro.definitions.add(index, new Token("Х", "DOT"));
        return pro;
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

    void Parse(String str){
        //ArrayList<String> program = new ArrayList<>();

        // ѕока что эта строка - "Ћексер". ƒл€ разбора требуетс€ один пробел между каждым токеном
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

        System.out.println("ѕолучено _" + str + "_");
        System.out.println("–азобрано _" + program + "_");

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
                System.out.println("”спех");
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
                // ”бираем комментарии
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
        //System.out.println("ќбработка : " + token.data);
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

                        // Ёто на example5 не вли€ет
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

    // “окен содеритс€ в массиве
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
        if(s.equals("Х"))
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
