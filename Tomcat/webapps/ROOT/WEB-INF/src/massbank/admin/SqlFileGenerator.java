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
 * DB�o�^�p��SQL�𐶐�����N���X
 *
 * ver 1.0.15 2011.10.04
 *
 ******************************************************************************/
package massbank.admin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import massbank.GetConfig;
import massbank.GetInstInfo;
import massbank.MassBankEnv;

public class SqlFileGenerator {

	private static final int TABLE_RECORD     = 0;
	private static final int TABLE_CH_NAME    = 1;
	private static final int TABLE_CH_LINK    = 2;
	private static final int TABLE_INSTRUMENT = 3;

	private int ver = 2;
	private String[] instNo = null;
	private String[] instName = null;
	private String[] instType = null;
	private HashMap<String, Integer> instNew = null;
	private ArrayList<String> usedNoList = null;
	private String valInst = "";
	private String name = "";
	private String value = "";
	private String acc = "";
	private String nameReco = "";
	private String valReco  = "";
	private String acInst   = "";
	private String acInstType = "";
	private String acMsType ="";
	private ArrayList<String> valNames = new ArrayList<String>();
	private ArrayList<String> valLinkNames = new ArrayList<String>();
	private ArrayList<String> valLinkIds = new ArrayList<String>();
	private Hashtable<String, String> existItem = new Hashtable<String, String>();

	/**
	 * �R���X�g���N�^
	 * @param baseUrl �x�[�XURL
	 * @param selDbName DB��
	 * @param ver ���R�[�h�t�H�[�}�b�g�o�[�W����
	 */ 
	public SqlFileGenerator( String baseUrl, String selDbName, int ver) {
		
		// ���R�[�h�t�H�[�}�b�g�o�[�W�����ޔ�
		this.ver = ver;
		
		// INSTRUMENT�����擾
		GetInstInfo instInfo = new GetInstInfo(MassBankEnv.get(MassBankEnv.KEY_BASE_URL));
		GetConfig conf = new GetConfig(baseUrl);
		String[] dbNameList = conf.getDbName();
		int dbIndex = 0;
		for ( int i = 0; i < dbNameList.length; i++ ) {
			if ( dbNameList[i].equals(selDbName) ) {
				dbIndex = i;
				break;
			}
		}
		instInfo.setIndex(dbIndex);
		this.instNo = instInfo.getNo();
		this.instName = instInfo.getName();
		this.instType = instInfo.getType();
		this.instNew = new HashMap<String, Integer>();
		usedNoList = new ArrayList<String>();
		for (String no : this.instNo) {
			usedNoList.add(no);
		}
	}

