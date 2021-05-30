package main;

import util.*;
import static util.SupportFunctions.*;
import java.io.*;
import java.util.*;


public class Grammar {

    public Token startSymbol;
    public ArrayList<Token> Terminals;
    public ArrayList<Token> NonTerminals;

    public ArrayList<Production> Productions;

    boolean isAugmented;

    public static void main(String[] args) {

    }

    public void printGrammar(){
        System.out.println();
        for (Production production : Productions)
            System.out.println(production.toString());
    }

    public void augmentGivenGrammar(){
        Token start = new Token(Productions.get(0).nonTerminal.data, "NONTERMINAL");
        start.data += "_st";
        startSymbol = start;
        Productions.add(0, new Production(start).add(Productions.get(0).nonTerminal));
        NonTerminals.add(0, start);
        isAugmented = true;
    }


    boolean isLeftRecursive(){
        for(Production pro : Productions)
            if(pro.nonTerminal.data.equals(pro.get(0).data))
                return true;
        return false;
    }


    public ArrayList<Token> Lexer(String input){
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



    public Grammar(String fileName) throws IOException {
        ArrayList<String> rawTokens = new ArrayList<>();
        File fileObject = new File(fileName);
        BufferedReader in = new BufferedReader(new FileReader(fileObject));
        String s;
        while ( (s = in.readLine()) != null) {
            Scanner lineScanner = new Scanner(s).useDelimiter("\\s");
            while (lineScanner.hasNextLine()) {
                String line = lineScanner.nextLine();
                // Убираем комментарии
                if(!line.contains("//") && line.length() > 1)
                    rawTokens.add(line);
            }
        }
        in.close();

        int countOfProduction = rawTokens.size();
        Productions = new ArrayList<>(countOfProduction);
        String temp;
        for (String rawToken : rawTokens) {
            temp = (String) rawToken.subSequence(0, rawToken.indexOf(":"));
            Productions.add(new Production(new Token(temp, "NONTERMINAL")));
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
