/*
 *
 */
package com.miconblog.jstools.taglib;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author tugs
 */
public class MergeFile {

	private static final long serialVersionUID = -7472955881649452866L;

	private String mergedFilePath;
	private List<String> mergeFileList;

	public String getMergedFilePath() {
		return mergedFilePath;
	}

	public void setMergedFilePath(String mergedFilePath) {
		this.mergedFilePath = mergedFilePath;
	}

	public List<String> getMergeFileList() {
		return mergeFileList;
	}

	public void setMergeFileList(List<String> mergeFileList) {
		this.mergeFileList = mergeFileList;
	}

	public void addMergeFileList(String mergeFile) {

		if (mergeFileList == null) {
			mergeFileList = new ArrayList<String>();
		}

		mergeFileList.add(mergeFile);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
}