	/**
	 * ���R�[�h�t�@�C���Ǎ���
	 * @param filePath ���R�[�h�t�@�C���̃p�X
	 */ 
	public void readFile( String filePath ) {
		try {
			this.valInst = "";
			this.name = "";
			this.value = "";
			this.acc = "";
			this.nameReco = "";
			this.valReco  = "";
			this.valNames.clear();
			this.valLinkNames.clear();
			this.valLinkIds.clear();
			String line = "";

			existItem.clear();

			// �t�@�C���Ǎ���
			BufferedReader in = new BufferedReader( new FileReader(filePath) );
			while ( ( line = in.readLine() ) != null ) {
				this.cutItem( line );
				if ( name.equals("ACCESSION") ) {
					this.acc = value;
				}
				//***********************************************
				// �e�[�u��:RECORD,INSTRUMENT
				//***********************************************
				else if (name.equals("CH$FORMULA")
					  || name.equals("CH$EXACT_MASS")
					  || name.equals("CH$SMILES")
					  || name.equals("CH$IUPAC")
					  || name.equals("DATE") ) {
					this.setRecord();
				}
				else if ( name.equals("AC$INSTRUMENT") ) {
					this.acInst = value;
				}
				else if ( name.equals("AC$INSTRUMENT_TYPE") ) {
					this.acInstType = value.trim();
					if ( ver == 1 ) {
						this.acMsType = "N/A";	// �t�H�[�}�b�g�o�[�W������1�̏ꍇ��MS_TYPE���K�{���ڂł͂Ȃ����ߕK��N/A�Ƃ���
						this.setRecord();
					}
				}
				else if ( name.equals("AC$MASS_SPECTROMETRY") && value.startsWith("MS_TYPE") && ver != 1 ) {
					this.acMsType = value.replaceAll("MS_TYPE", "").trim();
					this.setRecord();
				}
				//***********************************************
				// �e�[�u��:CH_NAME
				//***********************************************
				else if ( name.equals("CH$NAME") ) {
					this.setChName();
				}
				//***********************************************
				// �e�[�u��:CH_LINK
				//***********************************************
				else if ( name.equals("CH$LINK") ) {
					this.setChLink();
				}
			}
			in.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * �e���L���`�F�b�N
	 * @param type ���R�[�h�t�@�C���̃p�X
	 * @return true:����/false:�Ȃ�
	 */ 
	public boolean isExist( int type ) {
		boolean ret = true;
		switch ( type ) {
		case TABLE_RECORD:
			if ( nameReco.equals("") )  { ret = false; } break;
		case TABLE_CH_NAME:
			if ( valNames.size() == 0 ) { ret = false; } break;
		case TABLE_CH_LINK:
			if ( valLinkNames.size() == 0 ) { ret = false; } break;
		case TABLE_INSTRUMENT:
			if ( valInst.equals("") )  { ret = false; } break;
		default: break;
		}
		return ret;
	}

	/**
	 * ��������SQL�����擾
	 * @param type ���
	 * @return SQL��
	 */ 
	public String getSql( int type ) {
		String sql = "";
		switch ( type ) {
		case TABLE_RECORD:
			sql = "INSERT INTO RECORD(ID" + nameReco + ") VALUES('"
					+ acc + "'" + valReco + ");";
			break;
		case TABLE_CH_NAME:
			for ( int i = 0; i < valNames.size(); i++ ) {
				sql += "INSERT INTO CH_NAME VALUES('" + acc + "', '" + valNames.get(i) + "');";
				if ( i < valNames.size() - 1 ) {
					sql += "\n";
				}
			}
			break;
		case TABLE_CH_LINK:
			for ( int i = 0; i < valLinkNames.size(); i++ ) {
				sql += "INSERT INTO CH_LINK VALUES('" + acc + "', '" + valLinkNames.get(i) + "', '" + valLinkIds.get(i) + "');";
				if ( i < valLinkNames.size() - 1 ) {
					sql += "\n";
				}
			}
			break;
		case TABLE_INSTRUMENT:
			sql = "INSERT INTO INSTRUMENT(INSTRUMENT_NO, INSTRUMENT_TYPE, INSTRUMENT_NAME) VALUES(" + valInst + ");";
			break;
		default:
			break;
		}
		return sql;
	}

	/**
	 * RECORD,INSTRUMENT�e�[�u��SQL���Z�b�g
	 */ 
	private void setRecord() {
		if ( name.equals("CH$FORMULA")
	 	  || name.equals("CH$EXACT_MASS")
	 	  || name.equals("CH$SMILES")
	 	  || name.equals("CH$IUPAC")  ) {
			String exist = (String)this.existItem.get(name);
			if ( exist == null ) {
				existItem.put(name, "true");
			}
			else {
				return;
			}
			nameReco += ", " + name.substring(3);
			valReco += ", ";
			if ( name.equals("CH$EXACT_MASS") ) {
				try {
					double emass = Double.parseDouble(value);
					if ( emass > 0.0d ) {
						valReco += value;
					}
					else {
						valReco += "0";
					}
				}
				catch ( NumberFormatException e ) {
					valReco += "0";
				}
			}
			else {
				valReco += "'" + value + "'";
			}
		}
		else if ( name.equals("DATE") ) {
			nameReco += ", " + name;
			valReco += ", '" + value + "'";
		}
		else if ( (ver == 1 && name.equals("AC$INSTRUMENT_TYPE"))
				 || ver != 1 && name.equals("AC$MASS_SPECTROMETRY") && value.startsWith("MS_TYPE") ) {
			
			boolean isFound = false;
			int i = 0;
			for ( i = 0; i < this.instNo.length; i++ ) {
				if (  this.acInstType.equals(this.instType[i]) && this.acInst.equals(this.instName[i]) ) {
					isFound = true;
					break;
				}
			}
			if ( isFound ) {
				// DB�ɓo�^�ς�INSTRUMENT�̏ꍇ
				valReco += ", " + this.instNo[i];
			}
			else {
				// DB�ɖ��o�^INSTRUMENT�̏ꍇ
				int instNo = 1;
				String keyStr = this.acInstType + "\t" + this.acInst;
				if ( this.instNew.containsKey(keyStr) ) {
					// ���ɐV�K�o�^�������s���Ă���INSTRUMENT�̏ꍇ
					instNo = this.instNew.get(keyStr);
				}
				else {
					// �܂��V�K�o�^�������s���Ă��Ȃ�INSTRUMENT�̏ꍇ
					for (int tmpNo=instNo; tmpNo<Integer.MAX_VALUE; tmpNo++) {
						if (!usedNoList.contains(String.valueOf(tmpNo))) {
							usedNoList.add(String.valueOf(tmpNo));
							instNo = tmpNo;
							break;
						}
					}
					this.instNew.put(keyStr, instNo);
					valInst = "'" + instNo + "', '" + this.acInstType + "', '" + this.acInst + "'";
				}
				valReco += ", " + String.valueOf(instNo);
			}
			nameReco += ", INSTRUMENT_NO";
			
			nameReco += ", MS_TYPE";
			valReco += ", '" + this.acMsType + "'";	
		}
	}

	/**
	 * CH_NAME�e�[�u��SQL���Z�b�g
	 */ 
	private void setChName() {
		value = value.trim().replaceAll( "'", "\\\\'" );
		valNames.add( value );
	}

	/**
	 * CH_LINK�e�[�u��SQL���Z�b�g
	 */ 
	private void setChLink() {
		int pos = value.trim().indexOf(" ");
		if ( pos > 0 ) {
			value = value.trim().replaceAll( "'", "\\\\'" );
			valLinkNames.add( value.substring( 0, pos ) );
			valLinkIds.add( value.substring( pos + 1 ) );
		}
	}

	/**
	 * ���ږ��ƒl�̕���
	 * @param line �����Ώە�����
	 */ 
	private void cutItem( String line ) {
		this.name = "";
		this.value = "";
		int pos = line.indexOf(":");
		if ( pos >= 0 ) {
			this.name = line.substring( 0, pos );
			if ( line.length() > pos + 2 ) {
				this.value = line.substring( pos + 2 );
			}
		}
	}
}
