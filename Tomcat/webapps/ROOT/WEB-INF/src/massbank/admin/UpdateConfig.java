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
 * ���ݒ�t�@�C���̏����X�V����N���X
 *
 * ver 1.0.3 2010.11.25
 *
 ******************************************************************************/
package massbank.admin;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import massbank.MassBankEnv;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UpdateConfig {

	private final int MYSVR_INFO_NUM = 0;
	private Document doc;
	private String confPath;
	
	/**
	 * �f�t�H���g�R���X�g���N�^
	 */
	public UpdateConfig() {
		String confUrl = MassBankEnv.get(MassBankEnv.KEY_MASSBANK_CONF_URL);
		this.confPath = MassBankEnv.get(MassBankEnv.KEY_MASSBANK_CONF_PATH);
		try {
			// �h�L�������g�r���_�[�t�@�N�g���𐶐�
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();

			// �h�L�������g�r���_�[�𐶐�
			DocumentBuilder builder = dbfactory.newDocumentBuilder();

			// �p�[�X�����s����Document�I�u�W�F�N�g���擾
			this.doc = builder.parse( confUrl );
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �ǉ�
	 * �A�g�T�C�g�̐ݒ�̂ݒǉ���������
	 * @param siteNo �T�C�g�ԍ�
	 * @param name �T�C�g����
	 * @param longName �����O�T�C�g����
	 * @param url �T�[�oURL
	 * @param db DB����
	 * @return ����
	 */
	public boolean addConfig(int siteNo, String name, String longName, String url, String db) {
		if ( siteNo == MYSVR_INFO_NUM ) {
			return false;
		}
		addRelatedSetting(name, longName, url, db);
		
		saveConf();
		return true;
	}
	
	/**
	 * �ҏW
	 * @param siteNo �T�C�g�ԍ�
	 * @param internalSiteList �����T�C�g�ԍ����X�g
	 * @param name �T�C�g����
	 * @param longName �����O�T�C�g����
	 * @param url �T�[�oURL
	 * @param db DB����
	 * @return ����
	 */
	public boolean editConfig(int siteNo, ArrayList<Integer> internalSiteList, String name, String longName, String url, String db) {
		setSetting("Name", siteNo, name);
		setSetting("LongName", siteNo, longName);
		if ( siteNo == MYSVR_INFO_NUM ) {
			setSetting("FrontServer", siteNo, url);
			for (Integer internalSiteNo : internalSiteList) {
				if (internalSiteNo != MYSVR_INFO_NUM) {
					setSetting("URL", internalSiteNo, url);
				}
			}
			// BaseUrl �X�V
			MassBankEnv.setBaseUrl(url);
		}
		else {
			setSetting("URL", siteNo, url);
		}
		if ( db != null && !db.equals("") ) {
			setSetting("DB", siteNo, db);
		}
		
		saveConf();
		return true;
	}
	
	/**
	 * �폜
	 * �A�g�T�C�g�̐ݒ�̂ݍ폜��������
	 * @param �T�C�g�ԍ�
	 * @return ����
	 */
	public boolean delConfig(int siteNo) {
		if ( siteNo == MYSVR_INFO_NUM ) {
			return false;
		}
		delRelatedSetting(siteNo);
		
		saveConf();
		return true;
	}
	
	/**
	 * �ҏW���ʏ���
	 */
	private boolean setSetting( String tagName, int siteNo, String value ) {
		boolean ret = false;
		
		// �ݒ菈��
		if (siteNo == MYSVR_INFO_NUM) {
			ret = setServerSetting(tagName, siteNo, value);
		}
		else {
			ret = setRelatedSetting(tagName, siteNo, value);
		}
		return ret;
	}
	
	/**
	 * ���T�[�o�[�̐ݒ��ҏW����
	 */
	private boolean setServerSetting(String tagName, int siteNo, String value) {
		try {
			NodeList nodeList = doc.getDocumentElement().getElementsByTagName( "MyServer" );
			if ( nodeList == null ) {
				return false;
			}
			Element child = (Element)nodeList.item(0);
			NodeList childNodeList = child.getElementsByTagName( tagName );
			Element child2 = (Element)childNodeList.item(0);
			if ( child2 != null ) {
				if ( tagName.equals("FrontServer") || tagName.equals("MiddleServer") ) {
					child2.setAttribute("URL", value);
					return true;
				}
				else {
					Node node = child2.getFirstChild();
					if ( node != null ) {
						node.setNodeValue(value);
						return true;
					}
				}
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * �A�g�T�C�g�̐ݒ��ҏW����
	 */
	private boolean setRelatedSetting(String tagName, int siteNo, String value) {
		boolean ret = false;
		try {
			NodeList nodeList = doc.getDocumentElement().getElementsByTagName( "Related" );
			if ( nodeList == null ) {
				return ret;
			}
			for ( int i=0; i<nodeList.getLength(); i++ ) {
				if ( siteNo == (i+1) ) {
					Element child = (Element)nodeList.item(i);
					NodeList childNodeList = child.getElementsByTagName( tagName );
					Element child2 = (Element)childNodeList.item(0);
					if ( child2 == null ) {
						continue;
					}
					Node node = child2.getFirstChild();
					if ( node == null ) {
						continue;
					}
					node.setNodeValue(value);
					ret = true;
					break;
				}
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return ret;
	}
	
	
	/**
	 * �A�g�T�C�g�̐ݒ��ǉ�����
	 * @param name �T�C�g����
	 * @param longName �����O�T�C�g����
	 * @param url �T�[�oURL
	 * @param db DB����
	 * @return ����
	 */
	private boolean addRelatedSetting(String name, String longName, String url, String db) {
		boolean ret = false;
		try {
			// �m�[�h�ǉ�����
			Node root = doc.getDocumentElement();
			if (root.getNodeType() == Node.ELEMENT_NODE ) {
			
				Node nameNode = doc.createElement("Name");
				nameNode.appendChild(doc.createTextNode(name));
				
				Node longNameNode = doc.createElement("LongName");
				longNameNode.appendChild(doc.createTextNode(longName));
				
				Node urlNode = doc.createElement("URL");
				urlNode.appendChild(doc.createTextNode(url));
				
				Node dbNode = doc.createElement("DB");
				dbNode.appendChild(doc.createTextNode(db));
				
				Node browseModeNode = doc.createElement("BrowseMode");
				browseModeNode.appendChild(doc.createTextNode("3,5"));
				
				Node siteNode = doc.createElement("Related");
				siteNode.appendChild(nameNode);
				siteNode.appendChild(longNameNode);
				siteNode.appendChild(urlNode);
				siteNode.appendChild(dbNode);
				siteNode.appendChild(browseModeNode);
				
				// Timeout �^�O�̑O�Ƀm�[�h��ǉ�
				NodeList tmpNodeList = ((Element)root).getElementsByTagName("Timeout");
				if ( tmpNodeList.getLength() > 0 ) {
					root.insertBefore((Node)siteNode, tmpNodeList.item(0));
				}
				else {
					root.appendChild((Node)siteNode);
				}
				
				ret = true;
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * �A�g�T�C�g�̐ݒ���폜����
	 * @param siteNo �T�C�g�ԍ�
	 * @return ����
	 */
	private boolean delRelatedSetting(int siteNo) {
		boolean ret = false;
		try {
			Node root = doc.getDocumentElement();
			if (root.getNodeType() == Node.ELEMENT_NODE ) {
				NodeList nodeList = doc.getDocumentElement().getElementsByTagName( "Related" );
				if ( nodeList.getLength() >= (siteNo-1) ) {
					Element child = (Element)nodeList.item(siteNo-1);
					root.removeChild(child);
					ret = true;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
    /**
     * �ݒ���̏������ݏ���
     * @return ����
     */
    private boolean saveConf(){

		Transformer tf = null;
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			tf = factory.newTransformer();
			tf.setOutputProperty("indent",   "yes");
			tf.setOutputProperty("encoding", "utf-8");
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			return false;
		}
		
		// �����o��
		try {
			tf.transform(new DOMSource( doc ), new StreamResult( new File(confPath) ) );
		} catch (TransformerException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
    }    
}
