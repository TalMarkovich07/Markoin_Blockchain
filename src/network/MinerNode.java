package network;

import logic.*;
import model.*;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class MinerNode {
    private int myPort;
    private BigInteger myPubKey;
    private Set<Peer> peers = new HashSet<>();
    private Chain blockchain = Chain.getInstance();
    private AtomicBoolean stopMiningFlag = new AtomicBoolean(false);

    public MinerNode(int port, BigInteger pubKey) {
        this.myPort = port;
        this.myPubKey = pubKey;
    }

    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(myPort)) {
                System.out.println("[Server Started on port " + myPort + "]");
                while (true) {
                    try {
                        Socket socket = serverSocket.accept();
                        handleConnection(socket);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void handleConnection(Socket socket) {
        new Thread(() -> {
            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.flush();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                if(socket.getLocalPort() != myPort) {
                    out.writeObject("HELLO:"+ myPort);
                    out.flush();
                }

                while (true) {
                    try {
                        Object received = in.readObject();
                        if (received instanceof String msg && msg.startsWith("GET_BALANCE:")) {
                            // send balance to user
                            String pubKeyHex = msg.split(":")[1];
                            BigInteger pubKey = new BigInteger(pubKeyHex, 16);
                            double balance = blockchain.getBalance(pubKey);
                            out.writeObject(balance);
                            out.flush();
                        }

                        if (received instanceof String msg) {
                            handleStringMessage(msg, socket, in, out);
                        } else if (received instanceof Block b) {
                            handleReceivedBlock(b);
                        } else if (received instanceof ArrayList<?> receivedBlocks) {
                            handleChainSync((ArrayList<Block>) receivedBlocks, out);
                        } else if (received instanceof Transaction tr) {
                            handleTransaction(tr);
                        }
                    } catch (EOFException e) {
                        break;
                    }
                }
            } catch (Exception e) {
                    // Connection closed
                }
        }).start();
    }

    private void handleTransaction(Transaction tr) {
        if (!tr.verifySignature()) {
            System.out.println("[Transaction Rejected] Invalid Signature!");
            return;
        }

        double totalAmount = tr.getAmount() + tr.getFee();
        if (blockchain.getBalance(tr.getSenderPublicKey()) < totalAmount) {
            System.out.println("[Transaction Rejected] Insufficient Funds! Required: " + totalAmount);
            return;
        }

        blockchain.getMempool().insert(tr);
        System.out.println("[Transaction Verified & Added to Mempool]");
        broadcast(tr);
    }

    private void handleStringMessage(String msg, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
        try {
            if (msg.startsWith("HELLO:")) {
                // Step 1 & 2: Process HELLO and send HELLO_BACK
                int senderPort = Integer.parseInt(msg.split(":")[1]);
                String senderIp = socket.getInetAddress().getHostAddress();
                Peer newPeer = new Peer(senderIp, senderPort);
                if(peers.add(newPeer)){
                    System.out.println("[INFO] added new peer: "+ newPeer);
                }
                else{
                    System.out.println("[ERROR] peer "+newPeer+" already exists!");
                }

                System.out.println("\n[INFO] Received HELLO from " + newPeer);
                System.out.println("[INFO] Sending HELLO_BACK to " + newPeer);
                out.writeObject("HELLO_BACK:" + myPort);
                out.flush();

            }
            else if (msg.startsWith("HELLO_BACK:")) {
                int senderPort = Integer.parseInt(msg.split(":")[1]);
                String senderIp = socket.getInetAddress().getHostAddress();
                Peer peer = new Peer(senderIp, senderPort);
                System.out.println("[INFO] Received HELLO_BACK from " + peer);
                if(peers.add(peer))
                    System.out.println("[INFO] Added peer: " + peer);
                else
                    System.out.println("[INFO] Failed to add peer: " + peer);

                // Send My Blockchain
                System.out.println("[INFO] Sending Blockchain to " + peer);
                out.reset();
                out.writeObject(blockchain.getBlockchain());
                out.flush();

            }
        } catch (Exception e) {
            System.out.println("[Error] Handshake logic error: " + e.getMessage());
        }
    }


    private void handleReceivedBlock(Block b) {
        System.out.println("\n[Block Received] Hash: " + b.getHash());
        if (blockchain.isValidBlock(b)) {
            blockchain.addBlock(b);
            stopMiningFlag.set(true);
            blockchain.removeFromMempool(b.getTransactions());
            System.out.println("[Block Added to Chain]");
        }
    }

    private void handleChainSync(ArrayList<Block> newChain, ObjectOutputStream out) {
        if (newChain.size() > blockchain.getBlockchain().size()) {
            blockchain.replaceChain(newChain);
            System.out.println("[INFO] Chain synced");
        }
        else if(newChain.size() == blockchain.getBlockchain().size()){
            if(newChain!=blockchain.getBlockchain())
                System.out.println("[INFO] Chain was correct");
            else
                System.out.println("[ERROR] Different chains, same size.");
        }
        else {
            //My chain is longer, sending my chain.
            try {
                out.reset(); // Ensures the updated list is sent, not a cached version
                out.writeObject(blockchain.getBlockchain());
                out.flush();
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    public void startMining() {
        stopMiningFlag.set(false);
        new Thread(() -> {

            // get transactions from mempool
            ArrayList<Transaction> transactionsToMine = new ArrayList<>();
            while (!blockchain.getMempool().isEmpty() && transactionsToMine.size() < Block.MAX_TRANSACTIONS_PER_BLOCK)
                transactionsToMine.add(blockchain.getMempool().extractMax());

            // add reward transaction
            Transaction rewardTx = new Transaction(null, this.myPubKey, Block.reward, 0.0);
            transactionsToMine.addFirst(rewardTx);


            System.out.println("[Mining Started...]");
            long startTime = System.currentTimeMillis();
            Block newBlock = Block.mineBlock(transactionsToMine, blockchain.lastBlock(), stopMiningFlag);

            if (newBlock != null) {
                long duration = System.currentTimeMillis() - startTime;
                System.out.println("\n[Block Mined Successfully!] Time: " + duration + "ms");
                blockchain.addBlock(newBlock);
                broadcast(newBlock);
            } else {
                for( Transaction t : transactionsToMine) // if t is not in the last block, re-add it to mempool
                    if(!blockchain.lastBlock().getData().contains(t))
                        blockchain.getMempool().insert(t);

                System.out.println("\n[Mining Interrupted] A block was received from the network.");
            }
            //startMining();
        }).start();
    }

    public void connectToPeer(String ip, int port) {
        try{
            Socket socket = new Socket(ip, port);
            handleConnection(socket);
        } catch (IOException e) {
            System.out.println("[ERROR] Could not connect to peer: " + e.getMessage());
        }
    }
    private void sendHello(Peer peer){
        try (Socket socket = new Socket(peer.getIp(), peer.getPort());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.flush();
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                // Step 1: Send HELLO
                out.writeObject("HELLO:" + myPort);
                out.flush();
            } catch (IOException e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }  catch (IOException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    public void broadcast(Object data) {
        for (Peer peer : peers) {
            try (Socket socket = new Socket(peer.getIp(), peer.getPort());
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(data);
                out.flush();
            } catch (IOException e) {
                System.out.println("Could not reach peer: " + peer);
            }
        }
    }

    public void printChain() {
        blockchain.printChain();
    }

    public void printPeers() {
        System.out.println("Connected peers: " + peers);
    }
}