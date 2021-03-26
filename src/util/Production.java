package util;

import java.util.ArrayList;
import java.util.Objects;

public class Production {
    public Token nonTerminal;
    public ArrayList<Token> definition;


    public Production(Token left){
        nonTerminal = left;
        definition = new ArrayList<>();
    }

    public Production(Token left, boolean addEps){
        nonTerminal = left;
        definition = new ArrayList<>();
        definition.add(new Token("#", "EPSILON"));
    }

    public Production(Production pro){
        nonTerminal = new Token(pro.nonTerminal.data, pro.nonTerminal.type);
        definition = new ArrayList<>(pro.definition);
    }

    public Production(ItemLR item){
        nonTerminal = new Token(item.nonTerminal.data, item.nonTerminal.type);
        definition = new ArrayList<>(item.definition);
    }

    public Production add(Token token){
        definition.add(token);
        return this;
    }

    public int size(){
        return definition.size();
    }

    public Token get(int index){
        return definition.get(index);
    }


    public int getTokenIndex(Token token){
        for (int i = 0; i < definition.size(); i++) {
            if(definition.get(i).data.equals(token.data))
                return i;
        }
        return -1;
    }

    public boolean hasEpsilonProduction(){
        if(definition.size() != 1)
            return false;
        return definition.get(0).data.equals("#");
    }

    public int getIndexOfDot(){
        for (int i = 0; i < definition.size(); i++) {
            if(definition.get(i).type.equals("DOT"))
                return i;
        }
        return -1;
    }

    
    @Override
    public String toString(){
        String result = nonTerminal.data + " : ";
        for (Token t : definition) {
            result += t.data + " ";
        }
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Production that = (Production) o;

        if (!nonTerminal.data.equals(that.nonTerminal.data))
            return false;

        if(definition.size() != that.definition.size())
            return false;

        for (int i = 0; i < definition.size(); i++) {
            if(!definition.get(i).data.equals(that.definition.get(i).data))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nonTerminal, definition);
    }



}
