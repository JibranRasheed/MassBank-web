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
 * [WEB-API] ���C���N���X
 *
 * ver 1.0.3 2011.08.08
 *
 ******************************************************************************/
package massbank.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import massbank.GetInstInfo;
import massbank.JobInfo;
import massbank.JobManager;
import massbank.MassBankCommon;
import massbank.MassBankEnv;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

public class MassBankAPI {

	/**
	 * �f�t�H���g�R���X�g���N�^
	 */
	public MassBankAPI() {
	}


	/**
	 * �ގ��X�y�N�g���̌������s��
	 */
	public SearchResult searchSpectrum(
			String[] mzs, String[] intensities,
			String unit, String tolerance,
			String cutoff, String[] instrumentTypes,
			String ionMode, int maxNumResults )
		throws AxisFault {

		//---------------------------------------
		// �p�����[�^�`�F�b�N
		//---------------------------------------
		HashMap<String,Object> mapParam = new HashMap<String,Object>();
		// massTypes �͋����I��all���w��
		String[] keys = { "mzs", "intensities", "unit", "tolerance", "cutoff", "instrumentTypes", "massTypes", "ionMode" };
		Object[] vals = { mzs, intensities, unit, tolerance, cutoff, instrumentTypes, new String[]{"all"}, ionMode };
		for ( int i = 0; i < keys.length; i++ ) {
			mapParam.put( keys[i], vals[i] );
		}
		ApiParameter apiParam = new ApiParameter( "searchSpectrum", mapParam );
		if ( !apiParam.check() ) {
			// �p�����[�^�s���̏ꍇ�ASOAPFault ��Ԃ�
			String errDetail = apiParam.getErrorDetail();
			throw new AxisFault( "Invalid parameter : " + errDetail );
		}

		//---------------------------------------
		// CGI�p�p�����[�^���Z�b�g����
		//---------------------------------------
		String param = apiParam.getCgiParam();
		param += "&API=true";

		//---------------------------------------
		// MultiDispatcher�̌Ăяo��
		//---------------------------------------
		DispatchInvoker inv = new DispatchInvoker();
		inv.invoke( MassBankCommon.REQ_TYPE_SEARCH, param );
		return inv.getSearchResult(maxNumResults);
	}


	/**
	 * ���͋@���ʂ��擾����
	 */
	public String[] getInstrumentTypes() {
		GetInstInfo instInfo = new GetInstInfo(MassBankEnv.get(MassBankEnv.KEY_BASE_URL));
		String[] instTypes = instInfo.getTypeAll();
		return (String[])instTypes;
	}


	/**
	 * ���R�[�h�����擾����
	 */
	public RecordInfo[] getRecordInfo(String[] ids) throws AxisFault {
		//---------------------------------------
		// CGI�p�p�����[�^���Z�b�g����
		//---------------------------------------
		ApiParameter apiParam = new ApiParameter();
		String param = apiParam.getCgiParamId(ids);
		param += "&mode=all";

		//---------------------------------------
		// MultiDispatcher�̌Ăяo��
		//---------------------------------------
		DispatchInvoker inv = new DispatchInvoker();
		inv.invoke( MassBankCommon.REQ_TYPE_GETRECORD, param );
		ArrayList<String> ret = inv.getResponse();
		ArrayList<RecordInfo> list = new ArrayList<RecordInfo>();
		RecordInfo recInfo = null;
		StringBuffer info = null;
		for ( int i = 0; i < ret.size(); i++ ) {
			String line = (String)ret.get(i);
			String[] val = line.split("\t");
			String item = val[0];
			if ( item.indexOf("ACCESSION") >= 0 ) {
				recInfo = new RecordInfo();
				info = new StringBuffer("");
				String id = item.substring(11);
				recInfo.setId(id);
			}
			else if ( item.equals("//") ){
				recInfo.setInfo(info.toString());
				list.add(recInfo);
				continue;
			}
			info.append(item + "\n");
		}

		RecordInfo[] result = null;
		int num = list.size();
		if ( num == 0 ) {
			// 1��������Ȃ��ꍇ�́ASOAPFault��Ԃ�
			throw new AxisFault("Record is not found.");
		}
		else {
			result = new RecordInfo[num];
			list.toArray(result);
			return result;
		}
	}


