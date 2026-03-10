package logic;

import model.Block;
import model.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.math.BigInteger;

import static logic.HamiltonSolver.verifySolution;
import static model.Block.difficulty;
import static model.Block.firstBlock;

public class Chain {
    private static Chain instance;
    private HashMap<BigInteger, Double> balances;
    private ArrayList<Block> Blockchain;
    private ArrayList<Transaction> Mempool; //this will store all transactions that haven't been added to a block yet

    private Chain(){
        balances = new HashMap<>();
        Blockchain = new ArrayList<>();
        Mempool = new ArrayList<>();

        Block first = firstBlock();
        Blockchain.add(first);
    }

    public static Chain getInstance(){
        if(instance == null)
            instance = new Chain();
        return instance;
    }

    public void addWallet(BigInteger publicKey){
        if(balances.containsKey(publicKey))
            throw new RuntimeException("cryptography.Wallet already exists");
        balances.put(publicKey, 0.0);
    }

    public Double getBalance(BigInteger publicKey){
        if(balances.containsKey(publicKey))
            return balances.get(publicKey);
        return null;
    }
    public void setBalance(BigInteger publicKey, Double balance){
        balances.put(publicKey, balance);
    }
    public void transferAmount(Transaction tr) {
        BigInteger from = tr.getSenderPublicKey();
        BigInteger to = tr.getRecipientPublicKey();
        Double amount = tr.getAmount();

        Double sender = balances.get(from);
        Double receiver = balances.get(to);

        if (!balances.containsKey(from))
            throw new RuntimeException("Non-existing sender"); // later change to a custom exception
        if(!balances.containsKey(to))
            throw new RuntimeException("Non-existing receiver"); // later change to a custom exception


        balances.put(from, sender-amount);
        balances.put(to, receiver+amount);
    }
    public boolean valid(Block block){
        //checks if: solution to previous block is valid, if the hash is valid, if the previous hash is really the previous block's hash, and if all transactions are valid.
        Block last = Blockchain.getLast();

        // verify solution to previous block's riddle
        if(!verifySolution(last.getRiddle(), block.getPreviousSolution()))
            return false;

        // verify the hash starts with the 'difficulty' amount of 1's
        StringBuilder sb = new StringBuilder();
        sb.append("1".repeat(difficulty));
        if(!block.getHash().startsWith(sb.toString()))
            return false;

        //check that the block's previous hash is the last block's hash
        if(!last.getHash().equals(block.getPreviousHash()))
            return false;

        // validate each transaction
        ArrayList<Transaction> transactions = block.getTransactions();
        HashMap<BigInteger, Double> tempBalances = new HashMap<>(this.balances);
        for(Transaction transaction : transactions){
            if(!transaction.verifySignature())
                return false;

            
        }

        return true;
    }
    public void addBlock(Block block){
        Blockchain.add(block);
    }
    public void printChain(){
        for(Block block : Blockchain)
            System.out.println(block.toString());
    }
    public Block lastBlock(){
        return Blockchain.get(Blockchain.size()-1);
    }
}
