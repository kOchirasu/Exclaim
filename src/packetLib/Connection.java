package packetLib;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static tools.Validate.isValidIP;

public class Connection
{
	private String ip;
	private int port;
	private boolean connected = false;
	private Socket s;
	private DataOutputStream out;
	private DataInputStream in;
	
	public Connection(String ip, int port) //might want to validate ip and port?
	{
		setIP(ip);
		setPort(port);
	}
		
	public boolean connect()
	{
		try
		{
			s = new Socket(ip, port);
			System.out.println("Connection established with " + s.getRemoteSocketAddress());
			in = new DataInputStream(s.getInputStream());
			out = new DataOutputStream(s.getOutputStream());
			connected = true;
		}
		catch (Exception ex)
		{
			System.out.println("Connection failed.");
			//ex.printStackTrace();
			connected = false;
		}
		return connected;
	}
	
	public String recv()
	{
		try
		{
			return in.readUTF();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	
	public void send(String packet)
	{
		try
		{
			out.writeUTF(packet);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void disconnect()
	{
		try
		{
			s.close();
			connected = false;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void reconnect(String ip, int port)
	{
		
	}
	
	private void setIP(String ip)
	{
		if(isValidIP(ip))
			this.ip = ip;
		else
			throw new IllegalArgumentException(ip + " is not a valid IP Address.");
	}
	
	private void setPort(int port)
	{
		if(port > 1024 && port < 65535)
			this.port = port;
		else
			throw new IllegalArgumentException(port + " is not a valid Port.");
	}
	
	public String toString()
	{
		return ip + ":" + port;
	}
}
