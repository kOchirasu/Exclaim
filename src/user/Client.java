package user;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import cmdline.*;

public class Client
{	
	public Parser p;
	
	public Client()
	{
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
	
	public void print(String s)
	{
		System.out.print("C> " + s);
	}
	public void println(String s)
	{
		System.out.println("C> " + s);
	}
}
