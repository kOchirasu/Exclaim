package packetLib;

import static tools.HexEncoding.byteArrayToHexString;

import java.io.ByteArrayInputStream;

public class PacketReader extends ByteArrayInputStream
{
	public PacketReader(byte[] buf)
	{
		super(buf);
	}
	
	public byte readByte() {
		return (byte)read();
	}
	public short readShort() {
		return (short)(read() | read() << 8);
	}
	public int readInt() {
		return read() | read() << 8 | read() << 16 | read() << 24;
	}
	public long readLong() {
		return readInt() | (long)readInt() << 32;
	}
	
	public String readString()
	{
		int length = readShort();
		String str = "";
		for(int i = 0; i < length; i++)
			str += (char)read();
		return str;
	}
	
	public String readHexString(int count)
	{
		return byteArrayToHexString(readBytes(count));
	}
	
	public byte[] readBytes(int count)
	{
		byte[] b = new byte[count];
		for(int i = 0; i < count; i++)
			b[i] = (byte)read();
		return b;
	}
	
	//Add toHexString/toByteArray using MARK
}
