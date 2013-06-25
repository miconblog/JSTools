/*
 */
package com.miconblog.jstools.taglib;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * @author tugs
 */
public enum MergeFileType implements Serializable {

	JS("js", new JavascriptFileModifier());

	private String code;
	private FileModifier modifier;

	private static final String EXTENSION_BASE_CHARACTER = ".";

	private MergeFileType(String code, FileModifier modifier) {
		this.code = code;
		this.modifier = modifier;
	}

	public String modify(String fileContent) {
		return modifier.modify(fileContent);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public static MergeFileType findMergeFileType(String fileName) {

		if (StringUtils.contains(fileName, EXTENSION_BASE_CHARACTER + JS.getCode())) {
			return JS;
		}

		return null;
	}
}
