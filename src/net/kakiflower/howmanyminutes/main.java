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
	
	// JSONデータ取得先
	private String tdlUrl = "http://www.kakiflower.net/tdl.php";
	private String tdsUrl = "http://www.kakiflower.net/tds.php";
	
	// JSONデータ
	JSONClient jsonClient;
	private String json;
	JSONObject rootJsonObj;
	
	// 処理中ダイアログ
	ProgressDialog progressDialog;
	
	// アトラクション名
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
	
    // 待ち時間
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
    	    	
        // カスタムタイトルを使用する
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        // アクティビティをセット
        setContentView(R.layout.activity_main);        

        // カスタムタイトルをセット
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);

    	// 最新の待ち時間JSONデータを取得
        this.jsonClient = new JSONClient(this, jsonListener);
        this.jsonClient.execute(this.tdlUrl);
        
        //    	_reload();
        
        // データを格納するためのArrayListを宣言
        ArrayList<HashMap<String, String>> data
			= new ArrayList<HashMap<String, String>>();

		// 10ヶ国分繰り返し
        for(int i = 0; i< 10; i++){
        	HashMap<String, String> map
				= new HashMap<String, String>();
				
			// 国コードをmapに代入
        	map.put("atrc_name", atrc_name_list[i]);
        	
        	// 国名をmapに代入
        	map.put("wait", wait_list[i]);
        	
        	// 作成したmapをdataに追加
        	data.add(map);
         }
        /*
		 * 作成したdataとカスタマイズしたレイアウトrow.xmlを
         * 紐付けたSimpleAdapterを作成する
         */
        SimpleAdapter sa
			= new SimpleAdapter(this, data, R.layout.row, 
                new String[]{"atrc_name", "wait"},
                new int[]{R.id.atrc_name, R.id.wait}
        );
        // main.xmlのListViewにsaをセットします。
        ListView lv = (ListView)findViewById(R.id.listview);
        lv.setAdapter(sa);	

    }
    
    @Override
    public void onResume() {
    	
    	super.onResume();
    	
    }
    /*
     * 「更新」ボタンが押された時
     */
    public void reload(View v){
    	
        this.jsonClient = new JSONClient(this, jsonListener);
        this.jsonClient.execute(this.tdlUrl);
    	
//    	_reload();

    	// ダイアログ表示
//    	this.showLoading();

        // 待ち時間JSONデータを取得
//    	this.getJsonData();
    	
    	// ダイアログ非表示
//    	this.progressDialog.dismiss();
    	
//    	Log.d("json", this.json);
    }
    /*
     * 「更新」処理
     */
    private void _reload() {

    	// ダイアログ表示
    	this.showLoading();

        // 待ち時間JSONデータを取得
    	this.getJsonData();
    	
    	// ダイアログ非表示
//    	this.progressDialog.dismiss();
    	
    	Log.d("json", this.json);
    }
    /*
     * 「切替」ボタンが押された時
     */
    public void change(View v){
    	Toast.makeText(this, "切替が押されました。", Toast.LENGTH_LONG).show();
    	
    	// インテントのインスタンス生成
    	Intent intent = new Intent(main.this, sortMenu.class);
    	// 次画面のアクティビティ起動
    	startActivity( intent );
    }

    /*
     *  読み込み中ローディング表示
     */
    private void showLoading() {

    	// ProgressDialogインスタンスを生成
    	this.progressDialog = new ProgressDialog( this );
    	
    	// プログレススタイルを設定
    	this.progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	
    	// キャンセル可能
    	this.progressDialog.setCancelable(true);
    	
    	// タイトル
    	this.progressDialog.setTitle("最新情報を取得中");

    	// メッセージ
    	this.progressDialog.setMessage("しばらくお待ちください。");
    	
    	// 表示
    	this.progressDialog.show();

    }
    
    /*
     * 待ち時間リストの初期化
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
     * 待ち時間情報JSONデータを取得 
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