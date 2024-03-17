import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Main {
    public static Scanner scanner;
    static String inPath = "in.txt";
    static String outPath = "out.txt";
    public static void main(String[] args) {
        try {
            File input = new File(inPath);
            scanner = new Scanner(input);
            FileWriter writer = new FileWriter(outPath);
            MainThread mainThread = new MainThread(scanner, writer);
            Thread t = new Thread(mainThread);
            t.start();
            t.join();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}