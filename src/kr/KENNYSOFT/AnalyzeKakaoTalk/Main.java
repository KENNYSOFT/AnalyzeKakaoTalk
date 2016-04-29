package kr.KENNYSOFT.AnalyzeKakaoTalk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.sql.Types;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import com.opencsv.CSVWriter;

class AnalyzeKakaoTalk
{
	static long userId;

	public static void main(String args[]) throws Exception
	{
		getFiles();
		dump("KakaoTalk.db","chat_logs","KakaoTalk_dump.csv");
		dump("KakaoTalk2.db","friends","KakaoTalk2_dump.csv");
		decrypt("KakaoTalk_decrypted.csv");
		decrypt2("KakaoTalk2_decrypted.csv");
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

	static String getValue()
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
			return Long.valueOf(localcd.gga(getValue())).longValue();
		}
		catch(Exception e)
		{
			return 0;
		}
	}

	static void dump(String db,String table,String csv) throws IOException,SQLException
	{
		int now=0;
		Connection connection=DriverManager.getConnection("jdbc:sqlite:"+db);
		BufferedWriter buff=new BufferedWriter(new FileWriter(csv));
		buff.write("\ufeff");
		CSVWriter writer=new CSVWriter(buff,',');
		System.out.println("[Dump START] "+db+"/"+table+" -> "+csv+"");
		ResultSet rs=connection.createStatement().executeQuery("SELECT * FROM "+table);
		ResultSetMetaData rsmd=rs.getMetaData();
		String[] line=new String[rsmd.getColumnCount()];
		for(int i=0;i<rsmd.getColumnCount();++i)line[i]=rsmd.getColumnName(i+1);
		writer.writeNext(line);
		while(rs.next())
		{
			if(++now%10000==0)System.out.println("[Dump] Passed "+now+" items");
			for(int i=0;i<rsmd.getColumnCount();++i)
			{
				if(rsmd.getColumnType(i+1)==Types.INTEGER)line[i]=String.valueOf(rs.getLong(i+1));
				else line[i]=rs.getString(i+1);
			}
			writer.writeNext(line);
		}
		rs.close();
		connection.close();
		writer.close();
		System.out.println("[Dump END]");
		System.out.println("");
	}

	static void decrypt(String csv) throws IOException,SQLException
	{
		int now=0,toDecode[]=new int[]{5,6};
		Connection connection=DriverManager.getConnection("jdbc:sqlite:KakaoTalk.db");
		BufferedWriter buff=new BufferedWriter(new FileWriter(csv));
		buff.write("\ufeff");
		CSVWriter writer=new CSVWriter(buff,',');
		System.out.println("[Decrypt START] KakaoTalk.db/chat_logs -> "+csv);
		ResultSet rs=connection.createStatement().executeQuery("SELECT * FROM chat_logs");
		ResultSetMetaData rsmd=rs.getMetaData();
		String[] line=new String[rsmd.getColumnCount()];
		for(int i=0;i<rsmd.getColumnCount();++i)line[i]=rsmd.getColumnName(i+1);
		writer.writeNext(line);
		while(rs.next())
		{
			if(++now%1000==0)System.out.println("[Decrypt] Passed "+now+" items");
			for(int i=0;i<rsmd.getColumnCount();++i)
			{
				if(rsmd.getColumnType(i+1)==Types.INTEGER)line[i]=String.valueOf(rs.getLong(i+1));
				else line[i]=rs.getString(i+1);
			}
			int enc;
			if(line[10].contains("\"enc\":true"))enc=1;
			else enc=Integer.parseInt(line[10].substring(line[10].indexOf("\"enc\":")+6).split("\\D+")[0]);
			try
			{
				for(int i=0;i<toDecode.length;++i)line[toDecode[i]]=decode(Long.parseLong(line[4]),enc,line[toDecode[i]]);
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
		connection.close();
		writer.close();
		System.out.println("[Decrypt END]");
		System.out.println("");
	}

	static void decrypt2(String csv) throws IOException,SQLException
	{
		if(userId==0)userId=getUserId();
		int now=0,toDecode[]=new int[]{4,5,6,7,9,10,11,17,18,19,27,30,33};
		Connection connection=DriverManager.getConnection("jdbc:sqlite:KakaoTalk2.db");
		BufferedWriter buff=new BufferedWriter(new FileWriter(csv));
		buff.write("\ufeff");
		CSVWriter writer=new CSVWriter(buff,',');
		System.out.println("[Decrypt2 START] KakaoTalk2.db/friends -> "+csv);
		ResultSet rs=connection.createStatement().executeQuery("SELECT * FROM friends");
		ResultSetMetaData rsmd=rs.getMetaData();
		String[] line=new String[rsmd.getColumnCount()];
		for(int i=0;i<rsmd.getColumnCount();++i)line[i]=rsmd.getColumnName(i+1);
		writer.writeNext(line);
		while(rs.next())
		{
			if(++now%10000==0)System.out.println("[Decrypt2] Passed "+now+" items");
			for(int i=0;i<rsmd.getColumnCount();++i)
			{
				if(rsmd.getColumnType(i+1)==Types.INTEGER)line[i]=String.valueOf(rs.getLong(i+1));
				else line[i]=rs.getString(i+1);
			}
			int enc=rs.getInt("enc");
			try
			{
				for(int i=0;i<toDecode.length;++i)
				{
					if(line[toDecode[i]]==null||line[toDecode[i]].startsWith("{\""))continue;
					line[toDecode[i]]=decode(userId,enc,line[toDecode[i]]);
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
		connection.close();
		writer.close();
		System.out.println("[Decrypt2 END]");
		System.out.println("");
	}

	static String decode(long key,int enc,String message) throws Exception
	{
		if(message==null)return "";
		message=message.replace("　","").trim();
		if(message.length()==0||message.equals("{}")||message.equals("[]"))return "";
		return new n(key,enc).b(message);
	}
}