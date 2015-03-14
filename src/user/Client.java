package user;

import cmdline.*;
import packetLib.Connector;
import packetLib.FileTransfer;
import packetLib.PacketReader;
import packetLib.PacketWriter;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
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
    public HashMap<String, Connector> chatList;
    //joinList: Join List - Maps IP to Connection
    //List of connections requesting to join chat
    public HashMap<String, Connector> joinList;
    //waitList: Wait List - Maps IP to Connection
    //List of connections waiting to join
    public HashMap<String, Connector> waitList;
    //autoList: Maps IP to a boolean value signifying whether or not to auto-allow
    //Used for whitelist and blacklist auto accept/reject
    public ArrayList<String> aList; //allow list

    //Used to keep track of which IP you told to allow
    //Removes ip once they respond or disconnect
    private HashSet<String> rSet;

    public HashMap<String, File> fileList;

    public Client()
    {
        chatList = new HashMap<>();
        joinList = new HashMap<>();
        waitList = new HashMap<>();
        aList = new ArrayList<>();
        rSet  = new HashSet<>();
        fileList = new HashMap<>();

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
        System.out.println("Exclaim Chat Client Started...");
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

        Connector conn = new Connector(this, ip, port);
        conn.init();
        //Don't actually add connection until it is accepted
        waitList.put(ip, conn); //Store IP in waitlist
    }

    public void connectTo(ServerSocket ss)
    {
        Connector conn = new Connector(this, ss);

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

    public void chatAccept(String ip)
    {
        System.out.println("Accepting " + ip);
        Connector conn = joinList.get(ip);
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
        Connector conn = joinList.get(ip);
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

    private void addConnection(Connector conn)
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

    public void OnPacket(Connector conn, PacketReader pr)
    {
        //System.out.println("[RECV] " + pr.toHexString()); //print packet
        byte header = pr.readByte();
        switch (header)
        {
            case Header.CHAT: //1
                String msg = pr.readString();
                //TODO: lookup name instead of using ip:port as name
                Program.chatRoom.writeChat(conn.getIP(), msg);
                break;
            case Header.ACTION:
                byte aFunc = pr.readByte();
                Connector actionC = waitList.get(pr.readString());
                switch(aFunc)
                {
                    case 1: //accept
                        addConnection(actionC);
                        //Program.chatRoom.writeAlert("Connection accepted");
                        waitList.remove(actionC.getIP());
                        break;
                    case 2: //reject
                        waitList.get(actionC).disconnect();
                        //Program.chatRoom.writeAlert("Connection rejected");
                        waitList.remove(actionC.getIP());
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
            case Header.FILE_OFFER:
                String foName = pr.readString();
                long foSize = pr.readLong();
                PacketWriter fr = new PacketWriter(Header.FILE_REQUEST);
                File fo = fileOffer(conn.getIP(), foName, foSize);
                if(fo != null)
                {
                    fr.writeByte(1);
                    try
                    {
                        ServerSocket foSock = new ServerSocket(0);
                        fr.writeInt(foSock.getLocalPort()); //Maybe can use short if it handles signed values properly
                        fr.writeString(fo.getName());
                        FileTransfer ftS = new FileTransfer(foSock);
                        ftS.setFile(fo);
                        new Thread(ftS).start(); //will this hang if file sender disconnects?
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                else
                    fr.writeByte(0);
                conn.sendPacket(fr);
                break;
            case Header.FILE_REQUEST:
                if(pr.readByte() == 1)
                {
                    System.out.println(conn.getIP() + " accepted file transfer.");
                    try
                    {
                        FileTransfer frC = new FileTransfer(conn.getIP(), pr.readInt());
                        frC.init();
                        frC.sendFile(fileList.get(pr.readString()));
                    }
                    catch(Exception ex) //dont need?
                    {
                        ex.printStackTrace();
                        System.out.println("File transfer failed...");
                    }
                }
                else
                {
                    System.out.println(conn.getIP() + " rejected file transfer.");
                }
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

    private File fileOffer(String ip, String fileName, long size)
    {
        String foTitle = "File Transfer from " + ip;
        String foMsg1 = "File name: " + fileName + "\n";
        String foMsg2 = "File size: " + size + " bytes\n\n";
        int foReply = JOptionPane.showConfirmDialog(null, foMsg1 + foMsg2 + "Would you like to download this file?", foTitle, JOptionPane.YES_NO_OPTION);
        if(foReply == JOptionPane.YES_OPTION)
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose a location to save...");
            fileChooser.setSelectedFile(new File(fileName));
            if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
            {
                return fileChooser.getSelectedFile();
            }
        }
        return null;
    }

    private void initParser()
    {
        p.add("exit,quit", new ExitCommand());
        p.add("help,?", new HelpCommand());
        p.add("connect", new ConnectCommand());
        p.add("list", new ListCommand());
        p.add("message,msg", new MessageCommand());
    }
}
