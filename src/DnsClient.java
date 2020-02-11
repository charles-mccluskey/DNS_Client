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
			DatagramSocket clientSocket = new DatagramSocket();
			
			byte[] sendData = new byte[1024];
			byte[] ip = new byte[4];
			for(int i=0;i<args.length;i++) {
				if(args[i].charAt(0)=='@') {//we have the IP address
					System.out.println("we have the IP address");
					ip = parseIP(args[i]);
				}
			}
			for(int i=0;i<ip.length;i++) {
				System.out.println("ip out: "+ip[i]);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} 
		
	}
	
		private static byte[] parseIP(String rawIP) {
		StringBuilder builder = new StringBuilder(rawIP);
		builder.deleteCharAt(0);//delete @ char
		
		//SPLIT ISN'T WORKING >:L
		//gonna have to make my own tokenizer
		String[] indIP = new String[4];
		int index=0;
		while(builder.length()!=0) {
			String token = "";
			if(index !=3) {
				int i = builder.indexOf(".");
				token = builder.substring(0, i);
				builder.delete(0, i+1);
		
			}else {
				token = builder.toString();
				builder.delete(0, builder.length());
			}
			indIP[index] = token;//.toString();
			index++;
		}
		
		//We now have a string array, but we need an array of ints to get our byte array.
		int[] addr = new int[4];
		for(int i=0;i<indIP.length;i++) {
			addr[i]=Integer.parseInt(indIP[i]);
		}
		
		byte[] returnArr = new byte[4];
		for(int i=0;i<4;i++) {
			System.out.println("Addr at "+i+":"+addr[i]);
			returnArr[i]=(byte) addr[i];
		}
		
		return returnArr;
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
