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
 * �R�}���h���s���ʊi�[�f�[�^�N���X
 *
 * ver 1.0.1 2008.12.05
 *
 ******************************************************************************/
package massbank.admin;

import java.io.Serializable;

	public final class CmdResult implements Serializable {
		private int status = 0;
		private String stdout = "";
		private String stderr = "";

	/**
	 * �I���R�[�h��ݒ肵�܂�
	 */
	public void setStatus(int status){
		this.status = status;
	}
	/**
	 * �W���o�͂̓��e��ݒ肵�܂�
	 */
	public void setStdout(String msg){
		this.stdout = msg;
	}
	/**
	 * �G���[�o�͂̓��e��ݒ肵�܂�
	 */
	public void setStderr(String msg){
		this.stderr = msg;
	}
	/**
	 * �I���R�[�h���擾���܂�
	 */
	public int getStatus(){
		return this.status;
	}
	/**
	 * �W���o�͂̓��e���擾���܂�
	 */
	public String getStdout(){
		return this.stdout;
	}
	/**
	 * �G���[�o�͂̓��e���擾���܂�
	 */
	public String getStderr(){
		return this.stderr;
	}
}
