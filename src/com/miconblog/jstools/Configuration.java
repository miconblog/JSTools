package com.miconblog.jstools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Configuration {

	private String version = "2.7.0 nightly";
	private String releaseDate = "2012-08-14";
	private String output;
	private int compress;
	private int lineBreak;
	private String encoding;
	private boolean jslint;
	private boolean jsmeter;
	private boolean keeplicense;
	private boolean lineNumber;
	private boolean listout;
	private boolean tplcomplie;
	private Pattern removePattern;
	private ArrayList<String> error;
	private String fileType;
	private String mergeList;
	private String infoFile;
	private String infoType;
	private String jslistOption;
	private String tplcomplieCallback;

	public Configuration() {
		initialize();
	}

	private void initialize(){
		this.error = new ArrayList<String>();
		this.mergeList = "";
		this.output = "";
		this.compress = 0;
		this.lineBreak = -1;
		this.encoding = "UTF-8";
		this.removePattern = null;
		this.jslint = false;
		this.jsmeter = false;
		this.fileType = "JS";
		this.infoType = "";
		this.infoFile = "";
		this.jslistOption = "";
		this.lineNumber = false;
		this.listout = false;
	}
	
	public void printOptions(){
		System.out.printf("[JSTools] version %s (released %s)\n", version, releaseDate);
		if(error.size() > 0){
			System.err.printf("\t%-15s:%s\n", "WRONG-OPTIONS", error);
		}
		
		if(!tplcomplie && !listout){
			System.out.printf("\t%-15s:%d\n", "COMPRESS", compress);
			System.out.printf("\t%-15s:%s\n", "FILE-TYPE", fileType);
		}
		
		if(lineBreak > -1){
			System.out.printf("\t%-15s:%d\n", "LINE-BREAK", lineBreak);
		}
		
		System.out.printf("\t%-15s:%s\n","ENCODING", encoding);
		
		if(!mergeList.equals("")){
			System.out.printf("\t%-15s:%s\n","LIST-FILE", mergeList);
		}
		
		if(!output.equals("")){
			if(listout){
				System.out.printf("\t%-15s:%s\n","LIST-OUT", output);	
				return;
			}
			
			if(tplcomplie){
				System.out.printf("\t%-15s:%s\n","TPL-OUT", output);	
				return;
			}
			
			System.out.printf("\t%-15s:%s\n","OUTPUT", output);
		}
		
		if(!infoFile.equals("")){
			System.out.printf("\t%-15s:%s\n","STATICS", infoFile);
		}
		
		if(removePattern != null){
			System.out.printf("\t%-15s:%s\n","REMOVING PATTERN", removePattern);
		}
		
		if(!jslistOption.equals("")){
			System.out.printf("\t%-15s:%s [%s]\n","JSLint", jslint, jslistOption);
		}else{
			System.out.printf("\t%-15s:%s\n","JSLint", jslint);
		}
		
		System.out.printf("\t%-15s:%s\n","JSMeter", jsmeter);
		System.out.printf("\t%-15s:%s\n","LineNumber", lineNumber);
		System.out.printf("\t%-15s:%s\n","KeepLicense", keeplicense);
	}
	
	public void readOptions(File file)  throws IOException {
		
		RemoveBOM.process(file, new File("./conf.tmp"));
		File tmpFile = new File("./conf.tmp");
		BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream(tmpFile), this.encoding) );
		
		String strLine;
		while ((strLine = in.readLine()) != null) {
			if( isComment(strLine) ){ continue; }				// 주석이 있거나 빈 공백이면 그냥 패스 
			
			if( checkMergeList(strLine) ) { continue; } 		// 파일 목록 확인
			
			if( checkOutput(strLine) ) { continue; } 			// 출력 파일 옵션 확인
			
			if( checkCompress(strLine) ) { continue; } 			// 압축 옵션을 확인
			
			if( checkFileType(strLine) ) { continue; } 			// 압축파일 타입 옵션을 확인
			
			if( checkLineBreak(strLine) ) { continue; } 		// 라인 브레이크 옵션을 확인
			
			if( checkEncoding(strLine) ) { continue; } 			// 인코딩 옵션을 확인
			
			if( checkRemovePattern(strLine) ) { continue; } 	// 패턴 제거 옵션을 확인
			
			if( checkJSLint(strLine) ) { continue; } 			// JSLint 옵션을 확인
			
			if( checkJSMeter(strLine) ) { continue; } 			// JSMeter 옵션을 확인
			
			if( checkKeepLicense(strLine) ) { continue; } 		// KeepLicense 옵션을 확인
			
			if( checkTplComplie(strLine) ) { continue; } 		// TplComplie 옵션을 확인
			
			if( checkInfoFile(strLine) ) { continue; } 			// InfoFile 옵션을 확인
			
			if( checkLineNumber(strLine) ) { continue; } 		// LineNumber 옵션을 확인
			
			if( checkListOut(strLine) ) { continue; } 		// LineNumber 옵션을 확인

			this.error.add(strLine);
		}
		in.close();
		tmpFile.delete();
	}
	
	private boolean checkTplComplie(String strLine) {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:TplComplie|tplcomplie)\\s+|^(?:TplComplie|tplcomplie)\\s+)((?:'|\")([^'\"]*)(?:'|\"))?");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			tplcomplie = true;
			String value = matcher.group(1);
			
			if (value != null) {
				tplcomplieCallback = matcher.group(1);
			}
			return true;
		}
		return false;
	}

	private boolean checkKeepLicense(String strLine) {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:KeepLicense|keeplicense)\\s+|^(?:KeepLicense|keeplicense)\\s+)(.*)");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			if(matcher.group(1).equals("YES")){
				keeplicense = true;
				return true;
			}
			return true;
		}
		return false;
	}

	private boolean checkListOut(String strLine) {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:ListOut|listout)\\s+|^(?:ListOut|listout)\\s+)(.*)");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			if(matcher.group(1).equals("YES")){
				listout = true;
				return true;
			}
			return true;
		}
		return false;
	}

	private boolean checkLineNumber(String strLine) {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:LineNumber|line-number)\\s+|^(?:LineNumber|line-number)\\s+)(.*)");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			if(matcher.group(1).equals("YES")){
				lineNumber = true;
				return true;
			}
			return true;
		}
		return false;
	}

	private boolean checkInfoFile(String strLine) throws IOException {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:InfoFile|infofile)\\s+|^(?:InfoFile|infofile)\\s+)(?:'|\")([^'\"]*)(?:'|\")");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			File file = new File(matcher.group(1));
			setInfoFile(file.getCanonicalPath()); 
			return true;
		}
		return false;
	}

	private boolean checkMergeList(String strLine) throws IOException {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:MergeList|list)\\s+|^(?:MergeList|list)\\s+)(?:'|\")([^'\"]*)(?:'|\")");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			File file = new File(matcher.group(1));
			mergeList = file.getCanonicalPath(); 
			return true;
		}
		return false;
	}
	
	private boolean checkFileType(String strLine) {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:FileType|filetype)\\s+|^(?:FileType|filetype)\\s+)(.*)");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			if(matcher.group(1).toUpperCase().equals("CSS")){
				fileType = "CSS";
				return true;
			}
			
			if(matcher.group(1).toUpperCase().equals("JS")){
				fileType = "JS";
				return true;
			}
			
			if(matcher.group(1).toUpperCase().equals("TPL")){
				fileType = "TPL";
				return true;
			}
			return true;
		}
		return false;
	}
	
	private boolean checkJSLint(String strLine) throws IOException {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:JSLint|jslint)\\s+|^(?:JSLint|jslint)\\s+)(.*)");
		Pattern prnFile = Pattern.compile("(?:'|\")([^'\"]*)(?:'|\")");
		Matcher matcher =  prnValue.matcher(strLine);
		
		// YES or NO 매칭
		if(matcher.find()){
			String Option = matcher.group(1);
			matcher = prnFile.matcher(Option);
			
			// JSLint YES && OptionFile
			if(matcher.find()){
				File file = new File(matcher.group(1));
				jslistOption = file.getCanonicalPath(); 
				jslint = true;
				return true;
			}
			
			// only JSLint YES
			if(Option.equals("YES")){
				jslint = true;
				return true;
			}
		}
		// JSLint NO
		return false;
	}
	
	private boolean checkJSMeter(String strLine) {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:JSMeter|jsmeter)\\s+|^(?:JSMeter|jsmeter)\\s+)(.*)");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			if(matcher.group(1).equals("YES")){
				jsmeter = true;
				return true;
			}
			return true;
		}
		return false;
	}
	
	private boolean checkRemovePattern(String strLine) {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:RemovePattern|pattern)\\s+|^(?:RemovePattern|pattern)\\s+)/(.*)/");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			removePattern = Pattern.compile(matcher.group(1));
			return true;
		}
		return false;
	}

	private boolean checkEncoding(String strLine) {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:Encoding|encoding)\\s+|^(?:Encoding|encoding)\\s+)(?:'|\")([^'\"]*)(?:'|\")");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			encoding = matcher.group(1);
			return true;
		}
		return false;
	}

	private boolean checkLineBreak(String strLine) {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:Line-Break|line-break)\\s+|^(?:Line-Break|line-break)\\s+)(\\d+)");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			lineBreak = Integer.parseInt(matcher.group(1));
			return true;
		}
		return false;
	}

	private boolean checkCompress(String strLine) {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:Compress|compress)\\s+|^(?:Compress|compress)\\s+)(\\d+)");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			compress = Integer.parseInt(matcher.group(1));
			return true;
		}
		return false;
	}

	private boolean checkOutput(String strLine) throws IOException {
		Pattern prnValue = Pattern.compile("(?:^\\s+(?:Output|output)\\s+|^(?:Output|output)\\s+)(?:'|\")([^'\"]*)(?:'|\")");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			File file = new File(matcher.group(1));
			output = file.getCanonicalPath(); 
			return true;
		}
		return false;
	}
	
	/**
	 * 주석이나 빈 문자열인지 확인한다.
	 * @param strLine
	 * @return
	 */
	private boolean isComment(String strLine){
		if(strLine.length() < 1){ return true; }
		Pattern prnValue = Pattern.compile("(?:^#|\\^s+#).*");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			return true;
		}
		return false;
	}
	
	public void setMergeList(String string) {
		this.mergeList = string;
	}
	
	public void setOutput(String string) throws IOException {
		File file = new File(string);
		output = file.getCanonicalPath(); 
	}

	public void setEncoding(String string) {
		this.encoding = string; 
	}

	public void setLineBreak(String string) {
		this.lineBreak = Integer.parseInt(string);
	}

	public void setCompress(String string) {
		this.compress = Integer.parseInt(string);
	}

	public void setPattern(String string) {
		this.removePattern =  Pattern.compile(string);
	}
	
	public void setFileType(String string) {
		this.fileType = string; 
	}

	public int getCompress() {
		return this.compress;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public Pattern getPattern() {
		return removePattern;
	}

	public int getLineBreak() {
		return lineBreak;
	}

	public String getOutput() {
		return output;
	}

	public boolean getJSLint() {
		return jslint;
	}

	public void setJSLint(String string) {
		if ( string.equals("YES") ){
			jslint = true;
		}else{
			jslint = false;
		}
	}

	public String getFileType() {
		return fileType.toLowerCase();
	}

	public String getMergeList() {
		return mergeList;
	}

	public void setInfoFile(String infoFile) {
		this.infoType = infoFile.substring(infoFile.lastIndexOf(".")+1).toUpperCase();
		this.infoFile = infoFile;
	}

	public String getInfoFile() {
		return infoFile;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getInfoType() {
		return infoType;
	}

	public boolean getJSMeter() {
		return jsmeter;
	}
	
	public void setJSMeter(String string) {
		if ( string.equals("YES") ){
			jsmeter = true;
		}else{
			jsmeter = false;
		}
	}

	public boolean setLineNumber(String string) {
		if ( string.equals("YES") ){
			lineNumber = true;
		}else{
			lineNumber = false;
		}
		return false;
	}
	
	public boolean getLineNumber() {
		return lineNumber;
	}

	public String getJSLintOption() {
		return jslistOption;
	}
	public void setJSLintOption(String string)  throws IOException {
		File file = new File(string);
		jslistOption = file.getCanonicalPath(); 
	}

	public boolean getListOut() {
		return listout;
	}

	public void setListOut(String string) {
		if ( string.equals("YES") ){
			listout = true;
		}else{
			listout = false;
		}
		
	}

	public boolean getTplProc() {
		return tplcomplie;
	}
	
	public void setTplProc(String string) {
		if ( string.equals("YES") ){
			tplcomplie = true;
		}else{
			tplcomplie = false;
		}
	}

	public void setTplProcCallback(String string) {
		tplcomplieCallback = string; 
	}

	public String getTplProcCallback() {
		return tplcomplieCallback;
	}

	public boolean getKeepLicense() {
		return keeplicense;
	}

	public void setKeepCmt(String string) {
		if ( string.equals("YES") ){
			keeplicense = true;
		}else{
			keeplicense = false;
		}
		
	}
}
