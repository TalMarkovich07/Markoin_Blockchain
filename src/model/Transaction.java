package model;

import cryptography.Wallet;
import java.io.Serializable;
import java.math.BigInteger;

public class Transaction implements Serializable {
    private static int counter = 0;
    private int transactionID;
    private BigInteger senderPublicKey;
    private BigInteger recipientPublicKey;
    private double amount;
    private BigInteger signature;

    public Transaction(BigInteger sender, BigInteger recipientAddress, double amount) {
        this.transactionID = counter++;
        this.senderPublicKey = sender;
        this.recipientPublicKey = recipientAddress;
        this.amount = amount;
    }

    public BigInteger getSenderPublicKey() {
        return senderPublicKey;
    }
    public BigInteger getRecipientPublicKey() {
        return recipientPublicKey;
    }
    public double getAmount() {
        return amount;
    }

    public String getTransactionData() {
        return  Integer.toString(transactionID) + senderPublicKey + recipientPublicKey + amount;
    }

    public void setSignature(BigInteger signature) {
        this.signature = signature;
    }

    public boolean verifySignature() {
        if (signature == null || senderPublicKey == null) return false;

        String hexHash = Block.calculateHash(getTransactionData());
        BigInteger m = new BigInteger(hexHash, 16);

        BigInteger actual = Wallet.G.modPow(signature, Wallet.P);
        BigInteger expected = senderPublicKey.modPow(m, Wallet.P);

        return actual.equals(expected);
    }


}
