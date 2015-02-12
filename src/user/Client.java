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
		p.add("exit", new ExitCommand());
		p.add("help", new HelpCommand());
		p.add("connect", new ConnectCommand());
	}
	
	public void run() throws IOException
	{
		/*Connector c = new Connector();
		c.init();*/
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String command;
		while(true)
		{
			command = br.readLine();
			p.handle(this, command);
		}
	}
	
	public void print(String s)
	{
		System.out.print(s);
	}
	public void println(String s)
	{
		System.out.println(s);
	}
	
	public static void main(String[] args) throws IOException
	{	
		Client c = new Client();
		System.out.println("Exclaim Chat Client Started...");
		c.run();
	}
}
