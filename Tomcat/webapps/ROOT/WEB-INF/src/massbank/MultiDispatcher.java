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
 * CGI���}���`�X���b�h�ŋN������T�[�u���b�g
 *
 * ver 1.0.14 2011.05.30
 *
 ******************************************************************************/
package massbank;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class MultiDispatcher extends HttpServlet {
	
	private final String PROG_NAME = MassBankCommon.MULTI_DISPATCHER_NAME;

	/**
	 * HTTP���N�G�X�g����
	 */
	public void service(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {

		ServletContext context = getServletContext();
		PrintWriter out = res.getWriter();
		String msg = "";

		//---------------------------------------------------
		// �x�[�XURL�Z�b�g
		//---------------------------------------------------
		String path = req.getRequestURL().toString();
		int pos = path.indexOf( MassBankCommon.MULTI_DISPATCHER_NAME );
		String baseUrl = path.substring( 0, pos );

		//---------------------------------------------------
		// ���ݒ�t�@�C�����擾
		//---------------------------------------------------
		GetConfig conf = new GetConfig(baseUrl);
		boolean isTrace = conf.isTraceEnable();

		//---------------------------------------------------
		// ���N�G�X�g�p�����[�^�擾
		//---------------------------------------------------
		String type = "";
		Hashtable<String, Object> params = new Hashtable<String, Object>();
		Enumeration names = req.getParameterNames();
		while ( names.hasMoreElements() ) {
			String key = (String)names.nextElement();
			String val = req.getParameter( key );
			if ( key.equals("type") ) {
				params.put( key, val );
				type = val;
			}
			else if ( !key.equals("inst") && !key.equals("ms") ) {
				// �L�[��InstrumentType,MSType�ȊO�̏ꍇ��String�p�����[�^
				params.put( key, val );
			}
			else {
				// �L�[��InstrumentType,MSType�̏ꍇ��String�z��p�����[�^
				String[] vals = req.getParameterValues( key );
				params.put( key, vals );
			}
		}

		int typeNum = -1;
		for ( int i = 0; i < MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE].length; i++ ) {
			if ( type.equals( MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][i] ) ) {
				typeNum = i;
				break;
			}
		}
		if ( typeNum == -1 ) {
			// �G���[
			msg = "�p�����[�^type�s�� " + type;
			MassBankLog.ErrorLog( PROG_NAME, msg, context );
			return;
		}

		//---------------------------------------------------
		// �T�[�o�E�X�e�[�^�X���擾
		// (�T�[�o�Ď����s���Ă��Ȃ���Ζ������ŕԂ��Ă���)
		//---------------------------------------------------
		ServerStatus svrStatus = new ServerStatus(baseUrl);
		
		//---------------------------------------------------
		// �f�B�X�p�b�`����
		//---------------------------------------------------
		Dispatcher disp = new Dispatcher(context, conf, isTrace, svrStatus, typeNum, params);
		disp.dispatch();

		//---------------------------------------------------
		// ���ʎ擾
		//---------------------------------------------------
		if ( typeNum == MassBankCommon.CGI_TBL_TYPE_SEARCH ) {
			out.println( disp.getSortedResult() );
		}
		else {
			out.println( disp.getResult() );
		}
		out.close();

		msg = "�I��";
		MassBankLog.TraceLog( PROG_NAME, msg, context, isTrace );
	}


	/**
	 * �A�g�T�[�o�Ƀf�B�X�p�b�`���邽�߂̃N���X
	 */
	class Dispatcher {
		
		private ServletContext context = null;
		private List<RequestInfo> reqInfoList = null;
		private CallCgi[] thread = null;
		private int typeNum = -1;
		private ServerStatus svrStatus = null;
		private boolean isTrace = false;
		private int timeout = 0;
		private String[] urlList = null;
		private String[] dbNameList = null;
		private Hashtable<String, Object> params = null;
		
		/**
		 * �R���X�g���N�^
		 * @param context
		 * @param conf
		 * @param isTrace
		 * @param svrStatus
		 * @param typeNum
		 * @param params
		 */
		public Dispatcher(ServletContext context, GetConfig conf, boolean isTrace, 
				ServerStatus svrStatus, int typeNum, Hashtable<String, Object> params) {
			
			this.context = context;
			this.isTrace = isTrace;
			this.timeout = conf.getTimeout();
			this.urlList = conf.getSiteUrl();
			this.dbNameList = conf.getDbName();
			this.svrStatus = svrStatus;
			this.typeNum = typeNum;
			this.params = params;
		}
		
		/**
		 * �f�B�X�p�b�`����
		 */
		public void dispatch() {
			final int MYSVR_INFO_NUM = 0;

			//---------------------------------------------------
			// CGI�p��JSP�p�̃p�����[�^���Z�b�g
			//---------------------------------------------------
			Hashtable<String, Object> cgiParams = null;
			Hashtable<String, Object> jspParams = null;
			if ( params == null ) {
				cgiParams = new Hashtable<String, Object>();
				jspParams = new Hashtable<String, Object>();
			}
			else {
				cgiParams = new Hashtable<String, Object>(params);
				jspParams = new Hashtable<String, Object>(params);
			}
			// Peak Search, Peak Diff Search�ȊO�̏ꍇ�Atype�L�[���폜����
			if ( typeNum != MassBankCommon.CGI_TBL_TYPE_PEAK
			  && typeNum != MassBankCommon.CGI_TBL_TYPE_PDIFF ) {
				cgiParams.remove( "type" );
			}

			//---------------------------------------------------
			// URL�ƃp�����[�^���Z�b�g
			//---------------------------------------------------
			reqInfoList = new ArrayList<RequestInfo>();
			for ( int i = 0; i < urlList.length; i++ ) {
				String url = urlList[i];
				String dbName = dbNameList[i];

				// �A�g�T�[�o���A�N�e�B�u�ł͂Ȃ��ꍇ�̓X�L�b�v����
				// �T�[�o�Ď����s���Ă��Ȃ���Ζ�������True���Ԃ��Ă���
				if ( !svrStatus.isServerActive(url, dbName) ) {
					continue;
				}

				String reqUrl = url;
				Hashtable<String, Object> reqParams = null;

				//** ���T�[�o�[�̏ꍇ�Acgi�փA�N�Z�X **
				if ( i == MYSVR_INFO_NUM ) {
					reqUrl += "cgi-bin/" + MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_FILE][typeNum];
					reqParams = new Hashtable<String, Object>(cgiParams);
				}
				//** �A�g�T�[�o�[�̏ꍇ�ADispatcher.jsp����ăA�N�Z�X **
				else {
					reqUrl += "jsp/" + MassBankCommon.DISPATCHER_NAME;
					reqParams = new Hashtable<String, Object>(jspParams);
				}
				reqParams.put( "dsn", dbName );

				// �f�[�^�N���X��URL, �p�����[�^, siteNo���Z�b�g
				RequestInfo reqInfo = new RequestInfo( reqUrl, reqParams, i );
				reqInfoList.add(reqInfo);
			}

			//---------------------------------------------------
			// �X���b�h�N��
			//---------------------------------------------------
			this.thread = new CallCgi[this.reqInfoList.size()];
			for ( int i = 0; i < this.reqInfoList.size(); i++ ) {
				RequestInfo reqInfo = this.reqInfoList.get(i);
				String reqUrl = reqInfo.getUrl();
				Hashtable<String, Object> reqParams = reqInfo.getParam();

				// ���O�o��
				String param = "";
				for ( Enumeration<String> keys = reqParams.keys(); keys.hasMoreElements(); ) {
					String key = (String)keys.nextElement();
					if ( !key.equals("inst") && !key.equals("ms") ) {
						// �L�[��InstrumentType,MSType�ȊO�̏ꍇ��String�p�����[�^
						String val = (String)reqParams.get(key);
						param += key + "=" + val + "&";
					}
					else {
						// �L�[��InstrumentType,MSType�̏ꍇ��String�z��p�����[�^
						String[] vals = (String[])reqParams.get(key);
						for ( int j = 0; j < vals.length; j++ ) {
							param += key + "=" + vals[j] + "&";
						}
					}
				}
				param = param.substring( 0, param.length()-1 );
				String msg = "Call(" + Integer.toString(i+1) + ") : " + reqUrl + "?" + param;
				MassBankLog.TraceLog( PROG_NAME, msg, context, isTrace );

				// �N��
				this.thread[i] = new CallCgi( reqUrl, reqParams, timeout, context );
				this.thread[i].start();
			}

			//-------------------------------------------
			// �X���b�h�I���҂�
			//-------------------------------------------
			// HttpClient���Ń^�C���A�E�g��ݒ肵�Ă���̂Ŗ����ł��悢
			long until = System.currentTimeMillis() + timeout * 1000;
			boolean isRunning = true;
			while ( isRunning && System.currentTimeMillis() < until ) {
				isRunning = false;
				for ( int i = 0; i < this.thread.length; i++ ) {
					try {
						if ( this.thread[i].isAlive() ) {
							//** �X���b�h�I������܂őҋ@ **
							this.thread[i].join(500);
							isRunning = true;
						}
					}
					catch ( Exception e ) {
						// �G���[
						String msg = e.toString();
						MassBankLog.ErrorLog( PROG_NAME, msg, context );
					}
				}
			}
		}

		/**
		 * �X�R�A���Ń\�[�g���ꂽ���ʂ��擾����
		 * @return ����
		 */
		private String getSortedResult() {
			//-------------------------------------------
			// ���s���ʂ����X�g�Ɋi�[
			//-------------------------------------------
			StringBuffer res = new StringBuffer("");
			ArrayList<String> result = new ArrayList<String>();
			ArrayList<String> scoreList = new ArrayList<String>();
			for ( int i = 0; i < this.thread.length; i++ ) {
				if ( this.thread[i].result.length() == 0 ) {
					continue;
				}
				String[] lines = this.thread[i].result.replaceAll("\r","").split("\n");
				for ( int j = 0; j < lines.length; j++ ) {
					// Internal Server Error�̏ꍇ
					if ( lines[j].indexOf("<!") >= 0 ) {
						break;
					}

					// Site No �t��
					result.add( lines[j] + "\t" + this.reqInfoList.get(i).getSiteNo() );
				}
			}

			//-------------------------------------------
			// �X�R�A���X�g���쐬
			//-------------------------------------------
			String line = "";
			String[] item;
			for ( int i = 0; i < result.size(); i++ ){
				line = (String) result.get(i);
				item = line.split("\t");
				int pos1 = item[2].indexOf(".");
				String score = "";
				if ( pos1 > 0 ) { 
					score = "0" + item[2].substring(pos1);
				}
				else {
					score = "0";
				}
				scoreList.add( score + "\t" + Integer.toString(i) );
			}
			//-------------------------------------------
			// �X�R�A���X�g���\�[�g
			//-------------------------------------------
			Collections.sort(scoreList, new ScoreComparator());

			//-------------------------------------------
			// �X�R�A���Ō��ʂ�Ԃ�
			//-------------------------------------------
			for ( int i = 0; i < result.size(); i++ ) {
				line = (String)scoreList.get(i);
				item = line.split("\t");
				int no = Integer.parseInt(item[1]);
				line = (String)result.get(no);
				item = line.split("\t");
				String id    = item[0];
				String name  = item[1];
				String score = item[2];
				String ion   = item[3];
				// Quick Search by Peak�̌��ʂ̏ꍇ
				if ( item.length == 6 ) {
					String formula = item[4];
					String site = item[5];
					res.append( name + "\t" + id + "\t" + ion + "\t" + formula + "\t" + score + "\t" + site + "\n" );
				}
				// API�̏ꍇ
				else if ( item.length == 7 ) {
					String formula = item[4];
					String emass = item[5];
					String site = item[6];
					res.append( id + "\t" + name + "\t" + formula + "\t" + emass + "\t" + score + "\t" + site + "\n" );
				}
				// Nist Search�̌��ʂ̏ꍇ
				else {
					String site = item[4];
					res.append( id + "\t" + name + "\t" + score + "\t" + ion + "\t" + site + "\n" );
				}
			}
			return res.toString();
		}

		/**
		 * �ʏ�̃��X�|���X
		 * @return ����
		 */
		private String getResult() {
			StringBuffer res = new StringBuffer("");
			for ( int i = 0; i < this.thread.length; i++ ) {
				if ( this.thread[i].result.length() == 0 ) {
					continue;
				}
				String[] lines = this.thread[i].result.replaceAll("\r","").split("\n");
				for ( int j = 0; j < lines.length; j++ ) {
					// Internal Server Error�̏ꍇ
					if ( lines[j].indexOf("<!") >= 0 ) {
						break;
					}

					// Site No �t��
					res.append( lines[j] + "\t" + this.reqInfoList.get(i).getSiteNo() + "\n" );
				}
			}
			return res.toString();
		}
	}

	/**
	 * ���N�G�X�g���f�[�^�N���X
	 */
	class RequestInfo {
		private String url = "";
		private Hashtable<String, Object> param = null;
		private int siteNo = 0;

		/**
		 * �R���X�g���N�^
		 */
		public RequestInfo(String url, Hashtable<String, Object> param, int siteNo) {
			this.url = url;
			this.param = param;
			this.siteNo = siteNo;
		}
		/**
		 * URL�擾
		 */
		public String getUrl() {
			return this.url;
		}
		/**
		 * �p�����[�^�擾
		 */
		public Hashtable<String, Object> getParam() {
			return this.param;
		}
		/**
		 * �T�C�gNo�擾
		 */
		public String getSiteNo() {
			return String.valueOf(this.siteNo);
		}
	}

	/**
	 * Score���Ń\�[�g���邽�߂�Comparator
	 */
	public class ScoreComparator implements Comparator<Object> {
		public int compare(Object o1, Object o2){
			String[] val1 = ((String) o1).split("\t");
			String[] val2 = ((String) o2).split("\t");
			int ret = 0;
			if ( Float.parseFloat(val1[0]) < Float.parseFloat(val2[0]) ) {
				ret = 1;
			}
			else if ( Float.parseFloat(val1[0]) > Float.parseFloat(val2[0]) ) {
				ret = -1;
			}
			return ret;
		}
	}
}
