package logic;

import model.Block;
import model.MaxHeap;
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
    private MaxHeap<Transaction> Mempool; //this will store all transactions that haven't been added to a block yet

    private Chain(){
        balances = new HashMap<>();
        Blockchain = new ArrayList<>();
        Mempool = new MaxHeap<>();

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
            throw new RuntimeException("Wallet already exists");
        balances.put(publicKey, 0.0);
    }

    public Double getBalance(BigInteger publicKey){
        if(balances.containsKey(publicKey))
            return balances.get(publicKey);
        balances.put(publicKey, 0.0);
        return 0.0;
    }
    public void updateBalance(Transaction tr){
        BigInteger sender = tr.getSenderPublicKey();
        BigInteger receiver = tr.getRecipientPublicKey();
        Double amount = tr.getAmount();

        if(sender == null)
            balances.put(receiver, getBalance(receiver)+amount);
        else{
            if(!balances.containsKey(sender))
                throw new RuntimeException("sender does not exist");
            if(!balances.containsKey(receiver))
                balances.put(receiver, 0.0);
            if(balances.get(sender) < amount)
                throw new RuntimeException("sender is out of balance");

            balances.put(sender, balances.get(sender) - amount - tr.getFee());
            balances.put(receiver, balances.get(receiver) + amount);

        }

    }

    public boolean isValidBlock(Block block){
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


        ArrayList<Transaction> transactions = block.getTransactions();
        // validate first transaction
        Transaction coinbase = transactions.get(0);
        if (coinbase.getSenderPublicKey() != null) return false; // coinbase transaction must have no sender
        if (coinbase.getAmount() != Block.reward) return false;

        // validate the rest transaction (signature and double spending)
        HashMap<BigInteger, Double> tempBalances = new HashMap<>(this.balances);

        BigInteger miner = coinbase.getRecipientPublicKey();
        tempBalances.put(miner, tempBalances.getOrDefault(miner, 0.0) + coinbase.getAmount());//adds miner his reward
        for (int i = 1; i < transactions.size(); i++){
            if(!transactions.get(i).verifySignature())
                return false;

            BigInteger sender = transactions.get(i).getSenderPublicKey();
            BigInteger recipient = transactions.get(i).getRecipientPublicKey();
            if(!tempBalances.containsKey(sender))
                throw new RuntimeException("Non-existing sender");
            if(!tempBalances.containsKey(recipient))
                throw new RuntimeException("Non-existing recipient");

            double amount = transactions.get(i).getAmount();

            double senderBalance = tempBalances.getOrDefault(sender, 0.0);
            if(amount > senderBalance)
                return false;
            tempBalances.put(sender, senderBalance - amount);
            tempBalances.put(recipient, tempBalances.getOrDefault(recipient, 0.0) + amount);
        }

        return true;
    }
    public void addBlock(Block block){
        ArrayList<Transaction> transactions = block.getTransactions();
        for(Transaction tr : transactions)
            updateBalance(tr);
        Blockchain.add(block);
    }
    public void printChain(){
        for(Block block : Blockchain)
            System.out.println(block.toString());
    }
    public Block lastBlock(){
        return Blockchain.getLast();
    }
    public ArrayList<Block> getBlockchain(){
        return Blockchain;
    }
    public void replaceChain(ArrayList<Block> newChain){
        if (isChainValid(newChain) && newChain.size() > this.Blockchain.size()) {
            this.Blockchain = new ArrayList<>(newChain);
            System.out.println("[INFO] Chain Replaced");
            try{
                recalculateBalances();
                System.out.println("[INFO] Balances updated.");
            } catch(Exception e){
                System.out.println("[ERROR] Balances exception: " + e.getMessage());
            }
        }
    }
    public boolean isChainValid(ArrayList<Block> chainToValidate){
        if (chainToValidate == null || chainToValidate.isEmpty()) return false;

        for (int i = 1; i < chainToValidate.size(); i++) {
            Block current = chainToValidate.get(i);
            Block previous = chainToValidate.get(i - 1);

            if (!current.getPreviousHash().equals(previous.getHash())) return false;

            StringBuilder sb = new StringBuilder();
            sb.append("1".repeat(difficulty));
            if (!current.getHash().startsWith(sb.toString())) return false;

            if (!verifySolution(previous.getRiddle(), current.getPreviousSolution())) return false;
        }
        return true;
    }

    public void removeFromMempool(ArrayList<Transaction> transactionsInBlock){
        if (Mempool.isEmpty() || transactionsInBlock == null) return;

        MaxHeap<Transaction> updatedMempool = new MaxHeap<>();
        while(!Mempool.isEmpty()){
            Transaction cur = Mempool.extractMax();

            if(!transactionsInBlock.contains(cur))
                updatedMempool.insert(cur);
        }
        this.Mempool = updatedMempool;
        System.out.println("[Mempool Updated] Transactions already in block were removed.");
    }

    private void recalculateBalances() {
        balances.clear();
        for (int j = 1; j < Blockchain.size(); j++) {
            ArrayList<Transaction> txs = Blockchain.get(j).getTransactions();
            if(!balances.containsKey(txs.getFirst().getRecipientPublicKey()))
                balances.put(txs.getFirst().getRecipientPublicKey(), 0.0);
            balances.put(txs.getFirst().getRecipientPublicKey(), balances.get(txs.getFirst().getRecipientPublicKey()) + txs.getFirst().getAmount());
            for (int i = 1; i < txs.size(); i++) {
                Transaction tr = txs.get(i);
                BigInteger sender = tr.getSenderPublicKey();
                BigInteger recipient = tr.getRecipientPublicKey();
                double amount = tr.getAmount();

                if(!balances.containsKey(sender))
                    throw new RuntimeException("Non-existing sender");
                if(!balances.containsKey(recipient))
                    balances.put(recipient, 0.0);

                balances.put(sender, balances.getOrDefault(sender, 0.0) - amount - tr.getFee());
                balances.put(recipient, balances.getOrDefault(recipient, 0.0) + amount);
            }
        }
    }

    public MaxHeap<Transaction> getMempool() {
        return Mempool;
    }

}
