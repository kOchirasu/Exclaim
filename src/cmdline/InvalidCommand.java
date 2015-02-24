package cmdline;

import user.Client;

public class InvalidCommand implements Command
{
    String name;

    public void handle(Client c, String[] cmd)
    {
        c.println("Invalid command! Use /help");
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
