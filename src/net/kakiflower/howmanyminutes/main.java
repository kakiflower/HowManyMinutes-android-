package net.kakiflower.howmanyminutes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class main extends Activity {
	
	// �i�荞�ݏ���
	SharedPreferences sp;
	
	// JSON�f�[�^�擾��
	private String tdlUrl = "http://www.kakiflower.net/tdl.php";
	private String tdsUrl = "http://www.kakiflower.net/tds.php";
	
	// JSON�f�[�^
	JSONClient jsonClient;
	JSONObject rootJsonObj;
	ArrayList<atrcData> atrcList;
	
	// JSONListener
	GetJSONListener jsonListener = new GetJSONListener() {
		@Override
		public void onRemoteCallComplete(JSONObject jsonFromNet) {

			// JSON�f�[�^����atrcList�ɂ܂Ƃ߂�
			createAtrcList(jsonFromNet);
			
			// �\�[�g�����ɂ���ĕ��בւ����s��
			setFilter();
			
			// �҂����ԃ��X�g�̐������s��
			initAtrcList();
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

    	// �^�C�g���o�[�p�̃G���A����ݒ�
    	this._setTitleBarName();

    	// �ŐV�̑҂�����JSON�f�[�^���擾
    	this._reload();

    }
    
    /*
     * �u�X�V�v�{�^���������ꂽ��
     */
    public void reload(View v){

    	// �^�C�g���o�[�p�̃G���A����ݒ�
    	this._setTitleBarName();

    	// �X�V����
    	this._reload();
    }

    /*
     * �^�C�g���o�[�̐ݒ�
     */
    private void _setTitleBarName() {

    	// SharedPreferences�̎擾
        sp = getSharedPreferences("sort", Context.MODE_PRIVATE);
        String title_tmp;
        
        // �^�C�g���ɕ\�����Ă���G���A�u�f�B�Y�j�[�����h/�f�B�Y�j�[�V�[�v��ݒ�
        if ("TDS".equals(sp.getString("AREA", "TDS"))) {
        	title_tmp = "�f�B�Y�j�[�V�[";
        }
        else {
        	title_tmp = "�f�B�Y�j�[�����h";
        }
        
        TextView title = (TextView)findViewById(R.id.titleBarAreaName);
        title.setText(title_tmp);

    }
    /*
     * �ŐV�̑҂����ԏ����擾����
     */
    private void _reload(){

        // SharedPreferences�̎擾
        sp = getSharedPreferences("sort", Context.MODE_PRIVATE);
        
        // �Ǎ��ݐ�URL���w��
        String useUrl;
        String area = sp.getString("AREA", "TDS");

        // �f�B�Y�j�[�V�[
        if ("TDS".equals(area)) {
        	useUrl = this.tdsUrl;
        }
        // �f�B�Y�j�[�����h
        else {
        	useUrl = this.tdlUrl;
        }
        
        // JSON�f�[�^���
        this.jsonClient = new JSONClient(this, jsonListener);
        this.jsonClient.execute(useUrl);    	
    }

    /*
     * �u�ؑցv�{�^���������ꂽ��
     */
    public void change(View v){
    	Toast.makeText(this, "�ؑւ�������܂����B", Toast.LENGTH_LONG).show();
    	
    	// �C���e���g�̃C���X�^���X����
    	Intent intent = new Intent(main.this, sortMenu.class);
    	// ����ʂ̃A�N�e�B�r�e�B�N��
    	startActivityForResult( intent, 0);
    }
    
    // startActivityForResult �ŋN���������A�N�e�B�r�e�B��
    // finish() �ɂ��j�����ꂽ�Ƃ��ɃR�[�������
    // requestCode : startActivityForResult �̑������Ŏw�肵���l���n�����
    // resultCode : �N�����Activity.setResult �̑��������n�����
    // Intent data : �N����Activity���瑗���Ă��� Intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
   
    	switch (requestCode) {
    	case 0:
        if (resultCode == RESULT_OK) {

        	// �^�C�g���o�[�p�̃G���A����ݒ�
        	this._setTitleBarName();

        	// �ēǂݍ���
        	this._reload();
        }

        // �o�b�N�{�^����
    	default:
    		break;
    	}
    }
    
    /*
     * �҂�����JSON�f�[�^����҂����ԃ��X�g�𐶐�
     */
    private void createAtrcList(JSONObject rootJson) {

    	JSONArray jsons;
    	
    	try {
			int status = rootJson.getInt("status");
			Log.d("status", String.valueOf(status));
			
			jsons = rootJson.getJSONArray("attractions");
			
			this.atrcList = new ArrayList<atrcData>();
			
			for (int i = 0; i < jsons.length(); i++) {
				JSONObject o = jsons.getJSONObject(i);
				atrcData tmp = new atrcData();

				// �A�g���N�V���������擾
				tmp.setArea_name(o.getString("area_name"));
				tmp.setAtrc_name(o.getString("atrc_name"));
				tmp.setFp(o.getString("fp"));
				tmp.setRun(o.getString("run"));
				tmp.setUpdate(o.getString("update"));
				tmp.setWait(o.getString("wait"));
				
				// �A�g���N�V�������X�g�֒ǉ�
				this.atrcList.add(tmp);
				
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
     * �i�荞�݁E���בւ�������ݒ肷��B
     */
    private void setFilter() {
    	//TODO ������
    }
    
	/*
	 *  �҂����ԃ��X�g�̐������s��
	 */
	private void initAtrcList() {

		// �f�[�^���i�[���邽�߂�ArrayList��錾
        ArrayList<HashMap<String, String>> data
			= new ArrayList<HashMap<String, String>>();

		// �A�g���N�V���������J��Ԃ�
        for(int i = 0; i< this.atrcList.size(); i++){
        	
        	HashMap<String, String> map
				= new HashMap<String, String>();
			
        	atrcData tmp = this.atrcList.get(i);

        	Log.d("DEBUG", tmp.getArea_name());
        	
        	// �A�g���N�V�����e�f�[�^��map�ɑ��
        	map.put("area_name", tmp.getArea_name());
        	map.put("atrc_name", tmp.getAtrc_name());
        	map.put("fp", tmp.getFp());
        	map.put("run", tmp.getRun());
        	map.put("update", tmp.getUpdate());
        	map.put("wait", tmp.getWait());
        	map.put("my_attr_flg", String.valueOf(tmp.getMy_attr_flg()));
        	
        	// �쐬����map��data�ɒǉ�
        	data.add(map);
         }

        /*
		 * �쐬����data�ƃJ�X�^�}�C�Y�������C�A�E�grow.xml��
         * �R�t����CustomAdapter���쐬����
         */
        CustomAdapter ca = new CustomAdapter(this, data, R.layout.row,
        		new String[]{"atrc_name", "fp", "run", "update", "wait"},
        		new int[]{R.id.atrc_name, R.id.fp, R.id.run, R.id.update, R.id.wait}
        );
        
        // activity_main.xml��ListView�ɃJ�X�^���A�_�v�^���Z�b�g
        ListView lv = (ListView)findViewById(R.id.atrcList);
        lv.setAdapter(ca);		
	}
	// SimpleAdapter���p������CustomAdapter���쐬����
	public class CustomAdapter extends SimpleAdapter {
		
		LayoutInflater mLayoutInflater;
		
		// �R���X�g���N�^
		public CustomAdapter( Context context,
							  List<? extends Map<String, ?>> data,
									  int resource,
									  String[] from, int[] to) {
			super(context, data, resource, from, to);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			mLayoutInflater = LayoutInflater.from( getBaseContext() );
			
			// ���C�A�E�g�Ɂurow.xml�v��R�Â���
			convertView = mLayoutInflater.inflate(R.layout.row, parent, false);
			ListView listView = (ListView)parent;
			
			@SuppressWarnings("unchecked")
			// �Y���ʒu�̃f�[�^���擾
			Map<String, Object> data = (Map<String, Object>) listView.getItemAtPosition(position);

			// My�A�g���N�V�����A�C�R��
			
			// �p�[�N�G���A�l�[��
			//

			// �A�g���N�V������
			TextView atrc_name = (TextView)convertView.findViewById(R.id.atrc_name);
			atrc_name.setText((String)data.get("atrc_name"));
			
			// FP������̗��p�\����
			TextView fp = (TextView)convertView.findViewById(R.id.fp);
			String fp_tmp = (String)data.get("fp");

			// FP�p�X�A�C�R��
			ImageView fpIcon = (ImageView)convertView.findViewById(R.id.fpIcon);
			if ("".equals(fp_tmp)) {
				
				// FP�A�C�R�����\��(�Ԃ͋l�߂Ȃ�)
				fpIcon.setVisibility(View.INVISIBLE);

				// FP���Ԃ��\��(�Ԃ͋l�߂Ȃ�)
				fp.setVisibility(View.INVISIBLE);
			}
			else {
				fp.setText(fp_tmp);
				
				// FP�����I�����Ă���ꍇ�̓A�C�R����؂�ւ���
				if ("���ݔ������Ă���܂���".equals(fp_tmp)) {
					fpIcon.setImageResource(R.drawable.icon_30x20_fp_off);
				}
				else{
					fpIcon.setImageResource(R.drawable.icon_30x20_fp_on);					
				}
				
			}
			
			// �^�c��
			TextView run = (TextView)convertView.findViewById(R.id.run);
			String run_tmp = (String)data.get("run");
			run.setText(run_tmp);

			// �X�V����
			TextView update = (TextView)convertView.findViewById(R.id.update);
			String update_tmp = (String)data.get("update");

			// �󂾂����ꍇ�͔�\��(�Ԃ��l�߂�)
			if ("".equals(update_tmp)){
				update.setVisibility(View.GONE);				
			}
			else {
				update.setText("(�X�V���� " + update_tmp + ")");				
			}
			
			
			// �҂�����
			TextView wait = (TextView)convertView.findViewById(R.id.wait);
			String wait_tmp = (String)data.get("wait"); 

			// �󂾂����ꍇ�͂O���Z�b�g
			if ("".equals(wait_tmp)){
				wait_tmp = "0";
			}

			// �u�^�c���v���X�V���Ԃ�����ꍇ�̂ݕ\���B
			if ("�^�c��".equals(run_tmp)) {
				wait.setText(wait_tmp);
			}
			else {	
				// �҂����Ԃ��\��(�Ԃ��l�߂�)
				TextView waitLabel1 = (TextView)convertView.findViewById(R.id.waitLabel1);
				TextView waitLabel2 = (TextView)convertView.findViewById(R.id.waitLabel2);
				waitLabel1.setVisibility(View.GONE);
				wait.setVisibility(View.GONE);
				waitLabel2.setVisibility(View.GONE);
			}

			// �\���t���O
			//
			
			return convertView;
		}
	}
}