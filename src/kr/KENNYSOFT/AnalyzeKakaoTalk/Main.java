package kr.KENNYSOFT.AnalyzeKakaoTalk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import com.opencsv.CSVWriter;

class AnalyzeKakaoTalk
{
	public static void main(String args[]) throws Exception
	{
		getFiles();
		decrypt("KakaoTalk_decrypted.csv");
		decrypt2("KakaoTalk2_decrypted.csv");
		deleteFiles();
	}

	static void getFiles() throws IOException,InterruptedException
	{
		Process proc=null;
		System.out.println("[Get START]");
		proc=Runtime.getRuntime().exec("adb shell pm path com.kakao.talk".split(" "));
		proc.waitFor();
		BufferedReader br=new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String sLine;
		while((sLine=br.readLine())!=null)if(sLine.startsWith("package:"))Runtime.getRuntime().exec(("adb pull "+sLine.substring("package:".length())+" KakaoTalk.apk").split(" ")).waitFor();
		System.out.println("[Get] KakaoTalk.apk");
		Runtime.getRuntime().exec("adb install -r -d assets/kakaotalk210.apk".split(" ")).waitFor();
		System.out.println("[Get] Downgraded");
		for(int i=1;;++i)
		{
			if(i==1)System.out.println("[Get] Please check the screen");
			else System.out.println("[Get] Please check the screen ("+i+")");
			Runtime.getRuntime().exec("adb backup com.kakao.talk".split(" ")).waitFor();
			if(Runtime.getRuntime().exec("java -jar assets/abe.jar unpack backup.ab backup.tar".split(" ")).waitFor()==0)break;
		}
		System.out.println("[Get] backup.tar");
		TarArchiveInputStream tis=new TarArchiveInputStream(new FileInputStream("backup.tar"));
		TarArchiveEntry te;
		while((te=tis.getNextTarEntry())!=null)
		{
			String entryName=te.getName();
			if(!entryName.equals("apps/com.kakao.talk/db/KakaoTalk.db")&&!entryName.equals("apps/com.kakao.talk/db/KakaoTalk2.db")&&!entryName.equals("apps/com.kakao.talk/sp/KakaoTalk.perferences.xml"))continue;
			FileOutputStream fos=new FileOutputStream(entryName.substring(entryName.lastIndexOf('/')+1));
			int len;
			byte buffer[]=new byte[1024];
			while((len=tis.read(buffer))>0)fos.write(buffer,0,len);
			fos.close();
		}
		tis.close();
		Runtime.getRuntime().exec("adb install -r KakaoTalk.apk".split(" "));
		System.out.println("[Get END]");
		System.out.println("");
	}
	
	static void deleteFiles()
	{
		String toDelete[]=new String[]{"KakaoTalk.apk","backup.ab","backup.tar","KakaoTalk.db","KakaoTalk2.db","KakaoTalk.perferences.xml"};
		for(int i=0;i<toDelete.length;++i)new File(toDelete[i]).delete();
	}

	/* 1. Base
	 * Version: 4.3.0
	 * Location: com.kakao.talk.kal.sdhwkxyuak
	 * 
	 * # Return Value : "pch"
	 */
	static String getEntry()
	{
		return bbqzplvtdp.kal("6d7e7e");
	}

	static String getPreferenceValue()
	{
		String entry="name=\""+getEntry()+"\">",ret=null;
		try
		{
			BufferedReader in=new BufferedReader(new FileReader("KakaoTalk.perferences.xml"));
			String s;
			while((s=in.readLine())!=null)if(s.contains(entry))ret=s.substring(s.indexOf(entry)+entry.length()).split("<")[0];
			in.close();
		}
		catch(Exception e)
		{
		}
		return ret;
	}

	/* 1. Base
	 * Version: 4.3.0
	 * Location: com.kakao.talk.nck.ao
	 */
	static long getUserId()
	{
		byte[] egn = { 12, 10, -8, -43, -12, 44, 5, -8, -32, 7, 34, -24, -2, 3, 33, -33 };
		cd localcd = new cd(egn);
		try
		{
			return Long.valueOf(localcd.gga(getPreferenceValue())).longValue();
		}
		catch(Exception e)
		{
			return 0;
		}
	}
	
	static String decode(long key,int enc,String message) throws Exception
	{
		if(message==null||message.startsWith("{\""))return message;
		message=message.replace("　","").trim();
		if(message.length()==0||message.equals("{}")||message.equals("[]"))return message;
		return new n(key,enc).b(message);
	}

	static void dump(String db,String table,String csv) throws IOException,SQLException
	{
		int now=0,total;
		System.out.println("[Dump START] "+db+"/"+table+" -> "+csv+"");
		BufferedWriter buff=new BufferedWriter(new FileWriter(csv));
		buff.write("\ufeff");
		CSVWriter writer=new CSVWriter(buff,',');
		Connection connection=DriverManager.getConnection("jdbc:sqlite:"+db);
		Statement statement=connection.createStatement();
		ResultSet rs=statement.executeQuery("SELECT COUNT(*) FROM "+table);
		rs.next();
		total=rs.getInt(1);
		rs.close();
		rs=statement.executeQuery("SELECT * FROM "+table);
		ResultSetMetaData rsmd=rs.getMetaData();
		String[] line=new String[rsmd.getColumnCount()];
		for(int i=0;i<rsmd.getColumnCount();++i)line[i]=rsmd.getColumnName(i+1);
		writer.writeNext(line);
		while(rs.next())
		{
			if(++now%10000==0)System.out.println("[Dump] Passed "+now+" of "+total+" items");
			for(int i=0;i<rsmd.getColumnCount();++i)
			{
				if(rsmd.getColumnType(i+1)==Types.INTEGER)line[i]=String.valueOf(rs.getLong(i+1));
				else line[i]=rs.getString(i+1);
			}
			writer.writeNext(line);
		}
		rs.close();
		statement.close();
		connection.close();
		writer.close();
		buff.close();
		System.out.println("[Dump END]");
		System.out.println("");
	}

