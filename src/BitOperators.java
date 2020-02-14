import java.util.Random;

/**
 * This class acts as a library of methods used to manipulate DNS packets.
 * @author Darien Muse-Charbonneau, 260791466
 * @author Charles McCluskey, 260688016
 */
public class BitOperators {

	/**
	 * Takes in a byte and converts it to an 8-digit binary string.
	 * @param b the byte to be converted
	 * @return the corresponding binary string
	 */
	public static String convertByteToBinaryString(byte b) {
		String s = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
		return s;
	}
	
	/**
	 * Takes in an 8-digit binary string and converts it to a byte.
	 * @param s the binary string to be converted
	 * @return the corresponding byte
	 */
	public static byte convertBinaryStringToByte(String s) {
		byte b =  Byte.parseByte(s, 2);
		return b;
	}
	
	/**
	 * Takes in a DNS packet and sets its ID field to a random ID.
	 * @param data the DNS data
	 * @return the updated DNS packet
	 */
	public static byte[] setHeaderID(byte[] data) {
		Random rng = new Random();  //Produce random number generator
		byte[] id = new byte[2];  //id has size 2 bytes
		rng.nextBytes(id); //Apply random number to each byte in id
		
		//Set the id to the header
		data[0] = id[0];
		data[1] = id[1];
		
		return data;
	}
	
	/**
	 * Takes in a DNS packet and sets its QR field.
	 * @param data the DNS data
	 * @param isQuery if true, QR set to '0'. if false, QR set to '1'.
	 * @return the updated DNS packet
	 */
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
	
	/**
	 * Takes in a DNS packet and sets its OpCode field
	 * @param data the DNS data
	 * @param OpCode a 4-bit binary string
	 * @return the updated DNS packet
	 */
	public static byte[] setOpCode(byte[] data, String OpCode) {
		OpCode = OpCode.trim();
		//// make sure OpCode is at least 4 characters composed of 0's and 1's ////
		
		String s1 = convertByteToBinaryString(data[2]); //Get the binary string of data[2]
		String s2 = s1.charAt(0) + OpCode.substring(0, 3) + s1.substring(5, 7);
		data[2] = convertBinaryStringToByte(s2);

		return data;
	}
	
	/**
	 * Takes in a DNS packet and sets its AA field
	 * @param data the DNS packet
	 * @param isAuthority if true, AA set to '1'. if false, AA set to '0'.
	 * @return the updated DNS packet
	 */
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
	
	/**
	 * Takes in a DNS packet and sets its TC field
	 * @param data the DNS packet
	 * @param isTruncated if true, TC set to '1'. if false, TC set to '0'.
	 * @return the updated DNS packet
	 */
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
	
	/**
	 * Takes in a DNS packet and sets its RD field
	 * @param data the DNS packet
	 * @param isRecursive if true, RD set to '1'. if false, TC set to '0'.
	 * @return the updated DNS packet
	 */
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
	
	/**
	 * Takes in a DNS packet and sets its RA field
	 * @param data the DNS packet
	 * @param isRA if true, RA set to '1'. if false, set to '0'.
	 * @return the updated DNS packet
	 */
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
	
	/**
	 * Takes in a DNS packet and sets its Z field
	 * @param data the DNS packet
	 * @param Z a 3-bit binary string
	 * @return the updated DNS packet
	 */
	public static byte[] setZ(byte[] data, String Z) {
		Z = Z.trim();
		//// make sure Z is at least 3 characters composed of 0's and 1's ////
		
		String s1 = convertByteToBinaryString(data[3]); //Get the binary string of data[3]
		String s2 = s1.charAt(0) + Z.substring(0, 2) + s1.substring(4, 7);
		data[3] = convertBinaryStringToByte(s2);

		return data;
	}
	
	/**
	 * Takes in a DNS packet and sets its RCode field
	 * @param data the DNS packet
	 * @param RCode a 4-bit binary string
	 * @return the updated DNS packet
	 */
	public static byte[] setRCode(byte[] data, String RCode) {
		RCode = RCode.trim();
		//// make sure Z is at least 4 characters composed of 0's and 1's ////
		
		String s1 = convertByteToBinaryString(data[3]); //Get the binary string of data[3]
		String s2 = s1.substring(0,3) + RCode.substring(0, 3);
		data[3] = convertBinaryStringToByte(s2);

		return data;
	}
	
