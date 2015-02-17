package user;

import java.io.IOException;

public class Program
{
	public static void main(String[] args) throws IOException
	{
		/*PacketWriter pw = new PacketWriter(12);
		pw.writeShort(323);
		pw.writeInt(-123);
		pw.writeLong(992319323123L);
		pw.writeString("hey?");
		pw.writeHexString("AB CD EFFFF");
		System.out.println(pw);
		PacketReader pr = new PacketReader(pw.toByteArray());
		System.out.println(pr.readByte());
		System.out.println(pr.readShort());
		System.out.println(pr.readInt());
		System.out.println(pr.readLong());
		System.out.println(pr.readString());
		System.out.println(pr.readHexString(5));
		pr.close();
		System.exit(0);*/
		Client c = new Client();
		Listener l = new Listener(c);
		Thread listenThread = new Thread(l);
		listenThread.start();
		c.run();
	}
}
