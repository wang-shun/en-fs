package com.chinacreator.c2.fs.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/** 
* 摘要Builder类,MD5
*
* @author tyf 
* @param  
* @date 2014-6-12 下午4:35:19 
*/ 
public class SummaryBuilder {
	
	
	private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
        '9', 'a', 'b', 'c', 'd', 'e', 'f' }; 
	
	 /**
     * 对文件全文生成MD5摘要
     * @param inputStream 文件流
     * @return MD5摘要码
     */
    public static String getFileSummaryByMD5(InputStream inputStream) {
        InputStream fis = inputStream;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] buffer = new byte[2048];
            int length = -1;
            
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            
            byte[] b = md.digest();
            return byteToHexString(b);
           
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }finally {
            try {
                fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * 把byte[]数组转换成十六进制字符串表示形式
     * @param tmp    要转换的byte[]
     * @return 十六进制字符串表示形式
     */
    private static String byteToHexString(byte[] tmp) {
        char str[] = new char[16 * 2]; 
        int k = 0; 
        for (int i = 0; i < 16; i++) { 
            byte byte0 = tmp[i]; 
            str[k++] = hexDigits[byte0 >>> 4 & 0xf]; 
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str); 
    }


}
