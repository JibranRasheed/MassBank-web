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
 * Admin Tool ���ʃN���X
 *
 * ver 1.0.7 2009.02.18
 *
 ******************************************************************************/
package massbank.admin;

import java.io.*;

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
		String subDir = reqUrl.substring( pos1 + 1, pos2 );
		subDir = subDir.replace( "jsp", "" );
		subDir = subDir.replace( "mbadmin", "" );
		realPath = realPath.replace( subDir, "" );
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
	 * Gif���[�g�p�X�擾
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
	 * Apache��MassBank�f�B���N�g���̃p�X�擾
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
	 * admin.conf�ɒ�`���ꂽ�p�X���擾����
	 * @param name ���ږ�
	 */
	private String getSetting( String name, boolean isPath ) {
		String path = "";
		String line = "";
		try {
			BufferedReader in = new BufferedReader( new FileReader( confFilePath ) );
			while ( ( line = in.readLine() ) != null ) {
				int pos = line.indexOf( "=" );
				if ( pos >= 0 ) {
					String nameInfo = line.substring( 0, pos );
					String pathInfo = line.substring( pos + 1 );
					if ( name.equals( nameInfo ) ) {
						path = pathInfo.trim();
						break;
					}
				}
			}
			in.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		if ( isPath && !path.equals("") ) {
			// �p�X�����Ƀt�@�C���̋�؂蕶���Ȃ���Εt������
			char chrLast = path.charAt( path.length()-1 );
			if ( chrLast != '/' && chrLast != '\\' ) {
				path += File.separator;
			}
		}
		return path;
	}
}
