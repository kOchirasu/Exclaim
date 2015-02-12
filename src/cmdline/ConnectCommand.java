package cmdline;

import user.Client;

public class ConnectCommand implements Command
{
	String name;
	
	public void handle(Client c, String[] cmd)
	{
		if(cmd.length != 3)
			c.println("Available commands: " + c.p);
	}
	
	public String usage()
	{
		return "Usage: /" + name + "ip";
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
}
