package kr.KENNYSOFT.AnalyzeKakaoTalk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.opencsv.CSVWriter;

class AnalyzeKakaoTalk
{
	Connection connection,connection2;
	Statement statement,statement2;
	
	AnalyzeKakaoTalk() throws SQLException
	{
		connection=DriverManager.getConnection("jdbc:sqlite:KakaoTalk.db");
		statement=connection.createStatement();
		connection2=DriverManager.getConnection("jdbc:sqlite:KakaoTalk2.db");
		statement2=connection2.createStatement();
	}
	
	@Override
	public void finalize() throws SQLException
	{
		statement.close();
		connection.close();
		statement2.close();
		connection2.close();
	}
	
	public static void main(String args[]) throws IOException,InterruptedException,SQLException
	{
		AnalyzeKakaoTalk analyzer=new AnalyzeKakaoTalk();
		analyzer.getFiles();
		analyzer.decrypt("KakaoTalk_decrypted.csv");
		analyzer.decrypt2("KakaoTalk2_decrypted.csv");
		analyzer.analyze("KakaoTalk_analyzed.xlsx");
		analyzer.deleteFiles();
	}

	void getFiles() throws IOException,InterruptedException
	{
		System.out.println("[Get START]");
		Process proc=Runtime.getRuntime().exec("assets\\adb shell pm path com.kakao.talk".split(" "));
		proc.waitFor();
		BufferedReader br=new BufferedReader(new InputStreamReader(proc.getInputStream()));
		String sLine;
		while((sLine=br.readLine())!=null)if(sLine.startsWith("package:"))Runtime.getRuntime().exec(("assets\\adb pull "+sLine.substring("package:".length())+" KakaoTalk.apk").split(" ")).waitFor();
		System.out.println("[Get] KakaoTalk.apk");
		proc=Runtime.getRuntime().exec("assets\\adb install -r assets\\KakaoTalk210.apk".split(" "));
		proc.waitFor();
		br=new BufferedReader(new InputStreamReader(proc.getInputStream()));
		while((sLine=br.readLine())!=null)if(sLine.contains("INSTALL_FAILED_VERSION_DOWNGRADE"))Runtime.getRuntime().exec("assets\\adb install -r -d assets\\KakaoTalk210.apk".split(" ")).waitFor();
		System.out.println("[Get] Downgraded");
		for(int i=1;;++i)
		{
			if(i==1)System.out.println("[Get] Please check the screen");
			else System.out.println("[Get] Please check the screen ("+i+")");
			Runtime.getRuntime().exec("assets\\adb backup com.kakao.talk".split(" ")).waitFor();
			if(Runtime.getRuntime().exec("java -jar assets\\abe.jar unpack backup.ab backup.tar".split(" ")).waitFor()==0)break;
		}
		Runtime.getRuntime().exec("assets\\adb install -r KakaoTalk.apk".split(" "));
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
		System.out.println("[Get END]");
		System.out.println("");
	}

	void deleteFiles()
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
	static String getUserIdEntry()
	{
		return bbqzplvtdp.kal("6d7e7e");
	}

	String getPreferenceValue(String entry)
	{
		String tag="name=\""+entry+"\">",ret=null;
		try
		{
			BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream("KakaoTalk.perferences.xml"),"UTF-8"));
			String s;
			while((s=in.readLine())!=null)if(s.contains(tag))ret=s.substring(s.indexOf(tag)+tag.length()).split("<")[0];
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
	long getUserId()
	{
		byte[] egn = { 12, 10, -8, -43, -12, 44, 5, -8, -32, 7, 34, -24, -2, 3, 33, -33 };
		cd localcd = new cd(egn);
		try
		{
			return Long.valueOf(localcd.gga(getPreferenceValue(getUserIdEntry()))).longValue();
		}
		catch(Exception e)
		{
			return 0;
		}
	}

	String decode(long key,int enc,String message) throws Exception
	{
		if(message==null||message.startsWith("{\""))return message;
		message=message.replace("　","").trim();
		if(message.length()==0||message.equals("{}")||message.equals("[]"))return message;
		return new n(key,enc).b(message);
	}

