package user;

import cmdline.*;
import packetLib.Connection;
import packetLib.PacketReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.HashMap;

public class Client
{
    public Parser p;
    public HashMap<String, Connection> cList;

    public Client()
    {
        cList = new HashMap<>();
        p = new Parser();
        initParser();
        try
        {
            Listener l = new Listener(this, 2121);
            Thread listenThread = new Thread(l);
            listenThread.start();
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

    //Hello
    public void connectTo(String ip, int port)
    {
        Connection conn = new Connection(this, ip, port);
        connectTo(conn);
    }

    public void connectTo(ServerSocket ss)
    {
        Connection conn = new Connection(this, ss);
        connectTo(conn);
        Program.chatRoom.writeAlert(conn + " has connected.");
    }

    private void connectTo(Connection conn)
    {
        try
        {
            conn.init();
            //TODO: MAKE A BETTER FIX
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
        }
        catch (Exception ex)
        {
            System.out.println("Connection failed.");
            ex.printStackTrace();
        }
    }

    public void OnDisconnected(String ipPort)
    {
        Program.chatRoom.writeAlert(ipPort + " has disconnected.");
        if (cList.remove(ipPort) == null)
            throw new IllegalStateException("Not connected to " + ipPort);
        Program.chatRoom.removeContact(ipPort);
    }

    public void OnPacket(Connection conn, PacketReader pr)
    {
        byte header = pr.readByte();
        switch (header)
        {
            case 1:
                String msg = pr.readString();
                System.out.println("Got a message: " + msg);
                Program.chatRoom.writeChat(conn.toString(), msg);
                break;
            case -56: //200
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
