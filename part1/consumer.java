import java.io.*;
import java.net.*;
import java.util.Scanner;

public class consumer {
	
	
	public static String bus;
	public static String bus_route;
	
	public static String[] busLines = {"1151", "821", "750", "817", "818", "974", "1113", "816", "804", "1219", "1220", "938", "831", "819", "1180", "868", "824", "825", "1069", "1077"};
    public static String[] busLinesCon = {"021", "022", "024", "025", "026", "027", "032", "035", "036", "036", "036", "040", "046", "049", "051", "054", "057", "060", "1", "10"};

	public static void main(String[] args) throws UnknownHostException, IOException {
		bus = args[0];
		
		
		for (int i = 0; i < busLinesCon.length; i++){
			if(bus.compareTo(busLinesCon[i]) == 0  ) {
				bus = busLines[i];
			    break;
			}
		}
		new consumer().startClient();
	}

	public void startClient() throws UnknownHostException, IOException {
		Socket requestSocket = null;

		requestSocket = new Socket("192.168.1.65", 3421);
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
                 
				socket.close();
				
				if (broker1_buses.contains(bus)) {
					socket = new Socket(broker1.substring(0, broker1.length()-4), Integer.parseInt(broker1.substring(broker1.length()-4)));
				} else if (broker2_buses.contains(bus)) {
					socket = new Socket(broker2.substring(0, broker2.length()-4), Integer.parseInt(broker2.substring(broker2.length()-4)));
				} else if (broker3_buses.contains(bus)) {
					socket = new Socket(broker3.substring(0, broker3.length()-4), Integer.parseInt(broker3.substring(broker3.length()-4)));
				}
                
				out = new PrintStream(socket.getOutputStream());
				in = new Scanner(socket.getInputStream());
                
				Scanner in2 = new Scanner(System.in);
				
				out.println(1);
				out.flush();
				
				out.println(bus);
				out.flush();
				
				String rout_number = in.nextLine();
				
				
				System.out.println("There are "+ rout_number + "routes, give the the number of the route you are interested for.");
				
				
				
				out.println(in2.nextLine());
				
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
