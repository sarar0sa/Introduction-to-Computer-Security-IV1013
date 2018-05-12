import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PasswordCrack {
    static ArrayList<String> wordList, saltList, mangleList;
    static HashMap<String,String> userPass;


    public static void readFile() throws IOException {
        FileReader fr = new FileReader("/Users/sararosander/Desktop/KTH/Datasäkerhet/Module3/cracker-challenge/passwd2.txt");
        BufferedReader br = new BufferedReader(fr);
        String line;
        userPass = new HashMap();
        saltList = new ArrayList();
        wordList = new ArrayList();
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(":");
            userPass.put(parts[1],parts[0]);
            saltList.add(parts[1].substring(0,2));
            String[] name = parts[4].split(" ");

            for (String names: name) {
                wordList.add(names);
            }
        }
    }

    public static void readDict() throws IOException {
        FileReader fr = new FileReader("/Users/sararosander/Desktop/KTH/Datasäkerhet/Module3/PasswordCrack/dict.txt");
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
                wordList.add(line);
        }
        // Add common passwords
        wordList.add("1234");
        wordList.add("12345");
        wordList.add("123456");
        wordList.add("1234567");
        wordList.add("12345678");
        wordList.add("123456789");
        wordList.add("1234567890");
        wordList.add("qwerty");
        wordList.add("abc123");
        wordList.add("111111");
        wordList.add("1qaz2wsx");
        wordList.add("letmein");
        wordList.add("qwertyuiop");
        wordList.add("starwars");
        wordList.add("login");
        wordList.add("passw0rd");

        wordList.addAll(mangle());

    }

    public static void hashPass(){
        for (String word: wordList)
        {
            for (String salt: saltList) {
                String hash = jcrypt.crypt(salt,word);
                if(checkPass(hash) == true){
                    System.out.println("User: " + userPass.get(hash) + " has the password: " + word);
                    userPass.remove(hash);
                }

            }
        }

        }



    public static ArrayList mangle(){
         mangleList = new ArrayList();
        for (String word: wordList){

            mangleList.add(toLower(word));

            mangleList.add(toUpper(word));

            mangleList.add(deleteLast(word));

            mangleList.add(deleteFirst(word));

            mangleList.add(reverse(word));

            mangleList.add(duplicate(word));

            mangleList.add(mirror1(word));

            mangleList.add(mirror2(word));

            mangleList.add(capitalize(word));

            mangleList.add(ncapitalize(word));

            mangleList.add(toggle(word));

            mangleList.add(toggle2(word));


           for(int i = 48; i < 58; i++){
                mangleList.add((char)i + word);
                mangleList.add(word + (char)i);
            }

        }

        return mangleList;

    }

    public static String toUpper(String word){
        return word.toUpperCase();

    }

    public static String toLower(String word){
        return word.toLowerCase();

    }

    public static String deleteLast(String word){
        return word.substring(0,word.length() - 1);
    }

    public static String deleteFirst(String word){
        return word.substring(1);
    }

    public static String reverse(String word){
        return new StringBuilder(word).reverse().toString();
    }

    public static String duplicate(String word){

        return word + word;
    }

    public static String mirror1(String word){
        return reverse(word) + word;
    }

    public static String mirror2(String word){
        return word + reverse(word);
    }

    public static String capitalize(String word){
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public static String ncapitalize(String word){
        return word.substring(0, 1).toLowerCase() + word.substring(1).toUpperCase();
    }

    public static String toggle(String word){
        String toggled = "";

        for (int i = 0; i < word.length(); i++) {
            if (i % 2 == 0) {
                toggled += word.substring(i, i + 1).toUpperCase();
            } else {
                toggled += word.substring(i, i + 1);
            }
        }

        return toggled;
    }

    public static String toggle2(String word){
        String toggled = "";
        for (int i = 0; i < word.length(); i++) {
            if (i % 2 != 0) {
                toggled += word.substring(i, i + 1).toUpperCase();
            } else {
                toggled += word.substring(i, i + 1);
            }
        }
        return toggled;
    }

    public static boolean checkPass(String word){

        return userPass.containsKey(word);
    }

    public static void main(String[] args) throws IOException {
        readFile();
        readDict();
        hashPass();
    }
}
