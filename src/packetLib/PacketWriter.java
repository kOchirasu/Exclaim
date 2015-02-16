package packetLib;

import static tools.HexEncoding.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PacketWriter extends ByteArrayOutputStream
{
	public PacketWriter(int header) throws IOException
	{
		writeByte(header);
	}
	
	private void append(long v, int length)
	{
		for(int i = 0; i < length; i++)
		{
			write((byte)v);
			v >>= 8;
		}
	}
	
	public void writeByte(int v) {
		write((byte)v);
	}
	public void writeShort(int v) {
		append(v, 2);
	}
	public void writeInt(int v) {
		append(v, 4);
	}
	public void writeLong(long v) {
		append(v, 8);
	}
	
	//Might need to fix to account for excessively long strings
	public void writeString(String s)
	{
		append(s.length(), 2);
		for(int i = 0; i < s.length(); i++)
			write(s.charAt(i));
	}
	
	public void writeHexString(String s)
	{
		writeBytes(hexStringToByteArray(s));
	}
	
	public void writeBytes(byte[] bArr)
	{
		for(byte b : bArr)
			write(b);
	}
	
	public String toString() {
		return byteArrayToHexString(toByteArray());
	}
}
