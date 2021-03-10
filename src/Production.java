import java.util.ArrayList;
import java.util.Objects;

class Production {
    Token nonTerminal;
    ArrayList<Token> definition;


    Production(Token left){
        nonTerminal = left;
        definition = new ArrayList<>();
    }

    Production(Token left, boolean addEps){
        nonTerminal = left;
        definition = new ArrayList<>();
        definition.add(new Token("#", "EPSILON"));
    }

    Production(Production pro){
        nonTerminal = new Token(pro.nonTerminal.data, pro.nonTerminal.type);
        definition = new ArrayList<>(pro.definition);
    }

    Production add(Token token){
        definition.add(token);
        return this;
    }

    int size(){
        return definition.size();
    }

    Token get(int index){
        return definition.get(index);
    }


    int getTokenIndex(Token token){
        for (int i = 0; i < definition.size(); i++) {
            if(definition.get(i).data.equals(token.data))
                return i;
        }
        return -1;
    }

    boolean hasEpsilonProduction(){
        if(definition.size() != 1)
            return false;
        return definition.get(0).data.equals("#");
    }

    int getIndexOfDot(){
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
