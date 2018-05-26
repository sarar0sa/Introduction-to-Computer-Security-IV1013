import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.*;
import java.util.Arrays;

public class Hiddec {
    private Cipher cipher;
    private byte[] key = null, ctr = null, input = null;
    private String output = null;

    /**
     *
     * Extracts arguments entered by user.
     *
     * @param args
     * @throws IOException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     */
    public void getArgs(String[] args) throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        for (String arg: args) {
            String[] argument = arg.split("=");
            switch (argument[0]) {
                case "--key":
                    key = hexToByte(argument[1]);
                    break;

                case "--ctr":
                    ctr = hexToByte(argument[1]);
                    break;

                case "--input":
                    input = readInputFile(argument[1]);
                    break;

                case "--output":
                    output = argument[1];
                    break;

                default:
                    System.out.println("You have entered a invalid argument");
                    return;
            }
        }

        if (key == null|| input == null || output == null) {
            System.out.println("You need to at least specify the following arguments: --key --input --output");
            return;
        }

        begin();
    }

    /**
     *
     * Begins the decryption, either with ECB- or CTR-mode.
     *
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     */

    private void begin() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IOException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        if(ctr != null) {
            byte[] encryptedKey = hash(key);
            ctr(key, encryptedKey, ctr, input, output);
        }
        else if (ctr == null){
            byte[] encryptedKey = hash(key);
            ecb(key, encryptedKey, input, output);
        }
    }

    /**
     *
     * Converts a hexadecimal string to a byte array.
     *
     * @param key
     * @return
     */

    public byte[] hexToByte(String key){
        return DatatypeConverter.parseHexBinary(key);

    }

    /***
     *
     * Calculates the MD5 hash of a given key.
     *
     * @param key
     * @return
     */

    public byte[] hash(byte[] key) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("The hash algorithm is not available");
            System.exit(0);
        }
        md.update(key);
        byte[] digest = md.digest();

        return digest;
    }

    /**
     *
     * Reads the input file and store it in a byte array.
     *
     * @param input
     * @return
     * @throws IOException
     */

    public byte[] readInputFile(String input) throws IOException {
        File file = new File(input);
        byte[] byteArray = new byte[(int)file.length()];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.out.println("The input file could not be found");
            System.exit(0);
        }
        try {
            fis.read(byteArray);
        } catch (IOException e) {
            System.out.println("The input file could not be opened");
        }
        fis.close();

        return byteArray;

    }

    /**
     *
     * Decryption which uses CTR-mode. The hash value for the key are found in the input file
     * to mark the start and end of the data. The data is then extracted. To verify the
     * extracted data the hash value of the data are calculated and checked against the last
     * block of the blob.
     *
     *
     * @param key
     * @param encryptedKey
     * @param ctr
     * @param input
     * @param output
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */

    public void ctr(byte[] key, byte[] encryptedKey, byte[] ctr, byte[] input, String output) throws NoSuchPaddingException, InvalidAlgorithmParameterException, IOException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        int start = 0;
        int startIndex = findKey(key, input, encryptedKey, start, ctr);

        if(startIndex == -1){
            System.out.println("The file does not contain any hidden blob");
            return;
        }

        int endIndex = findEnd(input, encryptedKey, startIndex + 16);

        if(endIndex == -1){
            System.out.println("The file does not contain any hidden blob");
            return;
        }

        // Extract the data
        byte[] data = extractData(input, startIndex, endIndex, ctr, key);

        // Extract data to verify
        decrypt(Arrays.copyOfRange(input, endIndex, endIndex + 16));
        byte[] verifyData = cipher.doFinal(Arrays.copyOfRange(input, endIndex + 16, endIndex + 32));
        byte[] hashOfData = hash(data);

        if(Arrays.equals(verifyData,hashOfData)){
            writeToFile(data, output);
        }
        else {
            System.out.println("The data could not be verified as the correct data");
        }

    }
    /**
     *
     * Decryption which uses ECB-mode. The hash value for the key are found in the input file
     * to mark the start and end of the data. The data is then extracted. To verify the
     * extracted data the hash value of the data are calculated and checked against the last
     * block of the blob.
     *
     */


    public void ecb(byte[] key, byte[] encryptedKey, byte[] input, String output) throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        int start = 0;
        init(null, key);
        int startIndex = findEnd(input, encryptedKey, start);

        if(startIndex == -1){
            System.out.println("The file does not contain any hidden blob");
            return;
        }

        int endIndex = findEnd(input, encryptedKey, startIndex + 16);

        if(endIndex == -1){
            System.out.println("The file does not contain any hidden blob");
            return;
        }

        // Extract the data
        byte[] data = decrypt(Arrays.copyOfRange(input,startIndex + 16, endIndex));

        // Extract data to verify
        byte[] verifyData = decrypt(Arrays.copyOfRange(input,endIndex + 16, endIndex + 32));
        byte[] hashOfData = hash(data);

        if(Arrays.equals(verifyData,hashOfData)){
            writeToFile(data, output);
        }
        else {
            System.out.println("The data could not be verified as the correct data");
            return;
        }
    }

    /**
     *
     * Finds the first H(k) in the input file. Used only in CTR-mode.
     *
     *
     * @param key
     * @param input
     * @param encryptedKey
     * @param startSearch
     * @param ctr
     * @return
     * @throws NoSuchPaddingException
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     */

    public int findKey(byte[] key, byte[] input, byte[] encryptedKey, int startSearch, byte[] ctr) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        for(int i = startSearch; i <= input.length; i += 16){
            init(ctr, key);
            byte[] decrypted = decrypt(Arrays.copyOfRange(input, i, i + 16));
            if(Arrays.equals(decrypted,encryptedKey)){
                return i;
            }
        }
        return -1;
    }

    /**
     *
     * Used in CTR-mode to find the last H(k).
     * Used in ECB-mode to both find the first and last H(k)
     *
     *
     * @param input
     * @param encryptedKey
     * @param startSearch
     * @return
     * @throws NoSuchPaddingException
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     */


    public int findEnd(byte[] input, byte[] encryptedKey, int startSearch) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        for(int i = startSearch; i <= input.length; i += 16){
            byte[] decrypted = decrypt(Arrays.copyOfRange(input,i, i + 16));
            if(Arrays.equals(decrypted,encryptedKey)){
                return i;
            }
        }
        return -1;
    }

    /**
     *
     * Extracts the data between the two H(k). Used in CTR-mode only.
     *
     * @param input
     * @param start
     * @param end
     * @param ctr
     * @param key
     * @return
     * @throws NoSuchPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeyException
     */
    public byte[] extractData(byte[] input, int start, int end, byte[] ctr, byte[] key) throws NoSuchPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        init(ctr, key);
        decrypt(Arrays.copyOfRange(input,start,start + 16));
        return decrypt(Arrays.copyOfRange(input, start + 16, end));
    }

    /**
     *
     * Writes the results to a given output file specified by the user.
     *
     *
     * @param data
     * @param output
     * @throws IOException
     */

    public void writeToFile(byte[] data, String output) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(output);
        } catch (FileNotFoundException e) {
            System.out.println("The output file could not be found");
            return;
        }
        try {
            fos.write(data);
        } catch (IOException e) {
            System.out.println("The output file could not be opened");
        }
        fos.close();
    }

    /**
     *
     * Decrypts the file.
     *
     * @param input
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     */

    public byte[] decrypt(byte[] input) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        return cipher.update(input);
    }

    /**
     *
     *
     * Initializes the Cipher object, either in CTR- or ECB-mode.
     *
     * @param ctr
     * @param key
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */

    public void init(byte[] ctr, byte[] key) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        if(ctr == null){
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        }

        else {
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            IvParameterSpec iv = new IvParameterSpec(ctr);
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        }
    }

    public static void main(String args[]) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Hiddec hiddec = new Hiddec();
        if(args.length < 3){
            System.out.println("You have entered too few arguments");
            return;
        }
        else {
            hiddec.getArgs(args);
        }
    }
}

