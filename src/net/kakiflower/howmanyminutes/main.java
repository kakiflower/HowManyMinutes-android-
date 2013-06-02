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
	
	// 絞り込み条件
	SharedPreferences sp;
	
	// JSONデータ取得先
	private String tdlUrl = "http://www.kakiflower.net/tdl.php";
	private String tdsUrl = "http://www.kakiflower.net/tds.php";
	
	// JSONデータ
	JSONClient jsonClient;
	JSONObject rootJsonObj;
	ArrayList<atrcData> atrcList;
	
	// JSONListener
	GetJSONListener jsonListener = new GetJSONListener() {
		@Override
		public void onRemoteCallComplete(JSONObject jsonFromNet) {

			// JSONデータからatrcListにまとめる
			createAtrcList(jsonFromNet);
			
			// ソート条件によって並べ替えを行う
			setFilter();
			
			// 待ち時間リストの生成を行う
			initAtrcList();
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

    	// タイトルバー用のエリア名を設定
    	this._setTitleBarName();

    	// 最新の待ち時間JSONデータを取得
    	this._reload();

    }
    
    /*
     * 「更新」ボタンが押された時
     */
    public void reload(View v){

    	// タイトルバー用のエリア名を設定
    	this._setTitleBarName();

    	// 更新処理
    	this._reload();
    }

    /*
     * タイトルバーの設定
     */
    private void _setTitleBarName() {

    	// SharedPreferencesの取得
        sp = getSharedPreferences("sort", Context.MODE_PRIVATE);
        String title_tmp;
        
        // タイトルに表示しているエリア「ディズニーランド/ディズニーシー」を設定
        if ("TDS".equals(sp.getString("AREA", "TDS"))) {
        	title_tmp = "ディズニーシー";
        }
        else {
        	title_tmp = "ディズニーランド";
        }
        
        TextView title = (TextView)findViewById(R.id.titleBarAreaName);
        title.setText(title_tmp);

    }
    /*
     * 最新の待ち時間情報を取得する
     */
    private void _reload(){

        // SharedPreferencesの取得
        sp = getSharedPreferences("sort", Context.MODE_PRIVATE);
        
        // 読込み先URLを指定
        String useUrl;
        String area = sp.getString("AREA", "TDS");

        // ディズニーシー
        if ("TDS".equals(area)) {
        	useUrl = this.tdsUrl;
        }
        // ディズニーランド
        else {
        	useUrl = this.tdlUrl;
        }
        
        // JSONデータ解析
        this.jsonClient = new JSONClient(this, jsonListener);
        this.jsonClient.execute(useUrl);    	
    }

    /*
     * 「切替」ボタンが押された時
     */
    public void change(View v){
    	Toast.makeText(this, "切替が押されました。", Toast.LENGTH_LONG).show();
    	
    	// インテントのインスタンス生成
    	Intent intent = new Intent(main.this, sortMenu.class);
    	// 次画面のアクティビティ起動
    	startActivityForResult( intent, 0);
    }
    
    // startActivityForResult で起動させたアクティビティが
    // finish() により破棄されたときにコールされる
    // requestCode : startActivityForResult の第二引数で指定した値が渡される
    // resultCode : 起動先のActivity.setResult の第一引数が渡される
    // Intent data : 起動先Activityから送られてくる Intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
   
    	switch (requestCode) {
    	case 0:
        if (resultCode == RESULT_OK) {

        	// タイトルバー用のエリア名を設定
        	this._setTitleBarName();

        	// 再読み込み
        	this._reload();
        }

        // バックボタン等
    	default:
    		break;
    	}
    }
    
    /*
     * 待ち時間JSONデータから待ち時間リストを生成
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

				// アトラクション情報を取得
				tmp.setArea_name(o.getString("area_name"));
				tmp.setAtrc_name(o.getString("atrc_name"));
				tmp.setFp(o.getString("fp"));
				tmp.setRun(o.getString("run"));
				tmp.setUpdate(o.getString("update"));
				tmp.setWait(o.getString("wait"));
				
				// アトラクションリストへ追加
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
     * 絞り込み・並べ替え条件を設定する。
     */
    private void setFilter() {
    	//TODO 未実装
    }
    
	/*
	 *  待ち時間リストの生成を行う
	 */
	private void initAtrcList() {

		// データを格納するためのArrayListを宣言
        ArrayList<HashMap<String, String>> data
			= new ArrayList<HashMap<String, String>>();

		// アトラクション数分繰り返し
        for(int i = 0; i< this.atrcList.size(); i++){
        	
        	HashMap<String, String> map
				= new HashMap<String, String>();
			
        	atrcData tmp = this.atrcList.get(i);

        	Log.d("DEBUG", tmp.getArea_name());
        	
        	// アトラクション各データをmapに代入
        	map.put("area_name", tmp.getArea_name());
        	map.put("atrc_name", tmp.getAtrc_name());
        	map.put("fp", tmp.getFp());
        	map.put("run", tmp.getRun());
        	map.put("update", tmp.getUpdate());
        	map.put("wait", tmp.getWait());
        	map.put("my_attr_flg", String.valueOf(tmp.getMy_attr_flg()));
        	
        	// 作成したmapをdataに追加
        	data.add(map);
         }

        /*
		 * 作成したdataとカスタマイズしたレイアウトrow.xmlを
         * 紐付けたCustomAdapterを作成する
         */
        CustomAdapter ca = new CustomAdapter(this, data, R.layout.row,
        		new String[]{"atrc_name", "fp", "run", "update", "wait"},
        		new int[]{R.id.atrc_name, R.id.fp, R.id.run, R.id.update, R.id.wait}
        );
        
        // activity_main.xmlのListViewにカスタムアダプタをセット
        ListView lv = (ListView)findViewById(R.id.atrcList);
        lv.setAdapter(ca);		
	}
	// SimpleAdapterを継承したCustomAdapterを作成する
	public class CustomAdapter extends SimpleAdapter {
		
		LayoutInflater mLayoutInflater;
		
		// コンストラクタ
		public CustomAdapter( Context context,
							  List<? extends Map<String, ?>> data,
									  int resource,
									  String[] from, int[] to) {
			super(context, data, resource, from, to);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			mLayoutInflater = LayoutInflater.from( getBaseContext() );
			
			// レイアウトに「row.xml」を紐づける
			convertView = mLayoutInflater.inflate(R.layout.row, parent, false);
			ListView listView = (ListView)parent;
			
			@SuppressWarnings("unchecked")
			// 該当位置のデータを取得
			Map<String, Object> data = (Map<String, Object>) listView.getItemAtPosition(position);

			// Myアトラクションアイコン
			
			// パークエリアネーム
			//

			// アトラクション名
			TextView atrc_name = (TextView)convertView.findViewById(R.id.atrc_name);
			atrc_name.setText((String)data.get("atrc_name"));
			
			// FP発券後の利用可能時間
			TextView fp = (TextView)convertView.findViewById(R.id.fp);
			String fp_tmp = (String)data.get("fp");

			// FPパスアイコン
			ImageView fpIcon = (ImageView)convertView.findViewById(R.id.fpIcon);
			if ("".equals(fp_tmp)) {
				
				// FPアイコンを非表示(間は詰めない)
				fpIcon.setVisibility(View.INVISIBLE);

				// FP時間を非表示(間は詰めない)
				fp.setVisibility(View.INVISIBLE);
			}
			else {
				fp.setText(fp_tmp);
				
				// FP発券終了している場合はアイコンを切り替える
				if ("現在発券しておりません".equals(fp_tmp)) {
					fpIcon.setImageResource(R.drawable.icon_30x20_fp_off);
				}
				else{
					fpIcon.setImageResource(R.drawable.icon_30x20_fp_on);					
				}
				
			}
			
			// 運営状況
			TextView run = (TextView)convertView.findViewById(R.id.run);
			String run_tmp = (String)data.get("run");
			run.setText(run_tmp);

			// 更新時間
			TextView update = (TextView)convertView.findViewById(R.id.update);
			String update_tmp = (String)data.get("update");

			// 空だった場合は非表示(間を詰める)
			if ("".equals(update_tmp)){
				update.setVisibility(View.GONE);				
			}
			else {
				update.setText("(更新時間 " + update_tmp + ")");				
			}
			
			
			// 待ち時間
			TextView wait = (TextView)convertView.findViewById(R.id.wait);
			String wait_tmp = (String)data.get("wait"); 

			// 空だった場合は０をセット
			if ("".equals(wait_tmp)){
				wait_tmp = "0";
			}

			// 「運営中」かつ更新時間がある場合のみ表示。
			if ("運営中".equals(run_tmp)) {
				wait.setText(wait_tmp);
			}
			else {	
				// 待ち時間を非表示(間を詰める)
				TextView waitLabel1 = (TextView)convertView.findViewById(R.id.waitLabel1);
				TextView waitLabel2 = (TextView)convertView.findViewById(R.id.waitLabel2);
				waitLabel1.setVisibility(View.GONE);
				wait.setVisibility(View.GONE);
				waitLabel2.setVisibility(View.GONE);
			}

			// 表示フラグ
			//
			
			return convertView;
		}
	}
}