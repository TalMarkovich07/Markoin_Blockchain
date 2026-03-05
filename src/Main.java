import model.Block;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
    private static Set<Integer> peerPorts = new HashSet<>();
    private static int myPort;

    public static void main(String[] args) {
        Chain blockchain = Chain.getInstance();

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter my port: ");
        myPort = sc.nextInt();
        sc.nextLine();

        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(myPort)) {
                while (true) {
                    try (Socket socket = server.accept();
                         ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                        Object received = in.readObject();

                        if (received instanceof String && ((String) received).startsWith("HELLO:")) {
                            int newPeer = Integer.parseInt(((String) received).split(":")[1]);
                            if (peerPorts.add(newPeer)) {
                                System.out.println("\n[New Peer Discovered: " + newPeer + "]");
                                sendMessage(newPeer, "HELLO_BACK:" + myPort);
                            }
                        } else if (received instanceof String && ((String) received).startsWith("HELLO_BACK:")) {
                            int newPeer = Integer.parseInt(((String) received).split(":")[1]);
                            if (peerPorts.add(newPeer)) {
                                System.out.println("\n[Peer Confirmed: " + newPeer + "]");
                            }
                        } else if (received instanceof Block) {
                            Block b = (Block) received;
                            System.out.println("\n[Block Received] Hash: " + b.getHash());
                            if(blockchain.addBlock(b))
                                System.out.println("[Block Added] Hash: " + b.getHash());
                        }
                        System.out.print("> ");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        discoverPeers(8880, 8890);

        while (true) {
            System.out.println("\nOptions: \n1) Mine & Send Block \n2) Show Peers\n3) Print Blockchain");
            String choice = sc.nextLine();

            if (choice.equals("1")) {
                Block newBlock = new Block(new ArrayList<>(), blockchain.lastBlock());
                broadcastBlock(newBlock);
            } else if (choice.equals("2")) {
                System.out.println("Connected peers: " + peerPorts);
            } else if (choice.equals("3")) {
                blockchain.printChain();
            }
        }
    }

    private static void discoverPeers(int startPort, int endPort) {
        for (int p = startPort; p <= endPort; p++) {
            sendMessage(p, "HELLO:" + myPort);
        }
    }

    private static void sendMessage(int port, Object msg) {
        try (Socket s = new Socket("localhost", port);
             ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream())) {
            out.writeObject(msg);
            out.flush();
        } catch (IOException ignored) {}
    }

    private static void broadcastBlock(Block block) {
        for (int port : peerPorts) {
            sendMessage(port, block);
            System.out.println("Sent block to " + port);
        }
    }
}