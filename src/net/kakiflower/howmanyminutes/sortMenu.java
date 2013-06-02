package net.kakiflower.howmanyminutes;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class sortMenu extends Activity {

	public SharedPreferences sp;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sort_menu);
        
        // SharedPreferencesの取得
        sp = getSharedPreferences("sort", Context.MODE_PRIVATE);
        
        // 各アイテムへの初期値設定
        initSortMenu();
    }
    
    /*
     * 絞り込みメニューの初期化
     */
    private void initSortMenu() {
    	
        // エリア
        String area = sp.getString("AREA", "TDS");
        if ("TDS".equals(area)) {

        	// ディズニーシーを選択状態にする
        	RadioButton radioArea = (RadioButton)findViewById(R.id.radioTds);
        	radioArea.setChecked(true);
        }
        
        // Myアトラクション
        String atrc = sp.getString("ATRC", "OFF");
        if ("ON".equals(atrc)) {

        	// Myアトラクションのみ表示「ON」に設定
        	RadioButton radioAtrc = (RadioButton)findViewById(R.id.radioOn);
        	radioAtrc.setChecked(true);
        }
        
        // 待ち時間の設定値を反映
        int wait_index = sp.getInt("WAIT", 0);
        if (wait_index != 0) {
        	Spinner spnrWait = (Spinner)findViewById(R.id.spinnerWait);
        	spnrWait.setSelection(wait_index);
        }

        // 並び替えの設定値を反映
        int sort_index = sp.getInt("SORT", 0);
        if (sort_index != 0) {
        	Spinner spnrSort = (Spinner)findViewById(R.id.spinnerSort);
        	spnrSort.setSelection(sort_index);
        }
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
    	
    	finish();
    }
}
