package com.miconblog.jstools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import orgs.mozilla.javascript.*;

public class JSLint {
	
	public JSLint(ArrayList<String> mergeList, String encoding, String optionPath) throws IOException {
		URL path = getClass().getClassLoader().getResource("jslint.js");
		if(path == null) { return; }
		Reader in = new InputStreamReader(path.openStream()) ;
		
	    Context cx = Context.enter();
        try {
            // Initialize the standard objects (Object, Function, etc.)
            // This must be done before scripts can be executed. Returns
            // a scope object that we use in later calls.
            Scriptable scope = cx.initStandardObjects();
            Object result = cx.evaluateReader(scope, in, "<jslint>", 1, null);
         //   System.err.println(Context.toString(result));
            
            // Call function "f('my arg')" and print its result.
            Object fObj = scope.get("JSLINT", scope);
            if (!(fObj instanceof Function)) {
            	System.out.println("f is undefined or not a function.");
            } else {
            		
            	// JSLINT(sFile)
            	System.out.println("[JSLINT]");
            	for(int i=0; i < mergeList.size(); ++i){
            		
            		String file = mergeList.get(i);
            		RemoveBOM.process(new File(file), new File("./bom.tmp"));
        			File tmpFile = new File("./bom.tmp");
        			
        			// JSLint 옵션 파일이 지정되어 있다면,..
        			String option = "null";
        			if(!optionPath.equals("")){
        				File optFile = new File(optionPath);
        				
        				if(optFile.isFile()){
            				option = getSource(optFile.getAbsolutePath(), encoding);
            				//System.out.println(option);
            			}else{
            				System.err.println("[WARN] The option file doesn't exist. (" +  optionPath + ")");
            			}
        			}
        			
        			// JSLint 소스 얻어오기
                	String source = getSource(tmpFile.getAbsolutePath(), encoding);
                	tmpFile.delete();
                	
                	// JSLint ( source, option ) 실행
                	Function f = (Function)fObj;
                	result = f.call(cx, scope, scope, new Object[] {source, option});
                	
                	System.out.println(" ----------------- ");
                	System.out.println(" - " + mergeList.get(i));
                	
                	if (Context.toString(result).equals("false")){
                		System.out.printf("   %-3s| %-6s| %-11s| %-30s\n","No.","Lines","Characters","Reason");
                		// obj = JSLINT 
                		Scriptable obj = (Scriptable) scope.get("JSLINT", scope);

                		// data = JSLINT.data() 
                		Function fn = (Function) ScriptableObject.getProperty(obj, "data");
                		Scriptable data = (Scriptable) fn.call(cx, scope, obj, new Object[0]);
                		
                		// aError = data.errors 
                		Scriptable aError = (Scriptable) data.get("errors", data);
                		
                		double length = (Double) aError.get("length", aError);
                		
                		for(int j=0; j<length; ++j){
                    		// error = aError[i] 
                    		Scriptable error = (Scriptable)aError.get(j, aError);
                    		printError(error, mergeList.get(i), j+1);
                		}
                		
                	}else{
                		System.out.println("   No defects.");
                	}
            	}
            }

        } finally {
            // Exit from the context.
            Context.exit();
        }
	}

	private String getSource(String path, String encoding) throws IOException {
		BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream(path), encoding) );
		StringBuilder sb = new StringBuilder();
		String line = null;
		Pattern prnValue = Pattern.compile("(?:\\^s+//).*");
		
		while ((line = br.readLine()) != null) {
			
			// 한줄 주석 제거
			if(line.length() < 1){ continue; }
			Matcher matcher =  prnValue.matcher(line);
			if(matcher.find()){	continue; }
			
			sb.append(line);
			sb.append(System.getProperty("line.separator"));
		}

		br.close();
		return sb.toString();
	}

	private void printError(Scriptable obj, String path, int count) {
		if(obj == null){
			return;
		}
		
		String reason = (obj.get("reason", obj) == null )? "" : (String)obj.get("reason", obj) ;
		int line = 0;
		int character = 0;
		try{
			line = ((Double) obj.get("line", obj)).intValue();
			character = ((Double) obj.get("character", obj)).intValue();
		}catch(Exception e){
			character = ((Integer) obj.get("character", obj)).intValue();
		}
		System.out.printf("   %-3s| %-6s| %-11s| %-30s\n",count, line, character, reason);
	}
}
