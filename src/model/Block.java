package model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static logic.HamiltonSolver.findCycle;
import static model.Graph.firstBlockGraph;
import static model.Graph.generateGraph;

public class Block implements Serializable {
    public static int difficulty = 6;
    public static final int MAX_TRANSACTIONS_PER_BLOCK = 5;
    public static final Double reward = 3.125;

    private long timeStamp;
    private String hash;
    private String previousHash;
    private long nonce;
    private ArrayList<Transaction> data;

    private List<Integer> previousSolution;
    private Graph riddle;




    public static Block firstBlock(){
        return new Block();
    }

    public static Block mineBlock(ArrayList<Transaction> data, Block previousBlock, AtomicBoolean stopMiningFlag){
        Block block = new Block(data, previousBlock);
        block.previousSolution =  findCycle(previousBlock.riddle);
        block.riddle = generateGraph(difficulty);

        StringBuilder targetBuilder = new StringBuilder();
        for (int i = 0; i < difficulty; i++) targetBuilder.append("1");
        String target = targetBuilder.toString();

        System.out.println("Mining block... Looking for hash starting with: " + target);

        block.nonce = 0;
        block.hash = block.calculateBlockHash();

        while(!block.hash.startsWith(target)){
            if(stopMiningFlag.get()){
                return null;
            }
            block.nonce++;
            block.hash = block.calculateBlockHash();
            if(block.nonce%100000 == 0)
                System.out.print(".");
        }

        return block;

    }
    private Block(){
        data = new ArrayList<>();
        timeStamp = 1;
        previousHash = "-1";
        this.riddle = firstBlockGraph(difficulty);
        this.previousSolution = new ArrayList<>();
        this.hash = calculateBlockHash();
    }
    private Block(ArrayList<Transaction> data, Block previousBlock) {
        this.data = data;
        this.previousHash = previousBlock.getHash();
        this.timeStamp = new Date().getTime();
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
        String str = timeStamp +
                previousHash +
                nonce +
                data.toString() +
                previousSolution.toString() +
                riddle.toString();
        return calculateHash(str);
    }
    public static String calculateHash(String data){
        // "Polynomial Rolling Hash" algorithm to create a hash to any given string
        long hash = 0;
        long p = 31;
        long m = (long) 1e16 + 7;
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
    public List<Integer> getPreviousSolution(){ return previousSolution; }
    public Graph getRiddle(){ return riddle; }
    public String toString(){
        String str = "";
        str+=timeStamp;
        str+=": \nBlock's hash: "+hash;
        str+="\nPrevious block's hash: "+previousHash;
        str+="\nnonce: "+nonce;
        str+="\nBlock's data: "+data.toString();
        str+="\nPrevious solution: "+previousSolution.toString();
        str+="\nRiddle: "+riddle.toString();
        return str;
    }
}
