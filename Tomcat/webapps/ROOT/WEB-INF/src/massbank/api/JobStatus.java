/*******************************************************************************
 *
 * Copyright (C) 2010 JST-BIRD MassBank
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
 * [WEB-API] �W���u�X�e�[�^�X�f�[�^�N���X
 *
 * ver 1.0.0 2010.04.15
 *
 ******************************************************************************/
package massbank.api;

import massbank.JobManager;

public class JobStatus {

	private int statusCode = 0;
	private String status = "";
	private String reqDate = "";

	/**
	 * �R���X�g���N�^
	 */
	public JobStatus() {
	}

	/**
	 * �X�e�[�^�X���Z�b�g����
	 */
	public void setStatus(String val) {
		this.status = val;
		if ( val.equals(JobManager.STATE_WAIT) ) {
			statusCode = 0;
		}
		else if ( val.equals(JobManager.STATE_RUN) ) {
			statusCode = 1;
		}
		else if ( val.equals(JobManager.STATE_COMPLETE) ) {
			statusCode = 2;
		}
	}

	/**
	 * �W���u��t�������Z�b�g����
	 */
	public void setRequestDate(String val) {
		this.reqDate = val;
	}


	/**
	 * �X�e�[�^�X���擾����
	 */
	public String getStatus() {
		return this.status;
	}

	/**
	 * �X�e�[�^�X�R�[�h���擾����
	 */
	public String getStatusCode() {
		return String.valueOf(this.statusCode);
	}

	/**
	 * �W���u��t�������Z�b�g����
	 */
	public String getRequestDate() {
		return this.reqDate;
	}
}
