package cryptography;

import model.Block;
import model.Transaction;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Wallet {
    private BigInteger privateKey;
    private BigInteger publicKey;

    public static final BigInteger G = BigInteger.valueOf(7);
    public static final BigInteger P = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFFFFFFFFFF", 16);

    public Wallet(){
        generateKeyPair();
    }
    public double getBalance(){
        return 0;
    }
    public BigInteger getPublicKey(){ return publicKey;}
    public BigInteger getPrivateKey(){ return privateKey;} // later check if can change
    private void generateKeyPair(){ //
        SecureRandom r = new SecureRandom();
        BigInteger P_minus_1 = P.subtract(BigInteger.ONE);
        int bitLength = P.bitLength();
        do {
            privateKey = new BigInteger(bitLength-1, r);
        } while (privateKey.compareTo(BigInteger.ONE) <= 0 || privateKey.compareTo(P_minus_1) >= 0);

        publicKey = G.modPow(privateKey, P);
    }

    public static BigInteger sign(String data, BigInteger privateKey) {
        String hexHash = Block.calculateHash(data);

        // Convert hex to a positive BigInteger
        BigInteger m = new BigInteger(hexHash, 16);

        BigInteger P_minus_1 = P.subtract(BigInteger.ONE);

        // Apply Fermat's Little Theorem: (m * privateKey) mod (P-1)
        // Do not mod 'm' before multiplication to maintain mathematical integrity
        return m.multiply(privateKey).mod(P_minus_1);
    }

    public String getAddress() {
        return publicKey.toString(16);
    }

    public static Transaction sendMoney(BigInteger privateSender, BigInteger publicSender, BigInteger publicRecipient, double amount, double fee) {
        Transaction tr = new Transaction(publicSender, publicRecipient, amount, fee);
        String trData = tr.getTransactionData();
        tr.setSignature(sign(trData, privateSender));
        return tr;

    }

}