	/**
	 * �s�[�N�f�[�^���擾����
	 */
	public Peak[] getPeak(String[] ids) throws AxisFault {
		//---------------------------------------
		// CGI�p�p�����[�^���Z�b�g����
		//---------------------------------------
		ApiParameter apiParam = new ApiParameter();
		String param = apiParam.getCgiParamId(ids);
		param += "&mode=peak";

		//---------------------------------------
		// MultiDispatcher�̌Ăяo��
		//---------------------------------------
		DispatchInvoker inv = new DispatchInvoker();
		inv.invoke( MassBankCommon.REQ_TYPE_GETRECORD, param );
		ArrayList<String> ret = inv.getResponse();
		ArrayList<Peak> list = new ArrayList<Peak>();
		Peak peak = null;
		boolean isPeakLine = false;
		for ( int i = 0; i < ret.size(); i++ ) {
			String line = (String)ret.get(i);
			String[] val = line.split("\t");
			String item = val[0];
			if ( item.indexOf("ACCESSION") >= 0 ) {
				peak = new Peak();
				String id = item.substring(11);
				peak.setId(id);
			}
			else if ( item.equals("//") ){
				list.add(peak);
				isPeakLine = false;
				continue;
			}
			else if ( item.indexOf("PK$PEAK") >= 0 ){
				isPeakLine = true;
			}
			else {
				if ( isPeakLine ) {
					item = item.trim();
					String[] peakVals = item.split(" ");
					String mz = peakVals[0];
					String inte = peakVals[1];
					peak.addPeak(mz, inte);
				}
			}
		}

		int num = list.size();
		if ( num == 0 ) {
			// 1��������Ȃ��ꍇ�́ASOAPFault��Ԃ�
			throw new AxisFault("MassBank Record is not found.");
		}
		else {
			Peak[] result = new Peak[num];
			list.toArray(result);
			return result;
		}
	}


	/**
	 * �s�[�N�������s��
	 */
	public SearchResult searchPeak(
			String[] mzs,
			String relativeIntensity,
			String tolerance,
			String[] instrumentTypes,
			String ionMode,
			int maxNumResults )
		throws AxisFault {

		try {
			SearchResult ret = searchPeakCommon( false,
				mzs, relativeIntensity, tolerance,
				instrumentTypes, ionMode, maxNumResults
			);
			return ret;
		}
		catch (AxisFault ex) {
			throw ex;
		}
	}


	/**
	 * �s�[�N���������s��
	 */
	public SearchResult searchPeakDiff(
		String[] mzs,
		String relativeIntensity,
		String tolerance,
		String[] instrumentTypes,
		String ionMode,
		int maxNumResults ) throws AxisFault {

		try {
			SearchResult ret = searchPeakCommon( true,
				mzs, relativeIntensity, tolerance,
				instrumentTypes, ionMode, maxNumResults
			);
			return ret;
		}
		catch (AxisFault ex) {
			throw ex;
		}
	}


