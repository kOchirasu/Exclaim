package tools;

public class HexEncoding
{
	public static String byteToHex(byte b)
	{
		return String.format("%02X", b);
	}
	
	public static String byteArrayToHexString(byte[] bArr)
	{
		String str = "";
		for(byte b : bArr)
			str += byteToHex(b) + " ";
		return str;
	}
	
	public static byte hexToByte(String h)
	{
		if(!Validate.isValidHex(h))
			return 0;
		return (byte) Short.parseShort(h, 16);
	}
	
	public static byte[] hexStringToByteArray(String hString)
	{
		String[] hArr = hString.replaceAll("\\s", "").split("(?<=\\G..)");
		byte[] bArr = new byte[hArr.length];
		for(int i = 0; i < hArr.length; i++)
			bArr[i] = hexToByte(hArr[i]);
		return bArr;		
	}
}
