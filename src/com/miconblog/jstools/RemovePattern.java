package com.miconblog.jstools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.*;


public class RemovePattern {
	private String srcFile = "";
	private String destFile = "";
	private int findPatternCount = 0;
	private Pattern userPrn;
	private String encoding;
	
	RemovePattern (String srcFile, Pattern prn, String destFile, String encoding) throws IOException{
		this.srcFile = srcFile;
		this.destFile = destFile;
		this.userPrn = prn;
		this.encoding = encoding;
	}
	
	public void process() throws IOException{
		BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream(srcFile), this.encoding) );
		BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(this.destFile), this.encoding) );
		
	    String strLine;
  	  
	    while ((strLine = in.readLine()) != null) {
	    	 
	    	strLine = removePattern(strLine);
	    	
	    	if(this.hasTabSpaceNothing(strLine)){ continue; }
	    	  
	    	// 실제 코드만 쓴다.
	    	writeStrLine(strLine, out);
	    }
	    in.close();
		out.close();
		System.out.println("[INFO] Completed Removing UserPattern");
		System.out.println("[INFO] Total removed lines: " + this.findPatternCount );
	}
	
	private String removePattern(String strLine) {
		Matcher matcher = userPrn.matcher(strLine);
		
		if(matcher.find()){
			this.findPatternCount++;
			return strLine.replaceAll(userPrn.toString(), "");
		}
		return strLine;
	}

	private void writeStrLine(String strLine, BufferedWriter out) throws IOException{
		if(!hasTabSpaceNothing(strLine)){
			out.write(strLine+"\r\n");  
   	  	}
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
}
