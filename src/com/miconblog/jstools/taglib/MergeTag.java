package com.miconblog.jstools.taglib;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.SkipPageException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.miconblog.jstools.MergeFileList;

public class MergeTag extends BodyTagSupport {

	private static final long serialVersionUID = -4148258114404445596L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MergeTag.class);
	private static final Map<String, Boolean> MERGED_FILE_CHECKER = new ConcurrentHashMap<String, Boolean>();
	private static final long TIMESTAMP = System.currentTimeMillis();
	private static final String LINE_BREAK = "\n";

	private String debug;
	private String mergedFile;
	private String mergeListFile;
	
	static MergeFileList list = new MergeFileList();
 
	@Override
	public int doAfterBody() throws JspException {

		try {

			String content;

			if (isDebug() == false && existFile(mergedFile)) {
				
				LOGGER.info("Compress Mode");

				// 디버그 모드가 아니고, 머지 파일이 있는 경우
				content = includeStaticString(mergedFile);

			} else if( isDebug() == true && existListFile(mergeListFile) ){
				
				LOGGER.info("Debug Mode");
				
				File file = new File(mergeListFile);
				list.extractMergeFilesFromList(file, "JS");
				
				// 목록 파일에서 리스트 불러오기
				if(list.getListforCustomTag() == null){
					content = includeStaticString(mergedFile);
				}else{
					content = printFromMergeList(list.getListforCustomTag());	
				}
				
			} else {
				
				LOGGER.info("Inline Mode");
				// 디버그 모드거나 머지 파일이 없는 경우
				content = printForDebug();
			}

			getWriter().print(content);
		} catch (Exception e) {
			throw new JspException(e);
		}

		return SKIP_BODY;
	}

	/**
	 * 목록 파일에서 js 파일을 읽어 출력한다.
	 * @param mergeList
	 * @return
	 */
	private String printFromMergeList(ArrayList<String> mergeList) {
		
		StringBuilder sb = new StringBuilder();

		for (String js : mergeList) {
			
			if (StringUtils.isNotBlank(js)) {
				sb.append(includeStaticString(js)).append(LINE_BREAK);
			} else {
				sb.append(LINE_BREAK);
			}
		}

		return sb.toString();
	}

	protected JspWriter getWriter() {
		return getPreviousOut();
	}
	
	boolean existListFile(String mergeListFile){
		File file = new File(mergeListFile);
		return file.exists();
	}

	boolean existFile(String mergedFile) {

		String basePath = getBasePath();

		String absoulutePath = basePath + mergedFile;

		Boolean result = MERGED_FILE_CHECKER.get(absoulutePath);

		if (result != null) {
			return result;
		}

		boolean exists = new File(absoulutePath).exists();

		MERGED_FILE_CHECKER.put(absoulutePath, exists);

		if (exists == false) {
			LOGGER.info("merged file not exists : {}", mergedFile);
		}

		return exists;
	}

	protected String getBasePath() {
		return pageContext.getServletContext().getRealPath("/");
	}

	String printForDebug() throws SkipPageException {

		String[] jsList = extractStaticFileList();

		StringBuilder sb = new StringBuilder();

		for (String js : jsList) {

			if (StringUtils.isNotBlank(js)) {
				sb.append(includeStaticString(js)).append(LINE_BREAK);
			} else {
				sb.append(LINE_BREAK);
			}
		}

		return sb.toString();
	}

	String includeStaticString(String fileName) {

		String fileNameWithTimeStamp = fileName.trim() + "?" + TIMESTAMP;

		if (MergeFileType.findMergeFileType(fileName) == MergeFileType.JS) {
			return "<script charset=\"utf-8\" src=\"" + fileNameWithTimeStamp + "\"></script>";
		}

		return StringUtils.EMPTY;
	}

	String[] extractStaticFileList() throws SkipPageException {
		return StringUtils.split(StringUtils.replace(getJspBodyString(), "\r\n", "\n"), "\n");
	}

	String getJspBodyString() throws SkipPageException {
		return getBodyContent().getString();
	}


	private boolean isDebug() {
		return "Y".equals(debug);
	}
	
	/**
	 * TLD 파일에 기술된 속성값 설정
	 * @param mergedFile 압축한 JS 파일
	 */
	public void setMergedFile(String mergedFile) {
		this.mergedFile = mergedFile;
	}
	
	/**
	 * TLD 파일에 기술된 속성값 설정
	 * @param mergeListFile 압축할 목록 파일
	 */
	public void setMergeListFile(String mergeListFile) {
		this.mergeListFile = mergeListFile;
	}

	/**
	 * TLD 파일에 기술된 속성값 설정
	 * @param debug 디버그 속성 [Y/N]
	 */
	public void setDebug(String debug) {
		this.debug = debug;
	}

}
