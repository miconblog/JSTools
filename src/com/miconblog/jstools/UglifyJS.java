package com.miconblog.jstools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;

import orgs.mozilla.javascript.Context;
import orgs.mozilla.javascript.Function;
import orgs.mozilla.javascript.Scriptable;

public class UglifyJS {

	private String compressData;
	public UglifyJS(String inputFilePath, String outputFilePath, String encoding) throws IOException {
		URL path = getClass().getClassLoader().getResource("uglifyJS.js");
		if (path == null) {
			System.err.println("uglifyJS.js file dose not exist! : " + path);
			return;
		}
		
		// uglifyJS 파일을 읽어와서
		Reader in = new InputStreamReader(path.openStream());
		Context cx = Context.enter();

		try {
			// 로드한다.
			Scriptable scope = cx.initStandardObjects();
			cx.evaluateReader(scope, in, "<uglifyJS>", 1, null);
			//Object result = cx.evaluateReader(scope, in, "<uglifyJS>", 1, null);
			//System.err.println(Context.toString(result));

			// 로드가 제대로 됐는지 확인하기 위해 uglify 라는 함수가 있는 확인한다.
			Object fObj = scope.get("uglify", scope);
			if (!(fObj instanceof Function)) {
				System.err.println("'uglify' is undefined or not a function.");
			} else {
				
				// 압축할 파일을 읽어온다.
            	String source = getSource(inputFilePath, encoding);
        		Object functionArgs[] = { source };
        		
        		// uglify(sFile, [options]) 를 실행한다.
            	Function f = (Function)fObj;
            	compressData = (String) f.call(cx, scope, scope, functionArgs);
				//System.out.println(compressData);
				
				// 파일에 쓴다.
				writeToFile(outputFilePath,encoding);
			}
			
		} catch (Exception e){
			System.err.println("ERROR " + e);
		} finally {
			// Exit from the context.
			Context.exit();
		}
	}
	
	private void writeToFile (String path, String encoding) throws IOException {
			BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(path), encoding) );
			out.write(compressData);
			out.close();
	}
	
	private String getSource(String path, String encoding) throws IOException {
		BufferedReader br = new BufferedReader( new InputStreamReader( new FileInputStream(path), encoding) );
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append(System.getProperty("line.separator"));
		}

		br.close();
		return sb.toString();
	}
}
