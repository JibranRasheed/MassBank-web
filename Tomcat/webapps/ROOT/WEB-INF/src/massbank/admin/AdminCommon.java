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
 * ver 1.0.16 2010.11.25
 *
 ******************************************************************************/
package massbank.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import massbank.MassBankEnv;

/**
 * �Ǘ��Ґݒ苤�ʃN���X
 * �ȉ��̋@�\��񋟂���
 * 
 *   ���@�\��                                       ���擾�恄
 *   DB�T�[�o�z�X�g���擾�i�񐄏��j                 MassBankEnv
 *   CGI�w�b�_�擾                                  admin.conf(cgi_header)
 *   Annotation���[�g�p�X�擾�i�񐄏��j             MassBankEnv
 *   Molfile���[�g�p�X�擾�i�񐄏��j                MassBankEnv
 *   Profile���[�g�p�X�擾�i�񐄏��j                MassBankEnv
 *   GIF���[�g�p�X�擾�i�񐄏��j                    MassBankEnv
 *   GIFSMALL���[�g�p�X�擾�i�񐄏��j               MassBankEnv
 *   GIFLARGE���[�g�p�X�擾�i�񐄏��j               MassBankEnv
 *   �o�͐�p�X�擾                                 admin.conf(out_path)
 *   �o�͐�p�X�擾�i�񐄏��j                       MassBankEnv
 *   MassBank�f�B���N�g���p�X�擾�i�񐄏��j         MassBankEnv
 *   �Ǘ��Ҍ����t���O�擾                           admin.conf(admin)
 *   �|�[�^���T�C�g�t���O�擾                       admin.conf(portal)
 *   Peak Search�iMolecular Formula�j�\���t���O�擾 admin.conf(service_peakadv)
 *   Batch Service�\���t���O�擾                    admin.conf(service_batch)
 *   Substructure Search�iKNApSAcK�j�\���t���O�擾  admin.conf(service_knapsack)
 *   Advanced Search�\���t���O�擾                  admin.conf(service_advanced)
 *   SMTP�A�h���X�擾�i�񐄏��j                     MassBankEnv
 *   ���M�Җ��擾�i�񐄏��j                         MassBankEnv
 *   From�A�h���X�擾�i�񐄏��j                     MassBankEnv
 *   �X�P�W���[���擾                               admin.conf(schedule)
 *   
 *   ���񐄏��̋@�\�Ɋւ��Ă�MassBankEnv#get(String)�̎g�p�𐄏�����
 *   
 */
public class AdminCommon {
	
	/**
	 * �f�t�H���g�R���X�g���N�^
	 */
	public AdminCommon() {
	}
	
	/**
	 * �R���X�g���N�^
	 * @param reqUrl ���N�G�X�gURL
	 * @param realPath �A�v���P�[�V�����p�X�̐�΃p�X
	 * @deprecated �񐄏��R���X�g���N�^
	 * @see AdminCommon#AdminCommon()
	 */
	public AdminCommon( String reqUrl, String realPath ) {
	}
	
	/**
	 * DB�T�[�o�z�X�g���擾
	 * @deprecated �񐄏����\�b�h
	 * @see MassBankEnv#get(String)
	 */
	public String getDbHostName() {
		return MassBankEnv.get(MassBankEnv.KEY_DB_HOST_NAME);
	}
	
	/**
	 * CGI�w�b�_�擾
	 */
	public String getCgiHeader() {
		String header = getSetting( "cgi_header" );
		if ( !header.equals("") ) {
			header = "#! " + header;
		}
		return header;
	}
	
	/**
	 * Annotation���[�g�p�X�擾
	 * @deprecated �񐄏����\�b�h
	 * @see MassBankEnv#get(String)
	 */
	public String getDbRootPath() {
		return MassBankEnv.get(MassBankEnv.KEY_ANNOTATION_PATH);
	}
	
	/**
	 * Molfile���[�g�p�X�擾
	 * @deprecated �񐄏����\�b�h
	 * @see MassBankEnv#get(String)
	 */
	public String getMolRootPath() {
		return MassBankEnv.get(MassBankEnv.KEY_MOLFILE_PATH);
	}
	
	/**
	 * Profile���[�g�p�X�擾
	 * @deprecated �񐄏����\�b�h
	 * @see MassBankEnv#get(String)
	 */
	public String getProfileRootPath() {
		return MassBankEnv.get(MassBankEnv.KEY_PROFILE_PATH);
	}
	
	/**
	 * GIF���[�g�p�X�擾
	 * @deprecated �񐄏����\�b�h
	 * @see MassBankEnv#get(String)
	 */
	public String getGifRootPath() {
		return MassBankEnv.get(MassBankEnv.KEY_GIF_PATH);
	}
	
	/**
	 * GIFSMALL���[�g�p�X�擾
	 * @deprecated �񐄏����\�b�h
	 * @see MassBankEnv#get(String)
	 */
	public String getGifSmallRootPath() {
		return MassBankEnv.get(MassBankEnv.KEY_GIF_SMALL_PATH);
	}
	
