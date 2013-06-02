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
	
	// �A�g���N�V������
	String[] atrc_name_list = new String[] { 
        "�f�B�Y�j�[�V�[�E�g�����W�b�g�X�`�[�}�[���C��",
        "�t�H�[�g���X�E�G�N�X�v�����[�V�����g�U�E���I�i���h�`�������W�h",
        "�C���f�B�E�W���[���Y�iR�j�E�A�h�x���`���[�F�N���X�^���X�J���̖��{",
        "�V���h�o�b�h�E�X�g�[���[�u�b�N�E���H���b�W",
        "�C���f�B�E�W���[���Y�iR�j�E�A�h�x���`���[�F�N���X�^���X�J���̖��{���{���{���{���{���{���{",        
        "�C���f�B�E�W���[���Y�iR�j�E�A�h�x���`���[�F�N���X�^���X�J���̖��{���{���{���{���{���{���{���{",        
        "�C���f�B�E�W���[���Y�iR�j�E�A�h�x���`���[�F�N���X�^���X�J���̖��{���{���{���{���{���{���{���{���{",        
        "�C���f�B�E�W���[���Y�iR�j�E�A�h�x���`���[�F�N���X�^���X�J���̖��{���{���{���{���{���{���{���{���{���{",        
        "�C���f�B�E�W���[���Y�iR�j�E�A�h�x���`���[�F�N���X�^���X�J���̖��{���{���{���{���{���{���{���{���{���{���{",        
        "�C���f�B�E�W���[���Y�iR�j�E�A�h�x���`���[�F�N���X�^���X�J���̖��{���{���{���{���{���{���{���{���{���{���{���{",        
    }; 
	
    // �҂�����
	String[] wait_list = new String[] { 
        "10", "20", "30", "40", "50", 
        "60", "70", "80", "90", "100" 
    };
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // �J�X�^���^�C�g�����g�p����
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        // �A�N�e�B�r�e�B���Z�b�g
        setContentView(R.layout.activity_main);        

        // �J�X�^���^�C�g�����Z�b�g
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
        
        // �f�[�^���i�[���邽�߂�ArrayList��錾
        ArrayList<HashMap<String, String>> data
			= new ArrayList<HashMap<String, String>>();

		// 10�������J��Ԃ�
        for(int i = 0; i< 10; i++){
        	HashMap<String, String> map
				= new HashMap<String, String>();
				
			// ���R�[�h��map�ɑ��
        	map.put("atrc_name", atrc_name_list[i]);
        	
        	// ������map�ɑ��
        	map.put("wait", wait_list[i]);
        	
        	// �쐬����map��data�ɒǉ�
        	data.add(map);
         }
        /*
		 * �쐬����data�ƃJ�X�^�}�C�Y�������C�A�E�grow.xml��
         * �R�t����SimpleAdapter���쐬����
         */
        SimpleAdapter sa
			= new SimpleAdapter(this, data, R.layout.row, 
                new String[]{"atrc_name", "wait"},
                new int[]{R.id.atrc_name, R.id.wait}
        );
        // main.xml��ListView��sa���Z�b�g���܂��B
        ListView lv = (ListView)findViewById(R.id.listview);
        lv.setAdapter(sa);
    }
    
    /*
     * �u�X�V�v�{�^���������ꂽ��
     */
    public void reload(View v){
    	Toast.makeText(this, "�X�V��������܂����B", Toast.LENGTH_LONG).show();
    }
    
    /*
     * �u�ؑցv�{�^���������ꂽ��
     */
    public void change(View v){
    	Toast.makeText(this, "�ؑւ�������܂����B", Toast.LENGTH_LONG).show();
    	
    	// �C���e���g�̃C���X�^���X����
    	Intent intent = new Intent(main.this, sortMenu.class);
    	// ����ʂ̃A�N�e�B�r�e�B�N��
    	startActivity( intent );
    }

}