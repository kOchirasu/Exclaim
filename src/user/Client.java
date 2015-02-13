package user;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.HashMap;

import packetLib.Connection;
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
		p.add("exit,quit", new ExitCommand());
		p.add("help,?", new HelpCommand());
		p.add("connect", new ConnectCommand());
		p.add("list", new ListCommand());
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
		Connection conn = new Connection(ip, port);
		if(conn.connected)
		{
			cList.put(conn.toString(), conn);
			System.out.println(conn.recv());
			conn.send("client got your packet, here is the response");
		}
	}
	
	public void connectTo(ServerSocket ss)
	{
		Connection conn = new Connection(ss);
		if(conn.connected)
		{
			cList.put(conn.toString(), conn);
			conn.send("first packet sent by server :3");
			System.out.println(conn.recv());
		}
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