	void decrypt(String csv) throws IOException,SQLException
	{
		int now=0,total;
		ArrayList<String> toDecode=new ArrayList<String>(Arrays.asList("message","attachment"));
		System.out.println("[Decrypt START] KakaoTalk.db/chat_logs -> "+csv);
		BufferedWriter buff=new BufferedWriter(new FileWriter(csv));
		CSVWriter writer=new CSVWriter(buff,',');
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
			if(++now%10000==0)System.out.println("[Decrypt] Passed "+now+" of "+total+" items");
			try
			{
				int enc=1;
				try
				{
					enc=((Long)((JSONObject)new JSONParser().parse(rs.getString("v"))).get("enc")).intValue();
				}
				catch(Exception e)
				{
				}
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
				System.out.println("[Decrypt] Error occured at "+rs.getInt("_id"));
				e.printStackTrace();
				continue;
			}
			writer.writeNext(line);
		}
		rs.close();
		writer.close();
		buff.close();
		System.out.println("[Decrypt END]");
		System.out.println("");
	}

	void decrypt2(String csv) throws IOException,SQLException
	{
		int now=0,total;
		ArrayList<String> toDecode=new ArrayList<String>(Arrays.asList("uuid","phone_number","raw_phone_number","name","profile_image_url","full_profile_image_url","original_profile_image_url","status_message","v","ext","nick_name","contact_name","board_v"));
		System.out.println("[Decrypt2 START] KakaoTalk2.db/friends -> "+csv);
		long userId=getUserId();
		BufferedWriter buff=new BufferedWriter(new FileWriter(csv));
		CSVWriter writer=new CSVWriter(buff,',');
		ResultSet rs=statement2.executeQuery("SELECT COUNT(*) FROM friends");
		rs.next();
		total=rs.getInt(1);
		rs.close();
		rs=statement2.executeQuery("SELECT * FROM friends");
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
				System.out.println("[Decrypt2] Error occured at "+rs.getInt("_id"));
				e.printStackTrace();
				continue;
			}
			writer.writeNext(line);
		}
		rs.close();
		writer.close();
		buff.close();
		System.out.println("[Decrypt2 END]");
		System.out.println("");
	}
	
	HashMap<Long,String> parseFriends() throws IOException,SQLException
	{
		int now=0,total;
		System.out.println("[Parse START] KakaoTalk2.db/friends");
		HashMap<Long,String> friends=new HashMap<>();
		long userId=getUserId();
		friends.put(userId,getPreferenceValue("nickName"));
		ResultSet rs=statement2.executeQuery("SELECT COUNT(*) FROM friends");
		rs.next();
		total=rs.getInt(1);
		rs.close();
		rs=statement2.executeQuery("SELECT * FROM friends");
		while(rs.next())
		{
			if(++now%1000==0)System.out.println("[Parse] Passed "+now+" of "+total+" items");
			try
			{
				String name=rs.getString("nick_name");
				if(name==null||name.length()==0)name=rs.getString("contact_name");
				if(name==null||name.length()==0)name=rs.getString("name");
				friends.put(rs.getLong("id"),decode(userId,rs.getInt("enc"),name));
			}
			catch(Exception e)
			{
				System.out.println("[Parse] Error occured at "+rs.getInt("_id"));
				e.printStackTrace();
				continue;
			}
		}
		rs.close();
		System.out.println("[Parse END]");
		System.out.println("");
		return friends;
	}
	
	HashMap<Long,String> parseOpenProfiles() throws IOException,SQLException
	{
		int now=0,total;
		System.out.println("[Parse START] KakaoTalk.db/chat_rooms");
		HashMap<Long,String> openProfiles=new HashMap<>();
		ResultSet rs=statement.executeQuery("SELECT COUNT(*) FROM chat_rooms WHERE link_id<>-1");
		rs.next();
		total=rs.getInt(1);
		rs.close();
		rs=statement.executeQuery("SELECT * FROM chat_rooms WHERE link_id<>-1");
		while(rs.next())
		{
			if(++now%1000==0)System.out.println("[Parse] Passed "+now+" of "+total+" items");
			try
			{
				ResultSet rs2=statement2.executeQuery("SELECT * FROM open_profile WHERE link_id="+rs.getLong("link_id"));
				if(rs2.next())openProfiles.put(rs.getLong("id"),rs2.getString("nickname"));
			}
			catch(Exception e)
			{
				System.out.println("[Parse] Error occured at "+rs.getInt("_id"));
				e.printStackTrace();
				continue;
			}
		}
		rs.close();
		System.out.println("[Parse END]");
		System.out.println("");
		return openProfiles;
	}
	
	HashMap<Long,SXSSFSheet> parseRooms(SXSSFWorkbook workbook,HashMap<Long,String> friends) throws SQLException
	{
		int now=0,total;
		System.out.println("[Parse START] KakaoTalk.db/chat_rooms");
		HashMap<Long,SXSSFSheet> rooms=new HashMap<>();
		SXSSFSheet sheet=(SXSSFSheet)workbook.createSheet("(대화방없음)");
		sheet.setColumnWidth(3,288*32);
		sheet.setColumnWidth(4,144*32);
		SXSSFRow row=sheet.createRow(0);
		row.createCell(0).setCellValue("ID");
		row.createCell(1).setCellValue("보낸 사람");
		row.createCell(2).setCellValue("보낸 사람 ID");
		row.createCell(3).setCellValue("구분");
		row.createCell(4).setCellValue("내용");
		row.createCell(5).setCellValue("시간");
		row.createCell(6).setCellValue("비고");
		rooms.put(0L,sheet);
		ResultSet rs=statement.executeQuery("SELECT COUNT(*) FROM chat_rooms");
		rs.next();
		total=rs.getInt(1);
		rs.close();
		rs=statement.executeQuery("SELECT * FROM chat_rooms ORDER BY last_log_id DESC");
		while(rs.next())
		{
			if(++now%100==0)System.out.println("[Parse] Passed "+now+" of "+total+" items");
			String name=rs.getString("active_member_ids").replace("[","").replace("]","");
			if(name.length()==0)name="대화상대없음";
			else
			{
				for(String id:name.split(","))
				{
					if(friends.get(Long.parseLong(id))==null)name=name.replace(id,"(알수없음)");
					else name=name.replace(id,friends.get(Long.parseLong(id)));
				}
			}
			try
			{
				if(rs.getLong("link_id")!=-1)
				{
					ResultSet rs2=statement2.executeQuery("SELECT * FROM open_link WHERE id="+rs.getLong("link_id"));
					if(rs2.next())name=rs2.getString("name");
				}
				JSONArray array=(JSONArray)new JSONParser().parse(rs.getString("meta"));
				for(int i=0;i<array.size();++i)
				{
					if(((Long)((JSONObject)array.get(i)).get("type")).intValue()==3)
					{
						name=(String)((JSONObject)array.get(i)).get("content");
						break;
					}
				}
			}
			catch(Exception e)
			{
			}
			try
			{
				name=(String)((JSONObject)new JSONParser().parse(rs.getString("private_meta"))).get("name");
			}
			catch(Exception e)
			{
			}
			name=WorkbookUtil.createSafeSheetName(name);
			if(workbook.getSheet(name)!=null)
			{
				String name2;
				for(int i=2;;++i)
				{
					name2=name+" ("+i+")";
					if(name2.length()>31)name2=name.substring(0,31-(" ("+i+")").length())+" ("+i+")";
					if(workbook.getSheet(name2)==null)break;
				}
				name=name2;
			}
			sheet=(SXSSFSheet)workbook.createSheet(name);
			sheet.setColumnWidth(4,288*32);
			sheet.setColumnWidth(5,144*32);
			row=sheet.createRow(0);
			row.createCell(0).setCellValue("ID");
			row.createCell(1).setCellValue("보낸 사람");
			row.createCell(2).setCellValue("보낸 사람 ID");
			row.createCell(3).setCellValue("구분");
			row.createCell(4).setCellValue("내용");
			row.createCell(5).setCellValue("시간");
			row.createCell(6).setCellValue("비고");
			rooms.put(rs.getLong("id"),sheet);
		}
		rs.close();
		System.out.println("[Parse END]");
		System.out.println("");
		return rooms;
	}
	
	HashMap<Integer,String> parseTypes()
	{
		HashMap<Integer,String> types=new HashMap<>();
		types.put(0,"시스템");
		types.put(1,"메시지");
		types.put(2,"사진");
		types.put(3,"동영상");
		types.put(4,"연락처");
		types.put(5,"음성메시지");
		types.put(6,"이모티콘");
		types.put(7,"선물");
		types.put(9,"플러스친구");
		types.put(12,"이모티콘");
		types.put(13,"일정");
		types.put(14,"투표");
		types.put(16,"위치");
		types.put(17,"프로필");
		types.put(18,"파일");
		types.put(20,"이모티콘");
		types.put(21,"탭탭");
		types.put(22,"이모티콘");
		types.put(23,"검색");
		types.put(24,"게시판");
		types.put(51,"보이스톡");
		types.put(71,"광고");
		types.put(81,"상세소식");
		return types;
	}

	void analyze(String xlsx) throws IOException,SQLException
	{
		int now=0,total;
		SXSSFWorkbook workbook=new SXSSFWorkbook();
		workbook.getXSSFWorkbook().getProperties().getCoreProperties().setCreator("AnalyzeKakaoTalk");
		HashMap<Long,String> friends=parseFriends();
		HashMap<Long,String> openProfiles=parseOpenProfiles();
		HashMap<Long,SXSSFSheet> rooms=parseRooms(workbook,friends);
		HashMap<Integer,String> types=parseTypes();
		System.out.println("[Analyze START] KakaoTalk -> "+xlsx);
		ResultSet rs=statement.executeQuery("SELECT COUNT(*) FROM chat_logs");
		rs.next();
		total=rs.getInt(1);
		rs.close();
		rs=statement.executeQuery("SELECT * FROM chat_logs ORDER BY id ASC");
		while(rs.next())
		{
			if(++now%10000==0)System.out.println("[Analyze] Passed "+now+" of "+total+" items");
			SXSSFRow row=null;
			try
			{
				row=rooms.get(rs.getLong("chat_id")).createRow(rooms.get(rs.getLong("chat_id")).getLastRowNum()+1);
			}
			catch(Exception e)
			{
				row=rooms.get(0L).createRow(rooms.get(0L).getLastRowNum()+1);
			}
			try
			{
				row.createCell(0).setCellValue(rs.getLong("id"));
				int enc;
				if(rs.getString("v").contains("\"enc\":true"))enc=1;
				else enc=Integer.parseInt(rs.getString("v").substring(rs.getString("v").indexOf("\"enc\":")+6).split("\\D+")[0]);
				if(rs.getLong("user_id")==getUserId()&&openProfiles.get(rs.getLong("chat_id"))!=null)row.createCell(1).setCellValue(openProfiles.get(rs.getLong("chat_id")));
				else if(friends.get(rs.getLong("user_id"))==null)row.createCell(1).setCellValue("(알수없음)");
				else row.createCell(1).setCellValue(friends.get(rs.getLong("user_id")));
				row.createCell(2).setCellValue(rs.getLong("user_id"));
				String message=decode(rs.getLong("user_id"),enc,rs.getString("message"));
				if(rs.getLong("type")==0)
				{
					JSONObject json=(JSONObject)new JSONParser().parse(message);
					switch(((Long)json.get("feedType")).intValue())
					{
					case 1:
						message=((JSONObject)json.get("inviter")).get("nickName")+"님이 ";
						JSONArray members=(JSONArray)json.get("members");
						for(int i=0;i<members.size();++i)
						{
							message=message+((JSONObject)members.get(i)).get("nickName")+"님";
							if(i<members.size()-2) message=message+", ";
							else if(i==members.size()-2)message=message+"과 ";
						}
						message=message+"을 초대했습니다.";
						break;
					case 2:
						message=(String)((JSONObject)json.get("member")).get("nickName")+"님이 나갔습니다.";
						break;
					case 4:
						message=((JSONObject)((JSONArray)json.get("members")).get(0)).get("nickName")+"님이 들어왔습니다.";
						break;
					case 5:
						message="오픈채팅방 링크가 삭제되었습니다. 채팅방을 나가면 다시 참여할 수 없게됩니다.";
						break;
					case 6:
						message="존재하지 않거나 삭제된 오픈채팅방입니다.";
						break;
					}
					message="== "+message+" ==";
				}
				if(types.get((int)rs.getLong("type"))==null)row.createCell(3).setCellValue("알수없음("+rs.getLong("type")+")");
				else row.createCell(3).setCellValue(types.get((int)rs.getLong("type")));
				row.createCell(4).setCellValue(message);
				row.createCell(5).setCellValue(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(rs.getLong("created_at")*1000)));
				row.createCell(6).setCellValue(decode(rs.getLong("user_id"),enc,rs.getString("attachment")));
			}
			catch(Exception e)
			{
				System.out.println("[Analyze] Error occured at "+rs.getInt("_id"));
				e.printStackTrace();
				continue;
			}
		}
		rs.close();
		if(rooms.get(0L).getLastRowNum()==0)workbook.removeSheetAt(workbook.getSheetIndex(rooms.get(0L)));
		workbook.write(new FileOutputStream(xlsx));
		workbook.close();
		System.out.println("[Analyze END]");
		System.out.println("");
	}
}