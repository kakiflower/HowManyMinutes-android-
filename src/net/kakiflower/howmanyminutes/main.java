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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;

public class main extends SherlockActivity implements OnItemClickListener{

	// 絞り込み条件
	SharedPreferences sp;
	
	// JSONデータ取得先
	private String tdlUrl = "http://www.kakiflower.net/app/how-many-minute/tdl.php";
	private String tdsUrl = "http://www.kakiflower.net/app/how-many-minute/tds.php";

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

	// 開園状況 true:開演中, false:閉園中
	public Boolean openFlg;
	
	// JSONListener
	GetJSONListener jsonListener = new GetJSONListener() {
		@Override
		public void onRemoteCallComplete(JSONObject jsonFromNet) {
			
			// JSONデータからatrcListにまとめる
			if (createAtrcList(jsonFromNet)) {

				// ソート条件によって並べ替えを行う
				setFilter();
				
				// 待ち時間リストの生成を行う
				initAtrcList();
				
				// オープンしていない場合、絞り込み０件の場合にポップアップを表示
				openCheck();
			}
			else {
				
				// タイトル設定
				_setTitleBarName();
				
				// ネットワークエラー表示
				netWorkErrorDialog();
			}
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {

    	super.onCreate(savedInstanceState);

    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
		setTheme(R.style.Theme_Sherlock);

		// アイコンなし
        getSupportActionBar().setIcon(android.R.color.transparent);

        // アクティビティをセット
        setContentView(R.layout.activity_main);        
    }

    /*
     * 対象のアトラクションが存在しない場合ダイアログを表示、再度絞り込みメニューへ
     */
    public void openCheck() {
    	
    	// お知らせ用ダイアログ
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(true);
        
        // 閉園中の場合はダイアログ表示
    	if (!this.openFlg) {
    		alertDialogBuilder.setMessage(getResources().getString(R.string.now_park_close));
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.btn_label_ok),
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
    	}
    	// 開演中だが絞り込み該当0件の場合はダイアログ表示
    	else if(this.openFlg && this.atrcList.size() == 0) {
    		alertDialogBuilder.setMessage(getResources().getString(R.string.target_atrc_not));
            alertDialogBuilder.setPositiveButton(getResources().getString(R.string.btn_label_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        	// 絞り込み画面に遷移する
                        	change();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
    	}

    }

    /*
    * アクションバーのメニュー及びメニューボタン押下時のメニュー生成
	*/
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add("reload")
            .setIcon(android.R.drawable.ic_menu_rotate)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add("filter")
        	.setIcon(android.R.drawable.ic_menu_preferences)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        
        menu.add(getResources().getString(R.string.menu_label_reload))
            .setIcon(android.R.drawable.ic_menu_rotate)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        menu.add(getResources().getString(R.string.menu_label_pref))
        	.setIcon(android.R.drawable.ic_menu_preferences)
        	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

	/*
	 * アクションバーのボタンが押されたとき
	 */
	 @Override
	public boolean onOptionsItemSelected(MenuItem item) {

		 	String title = (String)item.getTitle();
		 
	        if ("reload".equals(title) || 
	        	getResources().getString(R.string.menu_label_reload).equals(title)) {
	        	this.reload();
	        }
	        else if ("filter".equals(title) || 
	        		getResources().getString(R.string.menu_label_pref).equals(title)) {
	        	this.change();
	        }
	        return super.onOptionsItemSelected(item);
	 }

    @Override
    public void onStart() {

    	// タイトルバー用のエリア名を設定
    	this._setTitleBarName();
    	
    	// 最新の待ち時間JSONデータを取得
    	// (ソート画面からバックして来た場合も強制的にリロード処理)
    	this._reload();
    	
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
    public void reload() {

    	// タイトルバー用のエリア名を設定
    	this._setTitleBarName();

    	// 更新処理
    	this._reload();

    	// 更新回数ログを収集
    	sp = PreferenceManager.getDefaultSharedPreferences(this);
        int reload_num = sp.getInt("reload", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("reload", reload_num++);
        editor.commit();
    	EasyTracker.getTracker().sendEvent("action", "button_press", "reload", (long)reload_num);

    }

    /*
     * タイトルバーの設定
     */
    private void _setTitleBarName() {

    	// SharedPreferencesの取得
    	sp = PreferenceManager.getDefaultSharedPreferences(this);
        
        // タイトルに表示しているエリア「ディズニーランド/ディズニーシー」を設定
    	String title = sp.getString("AREA", "area_tds");
        if ("area_tds".equals(title) ||  "".equals(title)) {
            getSupportActionBar().setTitle(getResources().getString(R.string.disney_sea));
        }
        else {
            getSupportActionBar().setTitle(getResources().getString(R.string.disney_land));
        }
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

        // ネットワーク未接続の場合はダイアログ表示
        if (isConnected()) {

        	// JSONデータ解析
            this.jsonClient = new JSONClient(this, jsonListener);
            this.jsonClient.execute(useUrl);    	
        }
        else {
        	
        	// ネットワークエラー用ダイアログ
        	netWorkErrorDialog();
        }
    }

    /*
     * ネットワークエラーダイアログ表示
     */
    public void netWorkErrorDialog() {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(getResources().getString(R.string.error_msg_conect));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.btn_label_ok),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
    /*
     * 「切替」ボタンが押された時
     */
    public void change() {
    	
    	// インテントのインスタンス生成
    	Intent intent = new Intent(main.this, sortMenu.class);
    	// 次画面のアクティビティ起動
    	startActivity(intent);
    }

    /*
     * 待ち時間JSONデータから待ち時間リストを生成
	 * @return true:JSON通信成功, false:JSON通信失敗
     */
    private Boolean createAtrcList(JSONObject rootJson) {

    	JSONArray jsons;
    	
    	try {
    		// 通信失敗時はエラー扱いとする。
//    		if (rootJson.get("status") == JSONObject.NULL) return false; 
    		if (rootJson == null) return false; 

    		int status = rootJson.getInt("status");

			// 開園状況 true:開演中, false:閉園中
			openFlg = (200 == status ? true : false);

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
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	return true;
    }
    
    /*
     * 絞り込み・並べ替え条件を設定する。
     */
    private void setFilter() {

        // SharedPreferencesの取得
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        // お気に入りのみ表示
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

    	// 待ち時間最大
    	int max;

        // SharedPreferencesの取得
    	sp = PreferenceManager.getDefaultSharedPreferences(this);
    	String waitTime = sp.getString("WAIT", "wait_not");

        // ３０分以内
        if ("wait_30minute".equals(waitTime)) {
        	max = 30;
        	// 解析タグ
        	EasyTracker.getTracker().sendEvent("filter", "wait_ptn", "wait_30", (long)0);
        }
        // ６０分以内
        else if ("wait_60minute".equals(waitTime)) {
        	max = 60;
        	// 解析タグ
        	EasyTracker.getTracker().sendEvent("filter", "wait_ptn", "wait_60", (long)0);
        }
        // 120分以内
        else if ("wait_120minute".equals(waitTime)) {
        	max = 999;        	
        	// 解析タグ
        	EasyTracker.getTracker().sendEvent("filter", "wait_ptn", "wait_120", (long)0);
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
    		
    		if (atrcTimeInt <= max) {
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
     * 絞り込み：お気に入りのみ
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

		// お気に入り登録確認ダイアログ
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // アラートダイアログのメッセージを設定します
        String q;
        String atrcName = atrcData_tmp.getAtrc_name();
        // メッセージタイプ種類を設定
        if (_isMyAttraction(atrcName)) {
        	q = atrcName + getResources().getString(R.string.dialog_msg_delete);
        }
        else {
        	q = atrcName + getResources().getString(R.string.dialog_msg_regist);        	
        }
        
        alertDialogBuilder.setMessage(q);

        // SharedPreferencesの取得
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        
    	// お気に入り表示モードフラグ
    	final Boolean bookMarkFlg = sp.getBoolean("BOOKMARK", false);

    	// アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.btn_label_hai),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

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

                    	// 現在のお気に入り状態を取得する
                    	Boolean visibleFlg = _isMyAttraction(tmp.getAtrc_name());
                    	
                		// SharedPreferenceにお気に入り情報を保存
                		_saveMyAttraction(tmp.getAtrc_name(), !visibleFlg);

	                	// ブックマーク表示限定かつブックマーク解除の場合はセルを追加しない。
                        // お気に入りアトラクションのみ表示
                        if (bookMarkFlg && visibleFlg) {
                        	
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
        alertDialogBuilder.setNeutralButton(getResources().getString(R.string.btn_label_iie),
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
				if (getResources().getString(R.string.atrc_msg_fp_pass_end).equals(fp_tmp)) {
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
				update.setText("(" + getResources().getString(R.string.atrc_msg_update) +  " "+ update_tmp + ")");				
			}
			
			// 待ち時間
			TextView wait = (TextView)convertView.findViewById(R.id.wait);
			String wait_tmp = (String)data.get("wait"); 

			// 空だった場合は０をセット
			if ("".equals(wait_tmp)){
				wait_tmp = "0";
			}

			// 「運営中」かつ更新時間がある場合のみ表示。
			if (getResources().getString(R.string.atrc_msg_run_ok).equals(run_tmp)) {
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
			
			return (hour1*60+minute1) < (hour2*60+minute2) ? 1 : -1;
		}
	}
	
	/*
	 * ネットワーク接続状態を返却する 
	 */
    public boolean isConnected() {
    	Context context = this.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if( ni != null ){
            return cm.getActiveNetworkInfo().isConnected();
        }
        return false;
    }
}