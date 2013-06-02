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
        
        // SharedPreferences�̎擾
        sp = getSharedPreferences("sort", Context.MODE_PRIVATE);
        
        // �e�A�C�e���ւ̏����l�ݒ�
        initSortMenu();
    }
    
    /*
     * �i�荞�݃��j���[�̏�����
     */
    private void initSortMenu() {
    	
        // �G���A
        String area = sp.getString("AREA", "TDS");
        if ("TDS".equals(area)) {

        	// �f�B�Y�j�[�V�[��I����Ԃɂ���
        	RadioButton radioArea = (RadioButton)findViewById(R.id.radioTds);
        	radioArea.setChecked(true);
        }
        
        // My�A�g���N�V����
        String atrc = sp.getString("ATRC", "OFF");
        if ("ON".equals(atrc)) {

        	// My�A�g���N�V�����̂ݕ\���uON�v�ɐݒ�
        	RadioButton radioAtrc = (RadioButton)findViewById(R.id.radioOn);
        	radioAtrc.setChecked(true);
        }
        
        // �҂����Ԃ̐ݒ�l�𔽉f
        int wait_index = sp.getInt("WAIT", 0);
        if (wait_index != 0) {
        	Spinner spnrWait = (Spinner)findViewById(R.id.spinnerWait);
        	spnrWait.setSelection(wait_index);
        }

        // ���ёւ��̐ݒ�l�𔽉f
        int sort_index = sp.getInt("SORT", 0);
        if (sort_index != 0) {
        	Spinner spnrSort = (Spinner)findViewById(R.id.spinnerSort);
        	spnrSort.setSelection(sort_index);
        }
    }
    
    /*
     * �u�L�����Z���v�{�^���������ꂽ��
     */
    public void pushCancel(View v){
    	finish();
    }

    /*
     * �u�n�j�v�{�^���������ꂽ��
     */
    public void pushOK(View v){

    	// �������ݗpShareadPreferences.Editor�I�u�W�F�N�g���擾
    	SharedPreferences.Editor editor = sp.edit();

    	// �e�A�C�e�����ƁA�����l�łȂ��ꍇ�͑I��l��ۑ�����
    	
    	// �G���A
    	RadioButton radioAreaTdl = (RadioButton)findViewById(R.id.radioTdl);
//    	RadioButton radioAreaTds = (RadioButton)findViewById(R.id.radioTds);

    	if (radioAreaTdl.isChecked()) {
        	Log.d("area", "�f�B�Y�j�[�����h�w��");
        	editor.putString("AREA", "TDL");
    	}
    	else {
        	Log.d("area","�f�B�Y�j�[�V�[�w��");    	
        	editor.putString("AREA", "TDS");
    	}
    	
    	// My�A�g���N�V�����i�荞��
    	RadioButton radioAtrcOff = (RadioButton)findViewById(R.id.radioOff);
//    	RadioButton radioAtrcOn = (RadioButton)findViewById(R.id.radioOn);

    	if (radioAtrcOff.isChecked()) {
        	Log.d("atrc", "My�A�g���N�V�����w��Ȃ�");    	
        	editor.putString("ATRC", "OFF");
    	}
    	else {
        	Log.d("atrc", "My�A�g���N�V�����w�肠��");    	
        	editor.putString("ATRC", "ON");
    	}

    	// ���Ԏw��
    	Spinner spinWait = (Spinner)findViewById(R.id.spinnerWait);
    	Log.d("spinWait", String.valueOf(spinWait.getSelectedItemPosition()));
    	editor.putInt("WAIT", spinWait.getSelectedItemPosition());
    	
    	// �\�[�g����
    	Spinner spinSort = (Spinner)findViewById(R.id.spinnerSort);
    	Log.d("spinSort", String.valueOf(spinSort.getSelectedItemPosition()));
    	editor.putInt("SORT", spinSort.getSelectedItemPosition());
    	
    	// �������݂��I��
    	editor.commit();
    	
    	finish();
    }
}
