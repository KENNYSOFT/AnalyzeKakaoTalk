package kr.KENNYSOFT.AnalyzeKakaoTalk;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;

/* 1. Base
 * Version: 3.7.0
 * Location: com.kakao.talk.util.n
 * 
 * 2. Prefix List (~2)
 * Version: 4.3.0
 * Location: com.kakao.talk.util.jmfresedgb
 * 
 * 3. Prefix List (~16)
 * Version: 5.5.5
 * Location: o.aCa
 * 
 * 4. Prefix List (~17)
 * Version: 5.6.0
 * Location: ?
 */
public final class n
{
	private static final byte[] a = { 15, 8, 1, 0, 25, 71, 37, -36, 21, -11, 23, -32, -31, 21, 12, 53 };
	private static final char[] b = { 22, 8, 9, 111, 2, 23, 43, 8, 33, 33, 10, 16, 3, 3, 7, 6 };
	private byte[] c = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	private Cipher d;
	private Cipher e;
	private int f;
	private static final String[] prefix = { "", "", "12", "24", "18", "30", "36", "12", "48", "7", "35", "40", "17", "23", "29", "isabel", "kale", "sulli" };

	public n(long paramLong, int paramInt) throws Exception
	{
		if (paramLong <= 0L) {
			throw new IllegalStateException("userId is not ready.");
		}
		this.f = paramInt;
		this.c = a(paramLong);
		SecretKeySpec localSecretKeySpec = new SecretKeySpec(SecretKeyFactory.getInstance("PBEWITHSHAAND256BITAES-CBC-BC").generateSecret(new PBEKeySpec(b, this.c, 2, 256)).getEncoded(), "AES");
		this.d = Cipher.getInstance("AES/CBC/PKCS5Padding");
		this.d.init(1, localSecretKeySpec, new IvParameterSpec(a));
		this.e = Cipher.getInstance("AES/CBC/PKCS5Padding");
		this.e.init(2, localSecretKeySpec, new IvParameterSpec(a));
	}

	private byte[] a(long paramLong) throws Exception
	{
		byte[] arrayOfByte1 = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		if (paramLong <= 0L) {
			return arrayOfByte1;
		}
		byte[] arrayOfByte2 = (prefix[this.f] + String.valueOf(paramLong)).getBytes("UTF-8");
		for (int i = 0; (i < arrayOfByte1.length) && (arrayOfByte2.length > i); i++) {
			arrayOfByte1[i] = arrayOfByte2[i];
		}
		return arrayOfByte1;
	}
	
	public final String a(String paramString)
	{
		String str;
		if (paramString == null)
			str = null;
		while (true)
		{
			try
			{
				str = new String(Base64.encode(this.d.doFinal(paramString.getBytes("UTF-8"))));
			}
			catch (Exception localException)
			{
				throw new RuntimeException(localException);
			}
			finally {}
			return str;
		}
	}
	
	public final String b(String paramString) throws Exception
	{
		String str;
		if (paramString == null)
			str = null;
		while (true)
		{
			try
			{
				str = new String(this.e.doFinal(Base64.decode(paramString)), "UTF-8");
			}
			catch (Exception localException)
			{
				throw new RuntimeException(localException);
			}
			finally {}
			return str;
		}
	}
}