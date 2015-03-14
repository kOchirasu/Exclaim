package packetLib;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;

import static tools.Validate.isValidIP;

public class Connection
{
    private final String AES_KEY;
    private ServerSocket ss;

    protected String name, ip;
    protected int port;
    protected DataInputStream in;
    protected DataOutputStream out;
    protected Socket sock;
    protected Cipher eCiph, dCiph;
    protected boolean request, encrypted;

    /* Creates a new connection (outgoing request)
     *
     */
    public Connection(String ip, int port, String key)
    {
        AES_KEY = key;
        setIP(ip);
        setPort(port);
        request = true;
    }

    /* Creates a new connection (incoming request)
     *
     */
    public Connection(ServerSocket ss, String key)
    {
        AES_KEY = key;
        this.ss = ss;
        request = false;
    }

    public void init()// throws IOException, GeneralSecurityException
    {
        try
        {
            if (request)
            {
                sock = new Socket();
                sock.connect(new InetSocketAddress(ip, port), 2000); //timeout 2000ms
            }
            else
            {
                sock = ss.accept();
                setIP(sock.getInetAddress().toString().substring(1));
                setPort(sock.getPort());
            }
            System.out.println("Connection established with " + sock.getRemoteSocketAddress());

            //Initialize Streams
            in = new DataInputStream(sock.getInputStream());
            out = new DataOutputStream(sock.getOutputStream());

            //Handshakes
            if (request)
            {
                /* Begin Client Handshake */
                int length = in.readInt();
                byte[] recvP = new byte[length];
                in.read(recvP);

                SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), "AES");
                PacketReader pr = new PacketReader(recvP);
                pr.readByte();

                dCiph = Cipher.getInstance("AES/CBC/PKCS5Padding");
                dCiph.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(pr.readBytes(16)));

                eCiph = Cipher.getInstance("AES/CBC/PKCS5Padding");
                eCiph.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(pr.readBytes(16)));
                encrypted = true;
            }
            else
            {
                /* Begin Server Handshake */
                setIP(sock.getInetAddress().toString().substring(1));
                setPort(sock.getPort());
                //Handshake
                SecretKeySpec key = new SecretKeySpec(AES_KEY.getBytes(), "AES");
                SecureRandom sr = new SecureRandom();
                PacketWriter pw = new PacketWriter(255);
                byte[] iv = new byte[16];

                sr.nextBytes(iv);
                pw.writeBytes(iv);
                eCiph = Cipher.getInstance("AES/CBC/PKCS5Padding");
                eCiph.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

                sr.nextBytes(iv);
                pw.writeBytes(iv);
                dCiph = Cipher.getInstance("AES/CBC/PKCS5Padding");
                dCiph.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
                encrypted = true;

                //Send IV over to client
                out.writeInt(pw.length());
                out.write(pw.toByteArray());
            }
        }
        catch(Exception ex)
        {
            System.out.println("Unable to connect to " + this);
            ex.printStackTrace();
        }
    }

    private void setIP(String ip)
    {
        if (isValidIP(ip))
            this.ip = ip;
        else
            throw new IllegalArgumentException(ip + " is not a valid IP Address.");
    }

    private void setPort(int port)
    {
        if (port > 1024 && port < 65535)
            this.port = port;
        else
            throw new IllegalArgumentException(port + " is not a valid Port.");
    }

    public String getIP()
    {
        return ip;
    }

    public String toString()
    {
        return ip + ":" + port;
    }
}
