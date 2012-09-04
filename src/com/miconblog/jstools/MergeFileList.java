package com.miconblog.jstools;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MergeFileList {
	private HashMap<String, String> hashTable;
	private ArrayList<String> exclude;
	private ArrayList<String> include;
	private String fileType;

	public MergeFileList() {
		this.hashTable = new HashMap<String, String>();
		this.exclude = new ArrayList<String>();
		this.include = new ArrayList<String>();
		this.fileType = "JS";
	}
	
	public void extractMergeFilesFromList(File file, String fileType) throws IOException {
		this.fileType = fileType;
		Pattern regex = Pattern.compile("(?:\\s*|\\t*)//.*");
		
		BufferedReader in = new BufferedReader(new FileReader(file));
		String strLine;
		while ((strLine = in.readLine()) != null) {
			strLine = strLine.replaceAll(regex.toString(), "");
			
			if( isComment(strLine) ){ continue; }				// 주석이 있거나 빈 공백이면 그냥 패스 
			
			if( isVariable(strLine) ){ continue; }				// 변수가 있으면 해시테이블에 일단 저장하고 패스
		
			strLine = replacePathValue(strLine);				// 치환할 변수가 있으면 치환한다. 
		
			if( checkInclude(strLine) ) { continue; } 			// 포함 하고픈 리스트가 있는지 확인
		
			if( checkExclude(strLine) ) { continue; } 			// 제외하고픈 리스트가 있는지 확인
			
			getFileList(new File(strLine), include);		
		}
		
		
		excludeFileFromMergeList();
		
	}
	
	/**
	 * 주석이나 빈 문자열인지 확인한다.
	 * @param strLine
	 * @return
	 */
	private boolean isComment(String strLine){
		strLine = strLine.trim();
		if(strLine.length() < 1){ return true; }
		Pattern prnValue = Pattern.compile("(?:^#|\\^s+#|^//|^s+//).*");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			return true;
		}
		return false;
	}
	
	private void excludeFileFromMergeList() {
		for(int i=0; i < exclude.size(); ++i){
			include.remove(exclude.get(i));
		}
	}
	private boolean checkInclude(String strLine) throws IOException {
		Pattern prnValue = Pattern.compile("(?:^\\s+Include\\s+|^Include\\s+)(.*)");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			getFileList(new File(matcher.group(1)), include);		
			return true;
		}
		return false;
	}
	
	private boolean checkExclude(String strLine) throws IOException {
		Pattern prnValue = Pattern.compile("(?:^\\s+Exclude\\s+|^Exclude\\s+)(.*)");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			getFileList(new File(matcher.group(1)), exclude);		
			return true;
		}
		return false;
	}
	
	
	private String replacePathValue(String strLine) {
		Pattern prnValue = Pattern.compile("\\{%=(.*)%\\}");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			strLine = strLine.replaceAll("\\{%=.*%\\}", hashTable.get(matcher.group(1)));
		}
		return strLine;
	}
	
	
	/**
	 * 변수가 있는지 확인하고, 변수가 있다면 해시 테이블에 저장해놓는다.
	 * @param strLine
	 * @return
	 */
	private boolean isVariable(String strLine){
		Pattern prnValue = Pattern.compile("^@([^\\s]*)\\s*=\\s*.*['\"](.*)['\"]");
		Matcher matcher =  prnValue.matcher(strLine);
		
		if(matcher.find()){
			hashTable.put(matcher.group(1), matcher.group(2));
			return true;
		}
		return false;
	}
	
	public void addMergeList(ArrayList<String> mergeList, String fileType) throws IOException {
		this.fileType = fileType.toUpperCase();
		ArrayList<String> merge = new ArrayList<String>();
		
		for(int i=0; i<mergeList.size(); ++i){
			File file = new File(mergeList.get(i));
			getFileList(file, merge);
		}
		
		for(int i=0; i<include.size(); ++i){
			if(!merge.contains(include.get(i))){
				merge.add(include.get(i));
			}
		}
		include.clear();
		include = merge;
	}

	public ArrayList<String> getMergeList() {
		return include;
	}

	public boolean canExcute() {
		if(include.size() > 0) return true;
		return false;
	}

	public void getFileList(File targetPath, ArrayList<String> arr) throws IOException {
	    if(targetPath.isDirectory()) {
	        String [] fl = targetPath.list();
	        String path = "";
	        File tmpFile = null;
	       
	        for(int i=0;i<fl.length;i++) {
	            tmpFile = new File(targetPath.getAbsolutePath().replaceAll("\\\\", "/")+"/"+fl[i]);
	            if(tmpFile.isDirectory()) {
	                getFileList(tmpFile, arr);
	            } else {
	            	if(fl[i].lastIndexOf(".") > 0){
						if(fl[i].substring(fl[i].lastIndexOf(".")).toUpperCase().equals("."+fileType.toUpperCase())) {
	            			path = targetPath.getCanonicalPath().replaceAll("\\\\", "/")+"/"+fl[i];
	            			if(!arr.contains(path)){
	            				arr.add(path);
	            			}
	            		}
	            	}
	            }
	        }
	    } else {
	    	if(targetPath.isFile()){
				// 파일이면 파일 목록에 등록해라!
	    		arr.add(targetPath.getCanonicalPath().replaceAll("\\\\", "/"));
			}else{
				System.err.println("[WARN] There is no files or no folders. - " +targetPath.getCanonicalPath().replaceAll("\\\\", "/"));
			}
	    }
	}
}

