package UI;

import cryptography.Wallet;
import model.Transaction;
import java.io.*;
import java.net.*;
import java.math.BigInteger;
import java.util.Scanner;

public class UserClient {
    private static final String MINER_IP = "127.0.0.1";
    private static int port;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("--- Welcome to the Blockchain User Client ---");
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
                    System.out.print("Enter Miner's port: ");
                    port = sc.nextInt();
                    sc.nextLine();
                    checkBalance(sc);
                    break;
                case "3":
                    System.out.print("Enter Miner's port: ");
                    port = sc.nextInt();
                    sc.nextLine();
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
        System.out.println("Enter Public Key: ");
        String pubKeyHex = sc.nextLine();

        try (Socket socket = new Socket(MINER_IP, port)){
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush(); // Send header immediately


            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject("GET_BALANCE:" + pubKeyHex);
            out.flush();

            double balance = (double) in.readObject();
            System.out.println("Current Balance: " + balance + " coins.");
            } catch (Exception e){
            System.out.println("Error connecting to miner: " + e.getMessage());
        }
    }

    private static void sendTransaction(Scanner sc){
        try {
            System.out.print("Your Public Key: ");
            BigInteger pubSender = new BigInteger(sc.nextLine(), 16);
            System.out.print("Recipient Public Key: ");
            BigInteger pubRecipient = new BigInteger(sc.nextLine(), 16);
            System.out.print("Amount to send: ");
            double amount = Double.parseDouble(sc.nextLine());
            System.out.print("Enter Miner's fee: ");
            double fee = Double.parseDouble(sc.nextLine());
            System.out.print("Your Private Key (to sign the transaction): ");
            BigInteger privateKey = new BigInteger(sc.nextLine(), 16);

            Transaction tr = Wallet.sendMoney(privateKey, pubSender, pubRecipient, amount, fee);
            if(tr.verifySignature()){
                System.out.println("Transaction verification success.");
                try (Socket socket = new Socket(MINER_IP, port)) {
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    out.writeObject(tr);
                    System.out.println("Transaction sent to Mempool!");

                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                throw new RuntimeException(e);
            }
            } else  {
                System.out.println("Transaction verification failed.");
            }
        } catch (Exception e){
            System.out.println("Transaction failed: " + e.getMessage());
        }
    }
}
