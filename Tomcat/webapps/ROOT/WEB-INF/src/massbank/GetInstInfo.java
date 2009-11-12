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
 * INSTRUMENT�����擾����N���X
 *
 * ver 1.0.7 2009.01.08
 *
 ******************************************************************************/
package massbank;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;
import massbank.MassBankCommon;

public class GetInstInfo {
	ArrayList<String>[] instNo   = null;
	ArrayList<String>[] instType = null;
	ArrayList<String>[] instName = null;
	private int index = 0;

	/**
	 * �R���X�g���N�^
	 */
	public GetInstInfo( String baseUrl ) {
		GetConfig conf = new GetConfig(baseUrl);
		String[] urlList = conf.getSiteUrl();

		// DB���INSTRUMENT�����擾����(CGI���s)
		String serverUrl = conf.getServerUrl();
		MassBankCommon mbcommon = new MassBankCommon();
		String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_INST];
		ArrayList<String> resultAll = mbcommon.execMultiDispatcher( serverUrl, typeName, "" );
		instNo = new ArrayList[urlList.length];
		instType = new ArrayList[urlList.length];
		instName = new ArrayList[urlList.length];
		for ( int i = 0; i < urlList.length; i++ ) {
			instNo[i]   = new ArrayList();
			instType[i] = new ArrayList();
			instName[i] = new ArrayList();
		}
		for ( int i = 0; i < resultAll.size(); i++ ) {
			String line = resultAll.get(i);
			String[] item = line.split("\t");
			int siteNo = Integer.parseInt( item[item.length-1] );
			instNo[siteNo].add( item[0] );
			instType[siteNo].add( item[1] );
			instName[siteNo].add( item[2] );
		}
	}

	/**
	 * �C���f�b�N�X���Z�b�g
	 */ 
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * INSTRUMENT_TYPE_NO���擾
	 */ 
	public String[] getNo() {
		return (String[])this.instNo[this.index].toArray( new String[0] );
	}

	/**
	 * INSTRUMENT_NAME���擾
	 */
	public String[] getName() {
		return (String[])this.instName[this.index].toArray( new String[0] );
	}

	/**
	 * INSTRUMENT_TYPE���d���Ȃ��Ŏ擾
	 */
	public String[] getType() {
		return (String[])this.instType[this.index].toArray( new String[0] );
	}

	/**
	 * INSTRUMENT_TYPE���擾
	 */
	public String[] getTypeAll() {
		ArrayList<String> instTypeList = new ArrayList();
		// �d�����Ȃ����̂��i�[
		for ( int i = 0; i < this.instType.length; i++ ) {
			for ( int j = 0; j < instType[i].size(); j++ ) {
				String type = instType[i].get(j);
				boolean isFind = false;
				for ( int k = 0; k < instTypeList.size(); k++ ) {
					if ( instTypeList.get(k).equals(type) ) {
						isFind = true;
						break;
					}
				}
				if ( !isFind ) {
					instTypeList.add( type );
				}
			}
		}
		// ���O���Ń\�[�g
		Collections.sort( instTypeList );
		return (String[])instTypeList.toArray( new String[0] );
	}

	/**
	 * INSTRUMENT_TYPE�̃O���[�v�����擾
	 */
	public Map<String, List<String>> getTypeGroup() {
		final String[] ionization = { "ESI", "EI", "Others" };

		String[] instTypes = getTypeAll();
		int num = ionization.length;
		List<String>[] listInstType = new ArrayList[num];
		for ( int i = 0; i < num; i++ ) {
			listInstType[i] = new ArrayList();
		}
		for ( int j = 0; j < instTypes.length; j++ ) {
			String val = instTypes[j];
			boolean isFound = false;
			for ( int i = 0; i < num; i++ ) {
				if ( val.indexOf(ionization[i]) >= 0 ) {
					listInstType[i].add(val);
					isFound = true;
					break;
				}
			}
			if ( !isFound ) {
				listInstType[num - 1].add(val);
			}
		}

		Map<String, List<String>> group = new TreeMap();
		for ( int i = 0; i < num; i++ ) {
			if ( listInstType[i].size() > 0 ) {
				group.put( ionization[i], listInstType[i] );
			}
		}
		return group;
	}
}
