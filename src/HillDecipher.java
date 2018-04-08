import org.jscience.mathematics.number.LargeInteger;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.DenseVector;


import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;


public class HillDecipher {

    private ArrayList<DenseVector<Real>> cipherArray;
    private ArrayList<Real> keyList;
    private String cipher;
    private BufferedWriter writer;
    private DenseMatrix<Real> keyMatrix, invMatrix, plainText;


    public DenseMatrix<Real> readCipher(String cipherFile, int blockSize) throws IOException {
        File file = new File(cipherFile);
        Scanner sc = new Scanner(file);
        cipher = sc.next();
        cipherArray = new ArrayList();

        for (int i = 0; i < cipher.length(); i += blockSize) {
            keyList = new ArrayList<>();
            for(int j = 0; j < blockSize; j++) {
                    int letterToNum = ((int) cipher.charAt(i+j));
                    keyList.add(Real.valueOf(letterToNum - 65));
                }
                cipherArray.add(DenseVector.valueOf(keyList));
        }

        return DenseMatrix.valueOf(cipherArray).transpose();
    }

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


    public void decode(DenseMatrix<Real> cipher, DenseMatrix<Real> keyMatrix, String plainFile, int radix) throws IOException {
        writer = new BufferedWriter(new FileWriter(plainFile));
        Real[][] arr = new Real[keyMatrix.getNumberOfRows()][keyMatrix.getNumberOfColumns()];

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
                writer.write(((plainText.get(i,j).intValue() % radix) + 65));
            }
        }
        writer.close();
    }

    public static void main(String[] args) throws IOException {
        HillDecipher hillDecipher = new HillDecipher();
        DenseMatrix cipher = hillDecipher.readCipher(args[4],Integer.parseInt(args[1]));
        DenseMatrix<Real> key = hillDecipher.readKey(args[2], Integer.parseInt(args[1]));
        hillDecipher.decode(cipher, key, args[3], Integer.parseInt(args[0]));
    }
}
