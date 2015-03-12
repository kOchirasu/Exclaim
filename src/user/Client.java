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
import java.util.HashSet;

public class Client
{
    public String myIP;
    public Parser p;
    //chatList: Connection List - Maps IP to Connection
    //List of active connections which have been accepted
    public HashMap<String, Connection> chatList;
    //joinList: Join List - Maps IP to Connection
    //List of connections requesting to join chat
    public HashMap<String, Connection> joinList;
    //waitList: Wait List - Maps IP to Connection
    //List of connections waiting to join
    public HashMap<String, Connection> waitList;
    //autoList: Maps IP to a boolean value signifying whether or not to auto-allow
    //Used for whitelist and blacklist auto accept/reject
    public ArrayList<String> aList; //allow list

    //Used to keep track of which IP you told to allow
    //Removes ip once they respond or disconnect
    private HashSet<String> rSet;

    public Client()
    {
        chatList = new HashMap<>();
        joinList = new HashMap<>();
        waitList = new HashMap<>();
        aList = new ArrayList<>();
        rSet  = new HashSet<>();

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

    public void sendAll(PacketWriter pw)
    {
        for(String s : chatList.keySet())
            chatList.get(s).sendPacket(pw);
    }

    public void connectTo(String ip, int port)
    {
        if(chatList.get(ip) != null)
        {
            Program.chatRoom.writeAlert("Already connected to " + ip);
            return;
        }
        else if(waitList.get(ip) != null)
        {
            Program.chatRoom.writeAlert("Already requesting connection from " + ip);
            return;
        }

        Connection conn = new Connection(this, ip, port);
        try
        {
            conn.init();
            //Don't actually add connection until it is accepted
            waitList.put(ip, conn); //Store IP in waitlist
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
            String ip = conn.getIP();

            //Check if connection is on allowed list.  If it is allow the connection and remove from allow list
            if(aList.contains(ip))
            {
                aList.remove(ip);
                addConnection(conn);

                //Respond with and accept message
                PacketWriter pw = new PacketWriter(Header.ACTION);
                pw.writeByte(1);
                pw.writeString(myIP);
                conn.sendPacket(pw);
            }
            else //Add connection to request list
            {
                //Add request if it doesn't exist
                if(!joinList.containsKey(ip))
                {
                    joinList.put(ip, conn);
                    if(Program.autoList.containsKey(ip))
                    {
                        if(Program.autoList.get(ip))
                            chatAccept(ip);
                        else
                            chatReject(ip);
                    }
                    else
                    {
                        //System.out.println("Adding request " + ip);
                        Program.mainProg.addRequest(ip);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            System.out.println("Connection failed.");
            ex.printStackTrace();
        }
    }

    public void chatAccept(String ip)
    {
        System.out.println("Accepting " + ip);
        Connection conn = joinList.get(ip);
        if (conn == null)
            throw new IllegalStateException("Not connected to " + ip);

        rSet.clear();
        for(String s : chatList.keySet())
            rSet.add(s);

        //Tell all peers to allow this new connection
        PacketWriter pw = new PacketWriter(Header.ALLOW);
        pw.writeString(ip); //IP of client being accepted
        sendAll(pw);

        //Respond with and accept message
        pw = new PacketWriter(Header.ACTION);
        pw.writeByte(1);
        pw.writeString(myIP);
        conn.sendPacket(pw);

        //Send chat room name
        pw = new PacketWriter(Header.CHAT_NAME);
        pw.writeString(Program.chatRoom.getName());
        conn.sendPacket(pw);

        if(chatList.size() == 0)
            joinList.remove(ip); //Remove from joining list

        addConnection(conn);
    }

    public void chatReject(String ip)
    {
        Connection conn = joinList.get(ip);
        if (conn == null)
            throw new IllegalStateException("Not connected to " + ip);

        //Respond with and reject message
        PacketWriter pw = new PacketWriter(Header.ACTION);
        pw.writeByte(2);
        pw.writeString(myIP);
        conn.sendPacket(pw);

        joinList.remove(ip);
        conn.disconnect();
    }

    private void addConnection(Connection conn)
    {
        String connName = conn.getIP();
        if (!chatList.containsKey(connName))
        {
            chatList.put(connName, conn);
            Program.chatRoom.addContact(connName);
            Program.chatRoom.writeAlert(conn + " has connected.");
            Program.chatRoom.joined(true);
        }
        else
        {
            Program.chatRoom.writeAlert("Already connected to " + connName);
        }
    }

    public void OnDisconnected(String ip)
    {
        if (chatList.remove(ip) == null) //not illegal state if rejected
        {
            if(waitList.remove(ip) != null)
                Program.chatRoom.writeAlert(ip + " has has rejected you");
        }
        else
        {
            Program.chatRoom.writeAlert(ip + " has disconnected.");
            Program.chatRoom.removeContact(ip);
            rSet.remove(ip);
            if(chatList.size() == 0)
                Program.chatRoom.joined(false);
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
                Connection actionC = waitList.get(pr.readString());
                switch(aFunc)
                {
                    case 1: //accept
                        addConnection(actionC);
                        Program.chatRoom.writeAlert("Connection accepted");
                        //Program.chatRoom.joined(true);
                        waitList.remove(actionC);
                        break;
                    case 2: //reject
                        waitList.get(actionC).disconnect();
                        Program.chatRoom.writeAlert("Connection rejected");
                        waitList.remove(actionC);
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
                conn.sendPacket(apw);
                break;
            case Header.ALLOW_RSP: //Forwarded allowed ip
                String forwardIP = pr.readString(); //IP of peer that will accept new connection
                PacketWriter fpw = new PacketWriter(Header.FORWARD);
                fpw.writeString(conn.getIP());
                joinList.get(forwardIP).sendPacket(fpw);

                rSet.remove(conn.getIP());
                if(rSet.size() == 0)
                {
                    joinList.remove(forwardIP);
                    System.out.println(forwardIP + " has been fully forwarded.");
                }
                break;
            case Header.FORWARD:
                connectTo(pr.readString(), 2121); //connect to forwarded peer
                break;
            case Header.CHAT_NAME:
                Program.chatRoom.setName(pr.readString());
                break;
            case Header.DISCONNECT: //Not really used
                Program.chatRoom.writeAlert(conn + " has disconnected.");
                chatList.remove(conn.getIP());
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
