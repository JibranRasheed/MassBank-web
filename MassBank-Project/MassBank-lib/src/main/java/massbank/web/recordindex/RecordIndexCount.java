package massbank.web.recordindex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import massbank.GetConfig;
import massbank.web.Database;

public class RecordIndexCount {

	private Database database = null;
	
	private Connection connection = null;
	
	private HttpServletRequest request = null;
	
	private GetConfig conf = null;
	
	public RecordIndexCount(HttpServletRequest request, GetConfig conf) {
		this.database = new Database(); 
		this.connection = this.database.getConnection();
		this.request = request;
		this.conf = conf;
	}	
	
	public ArrayList<String> exec() {
		return this.idxcnt();
	}
	
	// TODO remove sql queries from within the function
		public ArrayList<String> idxcnt() {
			ArrayList<String> resList = new ArrayList<String>();
			// TODO check if this site information is still necessary for parsing reasons
//			resList.add("site\t0");
			
			PreparedStatement stmnt;
			ResultSet res;
			
			// TODO get contributor information
			String sql = "SELECT SHORT_NAME AS CONTRIBUTOR, COUNT FROM (SELECT CONTRIBUTOR, COUNT(*) AS COUNT "
					+ "FROM RECORD GROUP BY CONTRIBUTOR) AS C, CONTRIBUTOR "
					+ "WHERE CONTRIBUTOR = ID";
			try {
				stmnt = connection.prepareStatement(sql);
				res = stmnt.executeQuery();
				while (res.next()) {
					resList.add("SITE:" + res.getString(1) + "\t" + res.getString("COUNT"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			// get instrument count
			sql = "SELECT AC_INSTRUMENT_TYPE as INSTRUMENT, COUNT(*) as COUNT FROM INSTRUMENT GROUP BY AC_INSTRUMENT_TYPE";
			try {
				stmnt = connection.prepareStatement(sql);
				res = stmnt.executeQuery();
				while (res.next()) {
					resList.add("INSTRUMENT:" + res.getString("INSTRUMENT") + "\t" + res.getString("COUNT"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			// get ms type count
			sql = "SELECT AC_MASS_SPECTROMETRY_MS_TYPE AS TYPE, COUNT(*) AS COUNT FROM RECORD GROUP BY AC_MASS_SPECTROMETRY_MS_TYPE";
			try {
				stmnt = connection.prepareStatement(sql);
				res = stmnt.executeQuery();
				while(res.next()) {
					resList.add("MS:" +  res.getString("TYPE") + "\t" + res.getString("COUNT"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			// get ion mode count
			sql = "SELECT AC_MASS_SPECTROMETRY_ION_MODE AS MODE, COUNT(*) AS COUNT FROM RECORD GROUP BY AC_MASS_SPECTROMETRY_ION_MODE";
			try {
				stmnt = connection.prepareStatement(sql);
				res = stmnt.executeQuery();
				while(res.next()) {
					resList.add("ION:" + res.getString("MODE") + "\t" + res.getString("COUNT"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			// get compound count by first letter
			sql = "SELECT FIRST AS COMPOUND, COUNT(*) AS COUNT FROM (SELECT SUBSTRING(UPPER(RECORD_TITLE),1,1) AS FIRST FROM RECORD) AS T GROUP BY FIRST";
			try {
				stmnt = connection.prepareStatement(sql);
				res = stmnt.executeQuery();
				Integer numCnt = 0;
				Integer othCnt = 0;
				while(res.next()) {
					String compound = res.getString("COMPOUND");
					if (compound.matches("[a-zA-Z]")) {
						resList.add("COMPOUND:" + compound + "\t" + res.getString("COUNT"));
					}
					else if (compound.matches("[0-9]")) {
						numCnt = numCnt + res.getInt("COUNT");
					} else {
						othCnt = othCnt + res.getInt("COUNT");
					}
				}
				resList.add("COMPOUND:1-9\t" + numCnt);
				resList.add("COMPOUND:Others\t" + othCnt);
			} catch (SQLException e) {
				e.printStackTrace();
			}

			
			// TODO is this still necessary there are no more merged spectra present in the data!?
			// get spectrum type count
			sql = "SELECT COUNT(*) AS COUNT FROM RECORD WHERE INSTR(RECORD_TITLE,'MERGED') = 0";
			try {
				stmnt = connection.prepareStatement(sql);
				res = stmnt.executeQuery();
				while(res.next()) {
					resList.add("MERGED:Normal\t" + res.getString("COUNT"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			sql = "SELECT COUNT(*) AS COUNT FROM RECORD WHERE INSTR(RECORD_TITLE,'MERGED') > 0";
			try {
				stmnt = connection.prepareStatement(sql);
				res = stmnt.executeQuery();
				while(res.next()) {
					resList.add("MERGED:Merged\t" + res.getString("COUNT"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return resList;
		}
}