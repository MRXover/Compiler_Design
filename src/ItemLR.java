import java.util.ArrayList;

public class ItemLR {
    // nonTerminal : definition , Terminal
    Token nonTerminal;
    ArrayList<Token> definition;
    Token terminal;

    ItemLR(Production pro){
        nonTerminal = new Token(pro.nonTerminal.data, pro.nonTerminal.type);
        definition = new ArrayList<>();
        definition.addAll(pro.definition);
    }

    ItemLR(int indexOfDot, Production pro){
        nonTerminal = new Token(pro.nonTerminal.data, pro.nonTerminal.type);
        definition = new ArrayList<>();
        definition.addAll(pro.definition);
        definition.add(indexOfDot, new Token("•", "DOT"));
    }

    void setTerminal(Token t){
        terminal = new Token(t.data, t.type);
    }

    Token getTerminal(){
        return terminal;
    }

    Token get(int index){
        return definition.get(index);
    }

    @Override
    public String toString() {
        return nonTerminal.data + " -> " + definition + ", " + terminal.data;
    }
}