	/**
	 * Takes in a DNS packet and initializes its header count fields
	 * @param data the DNS packet
	 * @return the updated DNS packet
	 */
	public static byte[] initializeHeaderCounts(byte[] data) {
		//index to set to 1 = 5
		//set 0s up to and including index 11
		String nul = "00000000";
		data[4] = convertBinaryStringToByte(nul);
		data[5] = convertBinaryStringToByte("00000001");
		for(int i=6;i<12;i++) {
			data[i] = convertBinaryStringToByte(nul);
		}

		return data;
	}
	
	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for (int j = 0; j < bytes.length; j++) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = HEX_ARRAY[v >>> 4];
	        hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	private static final Integer getAnswerTypeIndex(byte[] data, Integer answerIndex) {
		int i = answerIndex;
		while (!convertByteToBinaryString(data[i]).contentEquals("00000000") || !convertByteToBinaryString(data[answerIndex]).substring(0, 2).contentEquals("11")) {
			i++;
		}
		
		if (convertByteToBinaryString(data[answerIndex]).substring(0, 2).contentEquals("11")) {
			return i + 2;
		}
		
		return i + 1;
	} 
	
	public static final String getAnswerName(byte[] data, Integer answerIndex) {
		String name = "";
		//While zero flag or pointer has not been reached
		while( !convertByteToBinaryString(data[answerIndex]).contentEquals("00000000")) {
			Integer index = Integer.valueOf(convertByteToBinaryString(data[answerIndex]), 2);
			//System.out.println(convertByteToBinaryString(data[answerIndex]).substring(0, 2));
			if (!convertByteToBinaryString(data[answerIndex]).substring(0, 2).contentEquals("11")) {
				Integer limitIndex = answerIndex + index;
				answerIndex++;
				
				while(answerIndex <= limitIndex) {
					name += Character.toString((char) (data[answerIndex]));
					answerIndex++;
				}
				name += ".";
			}
			else {
				Integer offset = Integer.valueOf(convertByteToBinaryString(data[answerIndex]).substring(2, 8)+convertByteToBinaryString(data[answerIndex + 1]), 2);
				answerIndex = offset;
			}
		}
		
		return name.substring(0, name.length()-1);
	}
	
	public static String readRData(byte[] data, int ansIndex, boolean mx) {
		//ansIndex starts immediately after query data
		//rLength is located exactly 10 bytes further
		int dataLength = getUnsignedInt(data[ansIndex+10])<<8;
		dataLength += getUnsignedInt(data[ansIndex+11]);
		//dataLength now contains the length of rData.
		String rData="";
		int start = ansIndex+12;
		int boost=0;
		if(mx) {
			boost+=2;
		}
		//System.out.println("Bound: "+data.length);
		for(int i=start+boost;i<data.length-1;i++) {//iterate through the bytes of the rdata field
			byte sample = data[i];
			if(isPointer(sample)) {//if it's a pointer, then we need to go to point and read until null
				i++;
				int pointer=getUnsignedInt(data[i]);
				rData+=readUntilNull(data,pointer);
			}else if(getUnsignedInt(sample)>0){//if not pointer, it's a number and we need to read until null
				rData+=readUntilNull(data,i);
				i=skipToIndex-1;
			}else {//if neither, it's a null character. Insert a tab.
				rData+='\t';
			}
		}
		
		return rData;
	}
	
	private static int skipToIndex=0;
	private static String readUntilNull(byte[] data, int index) {
		String toReturn="";
		while(getUnsignedInt(data[index])!=0 && index<data.length) {
			if(isPointer(data[index])) {
				toReturn += readUntilNull(data,getUnsignedInt(data[index+1]));
				index++;
			}else {
				//differentiate between a char and a number of chars
				int size = getUnsignedInt(data[index]);
				int limit = index+size;
				//System.out.println("Limit = "+limit);
				index++;
				for(int i=0;i<size && index<data.length;i++,index++) {
					toReturn += (char) data[index];
					//System.out.println(toReturn);
					//System.out.println("Index ="+index);
				}
				toReturn +='.';
				if(index>=data.length) {
					break;
				}
			}
		}

		skipToIndex=index;
		return toReturn.substring(0, toReturn.length()-1);
	}
	
	private static boolean isPointer(byte data) {
		int test = data & 0xC0;
		int mask = 0xC0;
		if(test==mask) {
			return true;
		}else {
			return false;
		}

	}
	
	public static int getUnsignedInt(byte data) {
		int toReturn = data & 0xFF;
		return toReturn;
	}
	
}
