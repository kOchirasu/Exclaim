package cmdline;

import user.Client;

public class HelpCommand implements Command
{
	private String name;
	
	public void handle(Client c, String[] cmd)
	{
		if(cmd.length > 1 && cmd[1].equals("?"))
			c.println(usage());
		else
		{
			c.println("Client exiting.");
			System.exit(0);
		}
		if(cmd.length < 2 || cmd[1].equals("?"))
			c.println("Usage: /" + cmd[0] + "");
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
