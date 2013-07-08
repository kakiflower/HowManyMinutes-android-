package net.kakiflower.howmanyminutes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ad_stir.AdstirTerminate;
import com.ad_stir.AdstirView;
import com.google.analytics.tracking.android.EasyTracker;

public class main extends SherlockActivity implements OnItemClickListener{

	// 広告関連
    LinearLayout adLayout;
	AdstirView adstirView;
	Timer adTimer;
	
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
				
				// 運営状態の変更チェック、変更が有る場合はポップアップ表示
				_chkRunChange();
			}
			else {
				
				// タイトル設定
				_setTitleBarName();
				
	        	// ネットワークエラーダイアログ
	        	showDialog("",getResources().getString(R.string.error_msg_conect));
			}
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	super.onCreate(savedInstanceState);

    	// ActionSherlockデフォルトテーマ
		setTheme(R.style.Theme_Sherlock);

		// アイコンなし
        getSupportActionBar().setIcon(android.R.color.transparent);

        // アクティビティをセット
        setContentView(R.layout.activity_main);

        // 定期的に広告を読み直す
        adTimer = new Timer();
        TimerTask timerTask = new AdTimerTask(this);
        adTimer.scheduleAtFixedRate(timerTask, 0, 30000);
        
        // 広告用レイアウトを生成
        adLayout = (LinearLayout)findViewById(R.id.adLayout);
    }

    /**
     * 広告の生成
     */
    private void addAd() {
    	
    	// 広告が既に表示されている場合は破棄したうえで生成
    	if (null != this.adstirView) {
    		AdstirTerminate.init(this);
    		adLayout.removeAllViews();
    		adstirView = null;
    	}

    	// 広告生成
		this.adstirView = new AdstirView(this, "MEDIA-69d42e91", 1);
		adLayout.addView(this.adstirView, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
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
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        menu.add(getResources().getString(R.string.menu_label_pref))
        	.setIcon(android.R.drawable.ic_menu_preferences)
        	.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

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
    protected void onStart() {
 
		// 言語設定を反映
    	sp = PreferenceManager.getDefaultSharedPreferences(this);
    	String lang = sp.getString("LANGUAGE", Locale.getDefault().toString());

    	// タイトルバー用のエリア名を設定
    	this._setTitleBarName();

    	this.setLocale(lang);

        // 最新の待ち時間JSONデータを取得
    	// (ソート画面からバックして来た場合も強制的にリロード処理)
    	this._reload();
    	
    	super.onStart();
    	EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onStop() {
    	super.onStop();
    	EasyTracker.getInstance().activityStop(this);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	AdstirTerminate.init(this);
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
    	String title = sp.getString("AREA", "area_tdl");
        if ("area_tdl".equals(title) ||  "".equals(title)) {
            getSupportActionBar().setTitle(getResources().getString(R.string.disney_land));
        }
        else {
            getSupportActionBar().setTitle(getResources().getString(R.string.disney_sea));
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

        // ディズニーランド
        if ("area_tdl".equals(sp.getString("AREA", "area_tdl"))) {
        	useUrl = this.tdlUrl;
        }
        // ディズニーシー
        else {
        	useUrl = this.tdsUrl;
        }
    	
        // 端末の言語設定により取得JSONパラメタを付与
    	String locale = sp.getString("LANGUAGE", Locale.getDefault().toString());

    	// 日本語
    	if (locale.startsWith("ja")){
    	}
    	// 中国語(繁体)
        else if (locale.equalsIgnoreCase("zh_TW")){
        	useUrl += "?lang=ch_easy";
        }
    	// 中国語(簡体)
        else if (locale.startsWith("zh")){
        	useUrl += "?lang=ch_hard";
        }
        // 該当しない場合は英語で表記
        else {
        	useUrl += "?lang=en";        	
        }

        // ネットワーク未接続の場合はダイアログ表示
        if (isConnected()) {

        	// JSONデータ解析
            this.jsonClient = new JSONClient(this, jsonListener);
            this.jsonClient.execute(useUrl);    	
        }
        else {
        	
        	// ネットワークエラーダイアログ
        	showDialog("",getResources().getString(R.string.error_msg_conect));
        }
    }

    /*
     * ダイアログ表示(OKのみ)
     * @param String title タイトル
     * @param String msg   ダイアログ表示用のメッセージ
     */
    public void showDialog(String title, String msg) {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// 指定が有る場合のみタイトル表示
		if (!"".equals(title)) {
			alertDialogBuilder.setTitle(title);
		}

		alertDialogBuilder.setMessage(msg);
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
    		int ver = rootJson.getInt("ver");
    		
        	// アプリの最新バージョンチェック
        	this._appVersionCheck(ver);

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
        if ("area_tdl".equals(sp.getString("AREA", "area_tdl"))) {
        	EasyTracker.getTracker().sendEvent("filter", "area", "land", (long)0);
        }
        else {
        	EasyTracker.getTracker().sendEvent("filter", "area", "sea", (long)0);
        }
        
        // 待ち時間絞り込み(0：指定なし, 1：30分以内, 2：31分〜60分以内, 3：61分以降)
    	this._waitTimeBetweenOrder();

    	// 並び替え(デフォルト：待ち時間の長い順)
        String sortPtn = sp.getString("SORT", "sort_wait_long");
        if ("sort_wait_short".equals(sortPtn)) {
        	this._waitTimeShortOrder();        	
        }
        else if ("sort_wait_long".equals(sortPtn)) {
        	this._waitTimeLongOrder();        	
        }
        else if ("sort_update".equals(sortPtn)) {
        	this._updateTimeNewOrder();        	
        }
        else if ("sort_fp".equals(sortPtn)) {
        	this._fpAtrcOnly();
        }
        else {
        	// 指定なし(テーマポート別）
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
    	Collections.sort(this.atrcList, new waitTimeShortComprator());    	
    	// 解析タグ
    	EasyTracker.getTracker().sendEvent("filter", "button_press", "wait_order", (long)0);
    }

    /*
     * 並び替え：待ち時間の長い順
     */
    private void _waitTimeLongOrder() {
    	Collections.sort(this.atrcList, new waitTimeLongComprator());    	
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
    		if (!this._isMyAttraction(tmp.getArea_name(), tmp.getAtrc_name())) {
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
        		new String[]{"area_name", "atrc_name", "fp", "run", "update", "wait", "bookmark"},
        		new int[]{R.id.area_name, R.id.atrc_name, R.id.fp, R.id.run, R.id.update, R.id.wait, R.id.bookmark}
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
        String areaName = atrcData_tmp.getArea_name();
        // メッセージタイプ種類を設定
        if (_isMyAttraction(areaName, atrcName)) {
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
                    	Boolean visibleFlg = _isMyAttraction(tmp.getArea_name(), tmp.getAtrc_name());
                    	
                		// SharedPreferenceにお気に入り情報を保存
                		_saveMyAttraction(tmp.getArea_name(), tmp.getAtrc_name(), !visibleFlg);

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

	/**
	 * 更新前と更新後で運営状態に変化がないかチェック、変更がある場合はダイアログ表示を行う
	 */
	private void _chkRunChange() {

		// 閉園中はチェック対象外
		if (!this.openFlg) return;
		
		// SharedPreferencesの取得
		sp = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.Editor editor = sp.edit();
        
        // 時間取得
        Time time = new Time("Asia/Tokyo");
        time.setToNow();
        int now_date = time.month + time.monthDay;

        // 最新の運営状況
        String nowRun;
        
        // 前回の運営状況
        String prevRun;

    	// アトラクション情報
    	atrcData atrc;
        
        // 最終利用日を取得
        int prev_date;
        
        // 日付保存対象
        String key;

        // 端末の言語設定
    	String locale = sp.getString("LANGUAGE", Locale.getDefault().toString());

        // ディズニーランド
        if ("area_tdl".equals(sp.getString("AREA", "area_tdl"))) {
            key = "save_tdl_" + locale;
        }
        // ディズニーシー
        else {
        	key = "save_tds_" + locale;
        }
    	prev_date = sp.getInt(key, 0);

        // 最終利用日が異なる場合は初回アクセス扱いとし終了
        if (now_date != prev_date || prev_date == 0) {
        	
    		// 今日の日付を最終利用日とする
    		editor.putInt(key, now_date);

    		// 現在の運営状況を初期値として保存する
    		for (int i = 0; i < this.atrcList.size(); i++) {
    			
            	// 選択されたアトラクション情報
            	atrc = this.atrcList.get(i);

            	// 運営状況＋エリア名＋アトラクション名
            	nowRun = atrc.getRun() + "," + atrc.getArea_name() + "," + atrc.getAtrc_name();    			

            	// 保存キー名は「エリア名＋アトラクション名」
            	editor.putString(atrc.getArea_name() + atrc.getAtrc_name(), nowRun);

    		}

    		// 保存
    		editor.commit();
    		
    		return;
        }

        // ポップアップ用メッセージ
		String msg, area_name, atrc_name, run;
		String[] run_status;

		// 運営状況が変わっている場合はダイアログを表示(アトラクションの数だけ繰り返す。)
		for (int i = 0; i < this.atrcList.size(); i++) {
			
        	// 選択されたアトラクション情報
        	atrc = this.atrcList.get(i);
        	area_name = atrc.getArea_name();
        	atrc_name = atrc.getAtrc_name();
        	run = atrc.getRun();
        	
        	// 保存キー名は「エリア名＋アトラクション名」
        	prevRun = sp.getString(area_name + atrc_name, "");
			nowRun = run + "," + area_name + "," + atrc_name;

			// 変更がある場合
			if (!prevRun.equals(nowRun) && !"".equals(nowRun)) {
				
				// 前回の運営状況のみ取り出し
				run_status = prevRun.split(",");

				msg = atrc_name + "\n";
				msg += "(" + area_name + ")\n\n";
				msg += getResources().getString(R.string.dialog_msg_before) + "\n" + run_status[0] + "\n\n";
				msg += getResources().getString(R.string.dialog_msg_after) + "(" + getResources().getString(R.string.menu_label_reload) + " " + atrc.getUpdate() + ")\n" + run;
				showDialog(getResources().getString(R.string.dialog_title_chk_update), msg);
			}
			
        	// 運営状況＋エリア名＋アトラクション名
        	nowRun = run + "," + area_name + "," + atrc_name;    			

        	// 保存キー名は「エリア名＋アトラクション名」
        	editor.putString(area_name + atrc_name, nowRun);
		}
		
        editor.commit();

	}
	
	/**
	 * ローカルにお気に入りアトラクション情報を保存する
	 * 
	 * @param String areaName エリア名
	 * @param String atrcName アトラクション名
	 * @param Boolean true:保存する, false:保存しない
	 */
	private void _saveMyAttraction(String areaName, String atrcName, Boolean mode) {

		// SharedPreferencesの取得
        sp = getSharedPreferences("sort", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(areaName + atrcName, mode);
        editor.commit();
	}
	
	/**
	 * ローカルに保存されたお気に入りアトラクション情報を取得する
	 * 
	 * @param String areaName エリア名
	 * @param String atrcName アトラクション名
	 * @return Boolean true  引数で指定されたアトラクションがお気に入りに保存されている
	 *                 false 引数で指定されたアトラクションがお気に入りに保存されていない
	 */
	private Boolean _isMyAttraction(String areaName, String atrcName) {

		// SharedPreferencesの取得
        sp = getSharedPreferences("sort", Context.MODE_PRIVATE);
        return sp.getBoolean(areaName + atrcName, false);
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

			// 背景色
			listView.setBackgroundColor(Color.GRAY);
			
			// エリア名
			TextView area_name = (TextView)convertView.findViewById(R.id.area_name);
			area_name.setText("[" + (String)data.get("area_name") + "]");
			area_name.setTextColor(themeNameToThemeColor((String)data.get("area_name")));

			// アトラクション名
			TextView atrc_name = (TextView)convertView.findViewById(R.id.atrc_name);
			atrc_name.setText((String)data.get("atrc_name"));

			// ブックマークアイコン
			ImageView bookmark = (ImageView)convertView.findViewById(R.id.bookmark);

			// 空の場合は表示
			if (_isMyAttraction((String)data.get("area_name"), (String)data.get("atrc_name"))) {
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
						
			return convertView;
		}
	}
	
	/*
	 * 待ち時間の短い順にソートする処理を実装
	 */
	public class waitTimeShortComprator implements Comparator<atrcData>{
		
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
	 * 待ち時間の長い順にソートする処理を実装
	 */
	public class waitTimeLongComprator implements Comparator<atrcData>{
		
		@Override
		public int compare(atrcData lhs, atrcData rhs) {
			atrcData atrc1 = (atrcData)lhs;
			atrcData atrc2 = (atrcData)rhs;
			
			// 待ち時間データが空の場合は０分とする
			int wait1 = atrc1.getWait().equals("") ? 0 : Integer.parseInt(atrc1.getWait());
			int wait2 = atrc2.getWait().equals("") ? 0 : Integer.parseInt(atrc2.getWait());
			
			return wait1 < wait2 ? 1 : -1;
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
	
	/**
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
    
    /**
     * バージョンコードを取得する
     */
    private int getVersionCode(){
    	Context context = this.getApplicationContext();
    	PackageManager pm = context.getPackageManager();
        int versionCode = 0;
        try{
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        }catch(NameNotFoundException e){
            e.printStackTrace();
        }
        return versionCode;
    }
    
    /**
     * アプリのバージョンが最新ではない場合、インストールを促す
     */
    private void _appVersionCheck(int newVer) {

        int nowVer = getVersionCode();

        final String pacageName = this.getPackageName();
        // バージョンチェック
        if (newVer > nowVer) {
    	
	    	// お知らせ用ダイアログ
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

			// アラートダイアログのキャンセル不可
	        alertDialogBuilder.setCancelable(false);
	        
			alertDialogBuilder.setTitle(getResources().getString(R.string.dialog_title_update));
			alertDialogBuilder.setMessage(getResources().getString(R.string.dialog_msg_update));

			// はい
			alertDialogBuilder.setPositiveButton(getResources().getString(R.string.btn_label_hai),
	                new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int which) {
	            	 Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pacageName));
	            	 try{
	            		 startActivity(intent);
	            	 }catch (Exception e){
	            	     e.printStackTrace();
	            	 }
	            }
	        });

			AlertDialog alertDialog = alertDialogBuilder.create();
	        alertDialog.show();        	
        }
        
   }
    /**
     * テーマランド名からテーマランドカラー文字列を返却する
     */
    private int themeNameToThemeColor(String themeName) {

    	String textColor;

    	// TDL
    	if (themeName.equals(getResources().getString(R.string.theme_name_wb))) {
    		textColor = getResources().getString(R.string.theme_color_wb);
    	}
    	else if(themeName.equals(getResources().getString(R.string.theme_name_al))) {
    		textColor = getResources().getString(R.string.theme_color_al);    		
    	}
    	else if(themeName.equals(getResources().getString(R.string.theme_name_wl))) {
    		textColor = getResources().getString(R.string.theme_color_wl);    		
    	}
    	else if(themeName.equals(getResources().getString(R.string.theme_name_cc))) {
    		textColor = getResources().getString(R.string.theme_color_cc);    		
    	}
    	else if(themeName.equals(getResources().getString(R.string.theme_name_fl))) {
    		textColor = getResources().getString(R.string.theme_color_fl);    		
    	}
    	else if(themeName.equals(getResources().getString(R.string.theme_name_tt))) {
    		textColor = getResources().getString(R.string.theme_color_tt);    		
    	}
    	else if(themeName.equals(getResources().getString(R.string.theme_name_tl))) {
    		textColor = getResources().getString(R.string.theme_color_tl);    		
    	}
    	// TDS
    	else if(themeName.equals(getResources().getString(R.string.theme_name_mh))) {
    		textColor = getResources().getString(R.string.theme_color_mh);    		
    	}    	
    	else if(themeName.equals(getResources().getString(R.string.theme_name_aw))) {
    		textColor = getResources().getString(R.string.theme_color_aw);    		
    	}    	
    	else if(themeName.equals(getResources().getString(R.string.theme_name_pd))) {
    		textColor = getResources().getString(R.string.theme_color_pd);    		
    	}    	
    	else if(themeName.equals(getResources().getString(R.string.theme_name_ld))) {
    		textColor = getResources().getString(R.string.theme_color_ld);    		
    	}    	
    	else if(themeName.equals(getResources().getString(R.string.theme_name_ac))) {
    		textColor = getResources().getString(R.string.theme_color_ac);    		
    	}    	
    	else if(themeName.equals(getResources().getString(R.string.theme_name_ml))) {
    		textColor = getResources().getString(R.string.theme_color_ml);    		
    	}    	
    	else if(themeName.equals(getResources().getString(R.string.theme_name_mi))) {
    		textColor = getResources().getString(R.string.theme_color_mi);    		
    	}
    	else {
    		textColor = "#000000";
    	}

    	return Color.parseColor(textColor); 
    }
    /**
     * 指定された言語を設定する
     * @param lang 言語
     */
	void setLocale(String lang){
		Locale locale;
		
    	// 日本語
    	if (lang.startsWith("ja")){
    		locale = Locale.JAPAN;
    	}
    	// 中国語(繁体)
        else if (lang.equals("zh_TW")){
        	locale = Locale.TAIWAN;
        }
    	// 中国語(簡体)
        else if (lang.startsWith("zh")){
        	locale = Locale.CHINESE;
        }
        // 該当しない場合は英語で表記
        else {
        	locale = Locale.ENGLISH;        	
        }
    	
		// リソースの指定
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		Resources resource = getBaseContext().getResources();
		resource.updateConfiguration(config, null);		
	}
	
	/**
	 * 広告タイマー処理
	 */
	public class AdTimerTask extends TimerTask {
		  private Handler handler;
		  private Context context;
		    
		  public AdTimerTask(Context context) {
		    handler = new Handler();
		    this.context = context;
		  }
		    
		  @Override
		  public void run() {
		    handler.post(new Runnable() {
		      @Override
		      public void run() {
		        ((main)context).addAd();
		      }
		    });
		  }
		 
		}
}

