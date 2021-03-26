package util;

import java.util.ArrayList;

import static util.SupportFunctions.createDOT;

public class ItemLR {
    // nonTerminal : definition , Terminal
    public Token nonTerminal;
    public ArrayList<Token> definition;
    public Token terminal;

    public ItemLR(ItemLR item){
        nonTerminal = new Token(item.nonTerminal.data, item.nonTerminal.type);
        definition = new ArrayList<>();
        definition.addAll(item.definition);
        terminal = new Token(item.terminal.data, item.terminal.type);
    }

    public ItemLR(int indexOfDot, Production pro){
        nonTerminal = new Token(pro.nonTerminal.data, pro.nonTerminal.type);
        definition = new ArrayList<>();
        definition.addAll(pro.definition);
        definition.add(indexOfDot, createDOT());
    }

    public ItemLR(int indexOfDot, Production pro, Token terminal){
        nonTerminal = new Token(pro.nonTerminal.data, pro.nonTerminal.type);
        definition = new ArrayList<>();
        definition.addAll(pro.definition);
        definition.add(indexOfDot, createDOT());
        this.terminal = terminal;
    }

    public void setTerminal(Token t){
        terminal = new Token(t.data, t.type);
    }


    public Token get(int index){
        return definition.get(index);
    }

    public int size(){
        return definition.size();
    }

    public int getIndexOfDot(){
        for (int i = 0; i < definition.size(); i++) {
            if(definition.get(i).type.equals("DOT"))
                return i;
        }
        return -1;
    }

    @Override
    public String toString() {
        return nonTerminal.data + " -> " + definition + ", " + terminal.data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemLR that = (ItemLR) o;

        if (!nonTerminal.data.equals(that.nonTerminal.data))
            return false;

        if (!terminal.data.equals(that.terminal.data))
            return false;

        if(definition.size() != that.definition.size())
            return false;

        for (int i = 0; i < definition.size(); i++) {
            if(!definition.get(i).data.equals(that.definition.get(i).data))
                return false;
        }

        return true;
    }

}
