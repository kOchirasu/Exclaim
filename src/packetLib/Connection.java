package packetLib;

import static tools.Validate.isValidIP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import user.Client;

public class Connection implements Runnable
{
	private String ip;
	private int port, readFail = 0;
	private DataInputStream in;
	private DataOutputStream out;
	
	private Client c;
	private Socket sock;
	private ServerSocket ss;
	private Cipher eCiph, dCiph;
	
	private boolean request, encrypted;
	private final String AES_KEY = "Ulng9bhk9uYrSgps";//TqDOJbqhE1LZo3I7tt1CuyK30rRH22wPgE7A6zbxqq1MaPY0AJRDDPimiutnrwxcksrTW2SJzyYlOdqAWUKsXKZZ7ArwftXBO3ar75tSTbL8k0Qy";
	
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
	
	public void init() throws IOException, GeneralSecurityException
	{
		if(request)
		{
			sock = new Socket(ip, port);
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
		
		//Handshake
		if(request)
		{
			//System.out.println("BEGIN HANDSHAKE CLIENT!");
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
			
			pr.close();
			//System.out.println("HANDSHAKE COMPLETED CLIENT!");
		}
		else
		{
			//System.out.println("BEGIN HANDSHAKE SERVER!");
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
			pw.close();
			//System.out.println("HANDSHAKE COMPLETED SERVER!");
		}
		
		new Thread(this).start();
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
				
				c.OnPacket(new PacketReader(dCiph.doFinal(recvP)));
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				try { Thread.sleep(10000); } //Prevent fail packet reading
				catch (InterruptedException ex2) { Thread.currentThread().interrupt(); }
				if(++readFail > 10) //connection is probably dead
					break;
			}
		}
		
	}
	
	public void sendPacket(PacketWriter p)
	{
		if(!sock.isConnected())
			throw new IllegalStateException("Connection has not been established");
		if(!encrypted)
			throw new IllegalStateException("Handshake has not been receieved");
		byte[] packet = p.toByteArray();
		if(packet.length < 1)
			throw new IllegalArgumentException("Invalid packet length " + packet.length);
		
		try
		{
			byte[] encryptedSend = eCiph.doFinal(p.toByteArray());
			out.writeInt(encryptedSend.length);
			out.write(encryptedSend);
		}
		catch (Exception ex)
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
