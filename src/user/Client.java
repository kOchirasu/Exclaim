package user;

import cmdline.*;
import packetLib.Connection;
import packetLib.PacketReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import java.util.HashMap;

public class Client
{
    public String myIP;
    public Parser p;
    public HashMap<String, Connection> cList;
    public HashMap<String, Connection> jList;

    public Client()
    {
        cList = new HashMap<>();
        jList = new HashMap<>();
        p = new Parser();
        initParser();
        try
        {
            Listener l = new Listener(this, 2121);
            Thread listenThread = new Thread(l);
            listenThread.start();

            URL getIP = new URL("http://checkip.amazonaws.com");
            BufferedReader br = new BufferedReader(new InputStreamReader(getIP.openStream()));
            myIP = br.readLine(); //you get the IP as a String
            Program.chatRoom.writeAlert("My IP is: " + myIP);
        }
        catch (IOException ex)
        {
            System.out.println("Unable to bind port 2121");
            ex.printStackTrace();
        }
    }

    public void run() throws IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String command;
        println("Exclaim Chat Client Started...");
        while (true)
        {
            command = br.readLine();
            p.handle(this, command);
        }
    }

    public void connectTo(String ip, int port)
    {
        Connection conn = new Connection(this, ip, port);
        try
        {
            conn.init();
        }
        catch (Exception ex)
        {
            System.out.println("Connection failed.");
            ex.printStackTrace();
        }

        addConnection(conn);
    }

    public void connectTo(ServerSocket ss)
    {
        Connection conn = new Connection(this, ss);
        try
        {
            conn.init();
        }
        catch (Exception ex)
        {
            System.out.println("Connection failed.");
            ex.printStackTrace();
        }
        jList.put(conn.toString(), conn);
        Program.mainProg.addRequest(conn.toString());
    }

    public void chatAccept(String ipPort)
    {
        Connection conn = jList.get(ipPort);
        if(conn == null)
            throw new IllegalStateException("Not connected to " + ipPort);
        jList.remove(ipPort);
        addConnection(conn);
        //After listener accepts a connection, should forward all current peers
        /*PacketWriter pw = new PacketWriter(Header.FORWARD);
        pw.writeByte(cList.size()); //will be problem if > 127?
        for(String s : cList.keySet())
            pw.writeString(s);

        conn.sendPacket(pw);*/
    }

    public void chatReject(String ipPort)
    {
        Connection conn = jList.get(ipPort);
        if(conn == null)
            throw new IllegalStateException("Not connected to " + ipPort);
        jList.remove(ipPort);
        conn.disconnect();
    }

    private void addConnection(Connection conn)
    {
        //TODO: MAKE A BETTER FIX (Maybe prevent this from being possible)
        String connName = conn.toString();
        if (cList.containsKey(connName))
        {
            connName += (char) (Math.random() * 255);
            cList.put(connName, conn);
            Program.chatRoom.addContact(connName);
        }
        else
        {
            cList.put(connName, conn);
            Program.chatRoom.addContact(connName);
        }
        Program.chatRoom.writeAlert(conn + " has connected.");
    }

    public void OnDisconnected(String ipPort)
    {
        if (cList.remove(ipPort) == null) //not illegal state if rejected
        {
            Program.chatRoom.writeAlert(ipPort + " has been rejected.");
            //throw new IllegalStateException("Not connected to " + ipPort);
        }
        else
        {
            Program.chatRoom.writeAlert(ipPort + " has disconnected.");
            Program.chatRoom.removeContact(ipPort);
        }
    }

    public void OnPacket(Connection conn, PacketReader pr)
    {
        byte header = pr.readByte();
        switch (header)
        {
            case Header.CHAT: //1
                String msg = pr.readString();
                System.out.println("Got a message: " + msg);
                Program.chatRoom.writeChat(conn.toString(), msg);
                break;
            case Header.FORWARD: //10
                for(int i = pr.readByte();i > 0; i--)
                {
                    System.out.print("Need to request connect to ");
                    String addIP = pr.readString().split(":")[0];
                    if(!addIP.equals(myIP)) //Problem when connecting to localhost (causes loop)
                    {
                        System.out.println(addIP);
                        connectTo(addIP, 2121);
                    }
                }
                break;
            case Header.DISCONNECT: //200
                Program.chatRoom.writeAlert(conn + " has disconnected.");
                cList.remove(conn.toString());
                Program.chatRoom.removeContact(conn.toString());
                break;
            default:
                System.out.println("Invalid Packet Header: " + header);
        }
    }

    private void initParser()
    {
        p.add("exit,quit", new ExitCommand());
        p.add("help,?", new HelpCommand());
        p.add("connect", new ConnectCommand());
        p.add("list", new ListCommand());
        p.add("message,msg", new MessageCommand());
    }

    public void print(String s)
    {
        System.out.print("C> " + s);
    }

    public void println(String s)
    {
        System.out.println("C> " + s);
    }
}
