package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static model.Graph.generateGraph;

public class Block implements Serializable {
    public static int difficulty = 20;
    private long timeStamp;
    private String hash;
    private String previousHash;
    private ArrayList<Transaction> data;

    private List<Integer> previousSolution;
    private Graph riddle;

    public Block(){
        data = new ArrayList<>();
        timeStamp = new Date().getTime();
        previousHash = "-1";
        this.hash = calculateBlockHash();
        this.riddle = generateGraph(difficulty);
    }
    public Block(ArrayList<Transaction> data, Block previousBlock) {
        this.data = data;
        this.previousHash = previousBlock.getHash();
        this.timeStamp = new Date().getTime();
        this.hash = calculateBlockHash();
    }


    public void addToBlock(Transaction transaction) {
        this.data.add(transaction);
        timeStamp = new Date().getTime();
        this.hash = calculateBlockHash();
    }
    public ArrayList<Transaction> getTransactions(){
        return this.data;
    }

    public String calculateBlockHash(){
        // converts a block's info to string and returns it's hash
        String str = previousHash+Long.toString(timeStamp)+data.toString();
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

    public Graph createGraph(){
        return null;
    }
    public String getHash(){ return hash; }
    public String getPreviousHash(){ return previousHash; }
    public ArrayList<Transaction> getData(){ return data; }
    public Graph getRiddle(){ return riddle; }

    public String toString(){
        return timeStamp+":\nBlock's hash: "+hash+"\nprevious block's hash:"+previousHash+"\n"+data.toString();
    }
}
