package kr.KENNYSOFT.AnalyzeKakaoTalk;

import java.io.IOException;
import java.sql.SQLException;

public class Main
{
	public static void main(String args[]) throws IOException,InterruptedException,SQLException
	{
		AnalyzeKakaoTalk analyzer=new AnalyzeKakaoTalk();
		analyzer.getFiles();
		analyzer.decrypt("KakaoTalk_decrypted.csv");
		analyzer.decrypt2("KakaoTalk2_decrypted.csv");
		analyzer.analyze("KakaoTalk_analyzed.xlsx");
		analyzer.deleteFiles();
	}
}