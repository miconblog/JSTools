/*
 * @(#)MergeTag.java $version 2011. 12. 12.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.miconblog.jstools.taglib;

import java.io.File;
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

/**
 * @author tugs
 */
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

		LOGGER.debug("merged file : {}", mergedFile);

		try {

			String content;

			if (isDebug() == false && existFile(mergedFile)) {

				// 디버그 모드가 아니고, 머지 파일이 있는 경우
				content = includeStaticString(mergedFile);

// 일단 2.6.0 버전에서는 제외..				
//			} else if( isDebug() == true && existFile(mergeListFile) ){
//				
//				content = includeMergeList(mergeListFile);
				
			} else {
				
				// 디버그 모드거나 머지 파일이 없는 경우
				content = printForDebug();
			}

			getWriter().print(content);
		} catch (Exception e) {
			throw new JspException(e);
		}

		return SKIP_BODY;
	}

	private String includeMergeList(String mergeList) {
		
		
		
		
		return null;
	}

	protected JspWriter getWriter() {
		return getPreviousOut();
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
			LOGGER.warn("merged file not exists : {}", mergedFile);
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

		String fileNameWithTimeStamp = fileName + "?" + TIMESTAMP;

		if (MergeFileType.findMergeFileType(fileName) == MergeFileType.JS) {
			return "<script type=\"text/javascript\" charset=\"utf-8\" src=\"" + fileNameWithTimeStamp + "\"></script>";
		}

		return StringUtils.EMPTY;
	}

	String[] extractStaticFileList() throws SkipPageException {
		return StringUtils.split(StringUtils.replace(getJspBodyString(), "\r\n", "\n"), "\n");
	}

	String getJspBodyString() throws SkipPageException {
		return getBodyContent().getString();
	}

	public void setMergedFile(String mergedFile) {
		this.mergedFile = mergedFile;
	}

	public void setDebug(String debug) {
		this.debug = debug;
	}

	private boolean isDebug() {
		return "Y".equals(debug);
	}
}
