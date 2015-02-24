package cmdline;

import packetLib.PacketWriter;
import user.Client;

public class ExitCommand implements Command
{
    String name;

    /* A little weird if one client makes 2 connections to another client.
     * Since it will create multiple connections to same ip/port, the mapping will be overwritten...
     */
    public void handle(Client c, String[] cmd)
    {
        if (cmd.length == 1)
        {
            c.println("Client closing.");
            PacketWriter pw = new PacketWriter(200);
            for (String s : c.cList.keySet())
                c.cList.get(s).sendPacket(pw);
            //System.exit(0);
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
