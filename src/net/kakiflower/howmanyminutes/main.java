package net.kakiflower.howmanyminutes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.ParseException;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class main extends Activity {
	
	// JSON�f�[�^�擾��
	private String tdlUrl = "http://www.kakiflower.net/tdl.php";
	private String tdsUrl = "http://www.kakiflower.net/tds.php";
	
	// JSON�f�[�^
	JSONClient jsonClient;
	private String json;
	JSONObject rootJsonObj;
	
	// �������_�C�A���O
	ProgressDialog progressDialog;
	
	// �A�g���N�V������
	String[] atrc_name_list = new String[] { 
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        ""
    }; 
	
    // �҂�����
	String[] wait_list = new String[] { 
	        "",
	        "",
	        "",
	        "",
	        "",
	        "",
	        "",
	        "",
	        "",
	        ""
    };
	
	// JSONListener
	GetJSONListener jsonListener = new GetJSONListener() {
		@Override
		public void onRemoteCallComplete(JSONObject jsonFromNet) {
			initAtrcList(jsonFromNet);
			Log.d("json:status", "complete!!");
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
    	    	
        // �J�X�^���^�C�g�����g�p����
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        // �A�N�e�B�r�e�B���Z�b�g
        setContentView(R.layout.activity_main);        

        // �J�X�^���^�C�g�����Z�b�g
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

    	// �ŐV�̑҂�����JSON�f�[�^���擾
        this.jsonClient = new JSONClient(this, jsonListener);
        this.jsonClient.execute(this.tdlUrl);
        
        //    	_reload();
        
        // �f�[�^���i�[���邽�߂�ArrayList��錾
        ArrayList<HashMap<String, String>> data
			= new ArrayList<HashMap<String, String>>();

		// 10�������J��Ԃ�
        for(int i = 0; i< 10; i++){
        	HashMap<String, String> map
				= new HashMap<String, String>();
				
			// ���R�[�h��map�ɑ��
        	map.put("atrc_name", atrc_name_list[i]);
        	
        	// ������map�ɑ��
        	map.put("wait", wait_list[i]);
        	
        	// �쐬����map��data�ɒǉ�
        	data.add(map);
         }
        /*
		 * �쐬����data�ƃJ�X�^�}�C�Y�������C�A�E�grow.xml��
         * �R�t����SimpleAdapter���쐬����
         */
        SimpleAdapter sa
			= new SimpleAdapter(this, data, R.layout.row, 
                new String[]{"atrc_name", "wait"},
                new int[]{R.id.atrc_name, R.id.wait}
        );
        // main.xml��ListView��sa���Z�b�g���܂��B
        ListView lv = (ListView)findViewById(R.id.listview);
        lv.setAdapter(sa);	

    }
    
    @Override
    public void onResume() {
    	
    	super.onResume();
    	
    }
    /*
     * �u�X�V�v�{�^���������ꂽ��
     */
    public void reload(View v){
    	
        this.jsonClient = new JSONClient(this, jsonListener);
        this.jsonClient.execute(this.tdlUrl);
    	
//    	_reload();

    	// �_�C�A���O�\��
//    	this.showLoading();

        // �҂�����JSON�f�[�^���擾
//    	this.getJsonData();
    	
    	// �_�C�A���O��\��
//    	this.progressDialog.dismiss();
    	
//    	Log.d("json", this.json);
    }
    /*
     * �u�X�V�v����
     */
    private void _reload() {

    	// �_�C�A���O�\��
    	this.showLoading();

        // �҂�����JSON�f�[�^���擾
    	this.getJsonData();
    	
    	// �_�C�A���O��\��
//    	this.progressDialog.dismiss();
    	
    	Log.d("json", this.json);
    }
    /*
     * �u�ؑցv�{�^���������ꂽ��
     */
    public void change(View v){
    	Toast.makeText(this, "�ؑւ�������܂����B", Toast.LENGTH_LONG).show();
    	
    	// �C���e���g�̃C���X�^���X����
    	Intent intent = new Intent(main.this, sortMenu.class);
    	// ����ʂ̃A�N�e�B�r�e�B�N��
    	startActivity( intent );
    }

    /*
     *  �ǂݍ��ݒ����[�f�B���O�\��
     */
    private void showLoading() {

    	// ProgressDialog�C���X�^���X�𐶐�
    	this.progressDialog = new ProgressDialog( this );
    	
    	// �v���O���X�X�^�C����ݒ�
    	this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	
    	// �L�����Z���\
    	this.progressDialog.setCancelable(true);
    	
    	// �^�C�g��
    	this.progressDialog.setTitle("�ŐV�����擾��");

    	// ���b�Z�[�W
    	this.progressDialog.setMessage("���΂炭���҂����������B");
    	
    	// �\��
    	this.progressDialog.show();

    }
    
    /*
     * �҂����ԃ��X�g�̏�����
     */
    private void initAtrcList(JSONObject rootJson) {

    	JSONArray jsons;
    	
    	try {
			int status = rootJson.getInt("status");
			Log.d("status", String.valueOf(status));
			
			jsons = rootJson.getJSONArray("attractions");
			
			for (int i = 0; i < jsons.length(); i++) {
				JSONObject o = jsons.getJSONObject(i);

				Log.d("area_name", o.getString("area_name"));
				Log.d("atrc_name", o.getString("atrc_name"));
				Log.d("fp", o.getString("fp"));
				Log.d("run", o.getString("run"));
				Log.d("update", o.getString("update"));
				Log.d("wait", o.getString("wait"));
			}

		} catch (JSONException e) {
			Log.d("error", "JSONException");
			e.printStackTrace();
		}
    }
    
    /*
     * �҂����ԏ��JSON�f�[�^���擾 
     */
    private void getJsonData(){
    	DefaultHttpClient httpClient = new DefaultHttpClient();  
        HttpParams params = httpClient.getParams();  
        HttpConnectionParams.setConnectionTimeout(params, 1000);  
        HttpConnectionParams.setSoTimeout(params, 1000);  
        HttpGet httpRequest = new HttpGet(this.tdlUrl);  
        HttpResponse httpResponse = null;
        
        try {  
        	httpResponse = httpClient.execute(httpRequest);  
        } catch (ClientProtocolException e) {  
        	e.printStackTrace();  
        } catch (IOException e) {  
        	e.printStackTrace();  
        }  
        if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        	HttpEntity httpEntity = httpResponse.getEntity();  
  
        	try {  
        		this.json = EntityUtils.toString(httpEntity);  
        	} catch (ParseException e) {  
        		e.printStackTrace();  
        	} catch (IOException e) {  
        		e.printStackTrace();  
        	} finally {  
        		try {  
        			httpEntity.consumeContent();  
        		} catch (IOException e) {  
        			e.printStackTrace();  
        		}  
        	}  
        }  
        httpClient.getConnectionManager().shutdown();
    }   
}