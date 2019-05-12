import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class publisher {

    private static ArrayList<String> busIDs;
    private static int port;
    private static String path2 = Paths.get("busLinesNew.txt").toAbsolutePath().toString();
    private static String[] busLines;

    static {
        try {
            int size = countLinesNew(path2);
            busLines = new String[size];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String path = Paths.get("busPositionsNew.txt").toAbsolutePath().toString();

    public static void main(String[] args) throws IOException {
        busIDs = new ArrayList<String>();
        try {
            FileReader in = new FileReader(path2);
            BufferedReader br = new BufferedReader(in);

            String line;
            int i;
            i = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                busLines[i] = tokens[0];  //reading from first letter till first ','
                i++;
            }
            in.close();

        } catch (IOException e) {
            System.out.println("File Read Error");
        }
        port = 1871;
        if (args[0].compareTo("1") == 0) {
            for (int i = 0; i < 10; i++) {
                busIDs.add(busLines[i]);
            }
        } else if (args[0].compareTo("2") == 0) {
            port = 1917;
            for (int i = 10; i < busLines.length; i++) {
                busIDs.add(busLines[i]);
            }
        }
        new publisher().startPublisher();
    }

    public static int countLinesNew(String filename) throws IOException {
        return consumer.countLines(filename);
    }

    public void startPublisher() throws IOException {
        ServerSocket publisherSocket = new ServerSocket(port);
        Socket requestSocket = null;

        while (true) {
            requestSocket = publisherSocket.accept();

            new myThread(requestSocket).start();
        }
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
            } catch (IOException e) {
                e.printStackTrace();
            }

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

            String broker_message = in.nextLine();

            if (busIDs.contains(broker_message)) {
                if (broker1_buses.contains(broker_message)) {
                    out.println(Integer.parseInt(broker1.substring(broker1.length() - 4)));
                } else if (broker2_buses.contains(broker_message)) {
                    out.println(Integer.parseInt(broker2.substring(broker2.length() - 4)));
                } else if (broker3_buses.contains(broker_message)) {
                    out.println(Integer.parseInt(broker3.substring(broker3.length() - 4)));
                }

                try {
                    FileReader in2 = new FileReader(path);
                    BufferedReader br = new BufferedReader(in2);

                    String line;

                    while ((line = br.readLine()) != null) {
                        String[] t = line.split(",");
                        if (t[0].compareTo(broker_message) == 0) {
                            out.println(t[3] + " " + t[4]);
                            out.flush();
                            sleep(500);
                        }
                    }
                    out.println("stop");
                    out.flush();
                    in2.close();
                } catch (IOException e) {
                    System.out.println("File Read Error");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                out.println("Not found!");
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