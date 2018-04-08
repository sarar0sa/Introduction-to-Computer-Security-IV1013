import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class HillCipher  {
    private ArrayList msgArray;
    private BufferedWriter writer;

    private String msg;


    public void readMsg(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        Scanner sc = new Scanner(file);

        msg = sc.next();

        msgArray = new ArrayList();
        for (int i = 0; i < msg.length(); i++){
            int letterToNum = (int)msg.charAt(i) - 65;
            msgArray.add(i, letterToNum);
        }
    }

    public void encode(int blockSize, String keyFile, String cipherFile) throws IOException {
        File file = new File(keyFile);
        writer = new BufferedWriter(new FileWriter(cipherFile));
        int[][] keyMatrix = new int[blockSize][blockSize];

        try (Scanner sc = new Scanner(file)) {
           for(int i = 0; i < blockSize; i++ ){
               for(int j = 0; j < blockSize; j++){
                   keyMatrix[i][j] = sc.nextInt();
               }
           }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < msgArray.size(); i += keyMatrix.length){
            for(int j = 0; j < keyMatrix.length; j++){
                int encoded = 0;
                for(int k = 0; k < keyMatrix.length; k++){
                    encoded += ((int)msgArray.get(k+i) * keyMatrix[j][k]);
                }
                writer.write((encoded % 26) + 65);
            }

       }
        writer.close();
    }

    public static void main(String[] args)  {

        HillCipher hillCipher = new HillCipher();
        if(Integer.parseInt(args[0]) != 26 && Integer.parseInt(args[1]) != 3){
            throw new IllegalArgumentException("The program only supports a radix of 26 and a blocksize of 3, please try again");
        }

        else {

            try {
                hillCipher.readMsg(args[3]);
            } catch (FileNotFoundException e) {
                System.out.println("The file for the plaintext couldn't be open, please try again");
            }
            try {
                hillCipher.encode(Integer.parseInt(args[1]), args[2], args[4]);
            } catch (IOException e) {
                System.out.println("The filename for the keyfile doesn't exist or you wrote the filename for the cipherfile wrong, please try again");
            }

        }

        /*try {
            readMsg();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        encode();*/


    }
}



