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
 * [WEB-API] MultiDispatcher �T�[�u���b�g���Ăяo���N���X
 *
 * ver 1.0.1 2009.08.12
 *
 ******************************************************************************/
package massbank.api;

import java.util.ArrayList;
import java.util.Collections;
import org.apache.axis2.AxisFault;
import massbank.MassBankCommon;
import massbank.GetConfig;


public class DispatchInvoker {
	private String serverUrl = "";
	private ArrayList<String> response = null;
	private String typeName = "";

	/**
	 * �R���X�g���N�^
	 */
	public DispatchInvoker() {
		GetConfig conf = new GetConfig(MassBankAPI.BASE_URL);
		this.serverUrl = conf.getServerUrl();
	}

	/**
	 * MultiDispatcher�̌Ăяo��
	 */
	public void invoke( String typeName, String param ) {
		MassBankCommon mbcommon = new MassBankCommon();
//		System.out.println( "typeName:" + typeName  + "/" + param  + "/" + this.serverUrl );
		this.response = mbcommon.execMultiDispatcher( this.serverUrl, typeName, param );
		this.typeName = typeName;
	}

	/**
	 * ���X�|���X���擾����
	 */
	public ArrayList<String> getResponse() {
		return this.response;
	}


	/**
	 * �������ʂ��擾����
	 */
	public SearchResult getSearchResult( int maxNumResults ) {
		ArrayList<String> ret = this.response;
		Collections.sort(ret);	// �\�[�g����
		SearchResult result = new SearchResult();
		int hitCnt = ret.size();
		if ( hitCnt > 0 ) {
			if ( maxNumResults > 0 && hitCnt > maxNumResults ) {
				hitCnt = maxNumResults;
			}
			for ( int i = 0; i < hitCnt; i++ ) {
				String line = ret.get(i);
				String[] fields = line.split("\t");
				String id       = "";
				String name     = "";
				String formula  = "";
				String emass    = "";
				String hitScore = "";
				String score    = "";

				if ( this.typeName.equals(MassBankCommon.REQ_TYPE_SEARCH) ) {
					id       = fields[0];
					name     = fields[1];
					formula  = fields[2];
					emass    = fields[3];
					hitScore = fields[4];
					int pos = hitScore.indexOf(".");
					if ( pos > 0 ) {
						score = "0" + hitScore.substring(pos);
					}
					else {
						score = "0";
					}
				}
				else {
					name     = fields[0];
					id       = fields[1];
					formula  = fields[3];
					emass    = fields[4];
				}

				//---------------------------------------
				// �������ʂ��Z�b�g����
				//---------------------------------------
				Result info = new Result();
				info.setId(id);
				info.setTitle(name);
				info.setFormula(formula);
				info.setExactMass(emass);
				info.setScore(score);
				result.addInfo(info);
			}
		}
		return result;
	}
}
