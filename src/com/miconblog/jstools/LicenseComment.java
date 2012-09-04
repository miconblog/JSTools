package com.miconblog.jstools;

import java.util.HashMap;

public class LicenseComment {

	private boolean bFindLicense = false;
	private boolean bProcessEnd = false;
	private String str = "";
	private HashMap<Integer, String> map = new HashMap<Integer, String>();
	
	
	/**
	 * 라이센스 유지 옵션을 반환한다.
	 * @return
	 */
	public boolean isKeep() {
		return true;
	}

	/**
	 * 주석 검색을 위한 준비 단계
	 */
	public void initSearch() {
		bFindLicense = false;
		bProcessEnd = false;
		str = "";
	}
	
	
	public boolean findLicenseComment(String strLine) {
		
		// 라이센스는 하나만 있으니까.. 두번 찾을 필요는 없다.
		if( bProcessEnd ) { return false; }
		
		// 일단 라이센스 주석을 찾아보자!
		if ( !bFindLicense && strLine.contains("/***") ){
			bFindLicense = true;
			str = strLine+"\n";
			return true;
		}


		// 라이센스 주석을 찾았으면, 일단 담아두자!
		if( bFindLicense ){
			str += strLine+"\n";

			// 끝나는 주석이면, 더이상 검색할 필요없다고 알려주자!
			if( strLine.contains("*/")){
				bProcessEnd = true;
			}
			return true;
			
		}else{
			return false;
		}
	}

	/**
	 * 검색해서 저장해놓은 라이센스가 있는지 검색한다.
	 * @return
	 */
	public boolean hasLicense() {
		if( str.length() > 0){
			return true;
		}
		return false;
	}

	public int getLicenseId() {
		int count = map.size();
		if(count > 0){
			str = "\n" + str;
		}
		map.put(count, str);
		str = "";
		return count;
	}
	
	
	public int getSize() {
		return map.size();
	}
	
	public String getLicenseComment(int key){
		return (String) map.remove(key);
	}

}
