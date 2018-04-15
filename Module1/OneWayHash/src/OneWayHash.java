import java.io.UnsupportedEncodingException;
import java.security.*;


/**
 *
 * OneWayHash uses brute force to investigate how many trials it will take to break the weak collision
 * property of a hash function. It will only try to make a match with the first 24 bits of a digest.
 * The digest are created from an input string with the hash algorithm SHA-256.
 *
 */

public class OneWayHash {
    private byte[] digest, inputBytes, tryDigest;
    private int counter = 0;
    private String digestAlgorithm = "SHA-256";
    private String textEncoding = "UTF-8";


    /**
     *
     * Performs a brute force attack to search for a matching digest.
     * Prints out how many trials it took.
     *
     * @param digest the digest the comparing are made against
     * @throws UnsupportedEncodingException
     */


    public void bruteForce(byte[] digest) throws UnsupportedEncodingException {
        while(true) {
            counter++;
            String inputText = Long.valueOf(counter).toString();
            tryDigest = makeDigest(inputText);

            if (tryDigest[0] == digest[0] && tryDigest[1] == digest[1] && tryDigest[2] == digest[2]){
                System.out.println("It took " + counter + " number of times to find a matching message digest.");
                System.out.print("The matching digest became: ");
                printDigest(tryDigest);
                return;
            }
        }
    }

    /***
     *
     * Method that takes a text as an input and produces a message digest for that text.
     *
     * @param inputText
     * @return
     */

    public byte[] makeDigest(String inputText){
        try {
            MessageDigest md = MessageDigest.getInstance(digestAlgorithm);
            inputBytes = inputText.getBytes(textEncoding);
            md.update(inputBytes);
            digest = md.digest();

        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algorithm \"" + digestAlgorithm  + "\" is not available");
        } catch (Exception e) {
            System.out.println("Exception " + e);
        }

        return digest;
    }

    /**
     *
     * Prints out the created message digest
     *
     * @param digest
     */

    public static void printDigest(byte[] digest) {
        for (int i=0; i<digest.length; i++)
            System.out.format("%02x", digest[i]&0xff);
        System.out.println();
    }


    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        OneWayHash oneWayHash = new OneWayHash();
        String inputText = args[0];
        //String inputText = "helloo";
        byte[] theDigest = oneWayHash.makeDigest(inputText);
        System.out.print("Digest for the message \"" + inputText +"\", using SHA-256 is: ");
        printDigest(theDigest);
        oneWayHash.bruteForce(theDigest);
    }

}