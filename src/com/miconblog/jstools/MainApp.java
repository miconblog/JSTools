/**
 * JSTools (java 1.5 이상 지원)
 * 자바스크립트 배포 지원 도구
 * 기능
 * 1. 파일 머지
 * 2. 주석 제거 (line-number)
 * 3. 압축 및 난독화
 * 4. 사용자 패턴이 포함된 라인 제거(정규식만 지원)
 * 5. 통계 정보 출력
 * 6. JSLint (Clean Code) - 외부 옵션 지원
 * 7. JSMeter (Code Metrics)
 * 8. UglifyJS 압축 지원
 * 9. 태그 라이브러리 지원(jstools:merge)
 * 10. 라이센스 주석 유지 기능 추가
 * 11. 탬플릿 머지 및 컴파일 기능 추가
 *  
 * @author: realrap (AjaxUI) <byungdae.sohn@nhn.com>
 */
package com.miconblog.jstools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yahoo.platform.yui.compressor.YUICompressor;

public class MainApp {
	static ArrayList<String> mergeList = new ArrayList<String>(); 
	static HashMap<String, String> options = new HashMap<String, String>();
	static String processName = "";
	static Configuration conf = new Configuration();
	static MergeFileList list = new MergeFileList();
	static LicenseComment lc = new LicenseComment();
	static Statistics info = new Statistics();
	
	public static void main(String[] args) throws IOException{
		if(args.length < 1){
			printUsage();
			return;
		}
		
		// 매개변수가 올바른지 검사부터 하자.
		if(!validateParameters(args)){
			printUsage();
			return;
		}

		// 인라인 옵션 뽑아내기
		extractInlineOptions(args, options);

		// 분리해낸 옵션을 해석하기 
		parseOptions();
		
		// conf 설정에서 인라인 설정으로 덮어쓰기
		overrideConfiguration();
		
		// 옵션 유효성 검사
		if( !validateConfiguration() ){
			return;
		}
		
		// 최종 설정 출력하기
		conf.printOptions();
		
		// 최종 설정된 옵션을 통계정보에 설정하기..
		info.setConfigurations(conf);
		
		// 실행하자!
		if(list.canExcute()){
			if(conf.getListOut()){
				
				ArrayList<String> lists = list.getMergeList();
				
				BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(conf.getOutput()), "UTF-8") );
				
				for (int i=0; i<lists.size(); i++){
					String file = lists.get(i);
					out.write(file);
					out.write(System.getProperty("line.separator"));
				}
				out.close();
				System.out.println("[LIST OUT]" );
				System.out.printf("\tTask completed!" );
				return;
			}else{
				System.out.println("[LIST]\n   "+ list.getMergeList() );
			}
			
			if(conf.getTplProc()){
				mergeFiles();
				System.out.println("[Preprocessor]");
				tplPreprocessor(conf.getOutput(), conf.getTplProcCallback(), conf.getEncoding());
				return;
			}
			