	/**
	 * �o�b�`�������s��
	 */
	public String execBatchJob(
		String type,
		String mailAddress,
		String[] queryStrings,
		String[] instrumentTypes,
		String ionMode ) throws AxisFault {

		String jobId = "";

		// �N�G�����e���|�����t�@�C���ɏ����o��
		String tempFileName = "";
		try {
			tempFileName = queryStrsToTempFile(queryStrings);
		}
		catch (AxisFault ex) {
			throw ex;
		}

		//---------------------------------------
		// �p�����[�^�`�F�b�N
		//---------------------------------------
		HashMap<String,Object> mapParam = new HashMap<String,Object>();
		// massTypes �͋����I��all���w��
		String[] keys = { "instrumentTypes", "massTypes", "ionMode" };
		Object[] vals = { instrumentTypes, new String[]{"all"}, ionMode };
		for ( int i = 0; i < keys.length; i++ ) {
			mapParam.put( keys[i], vals[i] );
		}
		ApiParameter apiParam = new ApiParameter( "execBatchJob", mapParam );
		if ( !apiParam.check() ) {
			// �p�����[�^�s���̏ꍇ�ASOAPFault ��Ԃ�
			String errDetail = apiParam.getErrorDetail();
			throw new AxisFault( "Invalid parameter : " + errDetail );
		}
		String param = apiParam.getCgiParam();
		if ( param.charAt(0) == '&' ) {
			param = param.substring(1);
		}

		// �W���u�����Z�b�g����
		MessageContext context = MessageContext.getCurrentMessageContext();
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
		String time = sdf.format(new Date());
		JobInfo jobInfo = new JobInfo();
		jobInfo.setSessionId( "" );
		jobInfo.setIpAddr( (String)context.getProperty(MessageContext.REMOTE_ADDR) );
		jobInfo.setMailAddr( mailAddress );
		jobInfo.setTimeStamp( time );
		jobInfo.setQueryFileName( "" );
		jobInfo.setQueryFileSize( "" );
		jobInfo.setSearchParam( param );
		jobInfo.setTempName( tempFileName );
		JobManager jobMgr = new JobManager();
		try {
			jobId = jobMgr.addJobInfo(jobInfo);
			jobMgr.end();
		}
		catch (SQLException ex) {
			throw new AxisFault( "System error! [code=100]");
		}
		return jobId;
	}


	/**
	 * �w�肵���W���uID�̏����擾����
	 */
	public JobStatus getJobStatus(String jobId) throws AxisFault {
		JobStatus jobStatus = new JobStatus();
		JobManager jobMgr = new JobManager();
		JobInfo info = null;
		try {
			info = jobMgr.getJobInfo(jobId);
		}
		catch (SQLException ex) {
			throw new AxisFault( "System error! [code=110]");
		}

		jobMgr.end();
		if ( info != null ) {
			jobStatus.setStatus( info.getStatus() );
			jobStatus.setRequestDate( info.getTimeStamp() );
		}
		else {
			// �ΏۃW���u���Ȃ��ꍇ
			throw new AxisFault( "Job Not found");
		}
		return jobStatus;
	}


	/**
	 * �w�肵���W���uID�̌��ʂ��擾���܂�
	 */
	public ResultSet[] getJobResult(String jobId) throws AxisFault {
		ResultSet[] rsets = null;
		JobManager jobMgr = new JobManager();
		JobInfo info = null;
		try {
			info = jobMgr.getJobInfo(jobId);
		}
		catch (SQLException ex) {
			throw new AxisFault( "System error! [code=120]");
		}
		jobMgr.end();
		if ( info != null ) {
			ResultSet rset = null;
			ArrayList<ResultSet> rsetList= new ArrayList<ResultSet>();
			String res = info.getResult();
			String[] lines = res.split("\n");
			int cntLine = 0;
			String hit = "";
			for ( int l = 0; l < lines.length; l++ ) {
				String line = lines[l];
				if ( !line.equals("") ) {
					switch (cntLine) {
					case 0:
						rset = new ResultSet();
						rset.setQueryName(line);
						break;
					case 1:
						hit = line;
						if ( hit.equals("-1") ) {
							Result rs = new Result();
							rset.addInfo(rs);
							rset.setNumResults(-1);
						}
						break;
					case 2:
						break;
					default:
						String[] items = line.split("\t");
						Result rs = new Result();
						rs.setId(items[0]);
						rs.setTitle(items[1]);
						rs.setFormula(items[2]);
						rs.setExactMass(items[3]);
						rs.setScore(items[4]);
						rset.addInfo(rs);
					}
					cntLine++;
				}
				else {
					if ( cntLine > 0 ) {
						rsetList.add(rset);
						cntLine = 0;
					}
				}
			}
			if ( cntLine > 0 ) {
				rsetList.add(rset);
			}
			rsets = (ResultSet[])rsetList.toArray(new ResultSet[]{});
		}
		else {
			// �ΏۃW���u���Ȃ��ꍇ
			throw new AxisFault( "Job Not found");
		}
		return rsets;
	}


