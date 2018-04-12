import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class HillCipher  {
    private ArrayList msgArray;
    private BufferedWriter writer;
    private String msg;
    private Scanner sc;

    /**
     * Reads the plaintext from file, the plaintext has first been encoded by hillencode, and then stores
     * it in an Arraylist.
     *
     * @param fileName
     * @throws FileNotFoundException
     */

    public void readMsg(String fileName, int blocksize) throws FileNotFoundException {
        File file = new File(fileName);
        sc = new Scanner(file);
        msgArray = new ArrayList();

        while(sc.hasNext()){
            msg = sc.next();
            msgArray.add(msg);
        }
        addPadding(msgArray, blocksize);
    }

    /**
     *
     * Adds padding to the message
     *
     * @param msgArray
     * @param blocksize
     */

    public void addPadding(ArrayList msgArray, int blocksize){
        int newEnd = blocksize - (msgArray.size() % blocksize);
        int sizeArr = msgArray.size() + (blocksize - (msgArray.size() % blocksize));
        for(int i = msgArray.size(); i < sizeArr; i++){
            msgArray.add(i, newEnd);
        }
    }


    /**
     * Takes the number representation of the plaintext and decode/encode by multiplying
     * it with a the key/inverse matrix which are read from file and then stored in an array.
     *
     * The result from the multiplying is then written to file.
     *
     * @param blockSize
     * @param keyFile
     * @param cipherFile
     * @throws IOException
     */

    public void encode(int blockSize, String keyFile, String cipherFile, int radix) throws IOException {
        File file = new File(keyFile);
        try {
            writer = new BufferedWriter(new FileWriter(cipherFile));
        } catch (IOException e) {
            System.out.println("The plaintext couldn't be written to file, please try again");
        }
        int[][] keyMatrix = new int[blockSize][blockSize];

        try (Scanner sc = new Scanner(file)) {
            for(int i = 0; i < blockSize; i++ ){
                for(int j = 0; j < blockSize; j++){
                    keyMatrix[i][j] = sc.nextInt();
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("The file for the key couldn't be open, please try again");
            return;
        }

        for(int i = 0; i < msgArray.size(); i += keyMatrix.length){
            for(int j = 0; j < keyMatrix.length; j++){
                int encoded = 0;
                for(int k = 0; k < keyMatrix.length; k++){
                    encoded += (Integer.parseInt(String.valueOf(msgArray.get(k+i))) * keyMatrix[j][k]);
                }
                try {
                    writer.write(String.valueOf(encoded % radix));
                    writer.write(" ");
                } catch (IOException e) {
                    System.out.println("The plaintext couldn't be written to file, please try again");
                }
            }
        }
        writer.close();
    }

    public static void main(String[] args)  {
        HillCipher hillCipher = new HillCipher();
        if(Integer.parseInt(args[0]) > 256 && Integer.parseInt(args[1]) > 8){
            System.out.println("The program only supports a maximum radix of 256 and a maximum blocksize of 8, please try again");
            return;
        }

            try {
                hillCipher.readMsg(args[3], Integer.parseInt(args[1]));
            } catch (FileNotFoundException e) {
                System.out.println("The file for the plaintext couldn't be open, please try again");
                return;
            }
            try {
                hillCipher.encode(Integer.parseInt(args[1]), args[2], args[4], Integer.parseInt(args[0]));
            } catch (IOException e) {
                return;
            }
        }
    }
