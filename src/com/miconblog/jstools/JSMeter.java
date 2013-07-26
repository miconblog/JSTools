package com.miconblog.jstools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;

import orgs.mozilla.javascript.Context;
import orgs.mozilla.javascript.Function;
import orgs.mozilla.javascript.Scriptable;
import orgs.mozilla.javascript.ScriptableObject;

public class JSMeter {
	public JSMeter(ArrayList<String> mergeList, String encoding) throws IOException {

		URL path = getClass().getClassLoader().getResource("jsmeter.js");
		if (path == null) {
			System.err.println("jsmeter.js file does not exist. : " + path);
			return;
		}
		Reader in = new InputStreamReader(path.openStream());

		Context cx = Context.enter();
		Scriptable scope = cx.initStandardObjects();
		cx.evaluateReader(scope, in, "<jsmeter>", 1, null);
		// System.err.println(Context.toString(result));

		System.out.println("[JSMETER]");

		// JSMETER
		ScriptableObject JSMETER = (ScriptableObject)scope.get("JSMETER", scope);

		// GET var PARSE = JSMETER.make_parse();
		Function f = (Function)ScriptableObject.getProperty(JSMETER, "make_parse");
		ScriptableObject PARSE = (ScriptableObject)f.call(cx, scope, JSMETER, new Object[0]);
		if (!(PARSE instanceof Function)) {
			System.out.println("PARSE is undefined or not a function.");
		}

		// GET var COMPLEXITY = JSMETER.make_complexity();
		f = (Function)ScriptableObject.getProperty(JSMETER, "make_complexity");
		ScriptableObject COMPLEXITY = (ScriptableObject)f.call(cx, scope, JSMETER, new Object[0]);

		for (int i = 0; i < mergeList.size(); ++i) {

			// GET JS Source of File List
			String file = mergeList.get(i);
			String tmpBomFileName = new StringBuilder().append("./bom.tmp").append(System.nanoTime()).toString();
			RemoveBOM.process(new File(file), new File(tmpBomFileName));
			File tmpFile = new File(tmpBomFileName);
			String source = getSource(tmpFile.getAbsolutePath(), encoding);
			tmpFile.delete();

			//System.out.println(source);
			System.out.println(" ----------------- ");
			System.out.println(" - " + file);
			try {
				// GET var tree = PARSE(source);
				Object sourceArgs[] = {source};
				Object tree = ((Function)PARSE).call(cx, scope, scope, sourceArgs);
				//System.out.println("  [DEBUG]\n   " + scope.get("TTT", scope) + "\n   ------");

				// GET COMPLEXITY.complexity(tree, "code");
				f = (Function)ScriptableObject.getProperty(COMPLEXITY, "complexity");
				Object complexArgs[] = {tree, "code"};
				f.call(cx, scope, COMPLEXITY, complexArgs);

				// GET COMPLEXITY.getFunctions();
				f = (Function)ScriptableObject.getProperty(COMPLEXITY, "getFunctions");
				Scriptable fns = (Scriptable)f.call(cx, scope, COMPLEXITY, new Object[0]);

				printObject(cx, scope, fns);
			} catch (Exception e) {
				System.out.println("   " + e);
				System.out.println("   [WRAN] Please try again after JSLint cleaned!");
			}
		}
	}

	private String getSource(String path, String encoding) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), encoding));
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append(System.getProperty("line.separator"));
		}

		br.close();
		return sb.toString();
	}

	private void printObject(Context cx, Scriptable scope, Scriptable fns) {

		double length = (Double)fns.get("length", fns);
		int maxLen = 0;
		for (int i = 0; i < length; ++i) {
			Scriptable fn = (Scriptable)fns.get(i, fns);
			String name = ((String)fn.get("name", fn)).replace("[[code]].", "");
			maxLen = Math.max(maxLen, name.length());
		}
		String format = "   %-5s| %-" + (maxLen + 1) + "s| %-11s| %-6s| %-9s| %-10s| %-9s| %-6s| %-11s|\n";
		System.out.printf(format, "Line", "Function", "Statements", "Lines", "Comments", "Comments%", "Branches", "Depth", "Complexity");
		for (int i = 0; i < length; ++i) {
			Scriptable fn = (Scriptable)fns.get(i, fns);

			Function f = (Function)ScriptableObject.getProperty(fn, "linesF");
			int lines = ((Double)f.call(cx, scope, fn, new Object[0])).intValue();

			f = (Function)ScriptableObject.getProperty(fn, "complexityF");
			int cc = ((Double)f.call(cx, scope, fn, new Object[0])).intValue();
			String complexity = "";

			if (cc > 10) {
				int stars = cc / 10;
				for (int k = 0; k < stars; ++k) {
					complexity += "*";
				}
			}
			complexity += String.valueOf(cc);

			String name = ((String)fn.get("name", fn)).replace("[[code]].", "");
			int lineStart = ((Double)fn.get("lineStart", fn)).intValue();
			int stat = ((Double)fn.get("s", fn)).intValue();
			int comments = ((Double)fn.get("comments", fn)).intValue();
			float commentper = ((float)comments / (float)lines * 10000) / 100;
			int branches = ((Double)fn.get("b", fn)).intValue();
			int depth = ((Double)fn.get("blockDepth", fn)).intValue();

			System.out.printf(format, lineStart, name, stat, lines, comments, commentper, branches, depth, complexity);
		}
	}
}
