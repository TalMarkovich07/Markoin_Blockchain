package model;

import cryptography.Wallet;
import java.io.Serializable;
import java.math.BigInteger;

public class Transaction implements Serializable, Comparable<Transaction> {
    private static int counter = 0;
    private int transactionID;
    private BigInteger senderPublicKey;
    private BigInteger recipientPublicKey;
    private double amount;
    private double fee;
    private BigInteger signature;

    public Transaction(BigInteger sender, BigInteger recipientAddress, double amount, double fee) {
        this.transactionID = counter++;
        this.senderPublicKey = sender;
        this.recipientPublicKey = recipientAddress;
        this.amount = amount;
        this.fee = fee;
    }

    @Override
    public int compareTo(Transaction o) {
        // High fee = higher priority
        return Double.compare(this.fee, o.fee);
    }
    public double getFee() {
        return fee;
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
        return String.format(java.util.Locale.US, "%s:%s:%.4f:%.4f",
                senderPublicKey.toString(16),
                recipientPublicKey.toString(16),
                amount,
                fee);
    }

    public void setSignature(BigInteger signature) {
        this.signature = signature;
    }

    public boolean verifySignature() {
        if (signature == null || senderPublicKey == null) return false;

        // Generate the same hash as the sender
        String hexHash = Block.calculateHash(getTransactionData());

        // Ensure m is interpreted exactly as it was during the signing process
        BigInteger m = new BigInteger(hexHash, 16);

        // Calculate P-1 for the exponent math
        BigInteger P_minus_1 = Wallet.P.subtract(BigInteger.ONE);
        BigInteger mModP1 = m.mod(P_minus_1);

        // Equation Check: G^signature mod P == (G^privateKey)^m mod P
        // This is equivalent to G^(m * x) mod P == PublicKey^m mod P
        BigInteger leftSide = Wallet.G.modPow(signature, Wallet.P);
        BigInteger rightSide = senderPublicKey.modPow(mModP1, Wallet.P);

        if (!leftSide.equals(rightSide)) {
            System.out.println("[DEBUG] Data to sign: " + getTransactionData());
            System.out.println("[DEBUG] Hash (m): " + m.toString(16));
            System.out.println("[DEBUG] P: " + Wallet.P.toString(16));
            System.out.println("[DEBUG] m mod (P-1) hex: " + mModP1.toString(16));
            System.out.println("[DEBUG] Signature: " + signature.toString(16));
            System.out.println("[DEBUG] G: " + Wallet.G.toString(16));
            System.out.println("[DEBUG] Left Side (G^sig mod P): " + leftSide.toString(16));
            System.out.println("[DEBUG] Sender PublicKey: " + senderPublicKey.toString(16));
            System.out.println("[DEBUG] Right Side (PubKey^m mod P): " + rightSide.toString(16));
        }

        return leftSide.equals(rightSide);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;

        if (this.signature == null || that.signature == null) // check if one of the transactions is coinbase
            return this.amount == that.amount &&
                    this.recipientPublicKey.equals(that.recipientPublicKey);
        return this.signature.equals(that.signature);
    }

    public String transactionHash() {
        return Block.calculateHash(this.toString());
    }


}
