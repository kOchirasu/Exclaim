package packetLib;

import static tools.Validate.isValidIP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.crypto.Cipher;

import user.Client;

public class Connection implements Runnable
{
	private String ip;
	private int port;
	private DataInputStream in;
	private DataOutputStream out;
	
	private Client c;
	private Socket sock;
	private ServerSocket ss;
	private Cipher ciph;
	
	private boolean request;
	
	/* Creates a new connection (outgoing request)
	 * 
	 */
	public Connection(Client c, String ip, int port)
	{
		this.c = c;
		setIP(ip);
		setPort(port);
		request = true;
	}
	
	/* Creates a new connection (incoming request)
	 * 
	 */
	public Connection(Client c, ServerSocket ss)
	{
		this.c = c;
		this.ss = ss;
		request = false;
	}
	
	public void init() throws IOException
	{
		if(request)
			sock = new Socket(ip, port);
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
		
		Thread recvThread = new Thread(this);
		recvThread.start();
	}
	
	//begin receiving packets
	public void run()
	{
		while(true)
		{
			try
			{
				int length = in.readInt();
				byte[] recvP = new byte[length];
				in.read(recvP);
				c.OnPacket(new PacketReader(recvP));
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}
	/*KeyGenerator keygen = KeyGenerator.getInstance("AES");
	keygen.init(128);
	SecretKey key = keygen.generateKey();
	
	ciph = Cipher.getInstance("AES/CBC/PKCS5Padding");
	ciph.init(Cipher.ENCRYPT_MODE, key);
	
	byte[] iv = ciph.getIV();*/
	//System.out.println(Arrays.toString(iv));
	//IvParameterSpec ivSpec = new IvParameterSpec(iv);
	
	public void send(PacketWriter p)
	{
		if(!sock.isConnected())
			throw new IllegalStateException("Connection has not been established");
		//if(!encrypted)
		byte[] packet = p.toByteArray();
		if(packet.length < 1)
			throw new IllegalArgumentException("Invalid packet length " + packet.length);
		
		try
		{
			out.writeInt(p.length());
			out.write(p.toByteArray());
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void disconnect()
	{
		try {
			sock.close();
			c.OnDisconnected(toString());
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void setIP(String ip)
	{
		if(isValidIP(ip))
			this.ip = ip;
		else
			throw new IllegalArgumentException(ip + " is not a valid IP Address.");
	}
	
	private void setPort(int port)
	{
		if(port > 1024 && port < 65535)
			this.port = port;
		else
			throw new IllegalArgumentException(port + " is not a valid Port.");
	}
	
	public String toString()
	{
		return ip + ":" + port;
	}
	
	public boolean isConnected()
	{
		return sock.isConnected();
	}
}
