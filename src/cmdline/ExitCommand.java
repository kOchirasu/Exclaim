package cmdline;

import user.Client;

public class ExitCommand implements Command
{
	String name;
	public void handle(Client c, String[] cmd)
	{
		if(cmd.length == 1)
		{
			c.println("Client exiting.");
			System.exit(0);
		}
		c.println(usage());
	}
	
	public String usage()
	{
		return "Usage: /" + name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
}
