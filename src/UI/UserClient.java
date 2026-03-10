package UI;

import cryptography.Wallet;
import model.Transaction;
import java.io.*;
import java.net.*;
import java.math.BigInteger;
import java.util.Scanner;

public class UserClient {
    private static final String MINER_IP = "localhost";
    private static int MINER_PORT = 8888;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("--- Welcome to the Blockchain User Client ---");
        System.out.println("Please enter your miner's port: ");
        MINER_PORT = sc.nextInt();
        while(true){
            System.out.println("\nSelect an option:");
            System.out.println("1) Create New Wallet");
            System.out.println("2) Check Balance");
            System.out.println("3) Send Transaction");
            System.out.println("4) Exit");

            String choice = sc.nextLine();

            switch(choice){
                case "1":
                    createNewWallet();
                    break;
                case "2":
                    System.out.println("Enter Public key to check");
                    checkBalance(sc);
                    break;
                case "3":
                    sendTransaction(sc);
                    break;
                case "4":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void createNewWallet(){
        Wallet wallet = new Wallet();
        System.out.println("\n!!! SAVE THESE KEYS IN A SECURE PLACE !!!");
        System.out.println("Public Key (Address): " + wallet.getPublicKey().toString(16));
        System.out.println("Private Key: " + wallet.getPrivateKey().toString(16));
    }

    private static void checkBalance(Scanner sc) {
        System.out.print("Enter Public Key: ");
        String pubKeyHex = sc.nextLine();

        try (Socket socket = new Socket(MINER_IP, MINER_PORT)){
        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject("GET_BALANCE:" + pubKeyHex);
            double balance = (double) in.readObject();
            System.out.println("Current Balance: " + balance + " coins.");
            } catch (Exception e){
            System.out.println("Error connecting to miner: " + e.getMessage());
        }
    }

    private static void sendTransaction(Scanner sc){
        try {
            System.out.print("Your Private Key: ");
            BigInteger privKey = new BigInteger(sc.nextLine(), 16);
            System.out.print("Your Public Key: ");
            BigInteger pubSender = new BigInteger(sc.nextLine(), 16);
            System.out.print("Recipient Public Key: ");
            BigInteger pubRecipient = new BigInteger(sc.nextLine(), 16);
            System.out.print("Amount to send: ");
            double amount = Double.parseDouble(sc.nextLine());

            Transaction tr = Wallet.sendMoney(privKey, pubSender, pubRecipient, amount);

            try (Socket socket = new Socket(MINER_IP, MINER_PORT)) {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(tr);
                System.out.println("Transaction sent to Mempool!");

            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e){
            System.out.println("Transaction failed: " + e.getMessage());
        }
    }
}