			process();
			
			
			// 통계를 내자!
			info.setMergeList(list.getMergeList());
			info.print();
			if( !conf.getInfoFile().equals("")){
				if(conf.getInfoType().equals("TXT") || conf.getInfoType().equals("JSON") || conf.getInfoType().equals("LOG")){
					info.toFile();
					System.out.println("[END] " + conf.getInfoFile());
				}else {
					System.err.println("[WARN] Failed to write infomation! Please, check your file extension! (only for txt,json,log) '" + conf.getInfoFile() +"'");
				}
			}
		}else{
			// 목록 없으면 사용법 설명
			System.err.println("[ERROR] There is no files to process! - at least A file or more.\n");
			printUsage();
		}
	}
	
	private static void tplPreprocessor(String output, String callback, String encoding) throws IOException {
		File sourceFile = new File("./merge.tmp");
		File outputFile = new File(output);
		
		if( conf.getLineBreak() > -1){
			System.err.println("[WARN] 'line-brake' is not supported by UglifyJS.");
		}
		
		TemplatePreprocessor processor = new TemplatePreprocessor();
		boolean isOut = true;
		if( output.equals("") ){
			isOut = false;
		}
		processor.process(sourceFile, outputFile, callback, encoding, isOut);
		sourceFile.delete();
		processor.printInfo(outputFile);
		
	}

	private static boolean validateConfiguration() {
		if ( conf.getFileType().toUpperCase().equals("CSS") && (conf.getCompress() < 2)){
			System.err.println("[WARN]\n   If you use this option (compress=1), CSS Hack CAN NOT WORK.");
		}
		return true;
	}

	private static boolean validateParameters(String[] args) {
		Pattern supportPatten = Pattern.compile("^-(line-number|filetype|list|tplcomplie|conf|output|compress|jslint|jsmeter|keeplicense|encoding|pattern|line-break|infofile)");
		
		for(int i=0; i < args.length; ++i){
			if(args[i].startsWith("-")){
				Matcher matcher = supportPatten.matcher(args[i]);		
				if(!matcher.find()){
					System.err.println("[ERROR] The option is not supported '" + args[i] + "' is invalid.");
					return false;
				}
			}
		}
		return true;
	}

	private static void extractInlineOptions(String[] args, HashMap<String, String> map) throws IOException {
		Pattern outputPatten = Pattern.compile("-output=([^\\s]*)");
		Pattern compressPatten = Pattern.compile("-compress=(\\d)");
		Pattern fileTypePatten = Pattern.compile("-filetype=(js|css|tpl)");
		Pattern lineBreakPatten = Pattern.compile("-line-break=(\\d+)");
		Pattern confPatten = Pattern.compile("-conf=([^\\s]*)");
		Pattern listPatten = Pattern.compile("-list=([^\\s]*)");
		Pattern userPatten = Pattern.compile("-pattern=/(.*)/");
		Pattern encodingPatten = Pattern.compile("-encoding=([^\\s]*)");
		Pattern infoFilePatten = Pattern.compile("-infofile=([^\\s]*)");
		Pattern jslintPatten = Pattern.compile("-jslint=([^\\s]*)");
		Pattern tplcompliePatten = Pattern.compile("-tplcomplie=([^\\s]*)");
		
		Matcher matcher;
		
		// 기본값
		map.put("list", "");
		map.put("listout", "");
		map.put("tplcomplie", "");
		map.put("tplcomplie-callback", "");
		map.put("conf", "");
		map.put("output", "");
		map.put("pattern", "");
		map.put("compress", "");
		map.put("filetype", "");
		map.put("jslint", "");
		map.put("jslint-option", "");
		map.put("jsmeter", "");
		map.put("encoding", "");
		map.put("line-break", "");
		map.put("infofile", "");
		map.put("line-number", "");
		map.put("keeplicense", "");
		
		for(int i=0; i<args.length; ++i){
			String str = args[i];
			
			matcher = lineBreakPatten.matcher(str);
			if(matcher.find()){
				map.put("line-break", matcher.group(1));	
				continue;
			}
			
			matcher = userPatten.matcher(str);
			if(matcher.find()){
				map.put("pattern", matcher.group(1));	
				continue;
			}
			
			matcher = encodingPatten.matcher(str);
			if(matcher.find()){
				map.put("encoding", matcher.group(1));	
				continue;
			}
			
			if(str.contains("-jslint")){
				map.put("jslint", "YES");
				
				matcher = jslintPatten.matcher(str);
				if(matcher.find()){
					map.put("jslint-option", matcher.group(1));
				}
				continue;
			}
			
			if(str.contains("-line-number")){
				map.put("line-number", "YES");	
				continue;
			}
			
			if(str.contains("-jsmeter")){
				map.put("jsmeter", "YES");	
				continue;
			}
			
			if(str.contains("-keeplicense")){
				map.put("keeplicense", "YES");	
				continue;
			}
			
			if(str.contains("-listout")){
				map.put("listout", "YES");	
				continue;
			}
			
			if(str.contains("-tplcomplie")){
				map.put("tplcomplie", "YES");	

				matcher = tplcompliePatten.matcher(str);
				if(matcher.find()){
					map.put("tplcomplie-callback", matcher.group(1));
				}
				continue;
			}
			
			matcher = listPatten.matcher(str);
			if(matcher.find()){
				String path = matcher.group(1);
				File file = new File(path);
				map.put("list", file.getCanonicalPath());	
				continue;
			}
			
			matcher = confPatten.matcher(str);
			if(matcher.find()){
				String path = matcher.group(1);
				File file = new File(path);
				map.put("conf", file.getCanonicalPath());	
				continue;
			}
			
			matcher = outputPatten.matcher(str);
			if(matcher.find()){
				String path = matcher.group(1);
				File file = new File(path);
				map.put("output", file.getCanonicalPath());	
				continue;
			}
			
			matcher = infoFilePatten.matcher(str);
			if(matcher.find()){
				String path = matcher.group(1);
				File file = new File(path);
				map.put("infofile", file.getCanonicalPath());	
				continue;
			}
			
			matcher = compressPatten.matcher(str);
			if(matcher.find()){
				map.put("compress", matcher.group(1));
				continue;
			}
			
			matcher = fileTypePatten.matcher(str);
			if(matcher.find()){
				map.put("filetype", matcher.group(1));
				continue;
			}
			
			mergeList.add(args[i]);
		}
	}

	private static void process() throws IOException {
		
		mergeFiles();
		
		switch( conf.getCompress() ){
		case 0: // 머지 
			processName = "merge";
			removePattern();
			createOutput("./merge.tmp");
			break;
			
		case 1:	// 머지 + 주석제거
			processName = "merge + remove comments";
			removePattern();
			removeComments();
			createOutput("./remove.tmp");
			break;
			
		case 2: // 머지 + 압축(YUI)
			processName = "merge + compress";
			removePattern();
			compressFile(false);
			createOutput("./compress.tmp");
			break;
			
		case 3: // 머지 + 압축(YUI) + 난독화(obfuscate)
			processName = "merge + compress + obfuscate";
			removePattern();
			compressFile(true);
			createOutput("./compress.tmp");
			break;
		
		case 4: // 머지 + 압축(YUI) + 난독화(obfuscate) + 불필요한 세미콜론 제거
			processName = "merge + compress + obfuscate + remove semi";
			removePattern();
			compressFile(true);
			createOutput("./compress.tmp");
			break;
			
		case 5: // UglifyJS로 압축 
			processName = "compressed by UglifyJS";
			removePattern();
			uglifyFile();
			createOutput("./compress.tmp");
			break;
			
		default:
			System.out.println("[ERROR] compress option is from 0 to 5");
			if(!conf.getJSLint()){
				printUsage();
			}
		}

		cleanTmpFiles();	
		
		if(conf.getJSLint()){
			new JSLint(list.getMergeList(), conf.getEncoding(), conf.getJSLintOption());
		}
		
		if(conf.getJSMeter()){
			new JSMeter(list.getMergeList(), conf.getEncoding());
		}
	}

	private static void removePattern() throws IOException {
		if( conf.getPattern() != null ){
			File tmpMergeFile = new File("./merge.tmp");
			File tmpRemoveFile = new File("./remove.tmp");
			Pattern pattern = conf.getPattern();
			RemovePattern rp = new RemovePattern(tmpMergeFile.getCanonicalPath(), pattern, tmpRemoveFile.getCanonicalPath(), conf.getEncoding());
			rp.process();
			tmpMergeFile.delete();
			tmpRemoveFile.renameTo(tmpMergeFile);
			
		}
	}

	private static void uglifyFile() throws IOException {
		File tmpMergeFile = new File("./merge.tmp");
		File tmpFile = new File("./compress.tmp");
		
		if( conf.getLineBreak() > -1){
			System.err.println("[WARN] 'line-brake' is not supported by UglifyJS.");
		}
		
		new UglifyJS(tmpMergeFile.getCanonicalPath(),tmpFile.getCanonicalPath(), conf.getEncoding());
	}
	
	
	private static void compressFile(boolean bOfuscate) throws IOException {
		ArrayList<String> al = new ArrayList<String>();
		File tmpMergeFile = new File("./merge.tmp");
		File tmpFile = new File("./compress.tmp");
		al.add(tmpMergeFile.getCanonicalPath());
		al.add("-o");
		al.add(tmpFile.getCanonicalPath());
		if(!bOfuscate){
			al.add("--nomunge");
		}
		
		if(conf.getCompress() < 4){
			al.add("--preserve-semi");	
		}
		
		al.add("--type");
		al.add(conf.getFileType());
		
		al.add("--charset");
		al.add(conf.getEncoding());
		
		if( conf.getLineBreak() > -1){
			al.add("--line-break");
			al.add(Integer.toString(conf.getLineBreak()));
		}
		
		String [] options = (String[]) al.toArray(new String[al.size()]);
		System.out.println("[INFO] YUICompressor Option: " +  al);
		
		try {
			YUICompressor.main(options);
			
		} catch (Exception e) {
			System.out.println("[Error] Failed to compress!. Please, check your code with JSLint and fix your problems");
			e.printStackTrace();
		}
	}
	
	private static void createOutput(String path) throws IOException {
		File tmpFile = new File(path);
		String souceCode="";
		
		// 라이센스 주석 유지하기
		if( conf.getKeepLicense() && (lc.getSize() > 0)){
			BufferedReader in = new BufferedReader(  new InputStreamReader( new FileInputStream(tmpFile), conf.getEncoding()) );
			String strLine;
			while ((strLine = in.readLine()) != null) {
				souceCode += strLine +"\n";
			}
			in.close();
			
			while(lc.getSize() > 0){
				int i = lc.getSize()-1;
				String comment = lc.getLicenseComment(i);
				Pattern prn = Pattern.compile("LICENSE_COMMENT_BY_JSTOOLS_"+i+";?");
				Matcher matcher = prn.matcher(souceCode);
				if ( matcher.find() ){
					souceCode = souceCode.replace(matcher.group(0), comment);
				}
			}
			
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(tmpFile), conf.getEncoding());
			out.write(souceCode);
			out.close();
		}
		
		if( !conf.getOutput().equals("") ){
			File outputFile = new File( conf.getOutput() );
			
			BufferedReader in = new BufferedReader(  new InputStreamReader( new FileInputStream(tmpFile), conf.getEncoding()) );
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputFile), conf.getEncoding());
			
			if(conf.getLineNumber()){
				 String strLine;
				 int lineNumber = 0;
				 while ((strLine = in.readLine()) != null) {
					 out.write(++lineNumber + "\t" + strLine + "\r\n");
				 }
			}else {
				int strem = -1; 
				while((strem=in.read())!= -1){
					out.write(strem);
				}
			}
			in.close();
			out.close();
			System.out.println("[END]\n   " + processName + " completed!");
		}else{
			if( conf.getJSLint() || conf.getJSMeter()){ return ; }
			
			BufferedReader in = new BufferedReader(  new InputStreamReader( new FileInputStream(tmpFile), conf.getEncoding()) );
			String strLine;
			
			System.out.println("[OUT] ---------------------------");
			while((strLine=in.readLine())!= null){
				System.out.println(new String(strLine.getBytes(conf.getEncoding()), conf.getEncoding()));
			}
			in.close();
			System.out.println("[OUT] ---------------------------");
			System.out.println("[END] " + processName + " completed!");
		}
	}

	private static void cleanTmpFiles() throws IOException {
		File file = new File("./");
		if(file.isDirectory()) {
			 String [] fl = file.list();
		     File tmpFile = null;
		     
		     for(int i=0;i<fl.length;i++) {
	            tmpFile = new File(file.getAbsolutePath()+"\\"+fl[i]);
	            if(tmpFile.isFile()) {
	            	if(fl[i].lastIndexOf(".") > 0){
	            		if(fl[i].substring(fl[i].lastIndexOf(".")).toUpperCase().equals(".TMP")) {
	            			tmpFile.delete();
	            		}
	            	}
	            }
	        }
		}
	}

	private static void removeComments() throws IOException {
		File tmpMergeFile = new File("./merge.tmp");
		File tmpRemoveFile = new File("./remove.tmp");
		RemoveComment rc = new RemoveComment(tmpMergeFile.getCanonicalPath(), tmpRemoveFile.getCanonicalPath(), conf.getEncoding());
		rc.process();
	}

	private static void mergeFiles() throws IOException {
		File file = new File("./merge.tmp");
		MergeFiles mf = new MergeFiles(list.getMergeList(), file.getCanonicalPath(), conf.getEncoding());
		mf.process(lc, conf.getKeepLicense());
		info.setCodeLines(mf.getCodeLines());
	}

	
	/**
	 * 옵션을 해석한다.
	 * 1. conf 옵션을 먼저 해석한다.
	 * 2. 다른 옵션이 있으면 덮어 씌운다.
	 */ 
	private static void parseOptions() throws IOException {
		// conf 설정을 먼저 해석한다.
		if(!options.get("conf").equals("")){
			File file = new File(options.get("conf"));
			
			if(file.exists() && file.isFile()){
				conf.readOptions(file);
			}else{
				System.err.println("[WARN] The configuation file is not exist! - " + file.getCanonicalPath());
			}
		}
		
		// list 옵션이 있으면 conf로 설정한다.
		if(!options.get("list").equals("")){
			conf.setMergeList(options.get("list"));
		}
		
		// 목록파일을 해석한다.
		if(!conf.getMergeList().equals("")){
			File file = new File(conf.getMergeList());
			if(file.exists() && file.isFile()){
				list.extractMergeFilesFromList(file, conf.getFileType());
			}else{
				System.err.println("[WARN] The file list is not exist! - " + file.getCanonicalPath());
			}
		}
	}	
	
	private static void overrideConfiguration() throws IOException{
		if(!options.get("filetype").equals("")){
			conf.setFileType(options.get("filetype"));
		}
		
		if(!options.get("output").equals("")){
			conf.setOutput(options.get("output"));
		}
		
		if(!options.get("infofile").equals("")){
			conf.setInfoFile(options.get("infofile"));
		}
		
		if(!options.get("encoding").equals("")){
			conf.setEncoding(options.get("encoding"));
		}
		
		if(!options.get("line-break").equals("")){
			conf.setLineBreak(options.get("line-break"));
		}
		
		if(!options.get("compress").equals("")){
			conf.setCompress(options.get("compress"));
		}
		
		if(!options.get("pattern").equals("")){
			conf.setPattern(options.get("pattern"));
		}
		
		if(!options.get("jslint").equals("")){
			conf.setJSLint(options.get("jslint"));
			
			if(!options.get("jslint-option").equals("")){
				conf.setJSLintOption(options.get("jslint-option"));
			}
		}
		
		if(!options.get("jsmeter").equals("")){
			conf.setJSMeter(options.get("jsmeter"));
		}
		
		if(!options.get("keeplicense").equals("")){
			conf.setKeepCmt(options.get("keeplicense"));
		}
		
		if(!options.get("listout").equals("")){
			conf.setListOut(options.get("listout"));
		}
		
		if(!options.get("tplcomplie").equals("")){
			conf.setTplProc(options.get("tplcomplie"));
			
			if(!options.get("tplcomplie-callback").equals("")){
				conf.setTplProcCallback(options.get("tplcomplie-callback"));
			}
		}
		
		if(!options.get("line-number").equals("")){
			conf.setLineNumber(options.get("line-number"));
		}
		
		
		// 템플릿 컴파일일 경우 자동 타입 설정
		if(conf.getTplProc()){
			conf.setFileType("TPL");
		}
		
		if(mergeList.size() > 0){
			list.addMergeList(mergeList, conf.getFileType());
		}
	}
	
	private static void printUsage() {
		System.out.println("=====================");
		System.out.println("= JSTools ver "+ conf.getVersion() +" =");
		System.out.println("=====================");
		System.out.println(
				"\nUsage: java -jar JSTools <FILE LIST> [option]\n\n"
						
						+ "[FILE LIST]\n"
						+ " file1 file2 dir1 dir2...\n"
						+ " 1. Seperate files using white space.\n"
						+ " 2. All js files in the folder will be merged when you use a folder.\n"
						+ " 3. Be careful, the files will be randomly merged when you use a folder.\n"
						+ " 4. You can use a file list option '-list=xxxx'\n\n"
						
						+ "[List File Syntax]\n"
						+ " 1. Assign valuables.\tex) @xxx=\"d:/workspace\" \n"
						+ " 2. Using valuables. \tex) {%=xxx%}/test.js \n"
						+ " 3. Using a Folder.  \tex) /JS  //all files in the 'JS' folder will be merged.\n\n"
						
						+ "[Options]\n"
						+ "   -list=xxxx  \t\tlist file\n"
						+ "   -conf=xxxx  \t\tconfiguation file\n"
						+ "   -output=xxxx\t\toutput file, (default is stdout) \n"
						+ "   -compress=[0,1,2,3,4,5] 	0(default, Merge)\n"
						+ "\t\t\t\t1(Merge + Remove Comments)\n\t\t\t\t2(Merge + Compress)\n"
						+ "\t\t\t\t3(Merge + Compress + Ofuscate)\n\t\t\t\t4(Merge + Compress + Ofuscate + Semis)\n\t\t\t\t5(UglifyJS) \n"
						+ "   -filetype=[js|css|tpl] 	compressing file type - js(default) or css(css type is only for compress=2) or tpl(only for tplcompile) \n"
						+ "   -line-break=<NUM>	YUICompressor Line break option\n"
						+ "   -jslint[=xxxx]		JSLint (if you want custom options, assign a option file xxxx)\n"
						+ "   -jsmeter			Compute Complxity. \n"
						+ "   -encoding=xxxx		default is UTF-8\n"
						+ "   -infofile=xxxx 		compressing infomation will be out to file\n"
						+ "   -pattern=/xxxx/		remove a line if it contains the pattern.\n"
						+ "   -line-number		display line numbers in code. \n"
						+ "   -tplComplie		precomplie a template file. \n"
						+ "   -keeplicense		keep license comment. \n\n"
				
				+ "[Examples]\n"
				+ "java -jar JSTools file1.js ./file2.js ../file3.js dir1 ./dir2 ../dir3\n"
				+ "java -jar JSTools -list=mergelist.txt\n"
				+ "java -jar JSTools -conf=jstools.conf\n"
				+ "java -jar JSTools -list=mergelist.txt -conf=jstools.conf -output=./result.txt\n"
				+ "java -jar JSTools -conf=jstools.conf -commpress=2\n"
				+ "java -jar JSTools -conf=jstools.conf -jslint\n"
				+ "java -jar JSTools -conf=jstools.conf -jslint -jsmeter\n"
				+ "java -jar JSTools -conf=jstools.conf -jslint=jslint.conf -line-number -jsmeter\n"
		);
	}
}
