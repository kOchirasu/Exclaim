package cmdline;

import packetLib.Connection;
import packetLib.PacketWriter;
import user.Client;
import user.Header;

public class MessageCommand implements Command
{
    String name;

    //Need to fix to support multiple word messages
    public void handle(Client c, String[] cmd)
    {
        if (cmd.length == 3)
        {
            Connection dest = c.cList.get(cmd[1]);
            if (dest != null)
            {
                PacketWriter pw = new PacketWriter(Header.CHAT);
                pw.writeString(cmd[2]);
                dest.sendPacket(pw);
            }
            else
                c.println(cmd[1] + " is not an active connection.  Use '/list connections' for a list of active connections");
        }
        else
            c.println(usage());
    }

    public String usage()
    {
        return "Usage: /" + name + " ip:port message";
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
