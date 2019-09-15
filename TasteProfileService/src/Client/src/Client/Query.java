package Client;

import java.util.List;

public class Query {

    public String name;

    public List<String> args;

    @Override
    public String toString() {
        return "Query{" +
                "name='" + name + '\'' +
                ", args=" + args +
                '}';
    }

    public Query(String name, List<String> args) {
        this.name = name;
        this.args = args;
    }
}
