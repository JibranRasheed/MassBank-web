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
 * [WEB-API] ���ʃZ�b�g�f�[�^�N���X
 *
 * ver 1.0.0 2010.04.15
 *
 ******************************************************************************/
package massbank.api;

import java.util.ArrayList;

public class ResultSet {

	private String qname = "";
	private int num = 0;
	private ArrayList<Result> list = null;

	/**
	 * �R���X�g���N�^
	 */
	public ResultSet() {
		this.list = new ArrayList<Result>();
	}

	/**
	 * �N�G�������Z�b�g����
	 */
	public void setQueryName(String val) {
		this.qname = val;
	}

	/**
	 * �������ʏ���ǉ�����
	 */
	public void addInfo(Result val) {
		this.list.add(val);
	}

	/**
	 * �q�b�g�������R�[�h�̌������Z�b�g����
	 */
	public void setNumResults(int val) {
		this.num = val;
	}

	/**
	 * �N�G�������擾����
	 */
	public String getQueryName() {
		return this.qname;
	}

	/**
	 * �q�b�g�������R�[�h�̌������擾����
	 */
	public int getNumResults() {
		if ( this.num == -1 ) {
			return -1;
		}
		else {
			return this.list.size();
		}
	}

	/*
	 * �������ʏ������擾����
	 */
	public Result[] getResults() {
		Result[] ret = new Result[this.list.size()];
		this.list.toArray(ret);
		return ret;
	}
}
