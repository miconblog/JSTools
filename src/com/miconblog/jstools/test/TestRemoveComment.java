package com.miconblog.jstools.test;

import java.io.IOException;

import org.junit.Test;

import com.miconblog.jstools.RemoveComment;


import junit.framework.*;


public class TestRemoveComment extends TestCase{
	
	public TestRemoveComment(String name){
		super(name);
	}
	
	protected void setUp(){}
	protected void tearDown(){}
	
	@Test
	public void testSingleLineComment() throws IOException{
		assertEquals("var b = e.protocol + \"//\" + e.host; ", RemoveComment.removeSingleLineComment("var b = e.protocol + \"//\" + e.host; //.이거 주석입니다."));
		assertEquals("if (test(a)) {", RemoveComment.removeSingleLineComment("if (test(a)) {// 이것도 지워!!"));
		assertEquals("if (test(a)) {", RemoveComment.removeSingleLineComment("if (test(a)) {"));
		assertEquals("", RemoveComment.removeSingleLineComment("//var renderObject = aObject || this.aObject;"));
	}

	public void testHasLink(){
		assertEquals("var ncl = \'http // sdfjldsjfl\'; ", RemoveComment.removeSingleLineComment("var ncl = \'http // sdfjldsjfl\'; "));
		assertEquals("var ncl = \'http // sdfjldsjfl\'; ", RemoveComment.removeSingleLineComment("var ncl = \'http // sdfjldsjfl\'; // 이거 지워지나?;"));
		
		assertEquals("var b = e.protocol + \"//\" + e.host; ", RemoveComment.removeSingleLineComment("var b = e.protocol + \"//\" + e.host; "));

		assertEquals("url: \" http://static.naver.com/maps2/icons/path_m2.png\", ", RemoveComment.removeSingleLineComment("url: \" http://static.naver.com/maps2/icons/path_m2.png\", //아하하 "));
		assertEquals("url: \" http://static.naver.com/maps2/icons/path_m2.png\", ", RemoveComment.removeSingleLineComment("url: \" http://static.naver.com/maps2/icons/path_m2.png\", "));
	}
	
	public void testHasRegExp(){
		assertEquals("if (/^\\.\\//.test(a)) {	", RemoveComment.removeSingleLineComment("if (/^\\.\\//.test(a)) {	"));
		assertEquals("if (/^\\.\\//.test(a)) {	", RemoveComment.removeSingleLineComment("if (/^\\.\\//.test(a)) {	// 이것도 지워!!"));
		assertEquals("var sProtocol = (this._getLocation()).match(/^[a-zA-Z]+:\\/\\//);", RemoveComment.removeSingleLineComment("var sProtocol = (this._getLocation()).match(/^[a-zA-Z]+:\\/\\//);"));
	}
	
	public void testHasFlashObject(){
		assertEquals("pluginspage=\"http://www.macromedia.com/go/getflashplayer\" width=\"1\" height=\"1\" allowScriptAccess=\"always\" swLiveConnect=\"true\" FlashVars=\"activeCallback='+activeCallback+'\"></embed></object></div>');", 
		RemoveComment.removeSingleLineComment("pluginspage=\"http://www.macromedia.com/go/getflashplayer\" width=\"1\" height=\"1\" allowScriptAccess=\"always\" swLiveConnect=\"true\" FlashVars=\"activeCallback='+activeCallback+'\"></embed></object></div>');"));
	}
	
	public void testHasDocumentWrite(){
		assertEquals("document.write('<div style=\"position:absolute;top:-1000px;left:-1000px\"><object id=\"'+jindo.$Ajax.SWFRequest._tmpId+'\" width=\"1\" height=\"1\" classid=\"clsid:d27cdb6e-ae6d-11cf-96b8-444553540000\" codebase=\"http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0\"><param name=\"movie\" value=\"'+swf_path+'\"><param name = \"FlashVars\" value = \"activeCallback='+activeCallback+'\" /><param name = \"allowScriptAccess\" value = \"always\" /><embed name=\"'+jindo.$Ajax.SWFRequest._tmpId+'\" sRemoveComment=\"'+swf_path+'\" type=\"application/x-shockwave-flash\" pluginspage=\"http://www.macromedia.com/go/getflashplayer\" width=\"1\" height=\"1\" allowScriptAccess=\"always\" swLiveConnect=\"true\" FlashVars=\"activeCallback='+activeCallback+'\"></embed></object></div>');", 
		RemoveComment.removeSingleLineComment("document.write('<div style=\"position:absolute;top:-1000px;left:-1000px\"><object id=\"'+jindo.$Ajax.SWFRequest._tmpId+'\" width=\"1\" height=\"1\" classid=\"clsid:d27cdb6e-ae6d-11cf-96b8-444553540000\" codebase=\"http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0\"><param name=\"movie\" value=\"'+swf_path+'\"><param name = \"FlashVars\" value = \"activeCallback='+activeCallback+'\" /><param name = \"allowScriptAccess\" value = \"always\" /><embed name=\"'+jindo.$Ajax.SWFRequest._tmpId+'\" sRemoveComment=\"'+swf_path+'\" type=\"application/x-shockwave-flash\" pluginspage=\"http://www.macromedia.com/go/getflashplayer\" width=\"1\" height=\"1\" allowScriptAccess=\"always\" swLiveConnect=\"true\" FlashVars=\"activeCallback='+activeCallback+'\"></embed></object></div>'); // 이거 길다"));
	}
	
	public void testBlockComment() {
		assertEquals("", RemoveComment.removeBlockComment("/* NHN Web Standardization Team (http://html.nhndesign.com/) MJA 091029 */"));
		assertEquals("\":page/*page\" : \"page\"", RemoveComment.removeBlockComment("\":page/*page\" : \"page\""));
	}
	
	public void testBlockComment2() {
		assertEquals("", RemoveComment.removeBlockComment("/** NHN Web Standardization"));
	}
	
	public void testException1() {
		assertEquals("if (/^\\s*$/.test(sKeyword)) { ", RemoveComment.removeSingleLineComment("if (/^\\s*$/.test(sKeyword)) { // 아무것도 입력 안 했으면"));
	}
	
	public void testException2() {
		assertEquals("oOpt.price = String(oOpt.price).replace(/(\\d)(?=(?:\\d{3})+(?!\\d))/g,'$1,');	", RemoveComment.removeSingleLineComment("oOpt.price = String(oOpt.price).replace(/(\\d)(?=(?:\\d{3})+(?!\\d))/g,'$1,');	// 3자리씩 콤마 찍기"));
	}
	
	
}
