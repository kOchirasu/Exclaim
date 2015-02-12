package cmdline;

import java.util.HashMap;

import user.Client;

public class Parser
{
	private HashMap<String, Command> list;
	private Command invalid;
	
	public Parser()
	{
		list = new HashMap<>();
		invalid = new InvalidCommand(); //doesnt need a name
	}
	
	public void add(String s, Command c)
	{
		c.setName(s);
		list.put(s, c);
	}
	
	public void handle(Client c, String cmd)
	{
		if(cmd.charAt(0) == '/')
		{
			cmd = cmd.substring(1);
			String[] cmdArr = cmd.split(" +");
			Command result = list.get(cmdArr[0]);
			if(result != null)
				result.handle(c, cmdArr);
			else
				invalid.handle(c, cmdArr);
		}
		else
		{
			c.println("Chat >> " + cmd);
		}
		
	}
	
	public String toString()
	{
		String cmd = "";
		for(String s : list.keySet())
			cmd += s + " ";
		return cmd;
	}
}
