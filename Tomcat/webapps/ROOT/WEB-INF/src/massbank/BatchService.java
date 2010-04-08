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
 * �o�b�`�����T�[�r�X
 *
 * ver 1.0.3 2010.04.02
 *
 ******************************************************************************/
package massbank;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * �o�b�`�����T�[�r�XServlet�N���X
 */
public class BatchService extends HttpServlet {
	public static String BASE_URL = "";
	public static String REAL_PATH = "";
	public int cnt = 0;
	private BatchJobMonitor mon = null;

	/**
	 * �T�[�r�X�����������s��
	 */
	public void init() throws ServletException {
		String baseUrl = getInitParameter("baseUrl");
		BASE_URL = "http://localhost/MassBank/";
		if ( baseUrl != null ) {
			BASE_URL = baseUrl;
		}
		REAL_PATH = this.getServletContext().getRealPath("/");
		// �A�N�e�B�u��ԂɂȂ��Ă���W���u�𖢎��s��Ԃɂ���
		BatchJobManager job = new BatchJobManager();
		job.setPassiveAll();

		// ����I�ɃW���u���Ď�
		mon = new BatchJobMonitor();
		this.mon.start();
	}

	public void service(HttpServletRequest req, HttpServletResponse res) {
	}

	/**
	 * �T�[�r�X�I���������s��
	 */
	public void destroy() {
		// BatchJobMonitor�X���b�h�Ɋ��荞��
		this.mon.interrupt();
		// �I����ԃZ�b�g
		this.mon.setTerminate();

		// BatchJobMonitor�X���b�h���I������̂�҂�
		do {
			try { this.mon.join(200); }
			catch ( Exception e ) {}
		} while ( this.mon.isAlive() );
	}

	/**
	 * �W���u�Ď��N���X
	 */
	public class BatchJobMonitor extends Thread {
		private boolean isTerminated = false;
		private LinkedList thList = new LinkedList();

		public BatchJobMonitor() {
		}

		/**
		 * �X���b�h���N������
		 */
		public void run() {
			do {
				// �ҋ@
				try { sleep(10000); }
				catch (Exception e) {}

				// BatchJobWorker�X���b�h�I���`�F�b�N
				int size = this.thList.size();
				int n = 0;
				for ( int i = 0; i < size; i++ ) {
					BatchJobWorker thRunning = (BatchJobWorker)this.thList.get(n);
					if ( !thRunning.isAlive() ) {
						this.thList.remove(n);
					}
					else {
						n++;
					}
				}

				// BatchJobWorker�X���b�h���I��������
				if ( isTerminated ) {
					for ( int i = 0; i < this.thList.size(); i++ ) {
						BatchJobWorker thRunning = (BatchJobWorker)this.thList.get(i);
						thRunning.setTerminate();
					}
					break;
				}

				// �����s�W���u�̃��X�g���擾
				BatchJobManager job = new BatchJobManager();
				ArrayList<BatchJobInfo> entryList = job.getPassiveEntry();
				if ( entryList == null ) {
					continue;
				}
				int num = entryList.size();
				if ( num == 0 ) {
					continue;
				}
				
				// �W���u���s
				BatchJobWorker[] thread = new BatchJobWorker[num];
				for ( int i = 0; i < num; i++ ) {
					BatchJobInfo jobInfo = entryList.get(i);
					job.setEntry( jobInfo );
					job.setActive();
					thread[i] = new BatchJobWorker( jobInfo );
					thread[i].start();
					this.thList.add(thread[i]);
				}
			} while(true);
		}

		/**
		 * �I���t���O���Z�b�g����
		 */
		public void setTerminate() {
			isTerminated = true;
		}
	}
}