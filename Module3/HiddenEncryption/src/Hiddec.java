import javax.crypto.BadPaddingException;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.*;
import java.util.Arrays;

public class Hiddec {
    Cipher cipher;

    public String getArgs(String args){
        String[] argument = args.split("=");
        return argument[1];
    }

    public byte[] hexToByte(String key){
        return DatatypeConverter.parseHexBinary(key);

    }

    public byte[] hash(byte[] key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(key);
        byte[] digest = md.digest();

        return digest;
    }

    public byte[] readInputFile(String input) throws IOException {
        File file = new File(input);
        byte[] byteArray = new byte[(int)file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(byteArray);
        fis.close();

        return byteArray;

    }

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
        byte[] data = decrypt(Arrays.copyOfRange(input,startIndex+16, endIndex));

        // Extract data to verify
        byte[] verifyData = decrypt(Arrays.copyOfRange(input,endIndex + 16, endIndex + 32));
        byte[] hashOfData = hash(data);

        if(Arrays.equals(verifyData,hashOfData)){
            writeToFile(data, output);
        }
        else{
            System.out.println("The data could not be verified as the correct data");
        }
    }

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


    public int findEnd(byte[] input, byte[] encryptedKey, int startSearch) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        for(int i = startSearch; i <= input.length; i += 16){
            byte[] decrypted = decrypt(Arrays.copyOfRange(input,i, i + 16));
            if(Arrays.equals(decrypted,encryptedKey)){
                return i;
            }
        }
        return -1;
    }

    public byte[] extractData(byte[] input, int start, int end, byte[] ctr, byte[] key) throws NoSuchPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        init(ctr, key);
        decrypt(Arrays.copyOfRange(input,start,start + 16));
        return decrypt(Arrays.copyOfRange(input, start + 16, end));
    }

    public void writeToFile(byte[] data, String output) throws IOException {
        FileOutputStream fos = new FileOutputStream(output);
        fos.write(data);
        fos.close();
    }

    public byte[] decrypt(byte[] input) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        return cipher.update(input);
    }


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
        //CTR
        if(args.length == 4) {
            byte[] key = hiddec.hexToByte(hiddec.getArgs(args[0]));
            byte[] encryptedKey = hiddec.hash(key);
            byte[] ctr = hiddec.hexToByte(hiddec.getArgs(args[1]));
            byte[] input = hiddec.readInputFile(hiddec.getArgs(args[2]));
            String output = hiddec.getArgs(args[3]);

            hiddec.ctr(key, encryptedKey, ctr, input, output);
        }
        //ECB
        else if (args.length == 3){
            byte[] key = hiddec.hexToByte(hiddec.getArgs(args[0]));
            byte[] encryptedKey = hiddec.hash(key);
            byte[] input = hiddec.readInputFile(hiddec.getArgs(args[1]));
            String output = hiddec.getArgs(args[2]);

            hiddec.ecb(key, encryptedKey, input, output);
        }
        else{
            System.out.println("You have to many or too few arguments");
        }
    }
}

