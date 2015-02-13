package user;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable
{
	ServerSocket ss;
	public Server() throws IOException
	{
		ss = new ServerSocket(2121);
		//ss.setSoTimeout(10000);
	}
	
	public void run()
	{
		Socket cCon;
		System.out.println("S> Exclaim Chat Server started...");
		while(true)
		{
			try
			{
				//Wait for some connection
				System.out.println("Server waiting for connection...");
				cCon = ss.accept();
				System.out.println("S> Server accepted connection from " + cCon.getRemoteSocketAddress());
				DataOutputStream das = new DataOutputStream(cCon.getOutputStream());
				ss = new ServerSocket(0);
				das.writeInt(ss.getLocalPort());
				cCon.close();
				
				//Redirect the connection
				cCon = ss.accept();
				System.out.println("Chat connection established to " + cCon.getRemoteSocketAddress());
				das = new DataOutputStream(cCon.getOutputStream());
				das.writeUTF("My first message sent :3");
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
				System.out.println("S> Exclaim Chat Server shutdown...");
				break;
			}
		}
	}
}
