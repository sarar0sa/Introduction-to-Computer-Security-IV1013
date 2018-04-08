import org.jscience.mathematics.number.LargeInteger;
import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.DenseMatrix;
import org.jscience.mathematics.vector.Matrix;

import java.io.*;
import java.util.Random;


public class HillKeys {

        private  Random rand = new Random();
        private  Matrix<Real> key;
        private BufferedWriter writer;

        private Matrix<Real> generateKey(int radix, int blocksize) {
           Real[][] matrixArray = new Real[blocksize][blocksize];

           while(true) {
               for (int i = 0; i < blocksize; i++)
                   for (int j = 0; j < blocksize; j++)
                       matrixArray[i][j] = Real.valueOf(rand.nextInt(radix));

               key = DenseMatrix.valueOf(matrixArray);
               LargeInteger determinant = LargeInteger.valueOf(key.determinant().longValue());

               if(key.determinant() != Real.valueOf(0) && determinant.gcd(LargeInteger.valueOf(radix)) == LargeInteger.valueOf(1)); {
                   break;
               }
           }

           return key;
        }

        public void writeToFile(Matrix keyMatrix, String keyFile){
            File file = new File(keyFile);
            try {
                writer = new BufferedWriter(new FileWriter(file));
                writer.write(keyMatrix.toString().replaceAll("[{,}]",""));
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    public static void main(String[] args) throws IOException {
      HillKeys hillKeys = new HillKeys();
      Matrix key = hillKeys.generateKey(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
      hillKeys.writeToFile(key, args[2]);



      /*Matrix<Real> matrix = generateKey(26,3);
      writeToFile(matrix);*/

    }

    }
