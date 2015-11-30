package edu.happy.coustomnfc;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	private ImageView done; //用于选择的按钮
	private ImageView notice;//用于跳入写入界面写入Uri
	private TextView area,price,nutrition,time,name;
	private String mdata;//存储程序包的名字
	private NfcAdapter mNfcAdapter;
	private AlertDialog mDialog;
	private PendingIntent mPendingIntent;
	private int[] index = new int[7];
	private RelativeLayout show ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		name = (TextView)findViewById(R.id.name);
		area = (TextView) findViewById(R.id.text_area);
		price = (TextView) findViewById(R.id.text_price);
		nutrition = (TextView) findViewById(R.id.text_nutrition);
		time = (TextView) findViewById(R.id.leftDay);
		show = (RelativeLayout)findViewById(R.id.show);
		notice = (ImageView)findViewById(R.id.notice);
		done = (ImageView)findViewById(R.id.btn_done);
		mNfcAdapter =NfcAdapter.getDefaultAdapter(this);
	    mDialog = new AlertDialog.Builder(this).setNeutralButton("Ok", null).create();

	        //没有nfc硬件服务
	        if (mNfcAdapter == null) {
	            showMessage("提示","您的手机不支持NFC功能！");
	            return;
	        }
		
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()), 0);
	}
	
	//  展示没有nfc的窗口
    private void showMessage(String title, String message) {
        mDialog.setTitle(title);
        mDialog.setMessage(message);
        mDialog.show();
    }
    
	//点击事件的处理方法
	public void onClick(View v){
		switch(v.getId()){
//		选择app的按钮
		case R.id.btn_done:
			notice.setVisibility(View.VISIBLE);
			show.setVisibility(View.INVISIBLE);
			done.setVisibility(View.INVISIBLE);
			break;
		}
		
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//使当前窗口变得优先级最高
		if(mNfcAdapter!=null){
			mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(mNfcAdapter!=null){
			mNfcAdapter.disableForegroundDispatch(this);
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        readNfc(detectedTag,intent);          
	}
	private void readNfc(Tag tag,Intent intent){
		//判断是否是由读nfc标签打开的窗口
		//System.out.println("当前的格式是："+intent.getAction());
            if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
            //	System.out.println("进入判断");
            	Ndef ndef = Ndef.get(tag);
                Parcelable[] rawMgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                NdefMessage msg[] = null;
                int contentSize = 0;
                if(rawMgs != null){
                //	System.out.println("不为null");
                	msg = new NdefMessage[rawMgs.length];
                	for(int i =0; i<rawMgs.length ;i++){
                		msg[i] = (NdefMessage)rawMgs[i];
                		contentSize+=msg[i].toByteArray().length;
                	}
                }
                try{
                	
                	if(msg !=null){
                		//一般情况下只有一个ndefmessage和ndefrecord
                		NdefRecord record = msg[0].getRecords()[0];
                		boolean istext = false;
                		//第一判断内容是否是已知类型，包括RTD_text和RTD_uri
                		if(record.getTnf() == NdefRecord.TNF_WELL_KNOWN){
                				notice.setVisibility(View.INVISIBLE);
                			    ReadAndWriteTextRecord textRecord = new ReadAndWriteTextRecord(record);
                				//标签的信息例如：7733057006#丹麦香草威化饼干#食品#2014/10/29#2016/01/29#印尼#23#19.90
                				Good good = new Good();
                				int current = 0;
                				String info = textRecord.getText();
                				for(int i = 0;i < info.length();i++){
                					if(info.charAt(i)=='#'){
                						index[current] = i;
                						current++;
                					}
                				}
                				good.name = info.substring(index[0]+1,index[1]);
                				good.area = info.substring(index[4]+1, index[5]);
                				good.nutrition = info.substring(index[6]+1);
                				good.time = info.substring(index[3]+1,index[4]);
                				good.price = info.substring(index[5]+1,index[6]);
                				name.setText(good.name);
                				area.setText(good.area);
                				price.setText(good.price+"元");
                				nutrition.setText(good.nutrition+"NPV");
                				SimpleDateFormat format =  new SimpleDateFormat("yyyy/MM/dd");
                				        Date date;
                						date = format.parse(good.time);
                						long till_time = date.getTime();
                						long current_time = System.currentTimeMillis();
                					//	System.out.println("Format To times:"+till_time);
                					//	System.out.println("Current times:"+current_time);
                						if(current_time>till_time){
                							time.setText("这个商品已经过期！");
                							time.setTextColor(Color.RED);
                						}else{
                							long lefttime = till_time - current_time;
                							long t = lefttime/(24*60*60*1000);
                							int day = Integer.parseInt(String.valueOf(t));
                							time.setText("还有"+day+"天商品过期！");
                							time.setTextColor(Color.GREEN);
                						}
                	 				show.setVisibility(View.VISIBLE);
                	 				done.setVisibility(View.VISIBLE);
                        	}
                
                      }
	         }catch(Exception e){
	        	 e.printStackTrace();
	         }
        }
	} 
}
