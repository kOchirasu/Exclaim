package other;

import java.net.InetAddress;

public class Contact
{
    private String name;
    private InetAddress ip;

    public Contact(String name, InetAddress ip)
    {
        setName(name);
        setIP(ip);
    }

    public void setName(String name)
    {
        if (name.length() > 0)
            this.name = name;
        else
            this.name = ip.toString();
    }

    public void setIP(InetAddress ip)
    {
        this.ip = ip;
    }
}
