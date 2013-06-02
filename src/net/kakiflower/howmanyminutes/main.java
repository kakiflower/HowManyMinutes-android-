package net.kakiflower.howmanyminutes;

import java.util.ArrayList;
import java.util.HashMap;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class main extends Activity {
	
	// アトラクション名
	String[] atrc_name_list = new String[] { 
        "ディズニーシー・トランジットスチーマーライン",
        "フォートレス・エクスプロレーション“ザ・レオナルドチャレンジ”",
        "インディ・ジョーンズ（R）・アドベンチャー：クリスタルスカルの魔宮",
        "シンドバッド・ストーリーブック・ヴォヤッジ",
        "インディ・ジョーンズ（R）・アドベンチャー：クリスタルスカルの魔宮魔宮魔宮魔宮魔宮魔宮魔宮",        
        "インディ・ジョーンズ（R）・アドベンチャー：クリスタルスカルの魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮",        
        "インディ・ジョーンズ（R）・アドベンチャー：クリスタルスカルの魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮",        
        "インディ・ジョーンズ（R）・アドベンチャー：クリスタルスカルの魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮",        
        "インディ・ジョーンズ（R）・アドベンチャー：クリスタルスカルの魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮",        
        "インディ・ジョーンズ（R）・アドベンチャー：クリスタルスカルの魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮魔宮",        
    }; 
	
    // 待ち時間
	String[] wait_list = new String[] { 
        "10", "20", "30", "40", "50", 
        "60", "70", "80", "90", "100" 
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
    
    /*
     * 「更新」ボタンが押された時
     */
    public void reload(View v){
    	Toast.makeText(this, "更新が押されました。", Toast.LENGTH_LONG).show();
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

}