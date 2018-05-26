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
    private int offset = -1, size = -1;
    private String output;

    /**
     *
     * Extracts entered arguments
     *
     * @param args
     * @throws IOException
     * @throws NoSuchPaddingException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     */

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

                default:
                    System.out.println("You have entered a invalid argument");
                    return;
            }
        }

            if(template != null && size != -1 ){
                System.out.println("Only one of --template and --size can be set");
                return;
            }

            if(template == null && size == -1 ){
                System.out.println("At least one of template or size must be set");
                return;
            }

            if (key == null|| input == null || output == null){
                System.out.println("You need to at least specify the following arguments: --key --input --output (--template or --size)");
                return;
            }

            if(size != -1){
               template =  createTemplate(size);
            }

            begin();
    }

    /**
     *
     * Creates the hash of the key, the hash of data and then creates the blob,
     * which then will be placed in the template. If not offset has been entered by
     * user a random offset is created.
     *
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws IOException
     * @throws InvalidAlgorithmParameterException
     */

    public void begin() throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, IOException, InvalidAlgorithmParameterException {
        byte[] encryptedKey = hash(key);
        byte[] hashOfData = hash(input);
        byte[] theBlob = createBlob(encryptedKey, input, hashOfData);

        if(offset == -1){
            randomOffset();
            if(offset + theBlob.length > template.length){
                randomOffset();
            }
        }

        if(offset + theBlob.length > template.length){
            System.out.print("The blob can not fit into the template");
            return;
        }
        else {
            placeBlob(offset, template, theBlob, output, key);
        }
    }

    /**
     *
     * Converts a hexadeciaml string to a byte array.
     *
     * @param key
     * @return
     */


    public byte[] hexToByte(String key){
        return DatatypeConverter.parseHexBinary(key);
    }

    /**
     *
     * Creates a random template if the user has not specified one.
     *
     * @param size
     * @return
     */

    public byte[] createTemplate(int size){
        byte[] template = new byte[size];
        Random random = new Random();
        random.nextBytes(template);

        return template;
    }

    /**
     *
     * Creates a random offset if the user has not specified one.
     *
     */

    public void randomOffset() {
        while((offset % 16) != 0){
            offset  = new Random().nextInt(template.length);
        }
    }

    /***
     *
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
            System.out.println("The input or template file could not be found.");
            System.exit(0);
        }
        try {
            fis.read(byteArray);
        } catch (IOException e) {
            System.out.println("The input file could not be opened");
            System.exit(0);
        }
        fis.close();

        return byteArray;

    }

    /**
     *
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

    /***
     *
     *
     * Encrypts the file, either with ECB- or CTR-mode based on the users input.
     *
     * @param key
     * @param data
     * @return
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws InvalidAlgorithmParameterException
     */

    public byte[] encrypt(byte[] key, byte[] data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
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

    /**
     *
     *
     * Creates the blob
     *
     * @param encryptedKey
     * @param input
     * @param hashOfData
     * @return
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */

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

    /**
     *
     * Places the blob in the template.
     *
     * @param offset
     * @param template
     * @param theBlob
     * @param output
     * @param key
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     */

    public void placeBlob(int offset, byte[] template, byte[] theBlob, String output, byte[] key) throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        int counter = 0;
        theBlob = encrypt(key, theBlob);
        for(int i = offset; i < theBlob.length + offset; i++){
            template[i] = theBlob[counter];
            counter++;
        }
        writeToFile(template,output);
    }

    /**
     *
     * Writes the result to output file which has been specified by the user.
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
            System.out.println("The output file was not found");
            return;
        }
        try {
            fos.write(data);
        } catch (IOException e) {
            System.out.println("The blob could not be written to file");
            return;
        }
        fos.close();
    }


    public static void main(String args[]) throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        Hidenc hidenc = new Hidenc();
        if(args.length < 4){
            System.out.print("You have entered too few arguments");
            return;
        }

        else {
            hidenc.getArgs(args);
        }
    }

}
