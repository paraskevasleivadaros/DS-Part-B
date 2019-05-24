import java.io.*;
import java.math.*;
import java.net.*;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class broker {
	
	public static Hashtable <String, ArrayList<String>> br_bus;
	public static Hashtable <String, String> match;
	public static String IP = "192.168.1.140";
	public static String path = Paths.get("brokers.txt").toAbsolutePath().toString();
	public static String port;
	public static String[] busLines = {"1151", "821", "750", "817", "818", "974", "1113", "816", "804", "1219", "1220", "938", "831", "819", "1180", "868", "824", "825", "1069", "1077"};
	public String topic = " ";
	public String value = " ";
	public ArrayList<Socket> consumers = new ArrayList<Socket>();
	public ArrayList<String> consumers_bus = new ArrayList<String>();
	
	public static void main(String[] args) throws IOException {
		port = args[0];
		
		ArrayList br_hash = new ArrayList();
		try {
			br_hash = hashIPandPort();
			
			ArrayList br1_bus = new ArrayList();
			ArrayList br2_bus = new ArrayList();
			ArrayList br3_bus = new ArrayList();
			
			for (int i = 0; i < busLines.length; i++) {
				if (SHA1(busLines[i]).compareTo((String)br_hash.get(0)) == -1) {
					br1_bus.add(busLines[i]);
				} else if (SHA1(busLines[i]).compareTo((String)br_hash.get(1)) == -1) {
					br2_bus.add(busLines[i]);
				} else if (SHA1(busLines[i]).compareTo((String)br_hash.get(2)) == -1) {
					br3_bus.add(busLines[i]);
				} else {
					br1_bus.add(busLines[i]);
				}
			}
			
			br_bus = new Hashtable <String, ArrayList<String>>();

			br_bus.put(match.get(br_hash.get(0)), br1_bus);
			br_bus.put(match.get(br_hash.get(1)), br2_bus);
			br_bus.put(match.get(br_hash.get(2)), br3_bus);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		
		new broker().openServer();
	}
	
	public void openServer() throws IOException {
		ServerSocket providerSocket = new ServerSocket(Integer.parseInt(port));
		Socket connection = null;
		
		PrintStream out = null;
		Scanner in = null;
		
		while(true) {
			connection = providerSocket.accept();
			out = new PrintStream(connection.getOutputStream());
			in = new Scanner(connection.getInputStream());
			
			String type = in.nextLine();
//			System.out.println(type);
			if (Integer.parseInt(type) == -1) new publisherThread(connection).start();
			if (Integer.parseInt(type) == 0) new busThread(connection).start();
			if (Integer.parseInt(type) == 1) new consumerThread(connection).start();
		}
	}
	
	private class publisherThread extends Thread {
		Socket socket;
		
		public publisherThread(Socket socket) {
			this.socket = socket;
		}
		
		public void run() {
			PrintStream out = null;
			Scanner in = null;

			try {
				out = new PrintStream(socket.getOutputStream());
				in = new Scanner(socket.getInputStream());
				
				out.println(br_bus.toString());
				out.flush();
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
	
	private class busThread extends Thread {
		Socket socket;
		
		public busThread(Socket socket) {
			this.socket = socket;
		}
		
		public void run() {
			PrintStream out = null;
			Scanner in = null;

			PrintStream c_out = null;
			Scanner c_in = null;
			try {
				out = new PrintStream(socket.getOutputStream());
				in = new Scanner(socket.getInputStream());

				String bus;
				String latLong;
				
				do {
					topic = in.nextLine();
//					System.out.println(topic);
					bus = topic.substring(0, topic.indexOf(" "));
					latLong = topic.substring(topic.indexOf(" ")+1, topic.length());
					//System.out.println(consumers.size());
					for (int i = 0; i < consumers.size(); i++) {
						if (consumers_bus.get(i).compareTo(bus) == 0) {
//							System.out.println(i);
//							System.out.println(latLong);
							c_out = new PrintStream(consumers.get(i).getOutputStream());
							c_in = new Scanner(consumers.get(i).getInputStream());
							c_out.println(latLong);
							c_out.flush();
						}
					}
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
	
	private class consumerThread extends Thread {
		Socket socket;
		
		public consumerThread(Socket socket) {
			this.socket = socket;
		}
		
		public synchronized void run() {
			PrintStream out = null;
			Scanner in = null;
			
			try {
				out = new PrintStream(socket.getOutputStream());
				in = new Scanner(socket.getInputStream());
				
				//out.println(br_bus.toString());
				//out.flush();
				
				String sub_msg;
				
				out.println("Searching...");
				out.flush();
				
				sub_msg = in.nextLine();
				
//				System.out.println(sub_msg);
//				System.out.println(socket);
				
				consumers.add(socket);
				consumers_bus.add(sub_msg);
				
//				System.out.println(consumers);
//				System.out.println(consumers_bus);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static ArrayList hashIPandPort() throws NoSuchAlgorithmException {
		try{ 
		    FileReader in = new FileReader(path);
		    BufferedReader br = new BufferedReader(in);
		    ArrayList hashed = new ArrayList();
		    match = new Hashtable <String, String>();

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
	
	public static String SHA1 (String s) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA-1");
		mDigest.update(s.getBytes(), 0, s.length());
		return new BigInteger(1, mDigest.digest()).toString();
	}
}