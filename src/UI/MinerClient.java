package UI;

import network.MinerNode;

import java.math.BigInteger;
import java.util.Scanner;

public class MinerClient {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("--- Blockchain Miner Node ---");
        System.out.print("Enter port for this node: ");
        int port = Integer.parseInt(sc.nextLine());
        System.out.print("Enter public key for this node: ");
        BigInteger publicKey = new BigInteger(sc.nextLine(), 16);
        MinerNode node = new MinerNode(port, publicKey);
        node.startServer();

        System.out.println("Node started on port " + port);


        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1) Connect to a Peer");
            System.out.println("2) Start Mining");
            System.out.println("3) Print Blockchain");
            System.out.println("4) Print Peers");
            System.out.println("5) Exit");
            System.out.print("> ");

            String choice = sc.nextLine();

            if (choice.equals("1")) {
                /*System.out.print("Enter peer IP: ");
                String ip = sc.nextLine();*/
                String ip = "127.0.0.1";
                System.out.print("Enter peer port: ");
                int pPort = Integer.parseInt(sc.nextLine());
                node.connectToPeer(ip, pPort);
            }
            else if (choice.equals("2")) {
                node.startMining();
            }
            else if (choice.equals("3")) {
                node.printChain();
            }
            else if (choice.equals("4")) {
                node.printPeers();
            }
            else if (choice.equals("5")) {
                System.exit(0);
            }
            else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }
}