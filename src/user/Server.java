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
				cCon = ss.accept();
				System.out.println("S> Server accepted connection from " + cCon.getRemoteSocketAddress());
				DataOutputStream das = new DataOutputStream(cCon.getOutputStream());
				das.writeUTF(cCon.getLocalSocketAddress().toString());
				break;
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
				break;
			}
		}
	}
}
