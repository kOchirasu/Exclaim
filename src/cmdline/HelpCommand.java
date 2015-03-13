package cmdline;

import user.Client;

public class HelpCommand implements Command
{
    private String name;

    public void handle(Client c, String[] cmd)
    {
        if (cmd.length == 1)
            System.out.println("Available commands: " + c.p);
        else
            System.out.println(usage());
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
