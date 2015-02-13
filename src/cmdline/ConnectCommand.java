package cmdline;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import user.Client;
import static tools.Validate.isValidIP;

public class ConnectCommand implements Command
{
	String name;
	
	public void handle(Client c, String[] cmd)
	{
		if(cmd.length == 2)
		{
			if(isValidIP(cmd[1]))
			{
				c.println("Attempting to connect to " + cmd[1] + ":2121");
				try
				{
					Socket temp = new Socket(cmd[1], 2121);
					c.println("Connection successful.");
					DataInputStream in = new DataInputStream(temp.getInputStream());
					int newPort = in.readInt();
					temp.close();
					c.connectTo(cmd[1], newPort);
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
					c.println("Connection failed.");
				}
			}
			else
			{
				c.println(cmd[1] + " is not a valid IP address.");
			}
		}
		else
			c.println(usage());
	}
	
	public String usage()
	{
		return "Usage: /" + name + " ip";
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
}
