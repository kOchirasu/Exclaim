package user;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener implements Runnable
{
	Client c;
	ServerSocket listenSock;
	
	public Listener(Client c) throws IOException
	{
		listenSock = new ServerSocket(2121);
		this.c = c;
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
				cCon = listenSock.accept();
				System.out.println("S> Server accepted connection from " + cCon.getRemoteSocketAddress());
				DataOutputStream out = new DataOutputStream(cCon.getOutputStream());
				ServerSocket ss = new ServerSocket(0); //might want to make new reference to preserve initial one
				out.writeInt(ss.getLocalPort());
				cCon.close();
				
				c.connectTo(ss);
				//Redirect the connection (This connection should be moved into Client cList)
				/*cCon = ss.accept();
				System.out.println("Chat connection established to " + cCon.getRemoteSocketAddress());
				out = new DataOutputStream(cCon.getOutputStream());
				out.writeUTF("My first message sent :3");*/
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
