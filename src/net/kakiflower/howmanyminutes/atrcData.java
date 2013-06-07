package net.kakiflower.howmanyminutes;

import java.util.Comparator;

/*
 * アトラクション待ち時間情報クラス
 */
public class atrcData {

	private String area_name;		// パークエリア名
	private String atrc_name;		// アトラクション名
	private String fp;				// ファストパス発券後の利用可能時間
	private String run;				// 運営状況
	private String update;			// 更新時間 XX:XX
	private String wait;			// 待ち時間 XX:XX
	private String bookmark;		// Myアトラクションフラグ ON:指定あり , OFF:指定なし
	
	public String getArea_name() {
		return area_name;
	}

	public void setArea_name(String area_name) {
		this.area_name = area_name;
	}

	public String getAtrc_name() {
		return atrc_name;
	}
	
	public void setAtrc_name(String atrc_name) {
		this.atrc_name = atrc_name;
	}
	
	public String getFp() {
		return fp;
	}
	
	public void setFp(String fp) {
		this.fp = fp;
	}
	
	public String getRun() {
		return run;
	}
	
	public void setRun(String run) {
		this.run = run;
	}
	
	public String getUpdate() {
		return update;
	}
	
	public void setUpdate(String update) {
		this.update = update;
	}
	
	public String getWait() {
		return wait;
	}
	
	public void setWait(String wait) {
		this.wait = wait;
	}
	
	public String getBookmark() {
		return bookmark;
	}

	public void setBookmark(String bookmark) {
		this.bookmark = bookmark;
	}

}
