import java.util.Objects;

public class Token {
    String data;
    String type;

    Token(String str, String Type){
        data = str.replace(" ", "");
        type = Type;
    }

    Token(String str){
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

    boolean isEpsilon(){
        return type.equals("EPSILON");
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
