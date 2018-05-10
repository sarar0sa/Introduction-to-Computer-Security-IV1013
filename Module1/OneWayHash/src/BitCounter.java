import java.util.Arrays;

public class BitCounter {
    private static int diff = 0;

    public static void main(String[] args) throws NumberFormatException{
        String a = "9f8bc443c17bab18572b08a8a0c85fb59a97581dd0fbed0e9589b015ebb3c7ea";
        String b = "d2e6545b72469d5b05fa1c4c6584a87fc9f88124a2bf7fdaeb6dea3ca5706d8c";
        byte[] a1 = a.getBytes();
        byte[] a2 = b.getBytes();
        //int a2 = Integer.valueOf(b);

        for(int i = 0; i < a1.length; i++){
            // Returns number of 1-bits after OR, in other words, returns det differnce.
            diff += Integer.bitCount(a1[i] ^ a2[i]);
        }

       System.out.print(diff);

    }
}
