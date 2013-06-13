package net.kakiflower.howmanyminutes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.analytics.tracking.android.EasyTracker;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class main extends Activity implements OnItemClickListener{
	
	// 絞り込み条件
	SharedPreferences sp;
	
	// JSONデータ取得先
	private String tdlUrl = "http://www.kakiflower.net/tdl.php";
	private String tdsUrl = "http://www.kakiflower.net/tds.php";
	
	// 最後に選択したリスト番号
	private int _pos;
	
	// カスタムアダプタ
	public CustomAdapter ca;

	// リストビュー設置用ArrayList
	public ArrayList<HashMap<String, String>> data;
	
	// 表示用リストビュー
	public ListView lv;
	
	// JSONデータ
	public JSONClient jsonClient;
	public JSONObject rootJsonObj;
	public ArrayList<atrcData> atrcList;
	
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

    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this);
    }
    
    /*
     * 「更新」ボタンが押された時
     */
    public void reload(View v){

    	// タイトルバー用のエリア名を設定
    	this._setTitleBarName();

    	// 更新処理
    	this._reload();

    	// 更新回数ログを収集
    	sp = PreferenceManager.getDefaultSharedPreferences(this);
        int reload_num = sp.getInt("reload", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("reload", reload_num++);
    	EasyTracker.getTracker().sendEvent("action", "button_press", "reload", (long)reload_num);
    }

    /*
     * タイトルバーの設定
     */
    private void _setTitleBarName() {

    	// SharedPreferencesの取得
    	sp = PreferenceManager.getDefaultSharedPreferences(this);
        String title_tmp;
        
        // タイトルに表示しているエリア「ディズニーランド/ディズニーシー」を設定
        if ("area_tds".equals(sp.getString("AREA", "area_tds"))) {
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
		sp = PreferenceManager.getDefaultSharedPreferences(this);
        
        // 読込み先URLを指定
        String useUrl;

        // ディズニーシー
        if ("area_tds".equals(sp.getString("AREA", "area_tds"))) {
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

        	// 絞り込みを適用
        	
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

        // SharedPreferencesの取得
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        // Myアトラクションのみ表示
        if (sp.getBoolean("BOOKMARK", false)) {
        	this._MyAtrcOnly();
        }

    	// 解析タグ：ディズニーランド or ディズニーシー
        if ("area_tds".equals(sp.getString("AREA", "area_tds"))) {
        	EasyTracker.getTracker().sendEvent("filter", "area", "land", (long)0);
        }
        else {
        	EasyTracker.getTracker().sendEvent("filter", "area", "sea", (long)0);
        }
        
        // 待ち時間絞り込み(0：指定なし, 1：30分以内, 2：31分〜60分以内, 3：61分以降)
    	this._waitTimeBetweenOrder();

    	// 並び替え
        String sortPtn = sp.getString("SORT", "sort_not");
        if ("sort_wait".equals(sortPtn)) {
        	this._waitTimeShortOrder();        	
        }
        else if ("sort_update".equals(sortPtn)) {
        	this._updateTimeNewOrder();        	
        }
        else if ("sort_fp".equals(sortPtn)) {
        	this._fpAtrcOnly();
        }
        else {
        	// 指定なし
        }
    	
    }
    
    /*
     * 待ち時間指定で絞り込み 
     */
    private void _waitTimeBetweenOrder() {

    	int min;
    	int max;

        // SharedPreferencesの取得
    	sp = PreferenceManager.getDefaultSharedPreferences(this);
    	String waitTime = sp.getString("WAIT", "wait_not");
        
        // ３０分以内
        if ("wait_30minute".equals(waitTime)) {
        	min = 0;
        	max = 30;
        	// 解析タグ
        	EasyTracker.getTracker().sendEvent("filter", "wait_ptn", "middle_of_0_30", (long)0);
        }
        // ３１分〜６０分以内
        else if ("wait_60minute".equals(waitTime)) {
        	min = 31;
        	max = 60;
        	// 解析タグ
        	EasyTracker.getTracker().sendEvent("filter", "wait_ptn", "middle_of_30_60", (long)0);
        }
        // ６１分以降
        else if ("wait_60over".equals(waitTime)) {
        	min = 61;
        	max = 999;        	
        	// 解析タグ
        	EasyTracker.getTracker().sendEvent("filter", "wait_ptn", "60_over", (long)0);
        }
    	// 未指定の場合
        else {
        	// 解析タグ
        	EasyTracker.getTracker().sendEvent("filter", "wait_ptn", "no_select", (long)0);
        	return;
        }
        
    	// 待ち時間が指定した時間帯以外の場合は非表示とする。
    	int i = 0;
    	while(i < this.atrcList.size()) {
    		atrcData tmp = this.atrcList.get(i);
    		int atrcTimeInt;
    		String atrcTimeStr = tmp.getWait();
    		if("".equals(atrcTimeStr)){
    			atrcTimeInt = 0;
    		}
    		else {
    			atrcTimeInt = Integer.valueOf(atrcTimeStr);
    		}
    		
    		if (min <= atrcTimeInt && atrcTimeInt <= max) {
    			i++;
    		}
    		else {
    			this.atrcList.remove(i);
    		}
    	}
    }
    
    /*
     * 並び替え：待ち時間の短い順
     */
    private void _waitTimeShortOrder() {
    	Collections.sort(this.atrcList, new waitTimeComprator());    	
    	// 解析タグ
    	EasyTracker.getTracker().sendEvent("filter", "button_press", "wait_order", (long)0);
    }

    /*
     * 並び替え：更新時間の新しい順
     */
    private void _updateTimeNewOrder() {
    	Collections.sort(this.atrcList, new updateTimeComprator());
    	// 解析タグ
    	EasyTracker.getTracker().sendEvent("filter", "button_press", "update_order", (long)0);
    }
    
    /*
     * 絞り込み：ファストパス対応のみ
     */
    private void _fpAtrcOnly() {

    	// FP文言が空文字(設定なし)のアトラクションを除外する。
    	int i = 0;
    	while(i < this.atrcList.size()) {
    		atrcData tmp = this.atrcList.get(i);
    		String fp_tmp = tmp.getFp();
    		if ("".equals(fp_tmp)) {
    			this.atrcList.remove(i);
    		}
    		else {
    			i++;
    		}
    	}
    	// 解析タグ
    	EasyTracker.getTracker().sendEvent("filter", "button_press", "fp_only", (long)0);
    }
    
    /*
     * 絞り込み：MYアトラクションのみ
     */
    private void _MyAtrcOnly() {

    	// ブックマークに登録されていないアトラクションは削除する
    	int i = 0;
    	while(i < this.atrcList.size()) {
    		atrcData tmp = this.atrcList.get(i);
    		if (!this._isMyAttraction(tmp.getAtrc_name())) {
    			this.atrcList.remove(i);
    		}
    		else {
    			i++;
    		}
    	}
    	// 解析タグ：お気に入りのみ
    	EasyTracker.getTracker().sendEvent("filter", "button_press", "bookmark_only", (long)0);
    }
    
    /*
	 *  待ち時間リストの生成を行う
	 */
	private void initAtrcList() {

		// データを格納するためのArrayListを宣言
        data = new ArrayList<HashMap<String, String>>();

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
        	map.put("bookmark", String.valueOf(tmp.getBookmark()));

        	// 作成したmapをdataに追加
        	data.add(map);
         }

        /*
		 * 作成したdataとカスタマイズしたレイアウトrow.xmlを
         * 紐付けたCustomAdapterを作成する
         */
        this.ca = new CustomAdapter(this, data, R.layout.row,
        		new String[]{"atrc_name", "fp", "run", "update", "wait", "bookmark"},
        		new int[]{R.id.atrc_name, R.id.fp, R.id.run, R.id.update, R.id.wait, R.id.bookmark}
        );

        // activity_main.xmlのListViewにカスタムアダプタをセット
        lv = (ListView)findViewById(R.id.atrcList);
        lv.setAdapter(ca);
        
        // リスナーを登録する
        lv.setOnItemClickListener(this);
	}
	
	/*
	 * セルが選択された際の処理
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		// 最後に選択されたリスト番号を保持
		this._pos = position;
		
		// 選択されたアトラクション情報
		atrcData atrcData_tmp = this.atrcList.get(position);

		// Myアトラクション登録確認ダイアログ
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // アラートダイアログのタイトルを設定します
        alertDialogBuilder.setTitle("確認");
        
        // アラートダイアログのメッセージを設定します
        String q;
        String atrcName = atrcData_tmp.getAtrc_name();
        // メッセージタイプ種類を設定
        if (_isMyAttraction(atrcName)) {
        	q = atrcName +"¥nをMyアトラクションから解除しますか？";
        }
        else {
        	q = atrcName + "¥nをMyアトラクションに登録しますか？";        	
        }
        
        alertDialogBuilder.setMessage(q);
        
        // アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        alertDialogBuilder.setPositiveButton("ＯＫ",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	
                    	// TODO:修正中
                    	sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    	// 選択されたアトラクション情報
                    	atrcData tmp = atrcList.get(_pos);

                		// ローカルのアトラクション情報を更新
                		atrcList.set(_pos, tmp);
                		
	                	// 更新したいセルのデータを新たに作成
                    	HashMap<String, String> map = new HashMap<String, String>();
	                	map.put("area_name", tmp.getArea_name());
	                	map.put("atrc_name", tmp.getAtrc_name());
	                	map.put("fp", tmp.getFp());
	                	map.put("run", tmp.getRun());
	                	map.put("update", tmp.getUpdate());
	                	map.put("wait", tmp.getWait());
	                	
	                	// 作成したデータを削除し、削除したインデックスに追加を行う。
	                	data.remove(_pos);

                    	// 現在のお気に入り状態の逆を取得する
                    	Boolean mode = !_isMyAttraction(tmp.getAtrc_name());

                		// SharedPreferenceにお気に入り情報を保存
                		_saveMyAttraction(tmp.getAtrc_name(), mode);

	                	// ブックマーク表示限定かつブックマーク解除の場合はセルを追加しない。
                        // お気に入りアトラクションのみ表示
                        if (sp.getBoolean("BOOKMARK", false) && mode) {

                        	// アダプター用のデータが１つ減るため、ローカルのデータも１つ削除する
                        	atrcList.remove(_pos);
                        }
                        else {
                        	// ListView更新のため、削除➡追加を行う。
                        	data.add(_pos, map);
                        }

	                	// アダプタにデータ変更を通知
                		ca.notifyDataSetChanged();
                		
                		// リストビューの描画を更新
                		lv.invalidateViews();
                    }
                });
        
        // アラートダイアログの中立ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        alertDialogBuilder.setNeutralButton("キャンセル",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        // アラートダイアログのキャンセルが可能かどうかを設定します
        alertDialogBuilder.setCancelable(true);
        AlertDialog alertDialog = alertDialogBuilder.create();
        
        // アラートダイアログを表示します
        alertDialog.show();
		
	}
	
	/*
	 * ローカルにお気に入りアトラクション情報を保存する
	 * 
	 * @param String atrcName アトラクション名
	 * @param Boolean true:保存する, false:保存しない
	 */
	private void _saveMyAttraction(String atrcName, Boolean mode) {

		// SharedPreferencesの取得
        sp = getSharedPreferences("sort", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(atrcName, mode);
        editor.commit();
	}
	
	/*
	 * ローカルに保存されたお気に入りアトラクション情報を取得する
	 * 
	 * @param String atrcName アトラクション名
	 * @return Boolean true  引数で指定されたアトラクションがお気に入りに保存されている
	 *                 false 引数で指定されたアトラクションがお気に入りに保存されていない
	 */
	private Boolean _isMyAttraction(String atrcName) {

		// SharedPreferencesの取得
        sp = getSharedPreferences("sort", Context.MODE_PRIVATE);
        return sp.getBoolean(atrcName, false);
	}	
	
	/*
	 * SimpleAdapterを継承したCustomAdapterを作成する
	 */
	public class CustomAdapter extends SimpleAdapter{
		
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

			// パークエリアネーム
			// TODO 考慮不足

			// アトラクション名
			TextView atrc_name = (TextView)convertView.findViewById(R.id.atrc_name);
			atrc_name.setText((String)data.get("atrc_name"));

			// ブックマークアイコン
			ImageView bookmark = (ImageView)convertView.findViewById(R.id.bookmark);

			// 空の場合は表示
			if (_isMyAttraction((String)data.get("atrc_name"))) {
				bookmark.setVisibility(View.VISIBLE);
			}
			else {
				bookmark.setVisibility(View.INVISIBLE);
			}

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
	
	/*
	 * 待ち時間の短い順にソートする処理を実装
	 */
	public class waitTimeComprator implements Comparator<atrcData>{
		
		@Override
		public int compare(atrcData lhs, atrcData rhs) {
			atrcData atrc1 = (atrcData)lhs;
			atrcData atrc2 = (atrcData)rhs;
			
			// 待ち時間データが空の場合は０分とする
			int wait1 = atrc1.getWait().equals("") ? 0 : Integer.parseInt(atrc1.getWait());
			int wait2 = atrc2.getWait().equals("") ? 0 : Integer.parseInt(atrc2.getWait());
			
			return wait1 < wait2 ? -1 : 1;
		}
	}

	/*
	 * 更新時間の新しい順にソート
	 */
	public class updateTimeComprator implements Comparator<atrcData>{
		
		@Override
		public int compare(atrcData lhs, atrcData rhs) {
			atrcData atrc1 = (atrcData)lhs;
			atrcData atrc2 = (atrcData)rhs;
			
			// 待ち時間データが空の場合は０分とする
			int hour1;
			int hour2;
			int minute1;
			int minute2;
			
			if ("".equals(atrc1.getUpdate())) {
				hour1 = 0;
				minute1 = 0;
			}
			else {
				String[] tmp = atrc1.getUpdate().split(":");
				hour1 = Integer.parseInt(tmp[0]);
				minute1 = Integer.parseInt(tmp[1]);
			}

			if ("".equals(atrc2.getUpdate())) {
				hour2 = 0;
				minute2 = 0;
			}
			else {
				String[] tmp = atrc2.getUpdate().split(":");
				hour2 = Integer.parseInt(tmp[0]);
				minute2 = Integer.parseInt(tmp[1]);
			}

			/*
			int hour1 = atrc1.getUpdate().equals("") ? 0 : Integer.parseInt(atrc1.getUpdate().substring(0, 1)); 
			int minute1 = atrc1.getUpdate().equals("") ? 0 : Integer.parseInt(atrc1.getUpdate().substring(3, 4));
			int hour2 = atrc2.getUpdate().equals("") ? 0 : Integer.parseInt(atrc2.getUpdate().substring(0, 1)); 
			int minute2 = atrc2.getUpdate().equals("") ? 0 : Integer.parseInt(atrc2.getUpdate().substring(3, 4));
			*/
			Log.d("update1", atrc1.getUpdate());
			Log.d("update1", atrc2.getUpdate());
			Log.d("hour1", String.valueOf(hour1));
			Log.d("minute1", String.valueOf(minute1));
			Log.d("hour2", String.valueOf(hour2));
			Log.d("minute2", String.valueOf(minute2));
			
			return (hour1*60+minute1) < (hour2*60+minute2) ? 1 : -1;
		}
	}

}