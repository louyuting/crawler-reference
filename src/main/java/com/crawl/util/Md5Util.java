package com.crawl.util;

import java.security.MessageDigest;

public class Md5Util {

    /**
     * MD5
     * @param password
     * @return
     */
	public static String Convert2Md5(String password){
		MessageDigest md5 = null;  
        try{  
            md5 = MessageDigest.getInstance("MD5");  
        }catch (Exception e){  
            System.out.println(e.toString());  
            e.printStackTrace();  
            return "";  
        }
        //字符转换成字符数组
        char[] charArray = password.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)  {
            //将字符强转成字节数组
            byteArray[i] = (byte) charArray[i];
        }
        //散列
        byte[] md5Bytes = md5.digest(byteArray);  
        StringBuffer hexValue = new StringBuffer();  
        for (int i = 0; i < md5Bytes.length; i++){  
            int val = ((int) md5Bytes[i]) & 0xff;  
            if (val < 16)  
                hexValue.append("0");  
            hexValue.append(Integer.toHexString(val));  
        }
//        System.out.println(hexValue);
        return hexValue.toString();  
	}


    /**
     * 测试
     * @param args
     */
	public static void main(String args []){
		System.out.println(Convert2Md5("123SSStringBuStStringBufferringBufferffertringBuffertringBStringBufferuffer456StringBufferStringBufferefvdrgdrght").length());
	}
}
