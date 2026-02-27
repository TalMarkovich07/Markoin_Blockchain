import java.util.ArrayList;
import java.util.Date;

public class Block {
    public static int difficulty = 3;
    private String hash;
    private String previousHash;
    private ArrayList<String> data; //later change to <Transaction>
    private long timeStamp;
    private int nonce;

    public Block(ArrayList<String> data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.nonce = 0;
        this.hash = calculateBlockHash();
    }
    public void mineBlock(){
        String target = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring(0, difficulty).equals(target)){
            nonce++;
            hash = calculateBlockHash();
        }
    }

    public String calculateBlockHash(){
        // converts a block's info to string and returns it's hash using PRH hashing algorithm
        long hash = 0;
        long p = 31; // a small prime number
        long m = (long) 1e9 + 7; // a big prime number
        long pPow = 1;
        String str = previousHash+Long.toString(timeStamp)+Integer.toString(nonce)+data.toString();
        for(int i = 0; i < str.length(); i++){
            //for each char in the string, we multiply his ASCII value by pPow and take the module by m to get a normal number
            hash = (hash + (str.charAt(i) - 'a' + 1)*pPow) % m;
            pPow = (pPow*p)%m;
        }
        return Long.toHexString(hash);
    }
}
