import org.jscience.mathematics.number.LargeInteger;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.Matrix;

import java.io.*;
import java.util.Random;


public class HillKeys {
        private Random rand;
        private Matrix<Real> key;
        private BufferedWriter writer;

    /**
     *
     * Generates a random key matrix based on the chosen radix and blocksize
     *
     * @param radix
     * @param blocksize
     * @return
     *
     */

    private Matrix<Real> generateKey(int radix, int blocksize) {
            Real[][] matrixArray = new Real[blocksize][blocksize];
            rand = new Random();

            while (true) {
                for (int i = 0; i < blocksize; i++)
                    for (int j = 0; j < blocksize; j++)
                        matrixArray[i][j] = Real.valueOf(rand.nextInt(radix));

                    key = DenseMatrix.valueOf(matrixArray);
                    LargeInteger determinant = LargeInteger.valueOf(key.determinant().longValue());

                    if ((key.determinant() != Real.valueOf(0)) && (determinant.gcd(LargeInteger.valueOf(radix)).equals(LargeInteger.valueOf(1)))
                            && key.getNumberOfRows() == key.getNumberOfColumns()) {
                        break;
                    }
                }
                return key;
        }


    /**
     *
     * Writes the generated key to file
     *
     * @param keyMatrix
     * @param keyFile
     * @throws IOException
     */

    public void writeToFile(Matrix keyMatrix, String keyFile) throws IOException {
            File file = new File(keyFile);
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(keyMatrix.toString().replaceAll("[{,}]",""));
            writer.close();

        }

    public static void main(String[] args)  {
        if(Integer.parseInt(args[0]) > 256 && Integer.parseInt(args[1]) > 8){
            System.out.println("The program only supports a maximum radix of 256 and a maximum blocksize of 8, please try again");
            return;
        }

        HillKeys hillKeys = new HillKeys();
        Matrix key = hillKeys.generateKey(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

        try {
            hillKeys.writeToFile(key, args[2]);
        } catch (IOException e) {
            System.out.println("The key couldn't be written to file, please try again");
            return;
        }

    }

}
