package edu.happy.coustomnfc;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	private ImageView done; //����ѡ��İ�ť
	private ImageView notice;//��������д�����д��Uri
	private TextView area,price,nutrition,time,name;
	private ProgressBar bar;
	private String mdata;//�洢�����������
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
		bar = (ProgressBar)findViewById(R.id.progressBar);
		mNfcAdapter =NfcAdapter.getDefaultAdapter(this);
	    mDialog = new AlertDialog.Builder(this).setNeutralButton("Ok", null).create();

	        //û��nfcӲ������
	        if (mNfcAdapter == null) {
	            showMessage("��ʾ","�����ֻ���֧��NFC���ܣ�");
	            return;
	        }
		
		mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,getClass()), 0);
	}
	
	//  չʾû��nfc�Ĵ���
    private void showMessage(String title, String message) {
        mDialog.setTitle(title);
        mDialog.setMessage(message);
        mDialog.show();
    }
    
	//����¼��Ĵ�����
	public void onClick(View v){
		switch(v.getId()){
//		ѡ��app�İ�ť
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
		//ʹ��ǰ���ڱ�����ȼ����
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
		//�ж��Ƿ����ɶ�nfc��ǩ�򿪵Ĵ���
		//System.out.println("��ǰ�ĸ�ʽ�ǣ�"+intent.getAction());
            if(NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
            //	System.out.println("�����ж�");
            	Ndef ndef = Ndef.get(tag);
                Parcelable[] rawMgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                NdefMessage msg[] = null;
                int contentSize = 0;
                if(rawMgs != null){
                //	System.out.println("��Ϊnull");
                	msg = new NdefMessage[rawMgs.length];
                	for(int i =0; i<rawMgs.length ;i++){
                		msg[i] = (NdefMessage)rawMgs[i];
                		contentSize+=msg[i].toByteArray().length;
                	}
                }
                try{
                	
                	if(msg !=null){
                		//һ�������ֻ��һ��ndefmessage��ndefrecord
                		final NdefRecord record = msg[0].getRecords()[0];
                		boolean istext = false;
                		//��һ�ж������Ƿ�����֪���ͣ�����RTD_text��RTD_uri
                		if(record.getTnf() == NdefRecord.TNF_WELL_KNOWN){
                				notice.setVisibility(View.INVISIBLE);
                                bar.setVisibility(View.VISIBLE);
                				new Thread(new Runnable() {
									public void run() {
										 ReadAndWriteTextRecord textRecord = new ReadAndWriteTextRecord(record);
			                				//��ǩ����Ϣ���磺7733057006#���������������#ʳƷ#2014/10/29#2016/01/29#ӡ��#23#19.90
			                				Good good = new Good();
			                				int current = 0;
			                				String info = textRecord.getText();
			                				for(int i = 0;i < info.length();i++){
			                					if(info.charAt(i)=='#'){
			                						index[current] = i;
			                						current++;
			                					}
			                				}
			                				String id = info.substring(0,index[0]);
			                				good.name = info.substring(index[0]+1,index[1]);
			                				good.area = info.substring(index[4]+1, index[5]);
			                				good.nutrition = info.substring(index[6]+1);
			                				good.time = info.substring(index[3]+1,index[4]);
			                				good.price = info.substring(index[5]+1,index[6]);
			                				try {
												good.price = getPrice(id);
											} catch (Exception e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											} 
			                				Message message = new Message();
			                				message.what = 0;
			                				message.obj = good;
			                				myHandler.sendMessage(message);
									}
								}).start();
                        	}
                
                      }
	         }catch(Exception e){
	        	 e.printStackTrace();
	         }
        }
	} 
	
	Handler myHandler = new Handler(){
		
		public void handleMessage(Message msg) {
			if(msg.what==0){
				Good good = (Good)msg.obj;
				name.setText(good.name);
				area.setText(good.area);
				price.setText(good.price+"Ԫ");
				nutrition.setText(good.nutrition+"NPV");
				SimpleDateFormat format =  new SimpleDateFormat("yyyy/MM/dd");
				        Date date;
						try {
							date = format.parse(good.time);
							long till_time = date.getTime();
							long current_time = System.currentTimeMillis();
						
							//	System.out.println("Format To times:"+till_time);
							//	System.out.println("Current times:"+current_time);
							if(current_time>till_time){
								time.setText("�����Ʒ�Ѿ����ڣ�");
								time.setTextColor(Color.RED);
							}else{	
								long lefttime = till_time - current_time;
								long t = lefttime/(24*60*60*1000);
								int day = Integer.parseInt(String.valueOf(t));
								time.setText("����"+day+"����Ʒ���ڣ�");
								time.setTextColor(Color.GREEN);
							}
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	 				show.setVisibility(View.VISIBLE);
	 				done.setVisibility(View.VISIBLE);
	 				bar.setVisibility(View.INVISIBLE);
			}
		};
	};
	
	//������µļ۸�
		private String getPrice(String id) throws ClientProtocolException, IOException, JSONException{
			 System.out.println("get price");
			 String url = "http://10.60.42.70:3000/Android/price/"+id;
			 HttpResponse httpResponse = null;
			 HttpGet httpget = new HttpGet(url);
			 httpResponse = new DefaultHttpClient().execute(httpget);
			 String price=null;
			 String result=null;
			 if(httpResponse.getStatusLine().getStatusCode() == 200){
				 result =  EntityUtils.toString(httpResponse.getEntity());
				 JSONObject object = new JSONObject(result);
				 price = object.getString("price");
			 }
			 System.out.println("price is "+price);
			 return price;
		}
}
