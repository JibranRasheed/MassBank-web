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
 * FileUpload ���ʃN���X
 *
 * ver 1.0.0 2009.02.02
 *
 ******************************************************************************/
package massbank;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

/**
 * FileUpload ���ʃN���X
 */
public class FileUpload extends DiskFileUpload {

	// �f�t�H���g�A�b�v���[�h�p�X
	public static final String UPLOAD_PATH = System.getProperty("java.io.tmpdir");
	
	// �o�͐�p�X
	private String outPath = UPLOAD_PATH;
	
	// ���N�G�X�g���
	private HttpServletRequest req = null;

	// multipart/form-data���X�g
	private List<FileItem> fileItemList = null;
	
	// �A�b�v���[�h�t�@�C�����<�t�@�C����, �A�b�v���[�h����>
	private HashMap<String, Boolean> upFileMap = null;

	/**
	 * �R���X�g���N�^
	 * @param req ���N�G�X�g
	 * @param outPath �o�͐�p�X
	 */
	public FileUpload(HttpServletRequest req, String outPath ) {
		super();
		setSizeMax(-1);					// �T�C�Y
		setSizeThreshold(1024);			// �o�b�t�@�T�C�Y
		setRepositoryPath(outPath);		// �ۑ���t�H���_
		setHeaderEncoding("utf-8");		// �����G���R�[�f�B���O
		this.req = req;
		this.outPath = outPath;
	}
	
	/**
	 * ���N�G�X�g�p�����[�^���
	 * multipart/form-data�Ɋ܂܂�Ă���ʏ탊�N�G�X�g�����擾����
	 * ���s�����ꍇ��null��ԋp����
	 * @return �ʏ탊�N�G�X�g���MAP<�L�[, �l>
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, String[]> getRequestParam() {
		
		if (fileItemList == null) {
			try {
				fileItemList = (List<FileItem>)parseRequest(req);
			}
			catch (FileUploadException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		HashMap<String, String[]> reqParamMap = new HashMap<String, String[]>();
		for (FileItem fItem : fileItemList) {
			
			// �ʏ�t�B�[���h�̏ꍇ�i���N�G�X�g�p�����[�^�̒l���z��łȂ��ꍇ�j
			if (fItem.isFormField()) {
				String key = fItem.getFieldName();
				String val = fItem.getString();
				if ( key != null && !key.equals("") ) {
					reqParamMap.put(key, new String[]{val});
				}
			}
		}
		return reqParamMap;
	}
	
	/**
	 * �t�@�C���A�b�v���[�h
	 * multipart/form-data�Ɋ܂܂�Ă���t�@�C�����A�b�v���[�h����
	 * ���s�����ꍇ��null��ԋp����
	 * @return �A�b�v���[�h�t�@�C�����MAP<�t�@�C����, �A�b�v���[�h����>
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Boolean> doUpload() {
		
		if (fileItemList == null) {
			try {
			     fileItemList = (List<FileItem>)parseRequest(req);
			}
			catch (FileUploadException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		upFileMap = new HashMap<String, Boolean>();
		for (FileItem fItem : fileItemList) {
			
			// �t�@�C���t�B�[���h�̏ꍇ
			if ( !fItem.isFormField() ) {
				
				String key = "";
				boolean val = false;
				
				// �t�@�C�����擾�i���ˑ��Ńp�X��񂪊܂܂��ꍇ������j
				String filePath = (fItem.getName() != null) ? fItem.getName() : "";
				
				// �t�@�C�����擾�i�m���Ƀt�@�C�����݂̂��擾�j
				String fileName = (new File(filePath)).getName();
				int pos = fileName.lastIndexOf("\\");
				fileName = fileName.substring( pos + 1 );
				pos = fileName.lastIndexOf("/");
				fileName = fileName.substring( pos + 1 );
				
				// �t�@�C���A�b�v���[�h
				if ( !fileName.equals("") ) {
					key = fileName;
					File upFile = new File( outPath + "/" + fileName); 
					try {
						fItem.write( upFile );
						val = true;
					}
					catch ( Exception e) {
						e.printStackTrace();
						upFile.delete();
					}	
				}
				upFileMap.put(key, val);
			}
		}
		
		return upFileMap;
	}
	
	/**
	 * FileItem�̍폜
	 * �t�@�C���A�b�v���[�h�Ɋ֌W����ꎞ�f�B�X�N�̈���܂ށA
	 * �X�g���[�W��̃t�@�C���A�C�e�����폜����B
	 * FileItem�C���X�^���X���K�x�[�W�R���N�V�����ɂ����������ɂ��̃X�g���[�W�͍폜����邪�A
	 * ���̃��\�b�h�͑f�����m���ɍ폜�����{����B
	 * �S�Ă�multipart/form-data�Ɋ܂܂�Ă�������擾��ɌĂяo�����ƁB
	 */
	public void deleteFileItem() {
		if (fileItemList != null) {
			for (FileItem fItem : fileItemList) {
				fItem.delete();
			}			
		}
	}
	
	/**
	 * �A�b�v���[�h���ꂽ�t�@�C���̑S�폜
	 */
	public void deleteAllFile() {
		String fileName;
		boolean upResult;
		File targetFile;
		for (Map.Entry<String, Boolean> e : upFileMap.entrySet()) {
			fileName = e.getKey();
			upResult = e.getValue();
			targetFile = new File( outPath + "/" + fileName );
			if ( upResult && targetFile.exists() ) {
				targetFile.delete();
			}
		}
	}
	
	/**
	 * �A�b�v���[�h���ꂽ�t�@�C���̍폜
	 * @param fileName �폜�Ώۃt�@�C����
	 */
	public void deleteFile(String fileName) {
		File targetFile = new File ( outPath + "/" + fileName );
		if ( targetFile.exists() ) {
			targetFile.delete();
		}
	}
}
