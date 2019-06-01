import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.Paths;

public class publisher {

	public static ArrayList<String> busIDs;
	public String path = Paths.get("busPositionsNew.txt").toAbsolutePath().toString();
	public static String[] busLines = {"1151", "821", "750", "817", "818", "974", "1113", "816", "804", "1219", "1220", "938", "831", "819", "1180", "868", "824", "825", "1069", "1077"};
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		busIDs = new ArrayList<String>();
		if (args[0].compareTo("1") == 0) {
			for (int i = 0; i < 10; i++) {
				busIDs.add(busLines[i]);
			}
		} else if (args[0].compareTo("2") == 0) {
			for (int i = 10; i < busLines.length; i++) {
				busIDs.add(busLines[i]);
			}
		}
		new publisher().startPublisher();
	}
	
	public void startPublisher() throws IOException {
		Socket requestSocket = null;
		
		requestSocket = new Socket("192.168.1.2", 3421);
		
		PrintStream out = null;
		Scanner in = null;
		
		try {
			out = new PrintStream(requestSocket.getOutputStream());
			in = new Scanner(requestSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		out.println(-1);
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
		
		Socket socket = null;
		
		for (int i = 0; i < busIDs.size(); i++) {
			try {
				if (broker1_buses.contains(busIDs.get(i))) {
					socket = new Socket(broker1.substring(0, broker1.length()-4), Integer.parseInt(broker1.substring(broker1.length()-4)));
				} else if (broker2_buses.contains(busIDs.get(i))) {
					socket = new Socket(broker2.substring(0, broker2.length()-4), Integer.parseInt(broker2.substring(broker2.length()-4)));
				} else if (broker3_buses.contains(busIDs.get(i))) {
					socket = new Socket(broker3.substring(0, broker3.length()-4), Integer.parseInt(broker3.substring(broker3.length()-4)));
				}
				
				out = new PrintStream(socket.getOutputStream());
				in = new Scanner(socket.getInputStream());
				
				out.println(0);
				
				new myThread(requestSocket, busIDs.get(i), out, in).start();
				
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
		} 
		in.close();
		out.close();
		socket.close();
	}
	
	private class myThread extends Thread {
		Socket socket;
		String bus;
		PrintStream out;
		Scanner in;
		
		public myThread(Socket socket, String bus, PrintStream out, Scanner in) {
			this.socket = socket;
			this.bus = bus;
			this.out = out;
			this.in = in;
		}
		
		public void run() {
			try{				
				FileReader in2 = new FileReader(path);
				BufferedReader br = new BufferedReader(in2);

				String line;
				String topic;
				    
				while ((line = br.readLine()) != null) {
					String[] t = line.split(",");
					if (t[0].compareTo(bus) == 0) {
						topic = bus + " " + t[3]+ " " + t[4];
						out.println(topic);
						out.flush();
						sleep(500);
					}
				}
				in2.close();
			} catch (IOException e) {
				System.out.println("File Read Error");
			} catch (InterruptedException e) {
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