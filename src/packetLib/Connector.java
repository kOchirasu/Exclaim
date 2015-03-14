package packetLib;

import user.Client;

import java.io.IOException;
import java.net.ServerSocket;

public class Connector extends Connection implements Runnable
{
    private static final String AES_KEY = "Ulng9bhk9uYrSgps";
    private Client c;

    /* Creates a new connection (outgoing request)
     *
     */
    public Connector(Client c, String ip, int port)
    {
        super(ip, port, AES_KEY);
        this.c = c;
    }

    /* Creates a new connection (incoming request)
     *
     */
    public Connector(Client c, ServerSocket ss)
    {
        super(ss, AES_KEY);
        this.c = c;
    }

    @Override
    public void init()
    {
        super.init();
        new Thread(this).start(); //Start thread for receiving packets
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                int length = in.readInt();
                byte[] recvP = new byte[length];
                in.read(recvP);

                c.OnPacket(this, new PacketReader(dCiph.doFinal(recvP)));
            }
            catch(IOException ex) //Disconnected
            {
                disconnect();
                break;
            }
            catch(Exception ex) //Rejected?
            {
                //System.out.println("Something went wrong.  Not disconnect.");
                //ex.printStackTrace();
                disconnect();
                break;
            }
        }
    }

    public void sendPacket(PacketWriter pw)
    {
        if (!sock.isConnected())
            throw new IllegalStateException("Connection has not been established");
        if (!encrypted)
            throw new IllegalStateException("Handshake has not been received");

        byte[] packet = pw.toByteArray();
        if (packet.length < 1)
            throw new IllegalArgumentException("Invalid packet length " + packet.length);

        try
        {
            byte[] encryptedSend = eCiph.doFinal(pw.toByteArray());
            out.writeInt(encryptedSend.length);
            out.write(encryptedSend);
            //System.out.println("[SEND] " + p); //print packet
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void disconnect()
    {
        try
        {
            sock.close();
            c.OnDisconnected(ip);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
