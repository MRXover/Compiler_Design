package util;

import java.util.Objects;

public class Token {
    public String data;
    public String type;

    public Token(String str, String Type){
        data = str.replace(" ", "");
        type = Type;
    }

    public Token(String str){
        data = str.replace(" ", "");
        switch (str) {
            case  (":"):
                type = "ASSIGNMENT";
                break;
            case ("|"):
                type = "PIPE";
                break;
            case ("#"):
                type = "EPSILON";
                break;
            case ("$"):
                type = "END_MARKER";
                break;

            default:
                type = "ERROR";
                break;
        }
    }

    @Override
    public String toString(){
        return "(" + data + "," + type + ")";
    }

    @Override
    public boolean equals(Object o){
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Token token = (Token) o;
        return data.equals(token.data) && type.equals(token.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, type);
    }
}
