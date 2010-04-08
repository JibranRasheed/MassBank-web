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
 * �Ǘ��Ґݒ苤�ʃN���X
 *
 * ver 1.0.11 2010.04.06
 *
 ******************************************************************************/
package massbank.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * �Ǘ��Ґݒ苤�ʃN���X
 * �ȉ��̋@�\��񋟂���
 * 
 *   ���@�\��                       ��admin.conf �L�[����
 *   DB�T�[�o�z�X�g���擾           db_host_name
 *   CGI�w�b�_�擾                  cgi_header
 *   DB���[�g�p�X�擾               db_path
 *   Molfile���[�g�p�X�擾          mol_path
 *   Profile���[�g�p�X�擾          profile_path
 *   GIF���[�g�p�X�擾              gif_path
 *   �o�͐�p�X�擾                 out_path
 *   �o�͐�p�X�擾                 primary_server_url
 *   MassBank�f�B���N�g���p�X�擾   -
 *   �Ǘ��Ҍ����t���O�擾           admin
 *   �|�[�^���T�C�g�t���O�擾       portal
 *   SMTP�A�h���X�擾               mail_batch_smtp
 *   ���M�Җ��擾                   mail_batch_name
 *   From�A�h���X�擾               mail_batch_from
 *   
 */
public class AdminCommon {
	
	private String confFilePath = "";
	
	/**
	 * �R���X�g���N�^
	 * @param reqUrl ���N�G�X�gURL
	 * @param realPath �A�v���P�[�V�����p�X�̐�΃p�X
	 */
	public AdminCommon( String reqUrl, String realPath ) {
		int pos1 = reqUrl.indexOf( "/", (new String("http://")).length() );
		int pos2 = reqUrl.lastIndexOf( "/" );
		String subDir = "";
		if (pos1 + 1 < reqUrl.length()) {
			subDir = reqUrl.substring( pos1 + 1, pos2 );
			subDir = subDir.replace( "jsp", "" );
			subDir = subDir.replace( "mbadmin", "" );
			subDir = subDir.replace( "Knapsack", "" );
			if (!subDir.equals("")) {
				if (!subDir.endsWith("/")) {
					subDir += "/";
				}
				if ((new File(realPath).getName()).equals(subDir.substring(0, subDir.length()-1))) {
					subDir = "";
				}
			}
		}
		this.confFilePath = realPath + subDir + "mbadmin/admin.conf";
	}
	
	/**
	 * DB�T�[�o�z�X�g���擾
	 */
	public String getDbHostName() {
		String hostName = "localhost";
		String val = getSetting( "db_host_name", false );
		if ( !val.equals("") ) {
			hostName = val;
		}
		return hostName;
	}
	
	/**
	 * CGI�w�b�_�擾
	 */
	public String getCgiHeader() {
		String header = getSetting( "cgi_header", false );
		if ( !header.equals("") ) {
			header = "#! " + header;
		}
		return header;
	}
	
	/**
	 * DB���[�g�p�X�擾
	 */
	public String getDbRootPath() {
		String path = getSetting( "db_path", true );
		if ( path.equals("") ) {
			path = "/var/www/html/MassBank/DB/annotation/";
		}
		return path;
	}
	
	/**
	 * Molfile���[�g�p�X�擾
	 */
	public String getMolRootPath() {
		String path = getSetting( "mol_path", true );
		if ( path.equals("") ) {
			path = "/var/www/html/MassBank/DB/molfile/";
		}
		return path;
	}
	
	/**
	 * Profile���[�g�p�X�擾
	 */
	public String getProfileRootPath() {
		String path = getSetting( "profile_path", true );
		if ( path.equals("") ) {
			path = "/var/www/html/MassBank/DB/profile/";
		}
		return path;
	}
	
	/**
	 * GIF���[�g�p�X�擾
	 */
	public String getGifRootPath() {
		String path = getSetting( "gif_path", true );
		if ( path.equals("") ) {
			path = "/var/www/html/MassBank/DB/gif/";
		}
		return path;
	}
	
	/**
	 * �o�͐�p�X�擾
	 */
	public String getOutPath() {
		return getSetting( "out_path", true );
	}

	/**
	 * �v���C�}���T�[�oURL�擾
	 */
	public String getPServerUrl() {
		String url = getSetting( "primary_server_url", true );
		if ( url.equals("") ) {
			url = "http://www.massbank.jp/";
		}
		return url;
	}
	
	/**
	 * MassBank�f�B���N�g���p�X�擾
	 * Apache��MassBank�f�B���N�g���̃��A���p�X���擾����
	 */
	public String getMassBankPath() {
		String path = "";
		String dbPath = getDbRootPath();
		int pos = dbPath.lastIndexOf("DB");
		if ( pos >= 0 ) {
			path = dbPath.substring(0, pos);
		}
		return path;
	}

	/**
	 * �Ǘ��Ҍ����t���O�擾
	 */
	public boolean isAdmin() {
		boolean ret = false;
		String adminFlag = getSetting( "admin", false );
		if ( adminFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}

	/**
	 * �|�[�^���T�C�g�t���O�擾
	 */
	public boolean isPortal() {
		boolean ret = false;
		String adminFlag = getSetting( "portal", false );
		if ( adminFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}

	/**
	 * SMTP�A�h���X�擾�iBatch Service�p�j
	 */
	public String getMailSmtp() {
		return getSetting( "mail_batch_smtp", false );
	}
	
	/**
	 * ���M�Җ��擾�iBatch Service�p�j
	 */
	public String getMailName() {
		return getSetting( "mail_batch_name", false );
	}
	
	/**
	 * From�A�h���X�擾�iBatch Service�p�j
	 */
	public String getMailFrom() {
		return getSetting( "mail_batch_from", false );
	}
	
	/**
	 * admin.conf�ɒ�`���ꂽ�l���擾����
	 * �u#�v�Ŏn�܂�s�̓R�����g�s�Ƃ���
	 * @param key �L�[��
	 * @param isPath �擾���悤�Ƃ���l���p�X�ł��邩�ǂ���
	 */
	private String getSetting( String key, boolean isPath ) {
		String val = "";
		String line = "";
		try {
			BufferedReader in = new BufferedReader( new FileReader( confFilePath ) );
			while ( ( line = in.readLine() ) != null ) {
				if (line.startsWith("#")) {
					continue;
				}
				int pos = line.indexOf( "=" );
				if ( pos >= 0 ) {
					String keyInfo = line.substring( 0, pos );
					String valInfo = line.substring( pos + 1 );
					if ( key.equals( keyInfo ) ) {
						val = valInfo.trim();
						break;
					}
				}
			}
			in.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		if ( isPath && !val.equals("") ) {
			// �p�X�����Ƀt�@�C���̋�؂蕶���Ȃ���Εt������
			char chrLast = val.charAt( val.length()-1 );
			if ( chrLast != '/' && chrLast != '\\' ) {
				val += File.separator;
			}
		}
		return val;
	}
}
