package kr.KENNYSOFT.AnalyzeKakaoTalk;

/* 1. Base
 * Version: 4.3.0
 * Location: com.kakao.talk.kal.bbqzplvtdp
 */
@SuppressWarnings("unused")
public final class bbqzplvtdp
{
	private static final char[] gga = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102 };
	private static final byte[] kal = { 29, 29, 22, 33, 9, 4, 3, 8, 20, 11, 29, 20, 48, 30, 24, 44 };

	private static byte[] gga(String paramString)
	{
		int j = 0;
		byte[] arrayOfByte = new byte[paramString.length() / 2];
		int i = 0;
		while (j + 1 < paramString.length())
		{
			arrayOfByte[i] = ((byte)(Character.digit(paramString.charAt(j), 16) << 4));
			arrayOfByte[i] = ((byte)(arrayOfByte[i] + (byte)Character.digit(paramString.charAt(j + 1), 16)));
			j += 2;
			i += 1;
		}
		return arrayOfByte;
	}

	public static String kal(String paramString)
	{
		return kal(new String(gga(paramString)), kal);
	}

	private static String kal(String paramString, byte[] paramArrayOfByte)
	{
		if ((paramString == null) || (paramArrayOfByte == null)) {
			return null;
		}
		try
		{
			int j = paramString.length();
			int k = paramArrayOfByte.length;
			char[] arrayOfChar = new char[j];
			int i = 0;
			while (i < j)
			{
				arrayOfChar[i] = ((char)(paramString.charAt(i) ^ paramArrayOfByte[(i % k)]));
				i += 1;
			}
			paramString = new String(arrayOfChar);
			return paramString;
		}
		catch (Exception paramString2) {}
		return null;
	}
}