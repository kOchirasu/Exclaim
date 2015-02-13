package user;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import packetLib.Connection;
import cmdline.ConnectCommand;
import cmdline.ExitCommand;
import cmdline.HelpCommand;
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
		if(conn.connect())
			cList.put(conn.toString(), conn);
		System.out.println(conn.recv());
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
