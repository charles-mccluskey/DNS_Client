import java.util.Random;

public class BitOperators {

	public static String convertByteToBinaryString(byte b) {
		String s = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
		return s;
	}
	
	public static byte convertBinaryStringToByte(String s) {
		byte b =  Byte.parseByte(s, 2);
		return b;
	}
	
	public static byte[] setHeaderID(byte[] data) {
		Random rng = new Random();  //Produce random number generator
		byte[] id = new byte[2];  //id has size 2 bytes
		rng.nextBytes(id); //Apply random number to each byte in id
		
		//Set the id to the header
		data[0] = id[0];
		data[1] = id[1];
		
		return data;
	}
	
	public static byte[] setQR(byte[] data, boolean isQuery) {
		String s1 = convertByteToBinaryString(data[2]); //Get the binary string of data[2]
		String s2 = ""; //initialize empty String
				
		//Check if data is a query (0) or a response (1)
		if (isQuery) {
			s2 = '0'+ s1.substring(1,7);
		}
		else {
			s2 = '1'+ s1.substring(1,7);
		}
		
		//Convert new binary string back to a byte, plug back into data[2]
		data[2] = convertBinaryStringToByte(s2);
		
		return data;
	}
	
	public static byte[] setOpCode(byte[] data, String OpCode) {
		OpCode = OpCode.trim();
		//// make sure OpCode is at least 4 characters composed of 0's and 1's ////
		
		String s1 = convertByteToBinaryString(data[2]); //Get the binary string of data[2]
		String s2 = s1.charAt(0) + OpCode.substring(0, 3) + s1.substring(5, 7);
		data[2] = convertBinaryStringToByte(s2);

		return data;
	}
	
	public static byte[] setAA(byte[] data, boolean isAuthority) {
		String s1 = convertByteToBinaryString(data[2]); //Get the binary string of data[2]
		String s2 = ""; //initialize empty String
				
		//Check if name server is an authority (1) or not (0)
		if (isAuthority) {
			s2 = s1.substring(0,4) + '1' + s1.substring(6,7);
		}
		else {
			s2 = s1.substring(0,4) + '0' + s1.substring(6,7);
		}
		
		//Convert new binary string back to a byte, plug back into data[2]
		data[2] = convertBinaryStringToByte(s2);
		
		return data;
	}
	
	public static byte[] setTC(byte[] data, boolean isTruncated) {
		String s1 = convertByteToBinaryString(data[2]); //Get the binary string of data[2]
		String s2 = ""; //initialize empty String
				
		//Check if data is truncated (1) or not (0)
		if (isTruncated) {
			s2 = s1.substring(0,5) + '1' + s1.charAt(7);
		}
		else {
			s2 = s1.substring(0,5) + '0' + s1.charAt(7);
		}
		
		//Convert new binary string back to a byte, plug back into data[2]
		data[2] = convertBinaryStringToByte(s2);
		
		return data;
	}
	
	public static byte[] setRD(byte[] data, boolean isRecursive) {
		String s1 = convertByteToBinaryString(data[2]); //Get the binary string of data[2]
		String s2 = ""; //initialize empty String
				
		//Check if data is truncated (1) or not (0)
		if (isRecursive) {
			s2 = s1.substring(0,6) + '1';
		}
		else {
			s2 = s1.substring(0,6) + '0';
		}
		
		//Convert new binary string back to a byte, plug back into data[2]
		data[2] = convertBinaryStringToByte(s2);
		
		return data;
	}
	
	public static byte[] setRA(byte[] data, boolean isRA) {
		String s1 = convertByteToBinaryString(data[3]); //Get the binary string of data[3]
		String s2 = ""; //initialize empty String
				
		//Check if data is truncated (1) or not (0)
		if (isRA) {
			s2 = '1' + s1.substring(0,7);
		}
		else {
			s2 = '0' + s1.substring(0,7);
		}
		
		//Convert new binary string back to a byte, plug back into data[3]
		data[3] = convertBinaryStringToByte(s2);
		
		return data;
	}
	
	public static byte[] setZ(byte[] data, String Z) {
		Z = Z.trim();
		//// make sure Z is at least 3 characters composed of 0's and 1's ////
		
		String s1 = convertByteToBinaryString(data[3]); //Get the binary string of data[3]
		String s2 = s1.charAt(0) + Z.substring(0, 2) + s1.substring(4, 7);
		data[3] = convertBinaryStringToByte(s2);

		return data;
	}
	
	public static byte[] setRCode(byte[] data, String RCode) {
		RCode = RCode.trim();
		//// make sure Z is at least 4 characters composed of 0's and 1's ////
		
		String s1 = convertByteToBinaryString(data[3]); //Get the binary string of data[3]
		String s2 = s1.substring(0,3) + RCode.substring(0, 3);
		data[3] = convertBinaryStringToByte(s2);

		return data;
	}
}
