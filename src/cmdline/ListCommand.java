package cmdline;

import user.Client;

public class ListCommand implements Command
{
    String name;

    public void handle(Client c, String[] cmd)
    {
        if (cmd.length == 2)
        {
            switch (cmd[1])
            {
                case "connections":
                    System.out.println("Active connections: ");
                    for (String s : c.chatList.keySet())
                        System.out.println(s + ": " + c.chatList.get(s));
                    break;

                default:
                    System.out.println("Cannot list " + cmd[1]);
            }
        }
        else
            System.out.println(usage());
    }

    public String usage()
    {
        return "Usage: /" + name + " 'WHAT TO LIST'";
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
