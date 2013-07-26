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

/**
 * 파일을 병합한다.
 * 
 * @author SohnBD
 */
public class MergeFiles {
	private ArrayList<String> srcFiles;
	private String destFile = "";
	private String encoding = "UTF-8";
	private int total_lines = 0;

	MergeFiles(ArrayList<String> lists, String output, String encoding) throws IOException {
		this.srcFiles = lists;
		this.destFile = output;
		this.encoding = encoding;
	}

	public void process(LicenseComment lc, boolean isKeepLicense) throws IOException {
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.destFile), this.encoding));
		BufferedReader in;

		for (int i = 0; i < srcFiles.size(); i++) {
			String file = srcFiles.get(i);
			String tmpBomFileName = new StringBuilder().append("./bom.tmp").append(System.nanoTime()).toString();
			File srcfile = new File(file);
			if (srcfile.length() > 3) {
				RemoveBOM.process(srcfile, new File(tmpBomFileName));
			} else {
				System.err.println("[WARN] '" + file + "' is skipped, because the file is empty or too short!");
				continue;
			}

			File tmpFile = new File(tmpBomFileName);
			in = new BufferedReader(new InputStreamReader(new FileInputStream(tmpFile), this.encoding));

			String strLine;

			if (isKeepLicense) {
				lc.initSearch();
			}
			while ((strLine = in.readLine()) != null) {

				/**
				 * 1. 라이센스 유지 옵션이 true 일때만 진행한다.
				 * 2. 라이센스 옵션이 true이고, 라이센스 검색이 모두 끝났다면 더이상 검색하지 않는다.
				 *   --> lc.searchEnd 면 
				 */
				if (isKeepLicense) {

					if (lc.findLicenseComment(strLine)) {
						continue;
					}

					if (lc.hasLicense()) {
						int id = lc.getLicenseId();
						out.write("LICENSE_COMMENT_BY_JSTOOLS_" + id);
					}
				}

				out.write(new String(strLine.getBytes(this.encoding), this.encoding));
				out.write(System.getProperty("line.separator"));
				total_lines++;
			}
			in.close();
			tmpFile.delete();
		}
		out.close();
	}

	public int getCodeLines() {
		return total_lines;
	}
}
