/*******************************************************************************
 *
 * Copyright (C) 2008 JST-BIRD MassBank
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
 * �A�g�T�[�o�̏�Ԃ��Ǘ�����N���X
 *
 * ver 1.0.2 2012.11.01
 *
 ******************************************************************************/
package massbank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.*;
import java.text.DecimalFormat;
import massbank.GetConfig;
import massbank.ServerStatusInfo;
import massbank.svn.SVNUtils;

public class ServerStatus {
	private Properties proper = new Properties();
	//�Ǘ��t�@�C���̃p�X
	private String filePath = "";
	// �|�[�����O����
	private int pollInterval = 0;
	// �Ď��ΏۃT�[�o��
	private int serverNum = 0;
	// �T�[�o�E�X�e�[�^�X���
	private ServerStatusInfo[] statusList = null;
	// �Ǘ��t�@�C������
	private static final String PROF_FILE_NAME = "ServerStatus.inf";
	// �v���p�e�B�̃L�[��
	private static final String SERVER_KEY_NAME = "server";
	private static final String MANAGED_KEY_NAME = "status";

	private DecimalFormat decFormat = new DecimalFormat("00");

	/**
	 * �f�t�H���g�R���X�g���N�^
	 */
	public ServerStatus() {
		// �Ǘ��t�@�C���̃p�X���Z�b�g
		this.filePath = MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPPSERV_PATH) + PROF_FILE_NAME;
		setBaseInfo();
	}
	
	/**
	 * �R���X�g���N�^
	 * @param baseUrl �x�[�XURL
	 * @deprecated �񐄏��R���X�g���N�^
	 * @see ServerStatus#ServerStatus()
	 */
	public ServerStatus(String baseUrl) {
//		int pos1 = baseUrl.indexOf( "/", (new String("http://")).length() );
//		int pos2 = baseUrl.lastIndexOf( "/" );
//		String subDir = "";
//		if ( pos2 > pos1 ) {
//			subDir = baseUrl.substring( pos1 + 1, pos2 );
//		}
//		String path = System.getProperty("catalina.home") + "/webapps/ROOT/";
//		this.filePath = path + subDir + "/pserver/" + PROF_FILE_NAME;
//
//		this.baseUrl = baseUrl;
//		setBaseInfo();
		// �Ǘ��t�@�C���̃p�X�Z�b�g�̓f�t�H���g�R���X�g���N�^�ōs�����Ƃɂ���
		this();
	}

	/**
	 * �x�[�X�����Z�b�g
	 */
	public void setBaseInfo() {
		// �ݒ�t�@�C���Ǎ���
		GetConfig conf = new GetConfig(MassBankEnv.get(MassBankEnv.KEY_BASE_URL));
		// URL���X�g���擾
		String[] urls = conf.getSiteUrl();
		// DB�����X�g���擾
		String[] dbNames = conf.getDbName();
		// �Z�J���_��DB�����X�g���擾
		String[] db2Names = conf.getSecondaryDBName();
		// �T�[�o�����X�g���擾
		String[] svrNames = conf.getSiteName();
		// �t�����g�T�[�oURL���擾
		String serverUrl = conf.getServerUrl();
		// �|�[�����O�������擾
		this.pollInterval = conf.getPollInterval();

		// �Ď��ΏۃT�[�o��URL��DB�����i�[
		List<String> svrNameList = new ArrayList();
		List<String> urlList = new ArrayList();
		List<String> dbNameList = new ArrayList();
		List<String> db2NameList = new ArrayList();
		for ( int i = 0; i < urls.length; i++ ) {
			// �~�h���T�[�o�܂��́A�t�����g�T�[�o�Ɠ���URL�̏ꍇ�͑ΏۊO
			if ( i != GetConfig.MYSVR_INFO_NUM && !urls[i].equals(serverUrl) ) {
				svrNameList.add(svrNames[i]);
				urlList.add(urls[i]);
				dbNameList.add(dbNames[i]);
				db2NameList.add(db2Names[i]);
			}
		}

		// ��ԊǗ����X�g���Z�b�g
		this.serverNum = urlList.size();
		if ( this.serverNum > 0 ) {
			this.statusList = new ServerStatusInfo[this.serverNum];
			for ( int i = 0; i < svrNameList.size(); i++ ) {
				String svrName = svrNameList.get(i);	// �T�[�o��
				String url = urlList.get(i);			// URL
				String dbName = dbNameList.get(i);		// DB��
				String db2Name = db2NameList.get(i);	// �Z�J���_��DB��
				// �X�e�[�^�X�͖��Z�b�g
				this.statusList[i] = new ServerStatusInfo( svrName, url, dbName, db2Name );
			}
		}
	}

	/**
	 * �Ǘ��t�@�C���𐮍�����
	 */
	public void clean() {
		setBaseInfo();

		// �Ǘ��t�@�C����Ǎ��݁A�T�[�o�̏�Ԃ��擾����
		boolean[] isActiveList = getStatusList();
		if ( isActiveList == null ) {
			return;
		}
		int listNum = isActiveList.length;

		// �X�e�[�^�X�Z�b�g
		for ( int i = 0; i < listNum; i++ ) {
			setStatus( i, isActiveList[i] );
		}
		// �Ď��Ώۂ̃T�[�o���������ꍇ�A�]���ȏ����폜����
		if ( this.serverNum < listNum ) {
			for ( int i = this.serverNum; i < listNum; i++ ) {
				deleteStatus(i);
			}
		}

		// �Ǘ��t�@�C���ɕۑ�����
		store();
	}

	/**
	 * �Ď��ΏۃT�[�o�����擾����
	 * @return �Ď��ΏۃT�[�o��
	 */
	public int getServerNum() {
		return this.serverNum;
	}

	/**
	 * �|�[�����O�������擾����
	 * @return �|�[�����O����
	 */
	public int getPollInterval() {
		return this.pollInterval;
	}

	/**
	 * �T�[�o�E�X�e�[�^�X�����擾����
	 * @return �X�e�[�^�X���
	 */
	public ServerStatusInfo[] getStatusInfo() {
		// ��Ď��̏ꍇ�́Anull��Ԃ�
		if ( !isManaged() ) {
			return null;
		}

		// �Ǘ��t�@�C����Ǎ��݁A�T�[�o�̏�Ԃ��擾����
		boolean[] isActiveList = getStatusList();
		if ( isActiveList != null ) {
			int num = isActiveList.length;
			if ( isActiveList.length > statusList.length ) {
				num = statusList.length;
			}
			for ( int i = 0; i < num; i++ ) {
				this.statusList[i].setStatus(isActiveList[i]);
			}
		}
		return this.statusList;
	}

	/**
	 * �T�[�o�̏�Ԃ��Z�b�g����
	 * @param index ���X�g�̃C���f�b�N�X
	 * @param isActive ��� -- true:active / false:inactive
	 */
	public void setStatus(int index, boolean isActive) {
		String status = "";
		if ( isActive ) {
			status = "active";
		}
		else {
			status = "inactive";
		}
		String key = SERVER_KEY_NAME + decFormat.format(index);
		String val = status;
		try {
			synchronized (this.proper) {
				proper.setProperty( key, val );
			}
		}
		catch (Exception e) {
			Logger.global.severe( e.toString() );
		}
	}

	/**
	 * �T�[�o��Ԃ��폜����
	 * @param index ���X�g�̃C���f�b�N�X
	 */
	public void deleteStatus(int index) {
		
		String key = SERVER_KEY_NAME + decFormat.format(index);
		try {
			synchronized (this.proper) {
				proper.remove(key);
			}
		}
		catch (Exception e) {
			Logger.global.severe( e.toString() );
		}
	}

	/**
	  * �Ǘ��t�@�C���ɕۑ�����
	  */
	public void store() {
		FileOutputStream stream = null;
		try {
			synchronized (this.proper) {
				stream = new FileOutputStream(filePath);
				proper.store( stream, null );
				stream.close();
			}
		}
		catch (Exception e) {
			Logger.global.severe( e.toString() );
		}
	}

	/**
	 * �Ď���Ԃ��Z�b�g����
	 * @param isManaged true:�Ď��� / false:��Ď�
	 */
	public void setManaged(boolean isManaged) {
		// �Ǘ��t�@�C���Ǎ���
		read();

		// ��ԃZ�b�g
		String status = "";
		if ( isManaged ) {
			status = "managed";
		}
		else {
			status = "unmanaged";
		}
		try {
			synchronized (this.proper) {
				proper.setProperty( MANAGED_KEY_NAME, status );
			}
		}
		catch (Exception e) {
			Logger.global.severe( e.toString() );
		}
		store();
	}

	/**
	 * �Ď���Ԃ��ۂ�
	 * @return true:�Ď��� / false:��Ď�
	 */
	public boolean isManaged() {
		boolean isManaged = false;
		if ( read() ) {
			String status = proper.getProperty( MANAGED_KEY_NAME, "" );
			if ( status.equals("managed") ) {
				isManaged = true;
			}
		}
		return isManaged;
	}

	/**
	  * �Ǘ��t�@�C����ǂݍ���
	  */
	private boolean read() {
		File f = new File(this.filePath);
		if ( !f.exists() ) {
			return false;
		}
		try {
			synchronized (this.proper) {
				FileInputStream stream = new FileInputStream(filePath);
				proper.load(stream);
				stream.close();
			}
		}
		catch (Exception e) {
			Logger.global.severe( e.toString() );
		}
		return true;
	}

	/**
	 * �Ǘ��t�@�C����Ǎ��݁A�T�[�o�̏�Ԃ��擾����
	 * @return �T�[�o��ԃ��X�g -- true:active / false:inactive / null:�ΏۃT�[�o�Ȃ�
	 */
	private boolean[] getStatusList() {
		// �z��isActiveList��������
		boolean[] isActiveList = null;
		if ( this.serverNum > 0 ) {
			isActiveList = new boolean[this.serverNum];
			for ( int i = 0; i < this.serverNum; i++ ) {
				// true =�uActive�v���Z�b�g
				isActiveList[i] = true;
			}
			// �Ǘ��t�@�C���Ǎ���
			if ( !read() ) {
				// �t�@�C���Ȃ�
				return isActiveList;
			}
		}

		// �v���p�e�B���X�g�擾
		Map list = new TreeMap();
		Enumeration names = proper.propertyNames();
		while ( names.hasMoreElements() ) {
			String key = (String)names.nextElement();
			if ( key.indexOf(SERVER_KEY_NAME) >= 0 ) {
				String val = proper.getProperty(key);
				list.put( key, val );
			}
		}

		// �Ǘ��t�@�C������ǂݎ�������e���Z�b�g
		/* - serverNum �Ď��Ώۂ̃T�[�o��       */
		/* - list.size �Ǘ��t�@�C����̃T�[�o�� */
		int num = list.size();
		if ( num > 0 ) {
			isActiveList = new boolean[num];
			Iterator it = list.keySet().iterator();
			for ( int i = 0; i < num; i++ ) {
				if ( !it.hasNext() ) {
					break;
				}
				Object okey = it.next();
				String status = (String)list.get(okey);
				if ( status.equals("active") ) {
					isActiveList[i] = true;
				}
			}
		}
		return isActiveList;
	}

	/**
	 * �T�[�o�̏�Ԃ��擾����
	 * @return �T�[�o��ԃ��X�g -- true:active / false:inactive
	 */
	public boolean isServerActive(String url, String dbName) {
		ServerStatusInfo[] info = getStatusInfo();
		if ( info == null ) {
			return true;
		}
		for ( int i = 0; i < info.length; i++ ) {
			if ( url.equals( info[i].getUrl() ) && dbName.equals( info[i].getDbName() ) ) {
				if ( !info[i].getStatus() ) {
					return false;
				}
				break;
			}
		}
		return true;
	}

	/**
	 * 
	 */
	public String get2ndDbName(String url, String dbName) {
		ServerStatusInfo[] info = getStatusInfo();
		if ( info == null ) {
			return "";
		}
		for ( int i = 0; i < info.length; i++ ) {
			if ( url.equals( info[i].getUrl() ) && dbName.equals( info[i].getDbName() ) ) {
				return info[i].get2ndDbName();
			}
		}
		return "";
	}
}
