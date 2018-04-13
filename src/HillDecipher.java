import org.jscience.mathematics.number.LargeInteger;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.DenseVector;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

/**
 *
 * HillDecipher creates an inverted matrix from the key matrix and then decrypts the ciphertext.
 * The program only supports a maximum blocksize of 4, if higher some of the letters becomes decoded
 * wrong, and I really don't know why. All radices are supported; 26, 128, 256.
 *
 */

public class HillDecipher {

    private ArrayList<DenseVector<Real>> cipherArray;
    private ArrayList arr;
    private ArrayList<Real> keyList;
    private String cipher;
    private BufferedWriter writer;
    private DenseMatrix<Real> keyMatrix, invMatrix, plainText;

    /**
     *
     * Reads the number representation of the ciphertext and turns it into a matrix.
     *
     * @param cipherFile
     * @param blockSize
     * @return
     * @throws IOException
     */


    public DenseMatrix<Real> readCipher(String cipherFile, int blockSize) throws IOException {
        File file = new File(cipherFile);
        Scanner sc = new Scanner(file);
        cipherArray = new ArrayList();
        arr = new ArrayList();

        while(sc.hasNext()){
            cipher = String.valueOf(sc.nextInt());
            arr.add(cipher);
        }


        for (int i = 0; i < arr.size(); i += blockSize) {
            keyList = new ArrayList<>();
            for(int j = 0; j < blockSize; j++) {
                int letter = Integer.parseInt(String.valueOf(arr.get(i+j)));
                keyList.add(Real.valueOf(letter));
            }
            cipherArray.add(DenseVector.valueOf(keyList));
        }

        return DenseMatrix.valueOf(cipherArray).transpose();

    }

    /**
     *
     * Reads the key from file and stores it into a matrix.
     *
     * @param keyFile
     * @param blockSize
     * @return
     * @throws IOException
     */

    public DenseMatrix<Real> readKey(String keyFile, int blockSize) throws IOException {
        File file = new File(keyFile);
        Scanner sc = new Scanner(file);
        Real[][] matrixArray = new Real[blockSize][blockSize];
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                matrixArray[i][j] = Real.valueOf(sc.nextInt());
            }
        }
        keyMatrix = DenseMatrix.valueOf(matrixArray);
        return keyMatrix;
    }

    /**
     *
     * Decodes the cipher by creating the inverse to the key matrix
     * and multiply it to the cipher.
     *
     * @param cipher
     * @param keyMatrix
     * @param plainFile
     * @param radix
     * @throws IOException
     */


    public void decode(DenseMatrix<Real> cipher, DenseMatrix<Real> keyMatrix, String plainFile, int radix) throws IOException {
        Real[][] arr = new Real[keyMatrix.getNumberOfRows()][keyMatrix.getNumberOfColumns()];
        LinkedList temp = new LinkedList();

        LargeInteger determinant = LargeInteger.valueOf(keyMatrix.determinant().longValue());
        Real invDet = Real.valueOf(determinant.modInverse(LargeInteger.valueOf(radix)).longValue());
        invMatrix = keyMatrix.inverse().times(Real.valueOf(determinant.longValue()).times(invDet));

        for (int i = 0; i < invMatrix.getNumberOfRows(); i++){
            for(int j = 0; j < invMatrix.getNumberOfColumns(); j++){
                LargeInteger modNum = LargeInteger.valueOf(invMatrix.get(i,j).longValue()).mod(LargeInteger.valueOf(radix));
                arr[i][j] = Real.valueOf(modNum.longValue());
            }
        }

        DenseMatrix<Real> dKey = DenseMatrix.valueOf(arr);
        plainText = dKey.times(cipher).transpose();

        for(int i = 0; i < plainText.getNumberOfRows(); i++){
            for(int j = 0; j < plainText.getNumberOfColumns(); j++){
                temp.add(String.valueOf((plainText.get(i,j).intValue()) % radix));
            }
        }
        removePad(temp,plainFile);
    }

    /**
     * Removes the padding from the decrypted message and writes the plaintext to
     * a file as an number representation of the letters.
     *
     * To be able to see the plaintext hilldecode needs to be run.
     *
     * @throws IOException
     */

   public void removePad(LinkedList plain, String plainFile) throws IOException {
     writer = new BufferedWriter(new FileWriter(plainFile));
     int last = Integer.parseInt(String.valueOf(plain.getLast()));

     for(int i = 0; i < plain.size() - last; i++) {
         writer.write(String.valueOf(plain.get(i)));
         writer.write(" ");
     }
     writer.close();
    }

    public static void main(String[] args) {
        HillDecipher hillDecipher = new HillDecipher();
        DenseMatrix cipher;

        if(Integer.parseInt(args[0]) > 256 || Integer.parseInt(args[1]) > 4){
           System.out.println("The program only supports a maximum radix of 256 and a maximum blocksize of 8, please try again");
           return;
        }

        try {
            cipher = hillDecipher.readCipher(args[4],Integer.parseInt(args[1]));
        } catch (IOException e) {
            System.out.println("The cipher file couldn't be open, please try again");
            return;
        }
        DenseMatrix<Real> key;
        try {
            key = hillDecipher.readKey(args[2], Integer.parseInt(args[1]));
        } catch (IOException e) {
            System.out.println("The key file couldn't be open, please try again");
            return;
        }
        try {
            hillDecipher.decode(cipher, key, args[3], Integer.parseInt(args[0]));
        } catch (IOException e) {
            System.out.println("The plaintext couldn't be written to file, please try again");
            return;
        }
    }
}