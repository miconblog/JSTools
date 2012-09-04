package com.miconblog.jstools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 1. 원본 파일 사이즈
 * 2. 머지한 파일 사이즈
 * 3. 옵션에 따른 최적화 용량 
 * 
 * @author SOHNBD
 *
 */
public class Statistics {
	private int total_count = 0;
	private ArrayList<String> fileList;
	private long uncompressed_size = 0;
	private long compressd_size = 0;
	private Configuration conf;
	private int uncompressed_lines;
	private int compressed_lines;
		
	public Statistics() {}

	public void print() throws IOException{
		process();
		
		if(!conf.getOutput().equals("")){
			System.out.println("[RESULT INFO]");
			System.out.printf("   =================================================\n");
			System.out.printf("   %-14s %-22s %-22s \n", " ","Before", "After");
			System.out.printf("   %-14s %-22s %-22s \n", "File Count", total_count,1);
			System.out.printf("   %-14s %-22s %-22s \n", "Line Count", uncompressed_lines, compressed_lines);
			System.out.printf("   %-14s %-22s %-22s \n", "File Size", getFileSize(uncompressed_size), getFileSize(compressd_size));
			System.out.printf("   -------------------------------------------------\n");
			System.out.printf("   %-14s %s\n", "Line Compare: ", (uncompressed_lines - compressed_lines) + " reduced");
			System.out.printf("   %-14s %s\n", "Size Compare: ", getFileSize(uncompressed_size - compressd_size) + " ("+ getCompSize() + ")" );	
			System.out.printf("   =================================================\n");
		}
	}
	
	public void toFile () throws IOException, FileNotFoundException{
		
		Date today = new Date();
		SimpleDateFormat myDateType = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String todayStr = myDateType.format(today);
		
		if(conf.getInfoType().equals("TXT")){
			BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(conf.getInfoFile()), conf.getEncoding()) );
			out.write("Build Time: " + todayStr);
			out.write(System.getProperty("line.separator"));
			out.write("File Count: " + total_count +" ---> 1");
			out.write(System.getProperty("line.separator"));
			out.write("Line Count: " + uncompressed_lines +" ---> " + compressed_lines);
			out.write(System.getProperty("line.separator"));
			out.write("File Size : " + getFileSize(uncompressed_size) +" ---> " + getFileSize(compressd_size));
			out.write(System.getProperty("line.separator"));
			out.write("----------------------------------------------------------");
			out.write(System.getProperty("line.separator"));
			out.write("Compare code line : " + (uncompressed_lines - compressed_lines) + " reduced");
			out.write(System.getProperty("line.separator"));
			out.write("Compare file size : " + getFileSize(uncompressed_size - compressd_size) + " ("+ getCompSize() + ")" );	
			out.close();
		}
		
		if(conf.getInfoType().equals("JSON")){
			BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream(conf.getInfoFile()), conf.getEncoding()) );
			out.write("{'buildTime': '" + todayStr +"'");
			out.write(",'before': {'fileCount':" + total_count);
			out.write(",'lineCount': " + uncompressed_lines );
			out.write(",'fileSize': " + uncompressed_size + "}");
			out.write(",'after': {'fileCount':1");
			out.write(",'lineCount':" + compressed_lines );
			out.write(",'fileSize':" + compressd_size +"}");
			out.write(",'compare': {'codeLine':" + (uncompressed_lines - compressed_lines) );
			out.write(",'fileSize':" + (uncompressed_size - compressd_size) +"}" );
			out.write("}");
			out.close();
		}
		
		if(conf.getInfoType().equals("LOG")){
			BufferedWriter out = new BufferedWriter(new FileWriter(conf.getInfoFile(), true));
			out.write("[" + todayStr + "] files("+ total_count +") lines(" + uncompressed_lines + ", " + compressed_lines +") ");
			out.write("size(" + uncompressed_size + ", " + compressd_size + ") " );
			out.write("comp_lines(" + (uncompressed_lines - compressed_lines) + ") " );
			out.write("comp_size(" + (uncompressed_size - compressd_size) + ")" );
			out.write(System.getProperty("line.separator"));
			out.close();
		}
	}
	
	private String getCompSize(){
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setMaximumFractionDigits( 2 ) ;
		 
		BigDecimal b2 = BigDecimal.valueOf(uncompressed_size);		// 원본
		
		long result = uncompressed_size - compressd_size;
		String desc = "% saved";
		if(result < 0){
			desc = "% increse";
		}
		BigDecimal b3 = BigDecimal.valueOf(result * 100);		// 세이브
		BigDecimal b1 = b3.divide(b2, new MathContext(4));
		
		return Math.abs(b1.floatValue()) + desc;
	}
	
	private String getFileSize(long size){
		String unit = " bytes";
		
		if(size < 1000){
			return size + unit;
		}
		
		
		unit = " KB";
		
		DecimalFormat df = new DecimalFormat("#,##0");
		
		return  size / 1000 + unit + "(" + df.format(size) + ")";
	}

	private void process() throws IOException{
		
		// 기본 원본 파일 갯수와 크기 구하기
		for(int i=0; i < total_count; ++i){
			
			String path = fileList.get(i);
			File file = new File(path);
			
			if (file.exists()) {
			      long fileSize = file.length();
			      uncompressed_size += fileSize;
			}
			else System.err.println("파일이 없음...");
		}
		
		// 출력 파일이 없으면 통계를 못내.. 
		if(conf.getOutput().equals("")){
			return;
		}
		
		
		File file = new File(conf.getOutput());
		if (file.exists()) {
			compressd_size  = file.length();
		}
		
		BufferedReader in = new BufferedReader(new FileReader(conf.getOutput()));
	    while (in.readLine() != null) {
	    	compressed_lines++;
	    }
	    in.close();
	}

	public void setCodeLines(int lines) {
		uncompressed_lines = lines;
	}

	public void setMergeList(ArrayList<String> mergeList) {
		total_count  = mergeList.size();
		fileList = mergeList;
	}

	public void setConfigurations(Configuration conf) {
		this.conf = conf;
	}
}
