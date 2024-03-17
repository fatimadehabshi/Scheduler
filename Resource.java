public class Resource {
    String name;
    int total, remainder;

    public Resource(String name, int total, int remainder){
        this.name = name;
        this.total = total;
        this.remainder = remainder;
    }
}