package user;

import java.io.IOException;

public class Program
{
	public static void main(String[] args) throws IOException
	{	
		Client c = new Client();
		Listener s = new Listener(c);
		Thread serverThread = new Thread(s);
		serverThread.start();
		c.run();
	}
}
