import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class PasswordCrack {
     private ArrayList<String> wordList;
     private HashMap<String,String> userPass;
     private BufferedWriter bf;


    /**
     *
     * Reads the password file and stores the password and salt in a Hashmap.
     * Also extracts the names of the users and stores it in an Arraylist.
     *
     * @param file
     * @throws IOException
     */

    public void readPasswordFile(String file) throws IOException {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        bf = new BufferedWriter(new FileWriter("passwd2-plain.txt"));
        String line;
        userPass = new HashMap();
        wordList = new ArrayList();
        while ((line = br.readLine()) != null) {
            String[] parts = line.split(":");
            userPass.put(parts[1], parts[1].substring(0,2));
            String[] name = parts[4].split(" ");

            for (String names: name) {
                wordList.add(names);
            }
        }
    }

    /**
     *
     * Reads the dictionary file and adds it to the ArrayList.
     * Also add common words to the ArrayList.
     *
     * @param file
     * @throws IOException
     */

    public void readDict(String file) throws IOException {
        FileReader fr = new FileReader(file);
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

        crackPassword(wordList);

    }

    /**
     *
     * First checks the Wordlist without any mangles,
     * If, after that, it still exists users with uncrackade password mangle is
     * called recursive until all password has been cracked.
     *
     * @param words
     * @throws IOException
     */

    public void crackPassword(ArrayList<String> words) throws IOException {
        for (String word : words) {
            checkPass(word);
        }
        if(userPass.isEmpty() == false){
            crackPassword(mangle(words));
        }

        else {
            bf.close();
            return;
        }

    }

    /**
     *
     * Method that mangle the words.
     *
     * @param mangled
     * @return
     * @throws IOException
     */

    public ArrayList<String> mangle(ArrayList<String> mangled) throws IOException {
         ArrayList<String> mangleList = new ArrayList<>();
        for (String word: mangled) {
            mangleList.add(checkPass(toLower(word)));
            mangleList.add(checkPass(toUpper(word)));
            mangleList.add(checkPass(capitalize(word)));
            mangleList.add(checkPass(ncapitalize(word)));

            // If the word length is bigger than eight, a deleted letter won't change the hash
            if (word.length() <= 8) {
                mangleList.add(checkPass(deleteLast(word)));
                mangleList.add(checkPass(deleteFirst(word)));
            }

            mangleList.add(checkPass(reverse(word)));
            mangleList.add(checkPass(mirror1(word)));
            mangleList.add(checkPass(mirror2(word)));
            mangleList.add(checkPass(toggle(word)));
            mangleList.add(checkPass(toggle2(word)));

            // If the word is bigger than eight, a duplicate word or a added letter won't change the hash.
            if (word.length() < 8) {
                mangleList.add(checkPass(duplicate(word)));
                // Add number to the word
                for (int i = 48; i < 58; i++) {
                    mangleList.add(checkPass((char) i + word));
                    mangleList.add(checkPass(word + (char) i));
                }
            }

        }
        return mangleList;
    }

    /**
     *
     * The different methods used for mangle
     *
     * @param word
     * @return
     */

    public String toUpper(String word){
        return word.toUpperCase();

    }

    public String toLower(String word){
        return word.toLowerCase();

    }

    public String deleteLast(String word){
        return word.substring(0,word.length() - 1);
    }

    public String deleteFirst(String word){
        return word.substring(1);
    }

    public String reverse(String word){
        return new StringBuilder(word).reverse().toString();
    }

    public String duplicate(String word){
        return word + word;
    }

    public String mirror1(String word){
        return reverse(word) + word;
    }

    public String mirror2(String word){
        return word + reverse(word);
    }

    public String capitalize(String word){
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public String ncapitalize(String word){
        return word.substring(0, 1).toLowerCase() + word.substring(1).toUpperCase();
    }

    public String toggle(String word){
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

    public String toggle2(String word){
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


     /**
     *
     *  Checks if the word exists in the password file by first encrypt it with jcrypt.
     * Removes the users with password that have been cracked.
     * @param word
     * @return
     * @throws IOException
     */

    public String checkPass(String word) throws IOException {
        ArrayList<String> toRemove = new ArrayList();
        for (String password : userPass.keySet()) {
            String hash = jcrypt.crypt(userPass.get(password), word);
            if (userPass.containsKey(hash)) {
                bf.write(word);
                bf.newLine();
                System.out.println(word);
                toRemove.add(hash);
            }
        }
        for (String password : toRemove) {
            userPass.remove(password);
        }
        bf.flush();
        return word;
    }

    public static void main(String[] args) {
        PasswordCrack passwordCrack = new PasswordCrack();
        try {
            passwordCrack.readPasswordFile(args[1]);
        } catch (IOException e) {
            System.out.println("The password file couldn't be open, please try again");
        }
        try {
            passwordCrack.readDict(args[0]);
        } catch (IOException e) {
            System.out.println("The dictionary couldn't be open, please try again");
        }

    }
}
