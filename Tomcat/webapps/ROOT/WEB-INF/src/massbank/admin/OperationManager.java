/*******************************************************************************
 *
 * Copyright (C) 2009 JST-BIRD MassBank
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 *******************************************************************************
 *
 * ���[�U����Ǘ��N���X
 *
 * ver 1.0.0 2009.06.16
 *
 ******************************************************************************/
package massbank.admin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ���[�U����̊Ǘ����s��Singleton�N���X
 * �y�[�W�ɑ΂��鑀����s���郆�[�U�̐������s��
 */
public class OperationManager {

    /** �Ǘ��Ώۃy�[�W�iInstrument Manager�j */
    public final String P_INSTRUMENT = "Instrument";
    
    /** �Ǘ��Ώۃy�[�W�iRecord List�j */
    public final String P_RECORD = "Record";
    
    /** �Ǘ��Ώۃy�[�W�iStructure List�j */
    public final String P_STRUCTURE = "Structure";
    
    /** �����ʁi�X�V�n�j */
    public final String TP_UPDATE = "Update";
    
    /** �����ʁi�\���n�j */
    public final String TP_VIEW = "View";
    
    /** �B��̃C���X�^���X */
    private static final OperationManager instance = new OperationManager();
    
    /** ����Ǘ��p�R���N�V���� */
    private Set<String> oSet = Collections.synchronizedSet(new HashSet<String>());
    
    /**
     * �R���X�g���N�^
     */
    private OperationManager() {
    }

    /**
     * ���̃N���X�̗B��̃C���X�^���X��Ԃ�
     */
    public static OperationManager getInstance() {
        return instance;
    }
    
    /**
     * ����J�n
     * �Ώۃy�[�W�̑Ώ�DB�ɑ΂���X�V�n�����1���[�U�݂̂ɐ�������
     * ���̊֐����Ăяo�����ꍇ�͑���I����ɕK��endOparation�֐����Ăяo������
     * @param page �Ώۃy�[�W
     * @param type ������
     * @param db �Ώ�DB��
     * @return ����J�n����
     */
    public boolean startOparation(String page, String type, String db) {
    	boolean isOperation = false;
    	String key = page + type + db;
    	
    	// ���u���֘A�y�[�W�̃��[�U��������
    	if ( page.equals(P_INSTRUMENT) ) {
     		if ( !oSet.contains(P_INSTRUMENT + TP_UPDATE + db) &&
    				!oSet.contains(P_RECORD + TP_UPDATE + db) ) {
    			isOperation = true;
    		}
     		if ( isOperation && !type.equals(TP_VIEW)) {
        		// ��������������̏ꍇ�̓L�[��ێ�
     			oSet.add(key);
    		}    		
    	}
    	// ���R�[�h�֘A�y�[�W�̃��[�U��������
    	else if ( page.equals(P_RECORD) ) {
     		if ( !oSet.contains(P_RECORD + TP_UPDATE + db) &&
    				!oSet.contains(P_INSTRUMENT + TP_UPDATE + db) ) {
    			isOperation = true;
    		}
     		if ( isOperation && !type.equals(TP_VIEW) ) {
        		// ��������������̏ꍇ�̓L�[��ێ�
     			oSet.add(key);
    		}
    	}
    	// �\�����֘A�y�[�W�̃��[�U��������
    	else if ( page.equals(P_STRUCTURE) ) {
    		if ( !oSet.contains(P_STRUCTURE + TP_UPDATE + db) ) {
    			isOperation = true;
    		}
    		if ( isOperation && !type.equals(TP_VIEW) ) {
        		// ��������������̏ꍇ�̓L�[��ێ�
    			oSet.add(key);
    		}
    	}
    	
    	return isOperation;
    }
    
    /**
     * ����I��
     * �Ώۃy�[�W�̑Ώ�DB�ɑ΂��鑀��̐�������������
     * @param page �Ώۃy�[�W
     * @param type ������
     * @param db �Ώ�DB��
     * @return ����I������
     */
    public boolean endOparation(String page, String type, String db) {
    	String key = page + type + db;
    	
    	return oSet.remove(key);
    }
}
