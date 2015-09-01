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
 * �A�g�T�[�o���Ď�����풓�T�[�u���b�g
 *
 * ver 1.0.2 2012.11.01
 *
 ******************************************************************************/
package massbank.pserver;

import java.io.PrintWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import massbank.CallCgi;
import massbank.ServerStatus;
import massbank.ServerStatusInfo;
import massbank.pserver.SendMail;

public class ServerMonitor extends HttpServlet {
	public ServletContext context = null;
	public ServerStatus svrStatus = null;
	private ServerPolling poll = null;

	/**
	 * �����������s��
	 */
	public void init() throws ServletException {
		this.context = getServletContext();

		// �Ď��J�n
		managed();
	}

	/**
	 * HTTP���N�G�X�g����
	 */
	public void service(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {

		String action = "";
		if ( req.getParameter("act") == null ) {
			return;
		}
		action = req.getParameter("act");

		// �Ď��J�n
		if ( action.equals("Managed") ) {
			managed();
		}
		// �Ď���~
		else if ( action.equals("Unmanaged") ) {
			unmanaged();
		}

		PrintWriter out = res.getWriter();
		out.println("OK");
		out.flush();
	}

	/**
	 * �I���������s��
	 */
	public void destroy() {
		unmanaged();
	}

	/**
	 * �Ď����J�n����
	 */
	private void managed() {
		Logger.global.info( "managed start" );

		// �X���b�h���������Ă���ꍇ
		if ( this.poll != null && this.poll.isAlive() ) {
			// �I���t���O�������̏ꍇ
			if ( !poll.isTerminated() ) {
				return;
			}

			// �X���b�h�I�����ł���΁A�I������̂�҂�
			do {
				try {
					poll.join(200);
				}
				catch ( Exception e ) {
				}
			} while ( poll.isAlive() );
		}

		// ��Ԃ��u�Ď��v�ɃZ�b�g����
		this.svrStatus = new ServerStatus();
		svrStatus.setManaged(true);

		// �A�g�T�[�o���|�[�����O����X���b�h���N��
		this.poll = new ServerPolling();
		this.poll.start();
	}

	/**
	 * �Ď����~����
	 */
	private void unmanaged() {
		Logger.global.info( "unmanaged start" );

		// ServerPolling�X���b�h���������Ă��Ȃ��ꍇ�͉�������
		if ( !poll.isAlive() ) {
			return;
		}

		// ServerPolling�X���b�h�Ɋ��荞��
		poll.interrupt();

		// �I���t���O��L���ɂ���
		poll.setTerminate(true);

		// ServerPolling�X���b�h���I������̂�҂�
		do {
			try {
				poll.join(200);
			}
			catch ( Exception e ) {
			}
		} while ( poll.isAlive() );

		// �I���t���O�𖳌�����
		poll.setTerminate(false);

		// ��Ԃ��u��Ď��v�ɃZ�b�g����
		svrStatus.setManaged(false);
		this.svrStatus = null;

		Logger.global.info( "unmanaged end" );
	}

	/**
	 * �A�g�T�[�o���|�[�����O����X���b�h
	 */
	class ServerPolling extends Thread {
		// �^�C���A�E�g����
		private static final int TIMEOUT_SEC = 15;
		// �I���t���O
		private boolean isTerminated = false;

		/**
		 * �R���X�g���N�^
		 */
		public ServerPolling() {
		}

		/**
		 * �X���b�h�J�n
		 */
		public void run() {
			// �|�[�����O�������擾
			int pollInterval = svrStatus.getPollInterval();
			Logger.global.info( "polling start" );

			try { sleep( 2000 ); }
			catch (InterruptedException ex) { ex = null; }

			//-----------------------------------------------------------------
			// �|�[�����O����
			//-----------------------------------------------------------------
			do {
				// �I���t���O���L���ł���΃X���b�h���I������
				if ( isTerminated() ) {
					break;
				}

				// �Ǘ��t�@�C���̐���
				svrStatus.clean();

				// �Ď��Ώۂ̃T�[�o���Ȃ��ꍇ�͏I������
				if ( svrStatus.getServerNum() == 0 ) {
					Logger.global.info( "ServerNum=0" );
					svrStatus.setManaged(false);
					return;
				}

				// CGI��URL�ƃp�����[�^�̃��X�g���Z�b�g
				ServerStatusInfo[] info = svrStatus.getStatusInfo();
				int num = info.length;
				String[] urls = new String[num];
				Hashtable[] params = new Hashtable[num];
				boolean[] isActive = new boolean[num];
				for ( int i = 0; i < num; i++ ) {
					// URL���Z�b�g
					urls[i] = info[i].getUrl() + "cgi-bin/ServerCheck.cgi";
					// DB�����Z�b�g
					params[i] = new Hashtable();
					params[i].put( "dsn", info[i].getDbName() );
					// �X�e�[�^�X���Z�b�g
					isActive[i] = info[i].getStatus();
				}

				// �e�A�g�T�[�o��Ń`�F�b�N�v���O���������s����
				CallCgi[] thread = new CallCgi[num];
				for ( int i = 0; i < num; i++ ) {
					thread[i] = new CallCgi( urls[i], params[i], TIMEOUT_SEC, context );
					thread[i].start();
				}

				// CallCgi�X���b�h�I���҂�
				long until = System.currentTimeMillis() + TIMEOUT_SEC * 1000;
				boolean isRunning = true;
				while ( isRunning && System.currentTimeMillis() < until ) {
					isRunning = false;
					for ( int i = 0; i < num; i++ ) {
						try {
							if ( thread[i].isAlive() ) {
								// �X���b�h���I������܂őҋ@
								thread[i].join(200);
								isRunning = true;
							}
						}
						catch ( Exception e ) {
							// �G���[
							Logger.global.severe( e.toString() );
						}
					}
				}

				// ���ʎ擾
				boolean isUpate = false;
				for ( int i = 0; i < num; i++ ) {
					String res = thread[i].result;
					boolean isOK = false;
					if ( res.equals("OK") ) {
						isOK = true;
					}

					// ��ԕω�������ΕύX����
					if ( isOK != isActive[i] ) {
//						String state = "";
//						String subject = "";
//						String contents = "";
						svrStatus.setStatus( i, isOK );
						isActive[i] = isOK;
						isUpate = true;

						// �A���[�g���[�����M
//						if ( isOK ) {
//							state = "Server recovery";
//						}
//						else {
//							state = "Server failed  ";
//						}
//						subject = state + " (" + info[i].getServerName() + ")";
//						contents = state + "  : " + info[i].getServerName() + "\n\n";
//						contents += "CGI timeout      : " + TIMEOUT_SEC + " sec.\n";
//						contents += "Polling interval : " + pollInterval + " sec.\n";
//						SendMail.send( subject, contents );
					}
				}

				// �X�V������Εۑ�����
				if ( isUpate ) {
					svrStatus.store();
				}

				// �|�[�����O�����̎��ԑҋ@����
				try { sleep( pollInterval * 1000 ); }
				catch (InterruptedException ex) { ex = null; }

			} while(true);
		}

		/**
		 * �I���t���O���Z�b�g����
	   * @param enable  true:�L�� / false:����
		 */
		public void setTerminate(boolean enable) {
			this.isTerminated = enable;
		}

		/**
		 * �I���t���O���L����������
	   * @return true:�L�� / false:����
		 */
		public boolean isTerminated() {
			return this.isTerminated;
		}
	}
}
