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
 * [WEB-API] �������ʊi�[�f�[�^�N���X
 *
 * ver 1.0.0 2009.08.19
 *
 ******************************************************************************/
package massbank.api;

import java.util.ArrayList;

public class SearchResult {

	private ArrayList<Result> list = null;

	/**
	 * �R���X�g���N�^
	 */
	public SearchResult() {
		this.list = new ArrayList<Result>();
	}

	/**
	 * �q�b�g�������R�[�h�̏���ǉ�����
	 */
	public void addInfo(Result val) {
		this.list.add(val);
	}

	/**
	 * �q�b�g�������R�[�h�̌������擾����
	 */
	public int getNumResults() {
		return this.list.size();
	}

	/**
	 * �q�b�g�������R�[�h�̏����擾����
	 */
	public Result[] getResults() {
		Result[] ret = new Result[this.list.size()];
		this.list.toArray(ret);
		return ret;
	}
}
