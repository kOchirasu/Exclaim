package cmdline;

import user.Client;

import static tools.Validate.isValidIP;

public class ConnectCommand implements Command
{
    String name;

    public void handle(Client c, String[] cmd)
    {
        if (cmd.length == 2)
        {
            if (isValidIP(cmd[1]))
            {
                System.out.println("Attempting to connect to " + cmd[1] + ":2121");
                c.connectTo(cmd[1], 2121);
            }
            else
            {
                System.out.println(cmd[1] + " is not a valid IP address.");
            }
        }
        else
            System.out.println(usage());
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