	/**
	 * GIFLARGE���[�g�p�X�擾
	 * @deprecated �񐄏����\�b�h
	 * @see MassBankEnv#get(String)
	 */
	public String getGifLargeRootPath() {
		return MassBankEnv.get(MassBankEnv.KEY_GIF_LARGE_PATH);
	}
	
	/**
	 * �o�͐�p�X�擾
	 */
	public String getOutPath() {
		String outPath = getSetting( "out_path" );
		if ( !outPath.equals("") ) {
			// �p�X�����Ƀt�@�C���̋�؂蕶���Ȃ���Εt������
			char chrLast = outPath.charAt( outPath.length()-1 );
			if ( chrLast != '/' && chrLast != '\\' ) {
				outPath += File.separator;
			}
		}
		return outPath;
	}

	/**
	 * �v���C�}���T�[�oURL�擾
	 * @deprecated �񐄏����\�b�h
	 * @see MassBankEnv#get(String)
	 */
	public String getPServerUrl() {
		return MassBankEnv.get(MassBankEnv.KEY_PRIMARY_SERVER_URL);
	}
	
	/**
	 * MassBank�f�B���N�g���p�X�擾
	 * Apache��MassBank�f�B���N�g���̃��A���p�X���擾����
	 * @deprecated �񐄏����\�b�h
	 * @see MassBankEnv#get(String)
	 */
	public String getMassBankPath() {
		return MassBankEnv.get(MassBankEnv.KEY_APACHE_APPROOT_PATH);
	}

	/**
	 * �Ǘ��Ҍ����t���O�擾
	 */
	public boolean isAdmin() {
		boolean ret = false;
		String adminFlag = getSetting( "admin" );
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
		String portalFlag = getSetting( "portal" );
		if ( portalFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}

	/**
	 * Peak Search�iMolecular Formula�j�\���t���O�擾
	 */
	public boolean isPeakAdv() {
		boolean ret = false;
		String peakAdvFlag = getSetting( "service_peakadv" );
		if ( peakAdvFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}
	
	/**
	 * Batch Service�\���t���O�擾
	 */
	public boolean isBatch() {
		boolean ret = false;
		String batchFlag = getSetting( "service_batch" );
		if ( batchFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}

	/**
	 * Substructure Search�iKNApSAcK�j�\���t���O�擾
	 */
	public boolean isKnapsack() {
		boolean ret = false;
		String knapsackFlag = getSetting( "service_knapsack" );
		if ( knapsackFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}
	
	/**
	 * Advanced Search�\���t���O�擾
	 */
	public boolean isAdvanced() {
		boolean ret = false;
		String advancedFlag = getSetting( "service_advanced" );
		if ( advancedFlag.toLowerCase().equals("true") ) {
			ret = true;
		}
		return ret;
	}
	
	/**
	 * SMTP�A�h���X�擾�iBatch Service�p�j
	 * @deprecated �񐄏����\�b�h
	 * @see MassBankEnv#get(String)
	 */
	public String getMailSmtp() {
		return MassBankEnv.get(MassBankEnv.KEY_BATCH_SMTP);
	}
	
	/**
	 * ���M�Җ��擾�iBatch Service�p�j
	 * @deprecated �񐄏����\�b�h
	 * @see MassBankEnv#get(String)
	 */
	public String getMailName() {
		return MassBankEnv.get(MassBankEnv.KEY_BATCH_NAME);
	}
	
	/**
	 * From�A�h���X�擾�iBatch Service�p�j
	 * @deprecated �񐄏����\�b�h
	 * @see MassBankEnv#get(String)
	 */
	public String getMailFrom() {
		return MassBankEnv.get(MassBankEnv.KEY_BATCH_FROM);
	}
	
	/**
	 * �X�P�W���[���擾
	 */
	public ArrayList<String> getSchedule() {
		String[] tmp = getSetting( "schedule" ).split("\t");
		ArrayList<String> vals = new ArrayList<String>();
		for (String val : tmp) {
			if ( !val.trim().equals("") ) {
				vals.add(val.trim());
			}
		}
		return vals;
	}
	
	/**
	 * admin.conf�ɒ�`���ꂽ�l���擾����
	 * �u#�v�Ŏn�܂�s�̓R�����g�s�Ƃ���
	 * @param key �L�[��
	 * @return �l
	 */
	private String getSetting( String key ) {
		String adminConfPath = MassBankEnv.get(MassBankEnv.KEY_ADMIN_CONF_PATH);
		String val = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader( new FileReader( adminConfPath ) );
			String line = "";
			while ( ( line = br.readLine() ) != null ) {
				// "#" �Ŏn�܂�s�̓R�����g�s�Ƃ���
				if (line.startsWith("#") || line.equals("")) {
					continue;
				}
				int pos = line.indexOf( "=" );
				if ( pos >= 0 ) {
					String keyInfo = line.substring( 0, pos );
					String valInfo = line.substring( pos + 1 );
					if ( key.equals( keyInfo ) ) {
						if ( !key.equals("schedule") ) {
							val = valInfo.trim();
							break;
						}
						else {
							val += valInfo.trim() + "\t";
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try { if ( br != null ) { br.close(); } } catch (IOException e) {}
		}
		return val;
	}
}
