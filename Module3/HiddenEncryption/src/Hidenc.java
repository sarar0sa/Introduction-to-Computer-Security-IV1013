import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.Random;

public class Hidenc {

    public String getArgs(String args){
        String[] argument = args.split("=");
        return argument[1];
    }


    public byte[] hexToByte(String key){
        return DatatypeConverter.parseHexBinary(key);

    }

    public byte[] createTemplate(){
        byte[] template = new byte[1024];
        Random random = new Random();
        random.nextBytes(template);

        return template;
    }

    public byte[] readInputFile(String input) throws IOException {
        File file = new File(input);
        byte[] byteArray = new byte[(int)file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(byteArray);
        fis.close();

        return byteArray;

    }

    public byte[] hash(byte[] key) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(key);
        byte[] digest = md.digest();

        return digest;
    }

    public byte[] encrypt(byte[] key, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return cipher.doFinal(data);
    }

    public byte[] createBlob(byte[] encryptedKey, byte[] input, byte[] hashOfData, byte[] key) throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.write(encryptedKey);
        buffer.write(input);
        buffer.write(encryptedKey);
        buffer.write(hashOfData);
        byte[] theBlob = buffer.toByteArray();

        return theBlob;

    }

    public void placeBlob(int offset, byte[] template, byte[] theBlob, String output, byte[] key) throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        int counter = 0;
        for(int i = offset; i < theBlob.length + offset; i++){
            template[i] = theBlob[counter];
            counter++;
        }

        System.out.println(template.length);
        writeToFile(encrypt(key, template),output);
    }

    public void writeToFile(byte[] data, String output) throws IOException {
        FileOutputStream fos = new FileOutputStream(output);
        fos.write(data);
        fos.close();
    }


    public static void main(String args[]) throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        Hidenc hidenc = new Hidenc();
        byte[] template = hidenc.createTemplate();
        byte[] key = hidenc.hexToByte(hidenc.getArgs(args[0]));
        int offset = Integer.parseInt(hidenc.getArgs(args[1]));
        byte[] input = hidenc.readInputFile(hidenc.getArgs(args[2]));
        String output = hidenc.getArgs(args[3]);
        //String template = hidenc.getArgs(args[4]);

        byte[] encryptedKey = hidenc.hash(key);
        byte[] hashOfData = hidenc.hash(input);

        byte[] theBlob = hidenc.createBlob(encryptedKey, input, hashOfData, key);

        hidenc.placeBlob(offset,template,theBlob,output, key);

    }

}