	/**
	 * �s�[�N�������ʏ���
	 */
	private SearchResult searchPeakCommon (
		boolean isDiff,
		String[] mzs, 
		String relativeIntensity,
		String tolerance,
		String[] instrumentTypes,
		String ionMode,
		int maxNumResults ) throws AxisFault {

		//---------------------------------------
		// �p�����[�^�`�F�b�N
		//---------------------------------------
		HashMap<String,Object> mapParam = new HashMap<String,Object>();
		// massTypes �͋����I��all���w��
		String[] keys = { "mzs", "relativeIntensity", "tolerance", "instrumentTypes", "massTypes", "ionMode" };
		Object[] vals = { mzs, relativeIntensity, tolerance, instrumentTypes, new String[]{"all"}, ionMode };
		for ( int i = 0; i < keys.length; i++ ) {
			mapParam.put( keys[i], vals[i] );
		}
		ApiParameter apiParam = new ApiParameter( "searchPeak", mapParam );
		if ( !apiParam.check() ) {
			// �p�����[�^�s���̏ꍇ�ASOAPFault ��Ԃ�
			String errDetail = apiParam.getErrorDetail();
			throw new AxisFault( "Invalid parameter : " + errDetail );
		}
		String param = apiParam.getCgiParam();

		//---------------------------------------
		// MultiDispatcher�̌Ăяo��
		//---------------------------------------
		String typeName = MassBankCommon.REQ_TYPE_PEAK;
		if ( isDiff ) {
			typeName = MassBankCommon.REQ_TYPE_PEAKDIFF;
		}
		DispatchInvoker inv = new DispatchInvoker();
		inv.invoke( typeName, param );
		return inv.getSearchResult(maxNumResults);
	}

	/**
	 * �N�G�����e���|�����t�@�C���ɏ����o��
	 */
	private String queryStrsToTempFile(String[] queryStrings) throws AxisFault {

		// �e���|�����t�@�C���ɏ����o��
		PrintWriter writer = null;
		String tempPath = System.getProperty("java.io.tmpdir");
		String tempFileName = "";
		try {
			File fTempDir = new File(tempPath);
			File fTemp = File.createTempFile("batch", ".txt", fTempDir);
			tempFileName = fTemp.getName();
			writer = new PrintWriter(new BufferedWriter(new FileWriter(fTemp)));
		}
		catch (IOException ex) {
			throw new AxisFault("System error! [code=130]" );
		}

		for ( int i = 0; i < queryStrings.length; i++ ) {
			String q = queryStrings[i];
			String[] items = q.split(";");
			String compoundName = "";
			ArrayList<String> peaks = new ArrayList<String>();
			for ( int j = 0; j < items.length; j++ ) {
				String val = items[j].trim();
				if ( val.matches("(?i)^Name:.*") ) {
					compoundName = val.replace("(?i)^Name: *", "").trim();
				}
				else {
					val = val.replace(",", " ");
					peaks.add(val + ";");
				}
			}
			if ( peaks.size() > 0 ) {
				if ( !compoundName.equals("") ) {
					writer.println( "Name: " + compoundName );
				}
				for ( int j = 0; j < peaks.size(); j++ ) {
					writer.print( peaks.get(j) );
				}
				writer.println( System.getProperty("line.separator") );
			}
		}
		writer.flush();
		writer.close();
		return tempFileName;
	}


}
