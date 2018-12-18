/*
 * Copyright (C) 2018 Juan J. Martínez
 * 
 * All rights reserved. This complete software or any portion thereof
 * can be used as reference but may not be reproduced in any manner 
 * whatsoever without the express written permission of the owner.
 * 
 * The purpose of this is to be consulted and used as a referece of 
 * functionallyty.
 * 
 * Developed in Mexico City
 * First version, 2018
 *
 */

/**
 *
 * @author Juan J. Martínez
 * @email juanjmtzs@gmail.com
 * @phone +52-1-55-1247-8044
 * @linkedin https://www.linkedin.com/in/juanjmtzs/
 *
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UDP {

    private static MulticastSocket socket;
    private static InetAddress group;
    private static boolean continua = true;
    public static String receiverIP = "";
    public static int myLastReceived = 0;
    public static String userRecovery, passwordRecovery, ipRecovery, portRecovery, groupRecovery = "";
    public static String argsRecoverySnapshot[] = new String[10];

    public static String argsRecoveryGap[] = new String[9];

    public static ArrayList<String[]> ListRetransmissions = new ArrayList<String[]>();

    public static boolean GapsChecking = false;

    public static void kill() {
        continua = false;

        if (socket.isConnected()) {
            try {
                socket.leaveGroup(group);
            } catch (IOException ex) {
                Logger.getLogger(UDP.class.getName()).log(Level.SEVERE, null, ex);
            }
            socket.disconnect();
            socket.close();
        }

    }

    public static void StartHandling(String[] args) throws UnknownHostException, IOException {
        try {
            myLastReceived = 0;
            userRecovery = args[5];
            passwordRecovery = args[6];
            ipRecovery = args[7];
            portRecovery = args[8];
            groupRecovery = args[9];

            argsRecoverySnapshot[0] = ipRecovery;
            argsRecoverySnapshot[1] = portRecovery;
            argsRecoverySnapshot[2] = "";
            argsRecoverySnapshot[3] = userRecovery;
            argsRecoverySnapshot[4] = passwordRecovery;
            argsRecoverySnapshot[5] = groupRecovery;
            argsRecoverySnapshot[6] = "";
            argsRecoverySnapshot[7] = "0";
            argsRecoverySnapshot[8] = "4";
            argsRecoverySnapshot[9] = "";

            argsRecoveryGap[0] = ipRecovery;
            argsRecoveryGap[1] = portRecovery;
            argsRecoveryGap[2] = "";
            argsRecoveryGap[3] = userRecovery;
            argsRecoveryGap[4] = passwordRecovery;
            argsRecoveryGap[5] = groupRecovery;
            argsRecoveryGap[6] = "";

            group = InetAddress.getByName(args[0]);
            final int port = Integer.parseInt(args[1]);

            String service = args[2];
            receiverIP = args[3];

            //String userHomeFolder = System.getProperty("user.home") + "\\LogsBMV";
            try {
                DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
                Date date = new Date();
                System.setOut(new PrintStream(new File(args[4], Instant.now().toEpochMilli() + "_BMVMulticast_" + dateFormat.format(date) + ".log")));
            } catch (FileNotFoundException ex) {

                System.out.println("[Error]{{\"Error\":{\"Description\":" + ex + "}}");

            }

            socket = new MulticastSocket(port);
            if ("".equals(receiverIP)) {
                socket.setInterface(InetAddress.getLocalHost());
            } else {
                socket.setInterface(InetAddress.getByName(receiverIP));
            }
            socket.joinGroup(group);
            socket.setSoTimeout(39600000);
            if (socket.isClosed() == true) {
                System.out.println("Disconected");
            } else {

                try {
                    continua = true;

                    DatagramPacket packet = new DatagramPacket(new byte[64000], 64000);
                    System.out.println("[{\"Service Name\":\"BMV Multicast\"}");
                    while (continua != false) {
                        socket.receive(packet);

                        int packetSize = packet.getLength();
                        byte len[];
                        byte grupo[];
                        byte session[];
                        byte sequence[];
                        byte messagesInPackage[];
                        byte timestamp[];
                        byte messages[];

                        len = Arrays.copyOfRange(packet.getData(), 0, 2);
                        messagesInPackage = Arrays.copyOfRange(packet.getData(), 2, 3);
                        grupo = Arrays.copyOfRange(packet.getData(), 3, 4);
                        session = Arrays.copyOfRange(packet.getData(), 4, 5);
                        sequence = Arrays.copyOfRange(packet.getData(), 5, 9);
                        timestamp = Arrays.copyOfRange(packet.getData(), 9, 17);
                        messages = Arrays.copyOfRange(packet.getData(), 17, packetSize);

                        BigInteger seqNum = new BigInteger(sequence);

                        int mip = new BigInteger(messagesInPackage).intValue();
                        byte messagehelper[] = messages;
                        if (mip != 0) {
                            if (myLastReceived + 1 < seqNum.intValue()) {
                                int gap = seqNum.intValue() - myLastReceived-1;
                                int request = myLastReceived + 1;
                                myLastReceived = request + gap;

                                argsRecoveryGap[7] = request + "";
                                argsRecoveryGap[8] = gap + "";

                                System.out.println("[RECOVERY]{{\"GAP\":{\"Quantity\":" + gap + ",\"First Sequence\":" + request + ",\"Final Sequence\":" + (seqNum.intValue() - 1) + "}}");

                                if (gap > 1 && gap < 20000) {

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                new UnicastBMVNoBytes().StartHandling(argsRecoveryGap);

                                            } catch (Exception ex) {
                                                System.out.println("[Error]{{\"Error\":{\"Description\":" + ex + "}}");
                                            }
                                        }

                                    }).start();

                                } else if (gap > 20000) {

//      in this case you can request an snapshot
                                }

                            }
                            String jsonSonstructor = "";
                            jsonSonstructor = jsonSonstructor + "{";
                            jsonSonstructor = jsonSonstructor + "\"Next SeqNum\":" + seqNum + ",";
                            jsonSonstructor = jsonSonstructor + "\"Message\":{";
                            jsonSonstructor = jsonSonstructor + "\"Name\":\"Timestamp\",";

                            jsonSonstructor = jsonSonstructor + "\"Type\":";
                            jsonSonstructor = jsonSonstructor + "\"T\"";
                            jsonSonstructor = jsonSonstructor + ",";
                            jsonSonstructor = jsonSonstructor + "\"Timestamp\":";
                            jsonSonstructor = jsonSonstructor + new BigInteger(timestamp);
                            jsonSonstructor = jsonSonstructor + "}}";
                            System.out.println("," + jsonSonstructor);

                            for (int x = 1; x <= mip; x++) {
                                int size = new BigInteger(Arrays.copyOfRange(messagehelper, 0, 2)).intValue();
                                byte messageToDecode[] = Arrays.copyOfRange(messagehelper, 2, size + 2);
                                ByteBuffer message = ByteBuffer.wrap(messageToDecode);

                                Potocol protocol = new DecoderINTRABMV();
                                String JSON = protocol.parse(message, Long.parseLong(seqNum + ""));
                                System.out.println("," + JSON);

                                myLastReceived = seqNum.intValue();
                                seqNum = seqNum.add(BigInteger.valueOf(1));
                                if (mip > 1) {

                                    messages = Arrays.copyOfRange(messagehelper, size + 2, messagehelper.length);
                                    messagehelper = messages;
                                } else {
                                    break;
                                }
                            }
                        } else {
                            if (myLastReceived < seqNum.intValue()) {
                                int gap = seqNum.intValue() - myLastReceived-1;
                                int request = myLastReceived + 1;
                                myLastReceived = request + gap;

                                argsRecoveryGap[7] = request + "";
                                argsRecoveryGap[8] = gap + "";

                                System.out.println("[RECOVERY]{{\"GAP\":{\"Quantity\":" + gap + ",\"First Sequence\":" + request + ",\"Final Sequence\":" + (seqNum.intValue()-1) + "}}");

                                if (gap > 1 && gap < 20000) {

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                UnicastBMVNoBytes.StartHandling(argsRecoveryGap);

                                            } catch (Exception ex) {
                                                System.out.println("[Error]{{\"Error\":{\"Description\":" + ex + "}}");
                                            }
                                        }

                                    }).start();

                                } else if (gap > 20000) {

//                                 In this case you can request an snapshot
                                }

                            }
                        }

                        packet = new DatagramPacket(new byte[64000], 64000);
                    }

                } catch (IOException | NumberFormatException e) {

                    if (e.getMessage().equals("Receive timed out")) {
                        System.out.println("]");
                        System.out.println("Time Out");

                    } else {

                        System.out.println("]");
                        System.out.println("While Connected: " + e);
                    }
                }

            }

        } catch (Exception e) {
            if (e.getMessage().equals("Receive timed out")) {
                System.out.println("]");
                System.out.println("Time Out");
            } else {
                System.out.println("]");
                System.out.println("While Connected: " + e);
            }

        }
    }

    public static void GapChecker() throws UnknownHostException, IOException, InterruptedException {

        String argsRecovery[];
        GapsChecking = true;

        while (!ListRetransmissions.isEmpty()) {
            argsRecovery = ListRetransmissions.get(0);
            ListRetransmissions.remove(0);
            try {

                UnicastBMVNoBytes.StartHandling(argsRecovery);
                Thread.sleep(1000);

            } catch (IOException ex) {
                System.out.println("[Error]{{\"Error\":{\"Description\":" + ex + "}}");
            }

        }
        GapsChecking = false;
    }
}
