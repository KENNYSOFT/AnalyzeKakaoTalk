package kr.KENNYSOFT.AnalyzeKakaoTalk;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;

/* 1. Base
 * Version: 4.3.0
 * Location: com.kakao.talk.util.cd
 * 
 * # Reference
 * Version: 3.7.0
 * Location: com.kakao.talk.skeleton.g.y
 */
public final class cd
{
	private static final char[] gga = { 42, 10, 8, 120, 7, 55, 11, 33, 9, 33, 10, 13, 5, 2, 10, 7 };
	private static final byte[] kal = { 10, 2, 3, -4, 20, 73, 47, -38, 27, -22, 11, -20, -22, 37, 36, 54 };
	private Cipher kly;
	private Cipher tat;

	public cd(String paramString1, String paramString2, byte[] paramArrayOfByte)
	{
		try
		{
			SecretKey localSecretKeySpec = new SecretKeySpec(SecretKeyFactory.getInstance(paramString1).generateSecret(new PBEKeySpec(gga, paramArrayOfByte, 2, 256)).getEncoded(),"AES");
			this.kly = Cipher.getInstance(paramString2);
			this.kly.init(1, localSecretKeySpec, new IvParameterSpec(kal));
			this.tat = Cipher.getInstance(paramString2);
			this.tat.init(2, localSecretKeySpec, new IvParameterSpec(kal));
			return;
		}
		catch (GeneralSecurityException paramString11)
		{
			throw new RuntimeException(paramString11);
		}
	}

	public cd(byte[] paramArrayOfByte)
	{
		this("PBEWITHSHAAND256BITAES-CBC-BC", "AES/CBC/PKCS5Padding", paramArrayOfByte);
	}

	public final String gga(String paramString)
	{
		if (paramString == null) {
			paramString = null;
		}
		for (;;)
		{
			try
			{
				paramString = new String(this.tat.doFinal(Base64.decode(paramString)), "UTF-8");
			}
			catch (Exception paramString1)
			{
				throw new RuntimeException(paramString1);
			}
			finally {}
			return paramString;
		}
	}

	public final String kal(String paramString)
	{
		if (paramString == null) {
			paramString = null;
		}
		for (;;)
		{
			try
			{
				paramString = new String(Base64.encode(this.kly.doFinal(paramString.getBytes("UTF-8"))));
			}
			catch (Exception paramString1)
			{
				throw new RuntimeException(paramString1);
			}
			finally {}
			return paramString;
		}
	}
}