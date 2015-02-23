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
 * ver 1.0.1 2010.02.05
 *
 ******************************************************************************/
package massbank.admin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * ���[�U����̊Ǘ����s��Singleton�N���X
 * �y�[�W�ɑ΂��鑀����s���郆�[�U�̐������s��
 */
public class OperationManager {

    /** �Ǘ��Ώۃy�[�W�iDatabase Manager�j */
    public final String P_MANAGER = "Manager";
    
    /** �Ǘ��Ώۃy�[�W�iInstrument Editor�j */
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
    	String key = page + type;
    	if ( db != null ) {
    		key += db;
    	}
    	
    	// �f�[�^�x�[�X�Ǘ��֘A�y�[�W�̃��[�U��������
    	//  ->�f�[�^�x�[�X�̍X�V�Ɋւ��鏈���i�f�[�^�x�[�X�Ǘ��A���u���A���R�[�h�A�\�����j��
    	//    1�ł��s���Ă���ꍇ�͑���s��
    	if ( page.equals(P_MANAGER) ) {
    		isOperation = true;
    		Iterator<String> i = oSet.iterator();
    		while ( i.hasNext() ) { 
                String useKey = (String)i.next();
                if ( useKey.equals(P_MANAGER + TP_UPDATE) ||
                		useKey.indexOf(P_INSTRUMENT + TP_UPDATE) != -1 ||
                		useKey.indexOf(P_RECORD + TP_UPDATE) != -1 ||
                		useKey.indexOf(P_STRUCTURE + TP_UPDATE) != -1 ) {
                	
                	isOperation = false;
                	break;
                }
    		}
     		if ( isOperation && !type.equals(TP_VIEW)) {
        		// ��������������̏ꍇ�̓L�[��ێ�
     			oSet.add(key);
    		}    		
    	}
    	// ���u���֘A�y�[�W�̃��[�U��������
    	//  ->�f�[�^�x�[�X�Ǘ��A�Y��DB�̑��u���A�Y��DB�̃��R�[�h��
    	//    �������s���Ă���ꍇ�͑���s��
		else if ( page.equals(P_INSTRUMENT) ) {
     		if ( !oSet.contains(P_MANAGER + TP_UPDATE) &&
     				!oSet.contains(P_INSTRUMENT + TP_UPDATE + db) &&
    				!oSet.contains(P_RECORD + TP_UPDATE + db) ) {
    			isOperation = true;
    		}
     		if ( isOperation && !type.equals(TP_VIEW)) {
        		// ��������������̏ꍇ�̓L�[��ێ�
     			oSet.add(key);
    		}
    	}
    	// ���R�[�h�֘A�y�[�W�̃��[�U��������
    	//  ->�f�[�^�x�[�X�Ǘ��A�Y��DB�̑��u���A�Y��DB�̃��R�[�h��
    	//    �������s���Ă���ꍇ�͑���s��
    	else if ( page.equals(P_RECORD) ) {
     		if ( !oSet.contains(P_MANAGER + TP_UPDATE) &&
    				!oSet.contains(P_INSTRUMENT + TP_UPDATE + db) &&
    				!oSet.contains(P_RECORD + TP_UPDATE + db) ) {
    			isOperation = true;
    		}
     		if ( isOperation && !type.equals(TP_VIEW) ) {
        		// ��������������̏ꍇ�̓L�[��ێ�
     			oSet.add(key);
    		}
    	}
    	// �\�����֘A�y�[�W�̃��[�U��������
    	//  ->�f�[�^�x�[�X�Ǘ��A�Y��DB�̍\�����̏������s���Ă���ꍇ�͑���s��
    	else if ( page.equals(P_STRUCTURE) ) {
    		if ( !oSet.contains(P_MANAGER + TP_UPDATE) &&
    				!oSet.contains(P_STRUCTURE + TP_UPDATE + db) ) {
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
    	String key = page + type;
    	if ( db != null ) {
    		key += db;
    	}

    	return oSet.remove(key);
    }
}
