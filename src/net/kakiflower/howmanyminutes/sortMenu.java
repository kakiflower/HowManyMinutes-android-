package net.kakiflower.howmanyminutes;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.view.View;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;

public class sortMenu extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener{

	public SharedPreferences sp;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
  
    	// テーマを適用
		setTheme(R.style.Theme_Sherlock);

		super.onCreate(savedInstanceState);

		// 透明なアイコンを表示
        getSupportActionBar().setIcon(android.R.color.transparent);
    	
    	// レイアウトを割り当て
        addPreferencesFromResource(R.layout.filter);
        
        // SharedPreferencesの取得
        sp = getSharedPreferences("sort", Context.MODE_PRIVATE);

		// エリア
		ListPreference areaPref = (ListPreference)findPreference("AREA");
		if (null == areaPref.getValue()) {
			areaPref.setDefaultValue("area_tdl");
			areaPref.setSummary("ディズニーランド");
		}
		else {
			areaPref.setSummary(areaPref.getEntry());
		}

		// 待ち時間
		ListPreference waitPref = (ListPreference)findPreference("WAIT");
		if (null == waitPref.getValue()) {
			waitPref.setDefaultValue("wait_not");
			waitPref.setSummary("指定なし");
		}
		else {
			waitPref.setSummary(waitPref.getEntry());
		}
		
		// 条件で並べ替え
		ListPreference sortPref = (ListPreference)findPreference("SORT");
		if (null == sortPref.getValue()) {
			sortPref.setDefaultValue("sort_not");			
			sortPref.setSummary("指定なし");
		}
		else {
			sortPref.setSummary(sortPref.getEntry());
		}

    }

    /*
     * アクションバー生成
     */
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		
		// 保存
        menu.add("save")
            .setIcon(android.R.drawable.ic_menu_save)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }
	
	/*
	 * アクションバーのボタンが押されたとき
	 */
	 @Override
	public boolean onOptionsItemSelected(MenuItem item) {

	        if ("save".equals(item.getTitle())) {
	        	finish();
	        }
	        return super.onOptionsItemSelected(item);
	 }
    
    /*
     * ListPreferenceのサマリーを選択されたアイテムに差し替える
     * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
     */
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,  String key) {
		
		// エリア
		if (key.equals("AREA")) {
			ListPreference areaPref = (ListPreference)getPreferenceScreen().findPreference("AREA");
			areaPref.setSummary(areaPref.getEntry());
		}
		// 待ち時間
		else if (key.equals("WAIT")) {
			ListPreference waitPref = (ListPreference)getPreferenceScreen().findPreference("WAIT");
			waitPref.setSummary(waitPref.getEntry());
		}
		
		// 条件で並べ替え
		else if (key.equals("SORT")) {
			ListPreference sortPref = (ListPreference)getPreferenceScreen().findPreference("SORT");
			sortPref.setSummary(sortPref.getEntry());
		}
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

	@Override  
	protected void onResume() {  
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);  
	}  
	   
	@Override  
	protected void onPause() {  
	    super.onPause();  
	    getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);  
	}  
}
