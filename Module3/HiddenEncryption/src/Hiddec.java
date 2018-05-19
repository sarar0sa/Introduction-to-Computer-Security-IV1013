import javax.crypto.BadPaddingException;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.*;
import java.util.Arrays;

public class Hiddec {

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

    public int findKey(byte[] key, byte[] input, byte[] encryptedKey, int startSearch) throws NoSuchPaddingException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        for(int i = startSearch; i <= input.length - 16; i += 16){
            byte[] decrypted = decrypt(key, input, i, i + 16);
            if(Arrays.equals(decrypted,encryptedKey)){
                System.out.println(i);
                return i;
            }
        }
        return -1;
    }

    public void writeToFile(byte[] data, String output) throws IOException {
        FileOutputStream fos = new FileOutputStream(output);
        fos.write(data);
        fos.close();
    }

    public byte[] decrypt(byte[] key, byte[]input, int from, int to) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] copy = Arrays.copyOfRange(input, from, to);

        return cipher.update(copy);
    }


    public static void main(String args[]) throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Hiddec hiddec = new Hiddec();
        int start = 0;
        byte[] key = hiddec.hexToByte(hiddec.getArgs(args[0]));
        byte[] encryptedKey = hiddec.hash(key);
        byte[] input = hiddec.readInputFile(hiddec.getArgs(args[1]));
        String output = hiddec.getArgs(args[2]);

        int startIndex = hiddec.findKey(key,input, encryptedKey, start);

        if(startIndex == -1){
            System.out.println("The file does not contain any hidden blob");
            return;
        }

        int endIndex = hiddec.findKey(key, input, encryptedKey, startIndex + 16);

        byte[] data = hiddec.decrypt(key, input, startIndex + 16, endIndex);
        byte[] verifyData = hiddec.decrypt(key, input, endIndex + 16, endIndex + 32);

        byte[] hashOfData = hiddec.hash(data);

        System.out.println(Arrays.toString(hashOfData));
        System.out.println(Arrays.toString(verifyData));


        if(Arrays.equals(verifyData,hashOfData)){
            hiddec.writeToFile(data, output);
        }
        else{
            System.out.println("The data could not be verified as the correct data");
        }
    }
}
