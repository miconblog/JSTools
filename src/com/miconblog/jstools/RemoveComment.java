/**
 * 라인 주석과 블럭 주석을 모두 제거한다.
 * 
 * @author SohnBD
 */

package com.miconblog.jstools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.*;

public class RemoveComment {
	private String srcFile;
	private String destFile = "";
	private String encoding = "";
	private static boolean bBlockComment = false; 
	  
	public RemoveComment(){}
	RemoveComment (String srcFile, String destFile, String encoding) throws IOException{
		this.srcFile = srcFile;
		this.destFile = destFile;
		this.encoding = encoding;
	}
	
	/**
	 * 한줄짜리 블럭주석을 제거하면, bBlockComment = false;
	 * 여러줄짜리 블러주석을 제거중이라면, bBlockComment = true;
	 */
	public static String removeBlockComment(String strLine){
		Pattern cmtEndPrn = Pattern.compile(".*\\*/");
		Pattern cmtStartPrn = Pattern.compile("(?:/\\*.*)");
		Matcher matcher = cmtStartPrn.matcher(strLine);
  	  	
		// 시작 블럭주석을 포함하고 있다면,
		while (matcher.find()) {
  	  		bBlockComment = true; 
  	  		
  	  		
  	  		// 시작 블럭주석이 따옴표 안에 있다면, 시작 블럭이 아니다.
  	  		Pattern prnValue = Pattern.compile("\"[^\"]*/\\*[^\"]*\"|'[^']*/\\*[^']*'");
  	  		matcher = prnValue.matcher(strLine);
			if(matcher.find()){
				bBlockComment = false;
			}
  		  
  	  		// 닫는 블락 주석도 있다면, 한줄 블럭 주석 제거 
  	  		matcher = cmtEndPrn.matcher(strLine);
  	  		if(matcher.find()){
  	  			bBlockComment = false;
  	  		}
  	  		
  	  		// 블럭 주석 제거 (한줄 안에 있는 부분 블럭주석인 경우는 부분만 제거) 
	  	    strLine = strLine.replaceAll("/\\*.*\\*/","");
	  	    
	  	    
	  	    // 그래도 뭔가 남아있다면, 
	  	    if(bBlockComment){
	  	      strLine = strLine.replaceAll("/\\*.*","");
	  	    }
	  	    
	  	    
  	  	}
  	  	return strLine;
	}
	
	/**
	 * 라인 주석 제거
	 */
	public static String removeSingleLineComment(String strLine){
		if(!strLine.contains("//")){
			return strLine;
		}

		// document.write('xxx'); 형태의 경우 xxx 문자열은 탐색하지마!
		Pattern prnDomWrite = Pattern.compile("(document.write\\(.*\\))(.*)");
		Matcher matcher = prnDomWrite.matcher(strLine);
		if(matcher.find()){
			return matcher.group(1) + removeSingleLineComment(matcher.group(2)).trim();
		}
		
		// 따옴표 안에 주석표시가 포함되어 있는 경우는 무시하고 다음 케이스를 찾는다.
		Pattern prnValue = Pattern.compile("\"[^\"]*//[^\"]*\"|'[^']*//[^']*'");
		Pattern prnComment = Pattern.compile("(?:[^(?:http:)]|^)(//.*)");
		matcher = prnValue.matcher(strLine);

		if(matcher.find()){
			Matcher matcherComment = prnComment.matcher(strLine);
			if(matcherComment.find(matcher.end())){
			//	System.out.println("따옴표값 "+matcherComment.start() + "~"+matcherComment.end() +"번 문자열 제거: " + strLine);
				strLine = strLine.replace(matcherComment.group(1), "");
			//	System.out.println("결과:[" + strLine +"]\n\n");
			}
			return strLine;
		}
		
		// 정규식 필터링
		Pattern prnRegx = Pattern.compile("[^/]*//[.igm]");
		matcher = prnRegx.matcher(strLine);
		if(matcher.find()){
			Matcher matcherComment = prnComment.matcher(strLine);
			if(matcherComment.find(matcher.end())){
			//	System.out.println("정규식 "+matcherComment.start() + "~"+matcherComment.end() +"번 문자열 제거: " + strLine);
				strLine = strLine.replace(matcherComment.group(1), "");
			//	System.out.println("결과:[" + strLine +"]\n\n");
			}
			return strLine;
		}
		
		
		// 메소드 안에 정규식 필터링
		prnRegx = Pattern.compile("(/[^/]*//)");
		matcher = prnRegx.matcher(strLine);
		if(matcher.find()){
			Matcher matcherComment = prnComment.matcher(strLine);
			if(matcherComment.find(matcher.end())){
			//	System.out.println("정규식 "+matcherComment.start() + "~"+matcherComment.end() +"번 문자열 제거: " + strLine);
				strLine = strLine.replace(matcherComment.group(1), "");
			//	System.out.println("결과:[" + strLine +"]\n\n");
			}
			return strLine;
		}
		
		// 일반 주석 필터링
		matcher = prnComment.matcher(strLine);
		if(matcher.find()){
		//	System.out.println("일반주석 "+matcher.start() + "~"+matcher.end() +"번 문자열 제거: " + strLine);
			strLine = strLine.replace(matcher.group(1), "");
		//	System.out.println("결과:[" + strLine +"]\n\n");
		}
		return strLine;
	}
	
