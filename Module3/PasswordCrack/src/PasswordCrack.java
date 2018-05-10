import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PasswordCrack {
    static ArrayList<String> wordList, saltList;
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
            //System.out.println(parts[4].split(" "));

            /*for(int i = 0; i < parts.length; i++){
                System.out.println(parts[i]);
            }*/
        }
        //System.out.println(userPass.keySet());
        //System.out.println(userPass.values());
        //System.out.println(jcrypt.crypt("Li", "Liberterian"));
        //System.out.print(jcrypt.crypt("Si", "Liberterian"));

        /*for(int i = 0; i < saltList.size(); i++){
            System.out.println(saltList.get(i));
        }*/
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

         /*for(int i = 0; i < wordList.size(); i++){
                System.out.println(wordList.get(i));
            }*/

    }

    public static void hashPass(){
        for (String word: wordList)
        {
                isPass(word);
        }
        mangle();
    }

    public static void mangle(){
        for (String word: wordList){

            isPass(word.toLowerCase());

            isPass(word.toUpperCase());
            

        }

    }

    public static void isPass(String word){
        for (String salt: saltList) {
            String hash = jcrypt.crypt(salt,word);
            if(checkPass(hash) == true){
                System.out.println("User: " + userPass.get(hash) + " has the password: " + word);
                userPass.remove(hash);
            }
        }

    }


    public static boolean checkPass(String hash){

        return userPass.containsKey(hash);
    }


    public static void main(String[] args) throws IOException {
        readFile();
        readDict();
        hashPass();
    }
}
