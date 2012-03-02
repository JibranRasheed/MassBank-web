/*******************************************************************************
 *
 * Copyright (C) 2012 JST-BIRD MassBank
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
 * �t�@�C�����샆�[�e�B���e�B�N���X
 *
 * ver 1.0.5 2012.02.20
 *
 ******************************************************************************/
package massbank.admin;

import java.io.*;
import java.net.URL;
import java.util.logging.*;
import massbank.admin.CmdExecute;
import massbank.admin.CmdResult;

public class FileUtil {
	
	/** OS�� */
	private static String OS_NAME = System.getProperty("os.name");

	/**
	 * ZIP�`���̈��k�t�@�C�����쐬����
	 * @param zipFilePath ZIP�t�@�C���̃p�X
	 * @param filePath ���k���t�@�C��
	 * @return true:���� / false:���s
	 */
	public static boolean makeZip(String zipFilePath, String filePath) {
		String[] cmd = new String[]{ "zip", "-oqj", zipFilePath, filePath };
		return command( cmd, true );
	}
	
	/**
	 * �A�[�J�C�u���𓀂���iZIP�`���j
	 * @param archivePath �A�[�J�C�u�̃p�X
	 * @param destPath �𓀐�̃p�X
	 * @return true:���� / false:���s
	 */
	public static boolean unZip(String archivePath, String destPath) {
		String[] cmd = new String[]{ "unzip", "-oq", archivePath, "-d", destPath };
		return command( cmd, true );
	}
	
	/**
	 * �A�[�J�C�u���𓀂���
	 * @param archivePath �A�[�J�C�u�̃p�X
	 * @param destPath �𓀐�̃p�X
	 * @return true:���� / false:���s
	 */
	public static boolean uncompress(String archivePath, String destPath) {
		// �h���C�u��������ꍇ�͎�菜��
		int pos = archivePath.indexOf(":");
		if ( pos >= 0 ) {
			archivePath = archivePath.substring(pos + 1);
		}
		String cmd[] = new String[]{ "tar", "xfz", archivePath, "-C", destPath };
		return command( cmd, false );
	}

	/**
	 * �t�@�C�����R�s�[����
	 * @deprecated Windows�Ŏg�p�����ꍇ�̓R�s�[��t�@�C�����s�K�؂ȏ��L�҂ɂȂ�iOS�ˑ��j
	 * @param srcPath �R�s�[���t�@�C���̃p�X
	 * @param destPath �R�s�[��t�@�C���̃p�X
	 * @return true:���� / false:���s
	 */
	public static boolean copyFile(String srcPath, String destPath) {
		String[] cmd = new String[]{ "cp", "-pf", srcPath, destPath };
		return command( cmd, false );
	}

	/**
	 * �f�B���N�g�����R�s�[����
	 * @deprecated Windows�Ŏg�p�����ꍇ�̓R�s�[��f�B���N�g�����s�K�؂ȏ��L�҂ɂȂ�iOS�ˑ��j
	 * @param srcPath �R�s�[���f�B���N�g���̃p�X
	 * @param destPath �R�s�[��f�B���N�g���̃p�X
	 * @return true:���� / false:���s
	 */
	public static boolean copyDir(String srcPath, String destPath) {
		String[] cmd = new String[]{ "cp", "-pfr", srcPath, destPath };
		return command( cmd, false );
	}
	
	/**
	 * �t�@�C�����폜����
	 * @param filePath �폜����t�@�C���̃p�X
	 * @return true:���� / false:���s
	 */
	public static boolean removeFile(String filePath) {
		String[] cmd = new String[]{ "rm", "-f", filePath };
		return command( cmd, false );
	}

	/**
	 * �f�B���N�g�����폜����
	 * @param dirPath �폜����f�B���N�g���̃p�X
	 * @return true:���� / false:���s
	 */
	public static boolean removeDir(String dirPath) {
		String[] cmd = new String[]{ "rm", "-Rf", dirPath };
		return command( cmd, false );
	}

