import java.math.BigInteger;
import java.security.PublicKey;

public class Transaction {
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

    public String getTransactionData() {
        return  Integer.toString(transactionID) + senderPublicKey + recipientPublicKey + amount;
    }

    public void setSignature(BigInteger signature) {
        this.signature = signature;
    }


}
