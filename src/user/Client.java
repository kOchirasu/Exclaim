package user;

import cmdline.*;
import packetLib.Connection;
import packetLib.PacketReader;
import packetLib.PacketWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Client
{
    public String myIP;
    public Parser p;
    //cList: Connection List - Maps IP to Connection
    //List of active connections which have been accepted
    public HashMap<String, Connection> cList;
    //jList: Join List - Maps IP to Connection
    //List of connections requesting to join chat
    public HashMap<String, Connection> jList;
    //wList: Wait List - Maps IP to Connection
    //List of connections waiting to join
    public HashMap<String, Connection> wList;
    //aList: Allow List - IP
    //List of IPs that will be white listed for 1 connect
    public ArrayList<String> aList; //allow list

    public Client()
    {
        cList = new HashMap<>();
        jList = new HashMap<>();
        wList = new HashMap<>();
        aList = new ArrayList<>();

        //Initialize command line parser.  Not needed for GUI version
        p = new Parser();
        initParser();

        //Start up listener
        Listener l = new Listener(this, 2121);
        Thread listenThread = new Thread(l);
        listenThread.start();

        try
        {
            URL getIP = new URL("http://checkip.amazonaws.com");
            BufferedReader br = new BufferedReader(new InputStreamReader(getIP.openStream()));
            myIP = br.readLine(); //you get the IP as a String
            Program.chatRoom.writeAlert("My IP is: " + myIP);
        }
        catch (IOException ex)
        {
            System.out.println("Unable to get IP");
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
            //Don't actually add connection until it is accepted
            wList.put(ip, conn); //Store IP in waitlist
            //addConnection(conn);
        }
        catch (Exception ex)
        {
            Program.chatRoom.writeAlert("Unable to connect to " + conn);
            //ex.printStackTrace();
        }
    }

    public void connectTo(ServerSocket ss)
    {
        Connection conn = new Connection(this, ss);
        try
        {
            conn.init();

            //Check if connection is on allowed list.  If it is allow the connection and remove from allow list
            if(aList.contains(conn.getIP()))
            {
                aList.remove(conn.getIP());
                addConnection(conn);

                //Respond with and accept message
                PacketWriter pw = new PacketWriter(Header.ACTION);
                pw.writeByte(1);
                pw.writeString(myIP);
                conn.sendPacket(pw);
            }
            else //Add connection to request list
            {
                jList.put(conn.getIP(), conn);
                Program.mainProg.addRequest(conn.getIP());
            }
        }
        catch (Exception ex)
        {
            System.out.println("Connection failed.");
            //ex.printStackTrace();
        }
    }

    public void chatAccept(String ip)
    {
        Connection conn = jList.get(ip);
        if (conn == null)
            throw new IllegalStateException("Not connected to " + ip);

        //Tell all peers to allow this new connection
        PacketWriter pw = new PacketWriter(Header.ALLOW);
        pw.writeString(ip); //IP of client being accepted
        for(String s : cList.keySet())
            cList.get(s).sendPacket(pw);

        //Respond with and accept message
        pw = new PacketWriter(Header.ACTION);
        pw.writeByte(1);
        pw.writeString(myIP);
        conn.sendPacket(pw);

        //TODO: Don't remove this until you are finished forwarding?
        if(cList.size() == 0)
            jList.remove(ip); //Remove from joining list
        //Might want to delay a little before adding?
        addConnection(conn);
    }

    public void chatReject(String ip)
    {
        Connection conn = jList.get(ip);
        if (conn == null)
            throw new IllegalStateException("Not connected to " + ip);

        //Respond with and reject message
        PacketWriter pw = new PacketWriter(Header.ACTION);
        pw.writeByte(2);
        pw.writeString(myIP);
        conn.sendPacket(pw);

        jList.remove(ip);
        conn.disconnect();
    }

    private void addConnection(Connection conn)
    {
        //TODO: MAKE A BETTER FIX (Maybe prevent this from being possible)
        String connName = conn.getIP();
        if (!cList.containsKey(connName))
        {
            cList.put(connName, conn);
            Program.chatRoom.addContact(connName);
            Program.chatRoom.writeAlert(conn + " has connected.");
        }
        else //TODO:Problem is that this removes initial connection as well
        {
            if(connName.equals("127.0.0.1")) //TODO: Really weird results... Better remove before finalizing
                connName += " " + (int)(Math.random() * 99);
            cList.put(connName, conn);
            Program.chatRoom.addContact(connName);
            Program.chatRoom.writeAlert(conn + " has connected.");
            /*Program.chatRoom.writeAlert("Already connected to " + conn + ".");
            conn.disconnect();*/
        }
    }

    public void OnDisconnected(String ip)
    {
        if (cList.remove(ip) == null) //not illegal state if rejected
        {
            Program.chatRoom.writeAlert(ip + " has been rejected.");
        }
        else
        {
            Program.chatRoom.writeAlert(ip + " has disconnected.");
            Program.chatRoom.removeContact(ip);
        }
    }

    public void OnPacket(Connection conn, PacketReader pr)
    {
        //System.out.println("[RECV] " + pr.toHexString()); //print packet
        byte header = pr.readByte();
        switch (header)
        {
            case Header.CHAT: //1
                String msg = pr.readString();
                //TODO: lookup name instead of using ip:port as name
                Program.chatRoom.writeChat(conn.toString(), msg);
                break;
            case Header.ACTION:
                byte aFunc = pr.readByte();
                Connection actionC = wList.get(pr.readString());
                switch(aFunc)
                {
                    case 1: //accept
                        addConnection(actionC);
                        Program.chatRoom.writeAlert("Connection accepted");
                        wList.remove(actionC);
                        break;
                    case 2: //reject
                        wList.get(actionC).disconnect();
                        Program.chatRoom.writeAlert("Connection rejected");
                        wList.remove(actionC);
                        break;
                    default:
                        System.out.println("Not a valid action: " + aFunc);
                }
                break;
            case Header.ALLOW: //Tell peer to allow an IP
                String allowIP = pr.readString();
                aList.add(allowIP); //Add this ip to allow list
                PacketWriter apw = new PacketWriter(Header.ALLOW_RSP);
                apw.writeString(allowIP); //respond with allowed ip
                //apw.writeString(myIP); //respond with my IP aka conn.getIP()
                conn.sendPacket(apw);
                break;
            case Header.ALLOW_RSP: //Forwarded allowed ip
                String forwardIP = pr.readString(); //IP of peer that will accept new connection
                PacketWriter fpw = new PacketWriter(Header.FORWARD);
                fpw.writeString(conn.getIP());
                //need to send this packet to other connection...

                //ERROR: FORWARDIP lookup returns nothing!!
                System.out.println("Join List:");
                for(String s : jList.keySet())
                    System.out.println(s);
                jList.get(forwardIP).sendPacket(fpw);
                break;
            case Header.FORWARD:
                connectTo(pr.readString(), 2121); //connect to forwarded peer
                break;
            case Header.DISCONNECT: //Not really used
                Program.chatRoom.writeAlert(conn + " has disconnected.");
                cList.remove(conn.getIP());
                Program.chatRoom.removeContact(conn.getIP());
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

    public void println(String s)
    {
        System.out.println("C> " + s);
    }
}
