import java.io.IOException;
import java.net.*;
import java.util.BitSet;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;

public class DnsClient {
	private static int timeoutTime = 5;
	private static int maxRetries = 3;
	private static int udpPort = 53;
	private static int queryTypeIndex=0;
	private static String[] queryType= {"00000001","00000010","00001111"};
	
	private static String givenServer = "";
	private static String givenName = "";
	private static String givenPort = "";
	private static String givenMaxRetries = "";
	private static String givenTimeout = "";
	private static String givenQueryType = "A";

	private static Integer numberRetries = 0;
	private static long startTime = 0;
	private static long stopTime = 0;
	private static double responseTime = 0;
	private static int answerIndex=0;
	
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
		//arg indices not guaranteed. Just scan through array and see what's what.
		
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
					givenServer = args[i];
					if(i+1 == args.length) {
						printError("Missing URL");
						return;
					}else {
						ip = parseIP(args[i]);
						if(ip == null) {
							printError("IP address cannot include non-numbers");
							return;
						}
						i++;
						givenName = args[i];
						urlStringArr = args[i].split("[.]");
					}
					
				}else if(args[i].contentEquals("-t")) {
					i++;
					try {
						setTimeout(Integer.parseInt(args[i]));						
					}catch (NumberFormatException e) {
						printError("Incorrect input syntax. Parameter -t requires an integer after it to specify the timeout limit. Example: -t 3");
						return;
					}
					givenTimeout = args[i];
				}else if(args[i].contentEquals("-r")) {
					i++;
					try {
						setMaxRetries(Integer.parseInt(args[i]));							
					}catch (NumberFormatException e) {
						printError("Incorrect input syntax. Parameter -r requires an integer after it to specify the desired number of connection attempts. Example: -r 5");						
						return;
					}
					givenMaxRetries = args[i];
				}else if(args[i].contentEquals("-p")) {
					i++;
					try {
						setPort(Integer.parseInt(args[i]));							
					}catch (NumberFormatException e) {
						printError("Incorrect input syntax. Parameter -p requires an integer after it to specify the desired port. Example: -p 53");						
						return;
					}
				}else if(args[i].contentEquals("-mx") && mx_ns_flag) {
					mx_ns_flag=false;
					setQueryTypeIndex(2);
					givenQueryType = "MX";
				}else if(args[i].contentEquals("-ns") && mx_ns_flag) {
					mx_ns_flag=false;
					setQueryTypeIndex(1);
					givenQueryType = "NS";
				}else if((args[i].contentEquals("-ns")||args[i].contentEquals("-mx")) && !mx_ns_flag) {
					printError("Only either -ns or -mx flag or neither is allowed. Parsing first flag.");
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
			
			byteIndex++;//format QType
			sendData[byteIndex]=BitOperators.convertBinaryStringToByte("00000000");
			byteIndex++;
			sendData[byteIndex]= BitOperators.convertBinaryStringToByte(getQueryType(getQueryTypeIndex()));
			
			byteIndex++;//now format QClass
			sendData[byteIndex]=BitOperators.convertBinaryStringToByte("00000000");
			byteIndex++;
			sendData[byteIndex]=BitOperators.convertBinaryStringToByte("00000001");
			setAnswerIndex(byteIndex+1);
			
			byte[] data = new byte[byteIndex+1];
			for(int i=0;i<data.length;i++) {
				data[i] = sendData[i];
			}
			
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(getTimeout()*1000);
			
			DatagramPacket sendPacket = new DatagramPacket(data, data.length, addr, getPort());
			startTime = System.currentTimeMillis();
			
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			printQuerySummary(givenName, givenServer, givenQueryType);
			
			boolean timeout=true;
			for(int i=0;i<getMaxRetries();i++) {
				clientSocket.send(sendPacket);
				try {
					clientSocket.receive(receivePacket);
				}catch (IOException e){
					
				}
				
				if(receivePacket.getLength()==1024) {//length hasn't changed, we timed out.
					numberRetries++;
					System.out.println("Query timeout. Retrying.");
				}else {
					timeout=false;
					stopTime = System.currentTimeMillis();
					responseTime = ( (double)stopTime - (double)startTime)/1000.0;
					break;
				}
			}
			if(timeout) {
				System.out.println("ERROR; failed to contact server after "+getMaxRetries()+" attempts.");
				clientSocket.close();
				return;
			}
						
			//STDOUT: Print valid response
			printValidResponse(Double.toString(responseTime), Integer.toString(numberRetries));
			
			byte[] rawReceivedData = receivePacket.getData();

			byte[] receivedData = new byte[receivePacket.getLength()];
			for(int i=0;i<receivePacket.getLength();i++) {
				receivedData[i] = rawReceivedData[i];
			}
			clientSocket.close();
			
			printHex(receivedData);
			System.out.println("--------------------");
			printAnswerResponse(receivedData);
			if(getQueryTypeIndex()==0) {
				printAnswerWithARecord(receivedData);
			}
			
			System.out.println(BitOperators.getAnswerName(rawReceivedData, getAnswerIndex()));
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		
	}
	
	private static void printHex(byte[] data) {
		//A method I built to help another method I yoinked from stackoverflow.
		//Will print data from a byte array in hex, printing it in hextets.
		String hexDump = BitOperators.bytesToHex(data);
		int arrayIndex=0;
		for(int i=0;i<hexDump.length();i+=4) {
			if(i+4 > hexDump.length()) {
				System.out.println("["+arrayIndex+"]"+hexDump.substring(i, hexDump.length()));
			}else {
				System.out.print("["+arrayIndex+"]"+hexDump.substring(i, i+2));
				arrayIndex++;
				System.out.println("["+arrayIndex+"]"+hexDump.substring(i+2, i+4));
			}
			arrayIndex++;
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
		String[] indIP = builder.toString().split("[.]");
		if(indIP.length!=4) {
			printError("Incorrect IP address format.");
		}
		
		//We now have a string array, but we need an array of ints to get our byte array.
		int[] addr = new int[4];
		for(int i=0;i<indIP.length;i++) {
			try {
				addr[i]=Integer.parseInt(indIP[i]);
			}catch (NumberFormatException e){
				return null;
			}
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
	
	private static int getUnsignedInt(byte thing) {
		return (int) thing & 0xFF;
	}
	
	private static void setAnswerIndex(int t) {
		answerIndex = t;
	}
	
	private static int getAnswerIndex() {
		return answerIndex;
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
	
	private static void printQuerySummary(String name, String ipaddress, String type){
		System.out.println("DnsClient sending request for " + name);
		System.out.println("Server: " + ipaddress);
		System.out.println("Request type: " + type);
	}
	
	private static void printValidResponse(String time, String numretries){
		System.out.println("Response received after " + time + " seconds (" + numretries + " retries)");
	}
	
	private static void printAnswerResponse(byte[] data) {
		int answers = 0;
		answers += (int) (getUnsignedInt(data[6]))*16;
		answers += (int) (getUnsignedInt(data[7]));
		if(answers>0) {
			System.out.println("***Answer Section (" + answers + " records)***" );			
		}
	}
	
	private static void printAnswerWithARecord(byte[] data) {
		//String ipaddress, String secondsCanCache, String auth
		
		String auth="";
		String temp = BitOperators.convertByteToBinaryString(data[2]);
		if(temp.charAt(5)=='1') {//if authority
			auth = "auth";
		}else {
			auth = "nonauth";
		}//authority established
			
			
		int ttlIndex = getAnswerIndex()+6;//next 4 bytes are ttl
		int secondsCanCache = 0;//-i*2
		for(int i=0;i<4;i++) {//convert 32 bits to unsigned int
			secondsCanCache += (int) (getUnsignedInt(data[ttlIndex+i])*Math.pow(16, 6-i*2));
		}
		
		int ipLengthIndex = getAnswerIndex()+10;
		int dataLength = 0;
		dataLength += (int) (getUnsignedInt(data[ipLengthIndex]))*16;
		dataLength += (int) (getUnsignedInt(data[ipLengthIndex+1]));
		String ip = "";
		for(int i=0;i<dataLength;i++) {
			String tmp="";
			if(i==dataLength-1) {
				tmp = ""+getUnsignedInt(data[ipLengthIndex+2+i]);
			}else {
				tmp = ""+getUnsignedInt(data[ipLengthIndex+2+i])+".";				
			}
			ip=ip.concat(tmp);
		}
		System.out.println("IP" + "\t" + ip.toString() + "\t" + secondsCanCache + "\t" + auth);	
		
	}

	private static void printAnswerWithCNAMERecord(String alias, String secondsCanCache, String auth) {
		System.out.println("CNAME" + "\t" + alias + "\t" + secondsCanCache + "\t" + auth);
	}
	
	private static void printAnswerWithMXRecord(String alias, String pref, String secondsCanCache, String auth) {
		System.out.println("MX" + "\t" + alias + "\t" + pref + "\t" + secondsCanCache + "\t" + auth);
	}
	
	private static void printAnswerWithNSRecord(String alias, String secondsCanCache, String auth) {
		System.out.println("NS" + "\t" + alias + "\t" + secondsCanCache + "\t" + auth);
	}
	
	private static void printAdditionalResponse(byte[] data) {
		//examining what's in ARCOUNT: indices 10 and 11
		int numadditional=0;
		numadditional += getUnsignedInt(data[10])*16;
		numadditional += getUnsignedInt(data[11]);
		if(numadditional>0) {
			System.out.println("***Additional Section (" + numadditional + " records)***" );			
		}else {
			printNotFound();
		}
	}
	
	private static void printNotFound() {
		System.out.println("NOTFOUND");
	}
	
	private static void printError(String errorDescription) {
		System.out.println("Error" + "\t" + errorDescription);
	}
	
}