	static void decrypt(String csv) throws IOException,SQLException
	{
		int now=0,total;
		ArrayList<String> toDecode=new ArrayList<String>(Arrays.asList("message","attachment"));
		System.out.println("[Decrypt START] KakaoTalk.db/chat_logs -> "+csv);
		BufferedWriter buff=new BufferedWriter(new FileWriter(csv));
		buff.write("\ufeff");
		CSVWriter writer=new CSVWriter(buff,',');
		Connection connection=DriverManager.getConnection("jdbc:sqlite:KakaoTalk.db");
		Statement statement=connection.createStatement();
		ResultSet rs=statement.executeQuery("SELECT COUNT(*) FROM chat_logs");
		rs.next();
		total=rs.getInt(1);
		rs.close();
		rs=statement.executeQuery("SELECT * FROM chat_logs");
		ResultSetMetaData rsmd=rs.getMetaData();
		String[] line=new String[rsmd.getColumnCount()];
		for(int i=0;i<rsmd.getColumnCount();++i)line[i]=rsmd.getColumnName(i+1);
		writer.writeNext(line);
		while(rs.next())
		{
			if(++now%1000==0)System.out.println("[Decrypt] Passed "+now+" of "+total+" items");
			try
			{
				int enc;
				if(rs.getString("v").contains("\"enc\":true"))enc=1;
				else enc=Integer.parseInt(rs.getString("v").substring(rs.getString("v").indexOf("\"enc\":")+6).split("\\D+")[0]);
				for(int i=0;i<rsmd.getColumnCount();++i)
				{
					if(rsmd.getColumnType(i+1)==Types.INTEGER)line[i]=String.valueOf(rs.getLong(i+1));
					else
					{
						if(toDecode.contains(rsmd.getColumnName(i+1)))line[i]=decode(rs.getLong("user_id"),enc,rs.getString(i+1));
						else line[i]=rs.getString(i+1);
					}
				}
			}
			catch(Exception e)
			{
				System.out.println("[Decrypt] Error occured at "+line[0]);
				e.printStackTrace();
				continue;
			}
			writer.writeNext(line);
		}
		rs.close();
		statement.close();
		connection.close();
		writer.close();
		buff.close();
		System.out.println("[Decrypt END]");
		System.out.println("");
	}

	static void decrypt2(String csv) throws IOException,SQLException
	{
		int now=0,total;
		ArrayList<String> toDecode=new ArrayList<String>(Arrays.asList("uuid","phone_number","raw_phone_number","name","profile_image_url","full_profile_image_url","original_profile_image_url","status_message","v","ext","nick_name","contact_name","board_v"));
		System.out.println("[Decrypt2 START] KakaoTalk2.db/friends -> "+csv);
		long userId=getUserId();
		BufferedWriter buff=new BufferedWriter(new FileWriter(csv));
		buff.write("\ufeff");
		CSVWriter writer=new CSVWriter(buff,',');
		Connection connection=DriverManager.getConnection("jdbc:sqlite:KakaoTalk2.db");
		Statement statement=connection.createStatement();
		ResultSet rs=statement.executeQuery("SELECT COUNT(*) FROM friends");
		rs.next();
		total=rs.getInt(1);
		rs.close();
		rs=statement.executeQuery("SELECT * FROM friends");
		ResultSetMetaData rsmd=rs.getMetaData();
		String[] line=new String[rsmd.getColumnCount()];
		for(int i=0;i<rsmd.getColumnCount();++i)line[i]=rsmd.getColumnName(i+1);
		writer.writeNext(line);
		while(rs.next())
		{
			if(++now%1000==0)System.out.println("[Decrypt2] Passed "+now+" of "+total+" items");
			try
			{
				int enc=rs.getInt("enc");
				for(int i=0;i<rsmd.getColumnCount();++i)
				{
					if(rsmd.getColumnType(i+1)==Types.INTEGER)line[i]=String.valueOf(rs.getLong(i+1));
					else
					{
						if(toDecode.contains(rsmd.getColumnName(i+1)))line[i]=decode(userId,enc,rs.getString(i+1));
						else line[i]=rs.getString(i+1);
					}
				}
			}
			catch(Exception e)
			{
				System.out.println("[Decrypt2] Error occured at "+line[0]);
				e.printStackTrace();
				continue;
			}
			writer.writeNext(line);
		}
		rs.close();
		statement.close();
		connection.close();
		writer.close();
		buff.close();
		System.out.println("[Decrypt2 END]");
		System.out.println("");
	}
}