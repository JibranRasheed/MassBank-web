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
 * Tomcat�N�����Ɏ��s���W���[�����N������T�[�u���b�g
 *
 * ver 1.0.5 2010.11.22
 *
 ******************************************************************************/
package massbank;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @deprecated �񐄏��N���X
 * @see MassBankEnv
 */
public class StartupExecModule extends HttpServlet {
	
	public static String BASE_URL = "";
	public static String REAL_PATH = "";
	public static String MASTER_DB = "";
	public static String APPLICATION_TEMP = "";

	/**
	 * �T�[�r�X�����������s��
	 */
	public void init() throws ServletException {
//		String baseUrl = getInitParameter("baseUrl");
//		BASE_URL = "http://localhost/MassBank/";
//		if ( baseUrl != null ) {
//			BASE_URL = baseUrl;
//		}
//		REAL_PATH = this.getServletContext().getRealPath("/");
//		REAL_PATH = REAL_PATH.replace("api", "ROOT");
//
//		MASTER_DB = "";
//		if ( getInitParameter("masterDB") != null ) {
//			MASTER_DB = getInitParameter("masterDB");
//		}
//
//		int pos1 = BASE_URL.indexOf("/", 8);
//		String subDir = BASE_URL.substring(pos1, BASE_URL.length());
//		if ( subDir.equals("/") ) {
//			subDir = "";
//		}
//		APPLICATION_TEMP = REAL_PATH + subDir + "temp/";
		
		BASE_URL = MassBankEnv.get(MassBankEnv.KEY_BASE_URL);
		REAL_PATH = MassBankEnv.get(MassBankEnv.KEY_TOMCAT_DOCROOT_PATH);
		MASTER_DB = MassBankEnv.get(MassBankEnv.KEY_DB_MASTER_NAME);
		APPLICATION_TEMP = MassBankEnv.get(MassBankEnv.KEY_TOMCAT_APPTEMP_PATH);
	}

	public void service(HttpServletRequest req, HttpServletResponse res) {
	}

	/**
	 * �T�[�r�X�I���������s��
	 */
	public void destroy() {
	}
}
