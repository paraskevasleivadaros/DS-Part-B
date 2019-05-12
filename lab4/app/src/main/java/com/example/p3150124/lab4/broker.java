import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Scanner;

public class broker {

    public static String[] busLines;
    private static Hashtable<String, ArrayList<String>> br_bus;
    private static Hashtable<String, String> match;
    private static String path = Paths.get("brokers.txt").toAbsolutePath().toString();
    private static String path2 = Paths.get("busLinesNew.txt").toAbsolutePath().toString();
    private static String IP = "192.168.1.140";
    private static String port;

    static {
        try {
            int size = countLinesNew(path2);
            busLines = new String[size];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        port = args[0];
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
        ArrayList br_hash = new ArrayList();
        try {
            br_hash = hashIPandPort();

            ArrayList br1_bus = new ArrayList();
            ArrayList br2_bus = new ArrayList();
            ArrayList br3_bus = new ArrayList();

            for (String busLine : busLines) {
                if (SHA1(busLine).compareTo((String) br_hash.get(0)) == -1) {
                    br1_bus.add(busLine);
                } else if (SHA1(busLine).compareTo((String) br_hash.get(1)) == -1) {
                    br2_bus.add(busLine);
                } else if (SHA1(busLine).compareTo((String) br_hash.get(2)) == -1) {
                    br3_bus.add(busLine);
                } else {
                    br1_bus.add(busLine);
                }
            }

            br_bus = new Hashtable<String, ArrayList<String>>();

            br_bus.put(match.get(br_hash.get(0)), br1_bus);
            br_bus.put(match.get(br_hash.get(1)), br2_bus);
            br_bus.put(match.get(br_hash.get(2)), br3_bus);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }

        new broker().openServer();
    }

    public static ArrayList hashIPandPort() throws NoSuchAlgorithmException {
        try {
            FileReader in = new FileReader(path);
            BufferedReader br = new BufferedReader(in);
            ArrayList hashed = new ArrayList();
            match = new Hashtable<String, String>();

            String line;
            while ((line = br.readLine()) != null) {
                hashed.add(SHA1(line));
                match.put(SHA1(line), line);
            }
            in.close();
            Collections.sort(hashed);
            return hashed;
        } catch (IOException e) {
            System.out.println("File Read Error");
            return null;
        }
    }

    public static String SHA1(String s) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
        mDigest.update(s.getBytes(), 0, s.length());
        return new BigInteger(1, mDigest.digest()).toString();
    }

    public static int countLinesNew(String filename) throws IOException {
        return consumer.countLines(filename);
    }

    public void openServer() throws IOException {
        ServerSocket providerSocket = new ServerSocket(Integer.parseInt(port));
        Socket connection = null;

        while (true) {
            connection = providerSocket.accept();
            new myThread(connection).start();
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

            Socket requestSocket1 = null;
            Socket requestSocket2 = null;
            PrintStream p_out = null;
            Scanner p_in = null;
            PrintStream p1_out = null;
            Scanner p1_in = null;
            PrintStream p2_out = null;
            Scanner p2_in = null;

            try {
                out = new PrintStream(socket.getOutputStream());
                in = new Scanner(socket.getInputStream());

                out.println(br_bus.toString());
                out.flush();

                requestSocket1 = new Socket(IP, 1871);
                p1_out = new PrintStream(requestSocket1.getOutputStream());
                p1_in = new Scanner(requestSocket1.getInputStream());

                p1_out.println(br_bus.toString());
                p1_out.flush();

                requestSocket2 = new Socket(IP, 1917);
                p2_out = new PrintStream(requestSocket2.getOutputStream());
                p2_in = new Scanner(requestSocket2.getInputStream());

                p2_out.println(br_bus.toString());
                p2_out.flush();

                String sub_msg;
                String pub_msg;

                sub_msg = in.nextLine();

                p1_out.println(sub_msg);
                p1_out.flush();
                p2_out.println(sub_msg);
                p2_out.flush();

                if (p1_in.nextLine().compareTo(port) == 0) {
                    p_out = p1_out;
                    p_in = p1_in;
                } else if (p2_in.nextLine().compareTo(port) == 0) {
                    p_out = p2_out;
                    p_in = p2_in;
                }

                do {
                    pub_msg = p_in.nextLine();

                    out.println(pub_msg);
                    out.flush();
                } while (p_in.nextLine().compareTo("stop") != 0);

                out.println(pub_msg);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (this.socket != null) this.socket.close();
                if (p_in != null) p_in.close();
                if (p_out != null) p_out.close();
                if (p1_in != null) p1_in.close();
                if (p1_out != null) p1_out.close();
                if (p2_in != null) p2_in.close();
                if (p2_out != null) p2_out.close();
                if (requestSocket1 != null) requestSocket1.close();
                if (requestSocket2 != null) requestSocket2.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}