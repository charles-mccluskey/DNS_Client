import java.net.*;

public class DnsClient {

	public static void main(String[] args) {
		//Input argument indices
		//some may or may not be present, just need to scan the array for tokens -t, -r, -p, -mx/-nx, and the char @
		//args[0] = -t
		//args[1] = timeout
		//args[2] = -r
		//args[3] = max-retries
		//args[4] = -p
		//args[5] = port
		//args[6] = -mx or -ns
		//args[7] = @server
		//args[8] = name
		System.out.println("args 0 = "+args[0]);
		System.out.println("args 1 = "+args[1]);
		System.out.println("args 2 = "+args[2]);
		System.out.println("Number of args: "+args.length);
		
	}

}
