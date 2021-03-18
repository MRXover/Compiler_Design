import java.util.ArrayList;

public class ItemLR {
    // nonTerminal : definition , Terminal
    Token nonTerminal;
    ArrayList<Token> definition;
    Token terminal;

    ItemLR(ItemLR item){
        nonTerminal = new Token(item.nonTerminal.data, item.nonTerminal.type);
        definition = new ArrayList<>();
        definition.addAll(item.definition);
        terminal = new Token(item.terminal.data, item.terminal.type);
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

    int size(){
        return definition.size();
    }

    int getIndexOfDot(){
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
