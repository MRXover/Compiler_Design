import java.util.ArrayList;

class Production {
    Token nonTerminal;
    ArrayList<Token> definitions;


    Production(Token left){
        nonTerminal = left;
        definitions = new ArrayList<>();
    }

    Production(Token left, boolean addEps){
        nonTerminal = left;
        definitions = new ArrayList<>();
        definitions.add(new Token("#", "EPSILON"));
    }


    Production add(Token token){
        definitions.add(token);
        return this;
    }

    int size(){
        return definitions.size();
    }

    Token get(int index){
        return definitions.get(index);
    }

    boolean hasToken(Token wanted){
        return definitions.contains(wanted);
    }

    int getTokenIndex(Token token){
        for (int i = 0; i < definitions.size(); i++) {
            if(definitions.get(i).data.equals(token.data))
                return i;
        }
        return -1;
    }

    boolean hasEpsilonProduction(){
        if(definitions.size() != 1)
            return false;
        return definitions.get(0).data.equals("#");
    }

    String getProd(){
        String result = "";
        for (Token t : definitions) {
            result += t.data + " ";
        }
        return result;
    }

    
    @Override
    public String toString(){
        String result = nonTerminal.data + " : ";
        for (Token t : definitions) {
            result += t.data + " ";
        }
        return result;
    }

}
