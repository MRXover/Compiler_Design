import java.util.ArrayList;

public class ItemLR {
    // nonTerminal : definition , firstToken / secondToken
    Token nonTerminal;
    ArrayList<Token> definition;
    Token firstToken;
    Token secondToken;

    ItemLR(Production pro){
        nonTerminal = new Token(pro.nonTerminal.data, pro.nonTerminal.type);
        definition = new ArrayList<>();
        definition.addAll(pro.definition);
    }

}