	/**
     * 빈공백과 빈탭, 그리고 쓸것이 있는지 확인한다.
     */
	private boolean hasTabSpaceNothing(String strLine){
		Pattern cmtSpaceTab = Pattern.compile("^(?:\\s+|\\t+)$");
		Matcher matcher = cmtSpaceTab.matcher(strLine);
  	  	if(matcher.find()){ return true; }
	    if(strLine.length() < 1){ return true; }
		return false;
	}
	
	/**
	 * 쓸것이 있으면 쓴다.
	 * @param strLine
	 * @param out 
	 * @throws IOException
	 */
	private void writeStrLine(String strLine, BufferedWriter out) throws IOException{
		if(!hasTabSpaceNothing(strLine)){
			out.write(strLine+"\r\n");  
   	  	}
	}
	
	/**
	 * JSTools2 전용
	 * @throws IOException 
	 */
	public void process() throws IOException {
		Pattern cmtEndPrn = Pattern.compile(".*\\*/");
		Matcher matcher;
		BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream(srcFile), this.encoding) );
		BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(this.destFile), this.encoding) );
   	  
	    String strLine;
	    while ((strLine = in.readLine()) != null) {
	    	/**
	    	 * 닫는 블럭주석인지 확인
	    	 *  1- 블럭이면 : 닫는 블럭인지 확인하고, 닫는 블력이 아니면, 이 라인은 그냥 패스
	    	 *  2- 블럭이 아니면: 라인주석제거 고고싱
	    	 */
	    	if(bBlockComment){
	    		matcher = cmtEndPrn.matcher(strLine);
	    		if(matcher.find()){
	    			bBlockComment = false;
			  	    strLine = strLine.replaceAll(cmtEndPrn.toString(),"");
	    		}
	    		continue;
	    	}
	    	  	    	  
	    	// 라인 주석 제거
	    	strLine = removeSingleLineComment(strLine);
	  	      
	    	  
	    	// 한줄짜리 블럭주석이 아니면, 계속해서 주석라인을 제거한다.
	    	strLine = removeBlockComment(strLine);
	    	  
	    	// 블러주석제거후, 탭간격을 맞춘다.
	    	if(bBlockComment){
	    		writeStrLine(strLine, out);
	    		continue;
	    	}
	    	  
	    	// 실제 코드만 쓴다.
	    	writeStrLine(strLine, out);
	    }
	    in.close();
		out.close();
	}
}