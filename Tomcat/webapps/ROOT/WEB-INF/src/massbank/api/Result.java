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
 * [WEB-API] �������ʏڍ׊i�[�f�[�^�N���X
 *
 * ver 1.0.1 2010.04.15
 *
 ******************************************************************************/
package massbank.api;

import java.util.ArrayList;
import java.util.List;

public class Result {

	private String id = "";
	private String title = "";
	private String formula = "";
	private String exactMass = "";
	private String score = "";

	/**
	 * �R���X�g���N�^
	 */
	public Result() {
	}

	//--  setter���\�b�h ----------------------------------
	/**
	 * ID���Z�b�g����
	 */
	public void setId(String val) {
		this.id = val;
	}
	/**
	 * ���R�[�h�^�C�g�����Z�b�g����
	 */
	public void setTitle(String val) {
		this.title = val;
	}
	/**
	 * ���q�����Z�b�g����
	 */
	public void setFormula(String val) {
		this.formula = val;
	}
	/**
	 * �������ʂ��Z�b�g����
	 */
	public void setExactMass(String val) {
		this.exactMass = val;
	}
	/**
	 * �X�R�A���Z�b�g����
	 */
	public void setScore(String val) {
		this.score = "0";
		int pos = val.indexOf(".");
		if ( pos > 0 ) {
			this.score += val.substring(pos);
		}
	}

	//--  getter���\�b�h ----------------------------------
	/**
	 * ID���擾����
	 */
	public String getId() {
		return this.id;
	}
	/**
	 * ���R�[�h�^�C�g�����擾����
	 */
	public String getTitle() {
		return this.title;
	}
	/**
	 * ���q�����擾����
	 */
	public String getFormula() {
		return this.formula;
	}
	/**
	 * �������ʂ��擾����
	 */
	public String getExactMass() {
		return this.exactMass;
	}
	/**
	 * �X�R�A���擾����
	 */
	public String getScore() {
		return this.score;
	}
}
