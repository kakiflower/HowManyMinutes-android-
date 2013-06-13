package net.kakiflower.howmanyminutes;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.google.analytics.tracking.android.EasyTracker;

public class sortMenu extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	public SharedPreferences sp;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.filter);
        
        // SharedPreferencesの取得
        sp = getSharedPreferences("sort", Context.MODE_PRIVATE);

		// エリア
//		ListPreference areaPref = (ListPreference)getPreferenceScreen().findPreference("AREA");
		ListPreference areaPref = (ListPreference)findPreference("AREA");
		if (null == areaPref.getValue()) {
			areaPref.setDefaultValue("area_tdl");
			areaPref.setSummary("ディズニーランド");
		}
		else {
			areaPref.setSummary(areaPref.getEntry());
		}

		// 待ち時間
//		ListPreference waitPref = (ListPreference)getPreferenceScreen().findPreference("WAIT");
		ListPreference waitPref = (ListPreference)findPreference("WAIT");
		if (null == waitPref.getValue()) {
			waitPref.setDefaultValue("wait_not");
			waitPref.setSummary("指定なし");
		}
		else {
			waitPref.setSummary(waitPref.getEntry());
		}
		
		// 条件で並べ替え
//		ListPreference sortPref = (ListPreference)getPreferenceScreen().findPreference("SORT");
		ListPreference sortPref = (ListPreference)findPreference("SORT");
		if (null == sortPref.getValue()) {
			sortPref.setDefaultValue("sort_not");			
			sortPref.setSummary("指定なし");
		}
		else {
			sortPref.setSummary(sortPref.getEntry());
		}

        // 各アイテムへの初期値設定
//        initSortMenu();
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
        
    /*
     * 「キャンセル」ボタンが押された時
     */
    public void pushCancel(View v){
    	finish();
    }

    /*
     * 「ＯＫ」ボタンが押された時
     */
    public void pushOK(View v){

    	// TODO:ラジオボタン、スピナー取得をなくす。
    	
    	// 書き込み用ShareadPreferences.Editorオブジェクトを取得
    	SharedPreferences.Editor editor = sp.edit();

    	// 各アイテムごと、初期値でない場合は選択値を保存する
    	
    	// エリア
    	RadioButton radioAreaTdl = (RadioButton)findViewById(R.id.radioTdl);
//    	RadioButton radioAreaTds = (RadioButton)findViewById(R.id.radioTds);

    	if (radioAreaTdl.isChecked()) {
        	Log.d("area", "ディズニーランド指定");
        	editor.putString("AREA", "TDL");
    	}
    	else {
        	Log.d("area","ディズニーシー指定");    	
        	editor.putString("AREA", "TDS");
    	}
    	
    	// Myアトラクション絞り込み
    	RadioButton radioAtrcOff = (RadioButton)findViewById(R.id.radioOff);
//    	RadioButton radioAtrcOn = (RadioButton)findViewById(R.id.radioOn);

    	if (radioAtrcOff.isChecked()) {
        	Log.d("atrc", "Myアトラクション指定なし");    	
        	editor.putString("ATRC", "OFF");
    	}
    	else {
        	Log.d("atrc", "Myアトラクション指定あり");    	
        	editor.putString("ATRC", "ON");
    	}

    	// 時間指定
    	Spinner spinWait = (Spinner)findViewById(R.id.spinnerWait);
    	Log.d("spinWait", String.valueOf(spinWait.getSelectedItemPosition()));
    	editor.putInt("WAIT", spinWait.getSelectedItemPosition());
    	
    	// ソート条件
    	Spinner spinSort = (Spinner)findViewById(R.id.spinnerSort);
    	Log.d("spinSort", String.valueOf(spinSort.getSelectedItemPosition()));
    	editor.putInt("SORT", spinSort.getSelectedItemPosition());
    	
    	// 書き込みを終了
    	editor.commit();

    	setResult(RESULT_OK);
    	
    	finish();
    }
}
