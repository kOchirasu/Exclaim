package cmdline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import user.Client;

public class ConnectCommand implements Command
{
	String name;
	private final Pattern pattern = Pattern.compile(
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
			"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	
	public void handle(Client c, String[] cmd)
	{
		if(cmd.length == 2)
		{
			if(isValidIP(cmd[1]))
			{
				c.println("Attempting to connect to " + cmd[1] + " (not really!)");
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
	
	//http://www.mkyong.com/regular-expressions/how-to-validate-ip-address-with-regular-expression/
	private boolean isValidIP(String ip)
	{
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();
	}
}
