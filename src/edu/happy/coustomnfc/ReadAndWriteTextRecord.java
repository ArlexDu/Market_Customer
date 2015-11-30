package edu.happy.coustomnfc;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;

import android.nfc.NdefRecord;
/*
 * 用于解析nfc标签的信息
 */
public class ReadAndWriteTextRecord {

	private String mText;
	private NdefRecord ndefRecord;
	 
	public ReadAndWriteTextRecord(NdefRecord record) {
		// TODO Auto-generated constructor stub
		ndefRecord =record;
		parseinfromation();
	}
	
	public String getText(){
		return mText;
	}
	public NdefRecord getNdefRecode(){
		return ndefRecord;
	}
	/*
	 * 从nfc标签中读信息
	 * NdEF 文本格式
	 * 偏移量 0  长度 1 状态字节
	 * 偏移量 1 长度  n 语言编码，这个n由状态字节的后六位指定
	 * 偏移量 n+1 长度 m 文本数据
	 * 
	 * 状态字节编码格式
	 * 字节位 7   数据：0/1 0表示utf-8编码，1表示utf-16编码
	 * 字节位 6   数据：默认值0
	 * 字节位0到5 数据 表示语言编码长度 
	 */
	private void parseinfromation(){
		//判断是否是ndef格式
		if(ndefRecord.getTnf()!=NdefRecord.TNF_WELL_KNOWN){
			return;
		}
		if(!Arrays.equals(ndefRecord.getType(),NdefRecord.RTD_TEXT)){
			return;
		}
		try{
			byte[] payload = ndefRecord.getPayload();
			//payload[0]获取第一个字节（8位），ox80二进制就是10000000想与操作的话呢就会判断字节的第一位是1还是0
			//根据ndef的格式，payload的第一个字节的最高位代表编码格式，1是utf-16,0是utf-8
			String textEncoding = ((payload[0]&0x80)== 0)?"UTF-8":"UTF-16";
//			payload[0]获取第一个字节（8位）他的后六位是编码长度，ox3f二进制就是00111111与操作的话呢就会获得字节的后六位
			int languageCodeLength = payload[0] & 0x3f;
			
			String languageCode = new String(payload,1,languageCodeLength,"US-ASCII");
      //      System.out.println("languageCode is "+languageCode);			
			mText = new String(payload,languageCodeLength+1,payload.length - languageCodeLength - 1,textEncoding);
				
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
