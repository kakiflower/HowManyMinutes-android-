package net.kakiflower.howmanyminutes;

/*
 * �A�g���N�V�����҂����ԏ��N���X
 */
public class atrcData {

	private String area_name;		// �p�[�N�G���A��
	private String atrc_name;		// �A�g���N�V������
	private String fp;				// �t�@�X�g�p�X������̗��p�\����
	private String run;				// �^�c��
	private String update;			// �X�V���� XX:XX
	private String wait;			// �҂����� XX:XX
	private String bookmark;		// My�A�g���N�V�����t���O ON:�w�肠�� , OFF:�w��Ȃ�
	
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
