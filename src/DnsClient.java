import java.io.IOException;
import java.net.*;
import java.util.BitSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;

public class DnsClient {
	private static int timeoutTime = 5;
	private static int maxRetries = 3;
	private static int udpPort = 53;
	private static int queryTypeIndex=0;
	private static String[] queryType= {"00000001","00000010","00001111"};

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
			boolean mx_ns_flag = true;
			byte[] ip = new byte[4];
			byte[] sendData = new byte[1024];
			String[] urlStringArr= {};
			for(int i=0;i<args.length;i++) {//parse the args
				if(args[i].charAt(0)=='@') {//found the IP address
					if(i+1 == args.length) {
						System.out.println("Missing URL");
						return;
					}else {
						ip = parseIP(args[i]);
						i++;
						urlStringArr = args[i].split("[.]");
					}
					
				}else if(args[i].contentEquals("-t")) {
					i++;
					setTimeout(Integer.parseInt(args[i]));
				}else if(args[i].contentEquals("-r")) {
					i++;
					setMaxRetries(Integer.parseInt(args[i]));					
				}else if(args[i].contentEquals("-p")) {
					i++;
					setPort(Integer.parseInt(args[i]));
				}else if(args[i].contentEquals("-mx") && mx_ns_flag) {
					mx_ns_flag=false;
					setQueryTypeIndex(2);
				}else if(args[i].contentEquals("-ns") && mx_ns_flag) {
					mx_ns_flag=false;
					setQueryTypeIndex(1);
				}
			}
			sendData= initializeHeader(sendData);

			byte[] receiveData = new byte[1024];
			InetAddress addr = InetAddress.getByAddress(ip);
			//Starting at byte 12
			int byteIndex = 12;
			for(int i=0;i<urlStringArr.length;i++) {
				sendData[byteIndex] = (byte) urlStringArr[i].length();//plug in the word size
				byteIndex++;
				for(int j=0;j<urlStringArr[i].length();j++) {//plug each letter
					sendData[byteIndex] = (byte)urlStringArr[i].charAt(j);
					byteIndex++;
				}
			}
			//Enter null byte to terminate QName
			sendData[byteIndex]=BitOperators.convertBinaryStringToByte("00000000");
			
			//Either next byte or next even index byte. Current assumption: next byte
			/*if(byteIndex%2==0) {//increment the byte index to prep Qtype at next even index
				byteIndex++;
				sendData[byteIndex]=BitOperators.convertBinaryStringToByte("00000000");
			}*/
			byteIndex++;//format QType
			sendData[byteIndex]=BitOperators.convertBinaryStringToByte("00000000");
			byteIndex++;
			sendData[byteIndex]= BitOperators.convertBinaryStringToByte(getQueryType(getQueryTypeIndex()));
			
			byteIndex++;//now format QClass
			sendData[byteIndex]=BitOperators.convertBinaryStringToByte("00000000");
			byteIndex++;
			sendData[byteIndex]=BitOperators.convertBinaryStringToByte("00000001");
			
			byte[] data = new byte[byteIndex+1];
			for(int i=0;i<data.length;i++) {
				data[i] = sendData[i];
			}
			
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(getTimeout()*1000);
			
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, addr, getPort());
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			boolean timeout=true;
			for(int i=0;i<getMaxRetries();i++) {
				clientSocket.send(sendPacket);
				try {
					clientSocket.receive(receivePacket);
				}catch (IOException e){
					
				}
				
				if(receivePacket.getLength()==1024) {//length hasn't changed, we timed out.
					System.out.println("Query timeout. Retrying.");
				}else {
					timeout=false;
					break;
				}
			}
			if(timeout) {
				System.out.println("ERROR; failed to contact server after "+getMaxRetries()+" attempts.");
				clientSocket.close();
				return;
			}
			System.out.println("packet received");			
			byte[] rawReceivedData = receivePacket.getData();

			byte[] receivedData = new byte[receivePacket.getLength()];
			for(int i=0;i<receivePacket.getLength();i++) {
				receivedData[i] = rawReceivedData[i];
			}
			clientSocket.close();
			
			printHex(receivedData);
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	private static void printHex(byte[] data) {
		//A method I built to help another method I yoinked from stackoverflow.
		//Will print data from a byte array in hex, printing it in hextets.
		String hexDump = BitOperators.bytesToHex(data);
		
		for(int i=0;i<hexDump.length();i+=4) {
			if(i+4 > hexDump.length()) {
				System.out.println(hexDump.substring(i, hexDump.length()));
			}else {
				System.out.println(hexDump.substring(i, i+4));
			}

		}
	}
	
	private static byte[] initializeHeader(byte[] sendData) {
		sendData = BitOperators.setHeaderID(sendData);
		sendData = BitOperators.setQR(sendData, true);
		sendData = BitOperators.setOpCode(sendData, "0000");
		sendData = BitOperators.setAA(sendData, false);
		sendData = BitOperators.setTC(sendData, false);
		sendData = BitOperators.setRD(sendData, true);
		sendData = BitOperators.setRA(sendData, false);
		sendData = BitOperators.setZ(sendData, "000");//yes, 3 zeroes
		sendData = BitOperators.setRCode(sendData, "0000");
		sendData = BitOperators.initializeHeaderCounts(sendData);
		
		return sendData;
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
		for(int i =0;i<4;i++) {
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
	
	private static void setPort(int t) {
		udpPort = t;
	}
	
	private static int getPort() {
		return udpPort;
	}
	
	private static void setTimeout(int t) {
		timeoutTime = t;
	}
	
	private static int getTimeout() {
		return timeoutTime;
	}
	
	private static void setMaxRetries(int r) {
		maxRetries = r;
	}
	
	private static int getMaxRetries() {
		return maxRetries;
	}
	
	private static void setQueryTypeIndex(int i) {
		queryTypeIndex = i;
	}
	
	private static int getQueryTypeIndex() {
		return queryTypeIndex;
	}
	
	private static String getQueryType(int i) {
		return queryType[i];
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
