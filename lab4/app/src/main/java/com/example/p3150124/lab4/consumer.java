package com.example.p3150124.lab4;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class consumer {

    private static String IP = "192.168.1.22";
    private static String path2 = "C:\\Users\\paras\\AndroidStudioProjects\\DS-Part-B\\lab4\\app\\src\\main\\java\\com\\example\\p3150124\\lab4\\busLinesNew.txt";
    private static String bus;
    private static String[] busLines;
    private static String[] busLinesCon;
    private static int size;

    static {
        try {
            size = countLinesNew(path2);
            busLines = new String[size];
            busLinesCon = new String[size];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args) throws IOException {

        try {
            FileReader in = new FileReader(path2);
            BufferedReader br = new BufferedReader(in);

            String line;
            int i;
            i = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                busLines[i] = tokens[0];      // reading from first letter till first ','
                busLinesCon[i] = tokens[1];   // reading from second ',' till third ','
                i++;
            }
            in.close();

        } catch (IOException e) {
            System.out.println("File Read Error");
        }
        bus = args;
        for (int i = 0; i < busLinesCon.length; i++) {
            if (bus.compareTo(busLinesCon[i]) == 0) {
                bus = busLines[i];
                break;
            }
        }
        new consumer().startClient();
    }

    public static int countLinesNew(String filename) throws IOException {
        return countLines(filename);
    }

    static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];

            int readChars = is.read(c);
            if (readChars == -1) {
                // bail out if nothing to read
                return 0;
            }

            // make it easy for the optimizer to tune this loop
            int count = 0;
            while (readChars == 1024) {
                for (int i = 0; i < 1024; ) {
                    if (c[i++] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            // count remaining characters
            while (readChars != -1) {
                // System.out.println(readChars);
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            return count == 0 ? 1 : count;
        } finally {
            is.close();
        }
    }

    public void startClient() throws IOException {
        Socket requestSocket = null;

        requestSocket = new Socket(IP, 3421);
        new myThread(requestSocket).start();
    }

    private class myThread extends Thread {
        Socket socket;

        public myThread(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            PrintStream out = null;
            Scanner in = null;

            try {
                out = new PrintStream(socket.getOutputStream());
                in = new Scanner(socket.getInputStream());

                String broker_buses = in.nextLine();
                String[] tokens = broker_buses.split("], ");

                String broker1_buses = tokens[0];
                String broker2_buses = tokens[1];
                String broker3_buses = tokens[2];

                String broker1 = broker1_buses.substring(1, broker1_buses.indexOf("="));
                broker1_buses = broker1_buses.substring(broker1_buses.indexOf("="));

                String broker2 = broker2_buses.substring(0, broker2_buses.indexOf("="));
                broker2_buses = broker2_buses.substring(broker2_buses.indexOf("="));

                String broker3 = broker3_buses.substring(0, broker3_buses.indexOf("="));
                broker3_buses = broker3_buses.substring(broker3_buses.indexOf("="));

                socket.close();

                if (broker1_buses.contains(bus)) {
                    socket = new Socket(broker1.substring(0, broker1.length() - 4), Integer.parseInt(broker1.substring(broker1.length() - 4)));
                } else if (broker2_buses.contains(bus)) {
                    socket = new Socket(broker2.substring(0, broker2.length() - 4), Integer.parseInt(broker2.substring(broker2.length() - 4)));
                } else if (broker3_buses.contains(bus)) {
                    socket = new Socket(broker3.substring(0, broker3.length() - 4), Integer.parseInt(broker3.substring(broker3.length() - 4)));
                }

                out = new PrintStream(socket.getOutputStream());
                in = new Scanner(socket.getInputStream());

                out.println(bus);

                in.nextLine();

                do {
                    System.out.println(in.nextLine());
                } while (in.nextLine().compareTo("stop") != 0);

            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                in.close();
                out.close();
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}