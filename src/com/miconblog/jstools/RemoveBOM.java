package com.miconblog.jstools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class RemoveBOM {
	public static void process(File file, File out) throws IOException {
    	FileInputStream fs = new FileInputStream(file);
    	byte[] bom = new byte[3];          
    	fs.read(bom, 0, 3);
    	
    	byte b[] = new byte[(int)file.length()];
        int leng = 0;
    	
        if( (leng = fs.read(b)) > 0 ){
        	FileOutputStream os = new FileOutputStream(out);
        	if ( !byteToHex(bom).equals("EFBBBF") ) {
        		os.write(bom,0,3);
        	}
        	os.write(b,0,leng);
        	os.close();
    	}
    	    	
      	fs.close();
	}
	
	private static synchronized String byteToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for ( int i = 0; i < data.length;  i++) {
			buf.append(byteToHex(data[i]).toUpperCase());
		}
		return buf.toString();
	}

	private static synchronized String byteToHex(byte data) {
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data >>> 4) & 0x0F));
		buf.append(toHexChar(data & 0x0F));
		return buf.toString();
	}

	private static synchronized char toHexChar(int i) {
		if ((i >= 0) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('a' + (i -10));
		}
	}
}
