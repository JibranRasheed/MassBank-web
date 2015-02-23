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
 * �o�[�W�������f�[�^�N���X
 *
 * ver 1.0.2 2008.12.05
 *
 ******************************************************************************/
package massbank.admin;

/**
 * 
 * �o�[�W�������f�[�^�N���X
 *
 * @ver 1.0.0 2007.12.14
 * @ver 1.0.1 2008.01.22
 */
public class VersionInfo {
	// �z��ԍ�
	public static final int NAME    = 0;
	public static final int VERSION = 1;
	public static final int DATE    = 2;
	public static final int STATUS  = 3;
	// �X�e�[�^�XNO
	public static final int STATUS_NON = 0;
	public static final int STATUS_OLD = 1;
	public static final int STATUS_NEW = 2;
	public static final int STATUS_ADD = 3;
	public static final int STATUS_DEL = 4;

	private String name = "";
	private String ver  = "";
	private String date = "";
	private int status = STATUS_NON;

	public VersionInfo() {
	}

	/**
	 * �R���X�g���N�^
	 * @param items �i�[������
	 */
	public VersionInfo(String[] items) {
		this.name = items[NAME];
		this.ver  = items[VERSION];
		this.date = items[DATE];
	}

	/**
	 * �R���X�g���N�^
	 * @param name �t�@�C����
	 * @param ver  �o�[�W����
	 * @param name ���t
	 */
	public VersionInfo(String name, String ver, String date) {
		this.name = name;
		this.ver  = ver;
		this.date = date;
	}

	/**
	 * �t�@�C�������Z�b�g
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * �t�@�C�������擾
	 */
	public String getName() {
		return this.name;
	}
	/**
	 * �o�[�W�������擾
	 */
	public String getVersion() {
		return this.ver;
	}

	/**
	 * ���t���擾
	 */
	public String getDate() {
		return this.date;
	}

	/**
	 * �X�e�[�^�X�擾
	 */
	public String getStatus() {
		String ret = "";
		switch (this.status) {
		case STATUS_NON: ret = ""; break;
		case STATUS_OLD: ret = "OLD"; break;
		case STATUS_NEW: ret = "NEW"; break;
		case STATUS_ADD: ret = "ADD"; break;
		case STATUS_DEL: ret = "DEL"; break;
		}
		return ret;
	}

	/**
	 * �X�e�[�^�X�Z�b�g
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * �X�V�L������
	 */
	public boolean isUpdate() {
		if ( this.status == STATUS_NON ) {
			return false;
		}
		return true;
	}
}