	/**
	 * ������ύX����
	 * @param permission ����
	 * @param path �����ύX�Ώۂ̃f�B���N�g���������̓t�H���_�p�X
	 * @return true:���� / false:���s
	 */
	public static boolean changeMode(String permission, String path) {
		String[] cmd = new String[]{ "chmod", "-R", permission, path };
		return command( cmd, false );
	}
	
	/**
	 * �V�F�������s����
	 * @param filePath ���s����V�F���̃p�X
	 * @return true:���� / false:���s
	 */
	public static boolean executeShell(String filePath) {
		String[] cmd = new String[]{ filePath };
		return command( cmd, false );
	}

	/**
	 * �t�@�C�����_�E�����[�h����
	 * @param srcUrl �t�@�C���\�[�X��URL
	 * @param savePath �i�[��p�X
	 * @return true:���� / false:���s
	 */
	public static boolean downloadFile(String srcUrl, String savePath ) {
		try {
			URL url = new URL( srcUrl );
			InputStream inpstrm = url.openStream();
			OutputStream outstrm = new FileOutputStream(savePath);
			byte buf[] = new byte[8192];
			int len = 0;
			while( ( len = inpstrm.read(buf) ) != -1 ) {
				outstrm.write( buf, 0, len );
			}
			outstrm.flush();
			outstrm.close();
			inpstrm.close();
		}
		catch ( Exception ex ) {
			Logger.global.severe( ex.toString() );
			return false;
		}
		return true;
	}
	
	/**
	 * SQL�t�@�C�������s����
	 * @param host �����[�g�z�X�g��
	 * @param db �Ώۂ�DB��
	 * @param file ���s����t�@�C����
	 * @return true:���� / false:���s
	 */
	public static boolean execSqlFile(String host, String db, String file) {
		String opHost = "";
		if (host != null && !host.equals("")) {
			opHost = " --host=" + host;
		}
		String main = "mysql" + opHost + " --user=bird --password=bird2006 " + db + " < \"" + file + "\"";
		
		String[] cmd = null;
		if(OS_NAME.indexOf("Windows") != -1){
			cmd = new String[]{ "cmd", "/c", main };
		}
		else {
			cmd = new String[]{ "sh", "-c", main };
		}
		
		return command( cmd, true );
	}
	
	/**
	 * SQL�_���v�����s����
	 * @param host �����[�g�z�X�g��
	 * @param db �Ώۂ�DB��
	 * @param tables �Ώۂ̃e�[�u��
	 * @param file �o�͂���t�@�C����
	 * @return true:���� / false:���s
	 */
	public static boolean execSqlDump(String host, String db, String[] tables, String file) {
		String opHost = "";
		if (host != null && !host.equals("")) {
			opHost = " --host=" + host;
		}
		StringBuilder strTable = new StringBuilder();
		if (tables != null) {
			for (String table : tables) {
				strTable.append(" " + table);
			}
		}
		String main = "mysqldump" + opHost + " --user=bird --password=bird2006 " + db + strTable.toString() + " > \"" + file + "\"";
		
		String[] cmd = null;
		if(OS_NAME.indexOf("Windows") != -1){
			cmd = new String[]{ "cmd", "/c", main };
		}
		else {
			cmd = new String[]{ "sh", "-c", main };
		}
		
		return command( cmd, true );
	}
	
	/**
	 * �R�}���h�����s����
	 * @param cmd ���s�R�}���h
	 * @param isLongTimeOut �^�C���A�E�g�l�����t���O
	 * @return true:���� / false:���s
	 */
	public static boolean command(String[] cmd, boolean isLongTimeOut) {
		// �R�}���h���s
		CmdResult res = new CmdExecute(isLongTimeOut).exec(cmd);
			// �G���[�o�͂�����΃��O�ɏ����o��
		String err = res.getStderr();
		if ( !err.equals("") ) {
			String cmdline = "";
			for ( int i = 0; i < cmd.length; i++ ) {
				cmdline += cmd[i] + " ";
			}
			String crlf = System.getProperty("line.separator");
			String errMsg = crlf + "[Command] " + cmdline + crlf + "[Error Discription]" + crlf + err;
			Logger.global.warning( errMsg );
		}
			// �I���R�[�h�擾
		if ( res.getStatus() != 0 ) {
			return false;
		}
		return true;
	}
}
