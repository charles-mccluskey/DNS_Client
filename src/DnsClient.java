import java.net.*;
import java.util.BitSet;
import java.util.Random;
import java.nio.ByteBuffer;

public class DnsClient {
	private int timeoutTime = 5;
	private int maxRetries = 3;
	private int udpPort = 53;

	public static void main(String[] args) {
		//Input argument indices
		//some may or may not be present, just need to scan the array for tokens -t, -r, -p, -mx/-nx, and the char @
		//args[0] = -t (OPTIONAL)
		//args[1] = timeout
		//args[2] = -r (OPTIONAL)
		//args[3] = max-retries
		//args[4] = -p (OPTIONAL)
		//args[5] = port
		//args[6] = -mx or -ns (OPTIONAL)
		//args[7] = @server (REQUIRED)
		//args[8] = name (REQUIRED)
		
		//ip address of www.mcgill.ca: 132.216.177.160
		/*DNS packet structure:
		 * HEADER
		 * QUESTION
		 * ANSWER
		 * AUTHORITY
		 * ADDITIONAL
		 */
		try {
			ByteBuffer test = ByteBuffer.allocate(4);
			test.putChar(0,'b');
			System.out.println(test.getChar(0));
			System.out.println(test.getShort(2));
			
			DatagramSocket clientSocket = new DatagramSocket();
			/*
			byte[] sendData = new byte[1024];
			for(int i=0;i<args.length;i++) {
				if(args[i].charAt(0)=='@') {
					;
				}
			}*/
		} catch (SocketException e) {
			e.printStackTrace();
		} 
		
	}
	
	private static byte[] randomID() {
		Random rng = new Random();
		byte[] id = new byte[2];
		rng.nextBytes(id);
		return id;
	}
	
	private void setTimeout(int t) {
		timeoutTime = t;
	}
	
	private int getTimeout() {
		return timeoutTime;
	}
	
	private void setMaxRetries(int r) {
		maxRetries = r;
	}
	
	private int getMaxRetries() {
		return maxRetries;
	}
	
	private void printQuerySummary(String name, String ipaddress, String type){
		System.out.println("DnsClient sending request for " + name);
		System.out.println("Server: " + ipaddress);
		System.out.println("Request type: " + type);
	}
	
	private void printValidResponse(String time, String numretries){
		System.out.println("Response received after " + time + " seconds (" + numretries + " retries)");
	}
	
	private void printAnswerResponse(String numanswers) {
		System.out.println("***Answer Section (" + numanswers + " records)***" );
	}
	
	private void printAnswerWithARecord(String ipaddress, String secondsCanCache, String auth) {
		System.out.println("IP" + "\t" + ipaddress + "\t" + secondsCanCache + "\t" + auth);
	}

	private void printAnswerWithCNAMERecord(String alias, String secondsCanCache, String auth) {
		System.out.println("CNAME" + "\t" + alias + "\t" + secondsCanCache + "\t" + auth);
	}
	
	private void printAnswerWithMXRecord(String alias, String pref, String secondsCanCache, String auth) {
		System.out.println("MX" + "\t" + alias + "\t" + pref + "\t" + secondsCanCache + "\t" + auth);
	}
	
	private void printAnswerWithNSRecord(String alias, String secondsCanCache, String auth) {
		System.out.println("NS" + "\t" + alias + "\t" + secondsCanCache + "\t" + auth);
	}
	
	private void printAdditionalResponse(String numadditional) {
		System.out.println("***Additional Section (" + numadditional + " records)***" );
	}
	
	private void printNotFound() {
		System.out.println("NOTFOUND");
	}
	
	private void printError(String errorDescription) {
		System.out.println("Error" + "\t" + errorDescription);
	}
	
}
