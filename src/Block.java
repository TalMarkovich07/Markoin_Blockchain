import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Block implements Serializable {
    public static int difficulty = 3;
    private String hash;
    private String previousHash;
    private ArrayList<Transaction> data; //later change to <Transaction>
    private long timeStamp;
    private int nonce;

    public Block(ArrayList<Transaction> data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.nonce = 0;
        this.hash = calculateBlockHash();
    }
    public void addToBlock(Transaction transaction) {
        this.data.add(transaction);
        timeStamp = new Date().getTime();
        this.nonce = 0;
        this.hash = calculateBlockHash();
    }
    public ArrayList<Transaction> getTransactions(){
        return this.data;
    }
    public void mineBlock(){
        String target = new String(new char[difficulty]).replace('\0', '0');
        while(!hash.substring(0, difficulty).equals(target)){
            nonce++;
            hash = calculateBlockHash();
        }
    }

    public String calculateBlockHash(){
        // converts a block's info to string and returns it's hash
        String str = previousHash+Long.toString(timeStamp)+Integer.toString(nonce)+data.toString();
        return calculateHash(str);
    }
    public static String calculateHash(String data){
        // "Polynomial Rolling Hash" algorithm to create a hash to any given string
        long hash = 0;
        long p = 31;
        long m = (long) 1e9 + 7;
        long pPow = 1;
        for (int i = 0; i < data.length(); i++) {
            hash = (hash + (Math.abs(data.charAt(i)) + 1) * pPow) % m;
            pPow = (pPow * p) % m;
        }
        return Long.toHexString(hash);
    }
    public String getHash(){ return hash; }
    public String getPreviousHash(){ return previousHash; }
    public ArrayList<Transaction> getData(){ return data; }

    public String toString(){
        return timeStamp+":\nBlock's hash: "+hash+"\nprevious block's hash:"+previousHash+"\n"+data.toString();
    }
}
