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
 * �o�[�W�������Ǘ��N���X
 *
 * ver 1.0.3 2008.12.19
 *
 ******************************************************************************/
package massbank.admin;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.lang.NumberUtils;
import massbank.GetConfig;
import massbank.admin.AdminCommon;
import massbank.admin.VersionInfo;
import massbank.admin.FileUtil;

public class VersionManager {

	// �R���|�[�l���g��(�\����)
	public static final String[] COMPONENT_NAMES = {
		"Applet", "Common Lib", "JSP", "CGI", "Java Script",
		"CSS", "Admin Tool"
	};
	// �R���|�[�l���g�̃f�B���N�g��(�\����)
	public static final String[] COMPONENT_DIR = {
		"applet", "WEB-INF/lib", "jsp", "cgi-bin", "script",
		"css", "mbadmin"
	};

	// �R���|�[�l���g�̃o�[�W�����i�[��
	private static final int COMPONENT_APPLET    = 0;
	private static final int COMPONENT_LIB       = 1;
	private static final int COMPONENT_JSP       = 2;
	private static final int COMPONENT_CGI       = 3;
	private static final int COMPONENT_SCRIPT    = 4;
	private static final int COMPONENT_CSS       = 5;
	private static final int COMPONENT_ADMIN     = 6;

	// JavaScript, CSS�t�@�C���̃f�B���N�g��
	private static final String OTHER_DIR[] = {
		"script", "css"
	};
	// JavaScript, CSS�t�@�C���̊g���q
	private static final String OTHER_EXTENSION[] = {
		".js", ".css"
	};
	// JavaScript, CSS�t�@�C���̊i�[��
	private static final int OTHER_ARRAY_NUM[] = {
		COMPONENT_SCRIPT, COMPONENT_CSS
	};
	// Admin Tool�̃f�B���N�g��
	private static final String ADMIN_DIR[] = {
		"", "css/"
	};
	// Admin Tool�̊g���q
	private static final String ADMIN_EXTENSION_REGEX[] = {
		".*\\.(jsp|html)$", ".*\\.css$"
	};
	// �X�V���O�t�@�C��
	private static final String EXCLUSION_LIST[][] = {
		{ "JSP", "index.jsp" },
		{ "JSP", "BatchSearch.jsp" },
		{ "CGI", "BatchSender.cgi" }
	};
	// Common Lib �X�V���O�t�@�C��
	private static final String EXCLUSION_COM_LIB[] = {
		"catalina-root.jar"
	};
	// �A�[�J�C�u��
	private static String ARCHIVE_NAME = "update";

	// JSP��
	private String jspName = "";
	// �x�[�XURL
	private String baseUrl = "";
	// Apache��MassBan�p�X
	private String massBankPath = "";
	// Tomcat��ROOT�p�X
	private String webRootPath = "";
	// Tomcat��MassBank�p�X
	private String tomcatMbPath = "";
	// CGI�w�b�_�[
	private String cgiHeader = "";
	// �v���C�}���T�[�oURL
	private String priServerUrl = "";
	// ���T�[�oURL
	private String myServerUrl = "";
	// �o�[�W�������i�[�z��
	private List<VersionInfo>[] verInfoMyServer = null;
	private List<VersionInfo>[] verInfoPriServer = null;

	// �X�V�Ώۃt�@�C���̃J�E���g
	private int oldCnt = 0;
	private int addCnt = 0;
	private int newCnt = 0;
	private int delCnt = 0;

	/**
	 * �R���X�g���N�^
	 */
	public VersionManager(String reqUrl, String realPath) {
		int pos1 = reqUrl.indexOf("/jsp");
		// JSP�t�@�C�������Z�b�g
		this.jspName = reqUrl.substring( reqUrl.lastIndexOf("/") + 1 );
		// �x�[�XURL�Z�b�g
		this.baseUrl = reqUrl.substring( 0, pos1 + 1 );

		// �ݒ�t�@�C���Ǎ���
		GetConfig conf = new GetConfig(baseUrl);
		// URL���X�g�擾
		String[] urlList = conf.getSiteUrl();
		this.myServerUrl = urlList[GetConfig.MYSVR_INFO_NUM];

		AdminCommon admin = new AdminCommon(reqUrl, realPath);
		// �v���C�}���T�[�oURL�擾
		this.priServerUrl = admin.getPServerUrl();
		// Apache��MassBan�p�X�擾
		this.massBankPath = admin.getMassBankPath();
		// CGI�w�b�_�[�擾
		this.cgiHeader = admin.getCgiHeader();

		// Tomcat�p�X���Z�b�g
		String tomcatPath = System.getProperty("catalina.home");
		this.webRootPath = tomcatPath + "/webapps/ROOT/";
		String path = this.webRootPath;
		int pos2 = baseUrl.lastIndexOf("MassBank");
		if ( pos2 >= 0 ) {
			path += baseUrl.substring(pos2);
		}
		this.tomcatMbPath = path;
	}


	/**
	 * �A�b�v�f�[�g���������s����
	 * @return true:���� / false:�ُ�
	 */
	public boolean doUpdate(List copyFiles, List removeFiles) {
		// �v���C�}���T�[�o�ɑ΂��A�[�J�C�u�쐬���w������
		if ( !reqMakeArchive() ) {
			return false;
		}
		boolean isOK = false;
		
		// �v���C�}���T�[�o����A�[�J�C�u���_�E�����[�h����
		String archiveName = ARCHIVE_NAME + ".tgz";
		String srcUrl = priServerUrl + "temp/" + archiveName;
		String archivePath = tomcatMbPath + archiveName;
		FileUtil.downloadFile(srcUrl, archivePath);

		// �A�[�J�C�u���𓀂���
		boolean isPh1 = FileUtil.uncompress(archivePath, tomcatMbPath);

		// �X�V�Ώۃt�@�C�����R�s�[����
		boolean isPh2 = true;
		for ( int i = 0; i < copyFiles.size(); i++ ) {
			String relativePath = (String)copyFiles.get(i);
			String srcPath = tomcatMbPath + ARCHIVE_NAME + "/" + relativePath;
			String destPath = getAbsolutePath(relativePath) + relativePath;
			if ( isOverwriteHeader(relativePath) ) {
				overwriteHeader(srcPath);
			}
			if ( !FileUtil.copyFile( srcPath, destPath ) ) {
				isPh2 = false;
				break;
			}
		}

		// �X�V�Ώۃt�@�C�����폜����
		boolean isPh3 = true;
		if ( isPh2 ) {
			for ( int i = 0; i < removeFiles.size(); i++ ) {
				String relativePath = (String)removeFiles.get(i);
				String filePath = getAbsolutePath(relativePath) + relativePath;
				isOK = FileUtil.removeFile(filePath);
				if ( !isOK ) {
					isPh3 = false;
					break;
				}
			}
		}
		else {
			isPh3 = false;
		}

		// �A�[�J�C�u�폜
		FileUtil.removeDir(tomcatMbPath + "update");
		FileUtil.removeFile(archivePath);
		if ( isPh3 ) {
			isOK = true;
		}
		return isOK;
	}

	/**
	 * �A�[�J�C�u�쐬���������s����
	 * @return true:���� / false:���s
	 */
	public boolean doArchive() {
		String shellPath = "/MassBank/script/archiver.sh";
		return FileUtil.executeShell(shellPath);
	}

	/**
	 * �o�[�W�����`�F�b�N���������s����
	 * @param verInfoMyServer  ���T�[�o�̃o�[�W������񃊃X�g
	 * @return true:���� / false:�ُ�(�v���C�}���T�[�o�ɐڑ��ł��Ȃ��ꍇ)
	 */
	public boolean doCheckVersion(List<VersionInfo>[] verInfoMyServer) {
		// ���T�[�o�̃o�[�W��������ݒ肷��
		this.verInfoMyServer = verInfoMyServer;
		// �v���C�}���T�[�o�̃o�[�W���������擾
		this.verInfoPriServer = getVerPriServer();
		if ( verInfoPriServer == null ) {
			return false;
		}
		// �o�[�W�������X�e�[�^�X��ݒ�
		setVerStaus();
		return true;
	}

	/**
	 * �v���C�}���T�[�o�̃o�[�W�������擾���������s����
	 * @param verInfoServer �T�[�o�̃o�[�W������񃊃X�g
	 * @return �v���C�}���T�[�o�̃o�[�W�������i������j
	 */
	public String doGetVerPServer(List<VersionInfo>[] verInfoServer) {
		StringBuffer res = new StringBuffer();
		// �o�[�W���������^�u��؂�̌`���ŕԂ�
		for ( int i = 0; i < COMPONENT_NAMES.length; i++ ) {
			for ( int j = 0; j < verInfoServer[i].size(); j++ ) {
				VersionInfo verInfo = (VersionInfo)verInfoServer[i].get(j);
				res.append(
					COMPONENT_NAMES[i] + "\t" + verInfo.getName() + "\t"
					+ verInfo.getVersion() + "\t" + verInfo.getDate() + "\n" );
			}
		}
		return res.toString();
	}

	/**
	 * ���T�[�o�̃o�[�W������񃊃X�g���擾����
	 * @return ���T�[�o�̃o�[�W������񃊃X�g
	 */
	public List<VersionInfo>[] getVerMyServer() {
		List<VersionInfo>[] verInfoMyServer = new ArrayList[COMPONENT_NAMES.length];

		// Applet�o�[�W�����擾
		verInfoMyServer[COMPONENT_APPLET] = getVerApplet();

		// Common Lib�o�[�W�����擾
		verInfoMyServer[COMPONENT_LIB] = getVerComLib();

		// JSP�o�[�W�����擾
		String jspPath = tomcatMbPath + COMPONENT_DIR[COMPONENT_JSP] + "/";
		String extension = "jsp";
		verInfoMyServer[COMPONENT_JSP] = getVerOther( jspPath, extension );

		// CGI�o�[�W�����擾
		verInfoMyServer[COMPONENT_CGI] = getVerCgi();

		// CSS, Script�o�[�W�����擾
		for ( int i = 0; i < OTHER_DIR.length; i++ ) {
			String otherPath = massBankPath + OTHER_DIR[i] + "/";
			verInfoMyServer[OTHER_ARRAY_NUM[i]] = getVerOther( otherPath, OTHER_EXTENSION[i] );
		}

		// Admin Tool �o�[�W�����擾
		verInfoMyServer[COMPONENT_ADMIN] = getVerAdminTool();

		return verInfoMyServer;
	}

	/**
	 * �X�V�L���𔻒肷��
	 * @return true:�X�V���� / false:�X�V�Ȃ�
	 */
	public boolean isUpdate() {
		if ( oldCnt + addCnt + newCnt + delCnt == 0 ) {
			return false;
		}
		return true;
	}

	/**
	 * �X�e�[�^�X"OLD"�̃J�E���g���擾
	 */
	public int getOldCnt() {
		return oldCnt;
	}
	/**
	 * �X�e�[�^�X"ADD"�̃J�E���g���擾
	 */
	public int getAddCnt() {
		return addCnt;
	}
	/**
	 * �X�e�[�^�X"NEW"�̃J�E���g���擾
	 */
	public int getNewCnt() {
		return newCnt;
	}
	/**
	 * �X�e�[�^�X"DEL"�̃J�E���g���擾
	 */
	public int getDelCnt() {
		return delCnt;
	}

	/**
	 * �v���C�}���T�[�o�ɑ΂��A�[�J�C�u�쐬���w������
	 * @return true:���� / false:�ُ�
	 */
	private boolean reqMakeArchive() {
		String res = "";
		try {
			URL url = new URL( priServerUrl + "jsp/" + jspName + "?act=archive" );
			URLConnection con = url.openConnection();
			if ( con == null ) {
				return false;
			}
			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			String line = "";
			while ( ( line = in.readLine() ) != null ) {
				if ( !line.trim().equals("") ) {
					res += line.trim();
				}
			}
		}
		catch ( Exception e ) {
			return false;
		}
		if ( !res.equals("OK") ) {
			return false;
		}
		return true;
	}

	/**
	 * �v���C�}���T�[�o�̃o�[�W���������擾����
	 * @return �v���C�}���T�[�o�̃o�[�W������񃊃X�g
	 *         (�v���C�}���T�[�o�ɐڑ��ł��Ȃ��ꍇ��null)
	 */
	private List<VersionInfo>[] getVerPriServer() {
		List<VersionInfo>[] verInfoPriServer = new ArrayList[COMPONENT_NAMES.length];
		for ( int i = 0; i < COMPONENT_NAMES.length; i++ ) {
			verInfoPriServer[i] = new ArrayList();
		}
		String strUrl = priServerUrl + "jsp/" + jspName + "?act=get";
		try {
			URL url = new URL( strUrl );
			URLConnection con = url.openConnection();
			if ( con == null ) {
				return null;
			}
			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			String line = "";
			while ( ( line = in.readLine() ) != null ) {
				String data = line.trim();
				if ( data.equals("") ) {
					continue;
				}
				String[] items = data.trim().split("\t");
				String compoName = items[0];
				String name      = items[1];
				String ver       = items[2];
				String date      = items[3];

				// ���O�t�@�C���̓��X�g�ɒǉ����Ȃ�
				boolean isExclusion = false;
				for ( int j = 0; j < EXCLUSION_LIST.length; j++ ) {
					if ( compoName.equals(EXCLUSION_LIST[j][0])
					  && name.equals(EXCLUSION_LIST[j][1]) ) {
						isExclusion = true;
						break;
					}
				}
				if ( isExclusion ) {
					continue;
				}
				// �o�[�W�������i�[
				for ( int j = 0; j < COMPONENT_NAMES.length; j++ ) {
					VersionInfo verInfo = new VersionInfo(name, ver, date);
					if ( compoName.equals(COMPONENT_NAMES[j]) ) {
						verInfoPriServer[j].add(verInfo);
						break;
					}
				}
			}
			in.close();
		}
		catch ( Exception e ) {
			return null;
		}
		return verInfoPriServer;
	}

	/**
	 * �o�[�W�������̃X�e�[�^�X��ݒ肷��
	 */
	private void setVerStaus() {
		int oldCnt = 0;
		int addCnt = 0;
		int newCnt = 0;
		int delCnt = 0;

		for ( int i = 0; i < COMPONENT_NAMES.length; i++ ) {
			//-------------------------------------------------------------
			// ���T�[�o���̃t�@�C�����v���C�}���T�[�o�ɑ��݂��邩�`�F�b�N
			//-------------------------------------------------------------
			for ( int l1 = 0; l1 < verInfoMyServer[i].size(); l1++ ) {
				boolean isFound = false;
				VersionInfo verInfo1 = (VersionInfo)verInfoMyServer[i].get(l1);
				VersionInfo verInfo2 = null;
				// ���T�[�o���̃t�@�C����
				String name1 = verInfo1.getName();

				// ���O�t�@�C���̓`�F�b�N�ΏۊO�ɂ���
				boolean isExclusion = false;
				for ( int j = 0; j < EXCLUSION_LIST.length; j++ ) {
					if ( COMPONENT_NAMES[i].equals(EXCLUSION_LIST[j][0] )
					  && name1.equals(EXCLUSION_LIST[j][1]) ) {
						isExclusion = true;
						break;
					}
				}
				if ( isExclusion ) {
					continue;
				}
				// ���݃`�F�b�N
				for ( int l2 = 0; l2 < verInfoPriServer[i].size(); l2++ ) {
					verInfo2 = (VersionInfo)verInfoPriServer[i].get(l2);
					String name2 = verInfo2.getName();
					if ( name2.equals(name1) ) {
						isFound = true;
						break;
					}
				}

				//== �t�@�C�������������ꍇ ==
				if ( isFound ) {
					// �o�[�W�������̓��t
					String date1 = verInfo1.getDate();
					String date2 = verInfo2.getDate();
					// ���t���Ȃ����̂�0�l�߂ɂ���
					if ( date1.equals("-") ) {
						date1 = "00.00.00";
					}
					if ( date2.equals("-") ) {
						date2 = "00.00.00";
					}
					// �s���I�h��؂�œ��t�𕪊�
					String[] vals1 = date1.split("\\.");
					String[] vals2 = date2.split("\\.");

					// �N�������s���I�h�Ȃ��ŘA��
					String convDate1 = "";
					String convDate2 = "";

					for ( int k = 0; k < vals1.length; k++ ) {
						// �N��2���ȏ�̏ꍇ��2���ɂ���

						if ( k == 0 ) {
							vals1[k] = vals1[k].substring(vals1[k].length() - 2);
							vals2[k] = vals2[k].substring(vals2[k].length() - 2);
						}
						// �N����1���̏ꍇ�́A0�l��2���ɂ���
						convDate1 += "00".substring(vals1[k].length()) + vals1[k];
						convDate2 += "00".substring(vals2[k].length()) + vals2[k];
					}
					int iDate1 = Integer.parseInt(convDate1);
					int iDate2 = Integer.parseInt(convDate2);
					int status = 0;
					if ( iDate1 < iDate2 ) {
						//** �X�e�[�^�X�Z�b�g�u�Â��v
						status = VersionInfo.STATUS_OLD;
						oldCnt++;
					}
					else if ( iDate1 > iDate2 ) {
						//** �X�e�[�^�X�Z�b�g�u�V�����v
						status = VersionInfo.STATUS_NEW;
						newCnt++;
					}
					else {
						status = VersionInfo.STATUS_NON;
					}
					verInfo1.setStatus(status);
				}
				//== �t�@�C����������Ȃ��ꍇ ==  ���T�[�o�̂ݑ���
				else {
					//** �X�e�[�^�X�Z�b�g�u�폜�v
					verInfo1.setStatus(VersionInfo.STATUS_DEL);
					delCnt++;
				}
			}
			//-------------------------------------------------------------
			// �v���C�}���T�[�o���ɂ̂ݑ��݂���t�@�C�����`�F�b�N
			//-------------------------------------------------------------
			for ( int l1 = 0; l1 < verInfoPriServer[i].size(); l1++ ) {
				VersionInfo verInfo1 = (VersionInfo)verInfoPriServer[i].get(l1);
				VersionInfo verInfo2 = null;
				// �v���C�}���T�[�o���̃t�@�C����
				String name1 = verInfo1.getName();
				boolean isFound = false;

				// ���݃`�F�b�N
				for ( int l2 = 0; l2 < verInfoMyServer[i].size(); l2++ ) {
					verInfo2 = (VersionInfo)verInfoMyServer[i].get(l2);
					String name2 = verInfo2.getName();
					if ( name2.equals(name1) ) {
						isFound = true;
						break;
					}
				}
				if ( !isFound ) {
					VersionInfo verInfo3 = new VersionInfo( name1, "-", "-" );
					verInfoMyServer[i].add(verInfo3);
					//** �X�e�[�^�X�Z�b�g�u�ǉ��v
					verInfo3.setStatus(VersionInfo.STATUS_ADD);
					addCnt++;
				}
			}
		}
		this.oldCnt = oldCnt;
		this.addCnt = addCnt;
		this.newCnt = newCnt;
		this.delCnt = delCnt;
	}

	/**
	 * Applet�̃o�[�W���������擾����
	 * @return �o�[�W�������
	 */
	private List<VersionInfo> getVerApplet() {
		List verInfoList = new ArrayList<VersionInfo>();
	
		// applet�f�B���N�g���̃t�@�C�����X�g�擾
		String appletPath = massBankPath + "applet/";
		File file = new File( appletPath );
		String allList[] = file.list();
		ArrayList<String> targetList = new ArrayList();
		for ( int i = 0; i < allList.length; i++ ) {
			if ( allList[i].indexOf(".jar") >= 0 ) {
				targetList.add( allList[i] );
			}
		}
		// �t�@�C�����Ń\�[�g
		Collections.sort(targetList);
		for ( int i = 0; i < targetList.size(); i++ ) {
			String fileName = targetList.get(i);
			VersionInfo verInfo = getVerJarFile( appletPath + fileName );
			// �o�[�W�������i�[
			verInfo.setName(fileName);
			verInfoList.add(verInfo);
		}
		return verInfoList;
	}

	/**
	 * Common Lib�̃o�[�W���������擾����
	 * @return �o�[�W�������
	 */
	private List<VersionInfo> getVerComLib() {
		List verInfoList = new ArrayList<VersionInfo>();

		// WEB-INF/lib�f�B���N�g���̃t�@�C�����X�g�擾
		String libPath = webRootPath + "WEB-INF/lib/";
		File file = new File(libPath);
		String allList[] = file.list();
		ArrayList<String> targetList = new ArrayList();
		for ( int i = 0; i < allList.length; i++ ) {
			boolean isExclusion = false;
			for ( int j = 0; j < EXCLUSION_COM_LIB.length; j++ ) {
				if ( allList[i].equals(EXCLUSION_COM_LIB[j]) ) {
					isExclusion = true;
					break;
				}
			}
			if ( !isExclusion && allList[i].indexOf(".jar") >= 0 ) {
				targetList.add( allList[i] );
			}
		}
		// �t�@�C�����Ń\�[�g
		Collections.sort( targetList );
		for ( int i = 0; i < targetList.size(); i++ ) {
			String fileName = targetList.get(i);
			VersionInfo verInfo = getVerJarFile( libPath + fileName );
			// �o�[�W�������i�[
			verInfo.setName(fileName);
			verInfoList.add(verInfo);
		}
		return verInfoList;
	}

	/**
	 * CGI�̃o�[�W���������擾����
	 * @return �o�[�W�������
	 */
	private List<VersionInfo> getVerCgi() {
		List verInfoList = new ArrayList<VersionInfo>();

		// �o�[�W�������擾CGI�����s
		String strUrl = myServerUrl + "/cgi-bin/GetVersion.cgi";
		try {
			URL url = new URL(strUrl);
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			String line = "";
			while ( ( line = in.readLine() ) != null ) {
				String data = line.trim();
				if ( data.equals("") ) {
					continue;
				}
				String[] info = data.split("\t");
		
				// �X�y�[�X��؂�̃o�[�W�����Ɠ��t�����o��
				String name = info[0];
				String[] item = info[1].trim().split(" ");
				String ver  = item[0];
				String date = "-";
				for ( int j = 1; j < item.length; j++ ) {
					if ( !item[j].equals("") ) {
						date = item[j];
						break;
					}
				}
				// �o�[�W�������i�[
				verInfoList.add( new VersionInfo(name, ver, date) );
			}
			in.close();
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return verInfoList;
	}

	/**
	 * Admin Tool�̃o�[�W���������擾����
	 * @return �o�[�W�������
	 */
	private List<VersionInfo> getVerAdminTool() {
		List verInfoList = new ArrayList<VersionInfo>();

		// �t�@�C�����X�g�擾
		for ( int i = 0; i < ADMIN_DIR.length; i++ ) {
			String path = tomcatMbPath + "mbadmin/" + ADMIN_DIR[i];
			File file = new File(path);
			String allList[] = file.list();
			List<String> targetList = new ArrayList();
			for ( int j = 0; j < allList.length; j++ ) {
				if ( allList[j].matches(ADMIN_EXTENSION_REGEX[i]) ) {
					targetList.add(allList[j]);
				}
			}

			// �t�@�C�����Ń\�[�g
			Collections.sort( targetList );
			for ( int j = 0; j < targetList.size(); j++ ) {
				String filName = targetList.get(j);
				File file2 = new File( path + filName );
				// �f�B���N�g���͖�������
				if ( file2.isDirectory() ) {
					continue;
				}
				// �e�L�X�g�t�@�C���̃o�[�W�����擾
				VersionInfo verInfo = getVerTextFile( path + filName );
				// �o�[�W�������i�[
				verInfo.setName( ADMIN_DIR[i] + filName );
				verInfoList.add(verInfo);
			}
		}
		return verInfoList;
	}

	/**
	 * ���̑�(JSP,CSS, Script)�̃o�[�W���������擾����
	 * @return �o�[�W�������
	 */
	private List<VersionInfo> getVerOther(String path, String extension) {
		List verInfoList = new ArrayList<VersionInfo>();

		// �t�@�C�����X�g�擾
		File file1 = new File(path);
		String allList[] = file1.list();
		List<String> targetList = new ArrayList();
		for ( int i = 0; i < allList.length; i++ ) {
			if ( allList[i].indexOf(extension) >= 0 ) {
				targetList.add(allList[i]);
			}
		}

		// �t�@�C�����Ń\�[�g
		Collections.sort(targetList);
		for ( int i = 0; i < targetList.size(); i++ ) {
			String fileName = targetList.get(i);
			File file2 = new File( path + fileName );
			// �f�B���N�g���͖���
			if ( file2.isDirectory() ) {
				continue;
			}
			// �e�L�X�g�t�@�C���̃o�[�W�����擾
			VersionInfo verInfo = getVerTextFile( path + fileName );
			// �o�[�W�������i�[
			verInfo.setName(fileName);
			verInfoList.add(verInfo);
		}
		return verInfoList;
	}

	/**
	 * jar�t�@�C���̃o�[�W���������擾����
	 * @param path �t�@�C���̃p�X
	 * @return �o�[�W�������
	 */
	private VersionInfo getVerJarFile(String path) {
		VersionInfo verInfo = null;
		try {
			// �}�j���t�F�X�g���o�[�W�������擾
			JarFile jar = new JarFile( new File(path) );
			Manifest manifest = jar.getManifest();
			Attributes attributes = manifest.getMainAttributes();
			String item1 = attributes.getValue("Implementation-Version");
			if ( item1 == null ) {
				item1 = "-";
			}
			// �X�y�[�X��؂�̃o�[�W�����Ɠ��t�����o��
			String[] item2 = item1.trim().split(" ");
			String ver  = item2[0];
			String date = "-";
			if ( item2.length > 1 ) {
				date = item2[1];
			}
			verInfo = new VersionInfo(null, ver, date);
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return verInfo;
	}


	/*
	 * �e�L�X�g�t�@�C���̃o�[�W���������擾����
	 * @param path �t�@�C���̃p�X
	 * @return �o�[�W�������
	 */
	private VersionInfo getVerTextFile(String path) {
		VersionInfo verInfo = null;
		try {
			String info = "-";
			final String FIND_STR[] = { "//**", "**/" };
			BufferedReader in = new BufferedReader( new FileReader(path) );
			boolean isFound = false;
			boolean isEnd = false;
			String line = "";
			while ( ( line = in.readLine() ) != null ) {
				if ( isFound ) {
					// �w�b�_�[�����R�����g�J�n�s������
					for ( int i = 0; i < FIND_STR.length; i++ ) {
						int pos1 = line.indexOf(FIND_STR[i]);
						if ( pos1 >= 0 ) {
							isEnd = true;
							break;
						}
					}
					if ( isEnd ) {
						break;
					}
				}
				// �o�[�W�������L�q����������
				String find2 = "ver";
				int pos2 = line.indexOf(find2);
				if ( pos2 >= 0 ) {
					info = line.substring( pos2 + find2.length() + 1 ).trim();
					isFound = true;
				}
			}
			in.close();

			// �X�y�[�X��؂�̃o�[�W�����Ɠ��t�����o��
			String[] item = info.split(" ");
			// �o�[�W�����Z�b�g
			String ver = "-";
			String[] vals = null;
			for ( int j = 0; j < item.length; j++ ) {
				if ( !item[j].equals("") ) {
					vals = item[j].split("\\.");
					if ( NumberUtils.isNumber(vals[0])
					  && vals[0].length() < 4 ) {
						ver = item[0];
						break;
					}
				}
			}

			// ���t�Z�b�g
			String date = "-";
			for ( int j = 1; j < item.length; j++ ) {
				// �X�y�[�X�͓ǂݔ�΂�
				if ( !item[j].equals("") ) {
					vals = item[j].split("\\.");
					if ( NumberUtils.isNumber(vals[0])
					  && Integer.parseInt(vals[0]) > 2000 ) {
						date = item[j];
						break;
					}
				}
			}
			verInfo = new VersionInfo(null, ver, date);
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return verInfo;
	}

	/**
	 * �t�@�C���̐�΃p�X���擾����
	 * @param relativePath �t�@�C���̑��΃p�X
	 * @return �t�@�C���̐�΃p�X
	 */
	private String getAbsolutePath(String relativePath) {
		String absPath = "";
		int pos = relativePath.lastIndexOf("/");
		String dir = relativePath.substring( 0, pos );
		if ( dir.equals("jsp") || dir.equals("mbadmin") || dir.equals("mbadmin/css") ) {
			// Tomcat��MassBank�p�X���Z�b�g
			absPath = tomcatMbPath;
		}
		else if ( dir.equals("WEB-INF/lib") ) {
			// Tomcat�̃A�v���P�[�V�������[�g�p�X���Z�b�g
			absPath = webRootPath;
		}
		else {
			// Apache��MassBank�p�X���Z�b�g
			absPath = massBankPath;
		}
		return absPath;
	}

	/**
	 * CGI�w�b�_�[�̏��������v�ۂ𔻒肷��
	 * @param relativePath �t�@�C���̑��΃p�X
	 * @return true:�v / false:��
	 */
	private boolean isOverwriteHeader(String relativePath) {
		if ( cgiHeader.equals("") ) {
			return false;
		}
		int pos = relativePath.lastIndexOf("/");
		String dir = relativePath.substring( 0, pos );
		if ( dir.equals("cgi-bin") ) {
			return true;
		}
		return false;
	}

	/**
	 * CGI�w�b�_�[������������
	 * @param absPath �t�@�C���̃p�X
	 */
	private void overwriteHeader(String absPath) {
		try {
			// �t�@�C���Ǎ���
			InputStreamReader reader = new InputStreamReader(new FileInputStream(absPath), "UTF-8");
			BufferedReader br= new BufferedReader( reader );
			StringBuffer text = new StringBuffer("");
			String line = "";
			while ( ( line = br.readLine() ) != null ) {
				int pos = line.indexOf( "#!" );
				if ( pos >= 0 ) {
					// �w�b�_�[����������
					text.append( cgiHeader + "\n" );
				}
				else {
					text.append( line + "\n" );
				}
			}
			br.close();

			// �t�@�C��������
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(absPath), "UTF-8");
			BufferedWriter bw = new BufferedWriter( writer );
			bw.write( text.toString() );
			bw.flush();
			bw.close();			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
