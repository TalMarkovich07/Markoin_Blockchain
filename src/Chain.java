import model.Block;
import model.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.math.BigInteger;

public class Chain {
    private static Chain instance;
    private HashMap<BigInteger, Double> balances;
    private ArrayList<Block> Blockchain;
    private ArrayList<Transaction> Mempool; //this will store all transactions that haven't been added to a block yet

    private Chain(){
        balances = new HashMap<>();
        Blockchain = new ArrayList<>();
        Mempool = new ArrayList<>();

        Block first = new Block();
        Blockchain.add(first);
    }

    public static Chain getInstance(){
        if(instance == null)
            instance = new Chain();
        return instance;
    }

    public void addWallet(BigInteger publicKey){
        if(balances.containsKey(publicKey))
            throw new RuntimeException("Wallet already exists");
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
        return true;
    } // later change
    public boolean addBlock(Block block){
        if(!valid(block))
            return false;
        Blockchain.add(block);
        return true;
    }
    public void printChain(){
        for(Block block : Blockchain)
            System.out.println(block.toString());
    }
    public Block lastBlock(){
        return Blockchain.get(Blockchain.size()-1);
    }
}
