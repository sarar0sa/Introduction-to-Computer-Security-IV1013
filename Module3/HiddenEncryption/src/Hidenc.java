import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.security.*;
import java.util.ArrayList;
import java.util.Random;

public class Hidenc {
    private byte[] key, ctr, input, template = null;
    int offset, size = -1;
    String output;

    public void getArgs(String[] args) throws IOException, NoSuchPaddingException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        for (String arg: args) {
            String[] argument = arg.split("=");
            switch (argument[0]) {
                case "--key":
                    key = hexToByte(argument[1]);
                    break;

                case "--ctr":
                    ctr = hexToByte(argument[1]);
                    break;

                case "--offset":
                    offset = Integer.parseInt(argument[1]);
                    break;

                case "--input":
                    input = readInputFile(argument[1]);
                    break;

                case "--output":
                    output = argument[1];
                    break;

                case "--template":
                    template = readInputFile(argument[1]);
                    break;

                case "--size":
                    size = Integer.parseInt(argument[1]);
                    break;
            }
        }

            if(template != null && size != -1 ){
                System.out.println("Only one of --template and --size can be set");
                return;
            }

            /*if (key == null|| input == null || output == null || (template == null || size == -1)){
                System.out.println("Wrong arguments, the required arguments are: --key --input --output (--template or --size)");
                return;
            }*/

            if(size != -1){
               template =  createTemplate(size);
            }

            if(offset == -1){

            }

            begin();
    }


    public void begin() throws IllegalBlockSizeException, NoSuchAlgorithmException, IOException, BadPaddingException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] encryptedKey = hash(key);
        byte[] hashOfData = hash(input);
        byte[] theBlob = createBlob(encryptedKey, input, hashOfData);
        placeBlob(offset,template,theBlob,output, key);

    }


    public byte[] hexToByte(String key){
        return DatatypeConverter.parseHexBinary(key);

    }

    public byte[] createTemplate(int size){
        byte[] template = new byte[size];
        Random random = new Random();
        random.nextBytes(template);

        return template;
    }

    /*public int randomOffset(){
        Random random = new Random();
        int offset = random.nextInt() * 16;

        return template;
    }*/



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

    public byte[] encrypt(byte[] key, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        if(ctr == null){
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            return cipher.doFinal(data);

        }
        else {
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            IvParameterSpec iv = new IvParameterSpec(ctr);
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

            return cipher.doFinal(data);
        }
    }

    public byte[] createBlob(byte[] encryptedKey, byte[] input, byte[] hashOfData) throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        ArrayList<Byte> temp = new ArrayList<>();

        for (byte bit: encryptedKey) {
            temp.add(bit);
        }

        for (byte bit: input) {
            temp.add(bit);
        }

        for (byte bit: encryptedKey) {
            temp.add(bit);
        }

        for (byte bit: hashOfData) {
            temp.add(bit);
        }

        byte[] theBlob = new byte[temp.size()];
        for(int i = 0; i < temp.size(); i++){
            theBlob[i] = temp.get(i);
        }
        return theBlob;
    }

    public void placeBlob(int offset, byte[] template, byte[] theBlob, String output, byte[] key) throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        int counter = 0;
        theBlob = encrypt(key, theBlob);
        for(int i = offset; i < theBlob.length + offset; i++){
            template[i] = theBlob[counter];
            counter++;
        }
        writeToFile(template,output);
    }

    public void writeToFile(byte[] data, String output) throws IOException {
        FileOutputStream fos = new FileOutputStream(output);
        fos.write(data);
        fos.close();
    }


    public static void main(String args[]) throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        Hidenc hidenc = new Hidenc();
        /*byte[] template = hidenc.createTemplate();
        byte[] key = hidenc.hexToByte(hidenc.getArgs(args[0]));
        int offset = Integer.parseInt(hidenc.getArgs(args[1]));
        byte[] input = hidenc.readInputFile(hidenc.getArgs(args[2]));
        String output = hidenc.getArgs(args[3]);
        //String template = hidenc.getArgs(args[4]);*/
       // if(args.length > 6 || args.length < 6){
         //   System.out.print("You have too many or too few arguments, please try again");
        //}

        //else {
            hidenc.getArgs(args);
        //}

        /*byte[] encryptedKey = hidenc.hash(key);
        byte[] hashOfData = hidenc.hash(input);
        byte[] theBlob = hidenc.createBlob(encryptedKey, input, hashOfData);

        hidenc.placeBlob(offset,template,theBlob,output, key);*/

    }

}
