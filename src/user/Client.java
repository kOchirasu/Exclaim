package user;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.HashMap;

import packetLib.Connection;
import packetLib.PacketReader;
import packetLib.PacketWriter;
import cmdline.ConnectCommand;
import cmdline.ExitCommand;
import cmdline.HelpCommand;
import cmdline.ListCommand;
import cmdline.Parser;

public class Client
{	
	public Parser p;
	public HashMap<String, Connection> cList;
	
	public Client()
	{
		cList = new HashMap<>();
		p = new Parser();
		initParser();
	}
	
	public void run() throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String command;
		println("Exclaim Chat Client Started...");
		while(true)
		{
			command = br.readLine();
			p.handle(this, command);
		}
	}
	
	public void connectTo(String ip, int port)
	{
		Connection conn = new Connection(this, ip, port);
		connectTo(conn);
	}
	
	public void connectTo(ServerSocket ss)
	{
		Connection conn = new Connection(this, ss);
		connectTo(conn);
	}
	
	private void connectTo(Connection conn)
	{
		try
		{
			conn.init();
			cList.put(conn.toString(), conn);
		}
		catch(IOException ex)
		{
			System.out.println("Connection failed.");
			ex.printStackTrace();
		}
	}
	
	public void OnDisconnected(String ipPort)
	{
		if(cList.remove(ipPort) == null)
			throw new IllegalStateException("Not connected to " + ipPort);
	}
	
	public void OnPacket(PacketReader pr)
	{
		switch(pr.readByte())
		{
			case 1:
				System.out.println(pr.readString());
				break;
			default:
				System.out.println("Invalid Packet Header");
		}
	}
	
	private void initParser()
	{
		p.add("exit,quit", new ExitCommand());
		p.add("help,?", new HelpCommand());
		p.add("connect", new ConnectCommand());
		p.add("list", new ListCommand());
	}
	public void print(String s)
	{
		System.out.print("C> " + s);
	}
	public void println(String s)
	{
		System.out.println("C> " + s);
	}
}
