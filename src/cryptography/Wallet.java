package cryptography;

import model.Block;
import model.Transaction;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Wallet {
    private BigInteger privateKey;
    private BigInteger publicKey;

    public static final BigInteger G = BigInteger.valueOf(7);
    public static final BigInteger P = new BigInteger("FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1", 16);

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
        privateKey = new BigInteger(256, r); //private key will be a large prime
        publicKey = G.modPow(privateKey, P); //public key will be G (a small prime) to the power of the privateKey, module P (a large prime)
    }

    public static BigInteger sign(String data, BigInteger privateKey) {
        //'encrypt' given data's hash with private key. decryption can only be done using public key.
        String hexHash = Block.calculateHash(data);
        BigInteger m = new BigInteger(hexHash, 16);
        return m.multiply(privateKey).mod(P);
    }
    public String getAddress() {
        return publicKey.toString(16);
    }

    public static Transaction sendMoney(BigInteger privateSender, BigInteger publicSender, BigInteger publicRecipient, double amount) {
        Transaction tr = new Transaction(publicSender, publicRecipient, amount);
        String trData = tr.getTransactionData();
        tr.setSignature(sign(trData, privateSender));
        return tr;

    }

}
