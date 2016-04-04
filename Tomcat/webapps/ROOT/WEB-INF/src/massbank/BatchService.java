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
 * �o�b�`�����T�[�r�X
 *
 * ver 1.0.7 2012.09.07
 *
 ******************************************************************************/
package massbank;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BatchService extends HttpServlet {
	public static final int MAX_NUM_JOB = 5;
	public int cnt = 0;
	private JobMonitor mon = null;

	/**
	 * �T�[�r�X�����������s��
	 */
	public void init() throws ServletException {
		try {
			if (InetAddress.getLocalHost().getHostName().equals("sv21")) { 
				return;
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		// ����I�ɃW���u���Ď�
		mon = new JobMonitor();
		this.mon.start();
	}

	public void service(HttpServletRequest req, HttpServletResponse res) {
	}

	/**
	 * �T�[�r�X�I���������s��
	 */
	public void destroy() {
		// �I����ԃZ�b�g
		this.mon.setTerminate();

		// JobMonitor�X���b�h�Ɋ��荞��
		this.mon.interrupt();

		// JobMonitor�X���b�h���I������̂�҂�
		do {
			try { this.mon.join(200); }
			catch ( Exception e ) {}
		} while ( this.mon.isAlive() );
	}

	/**
	 * �W���u�Ď��N���X
	 */
	public class JobMonitor extends Thread {
		private boolean isTerminated = false;
		private LinkedList thList = new LinkedList();

		public JobMonitor() {
		}

		/**
		 * �X���b�h���N������
		 */
		public void run() {
			try { sleep(5000); }
			catch (Exception e) {}

			JobManager jobMgr = new JobManager();
			try {
				// �A�N�e�B�u��ԂɂȂ��Ă���W���u�𖢎��s��Ԃɂ���
				jobMgr.setInitStatus();
			}
			catch (Exception e) {
				e.printStackTrace();
				return;
			}

			do {
				// �ҋ@
				try { sleep(10000); }
				catch (Exception e) {}

				// BatchJobWorker�X���b�h�I���`�F�b�N
				int size = this.thList.size();
				int n = 0;
				for ( int i = 0; i < size; i++ ) {
					BatchSearchWorker thRunning = (BatchSearchWorker)this.thList.get(n);
					if ( !thRunning.isAlive() ) {
						this.thList.remove(n);
					}
					else {
						n++;
					}
				}

				// BatchSearchWorker�X���b�h���I��������
				if ( isTerminated ) {
					for ( int i = 0; i < this.thList.size(); i++ ) {
						BatchSearchWorker thRunning = (BatchSearchWorker)this.thList.get(i);
						thRunning.setTerminate();
					}
					break;
				}

				// �����s�W���u�̃��X�g���擾
				try {
					ArrayList<JobInfo> jobList = jobMgr.getWaitJobList();
					if ( jobList == null ) {
						continue;
					}
					int numWait = jobList.size();
					if ( numWait == 0 ) {
						continue;
					}

					// �����Ɏ��s�ł���W���u���𐧌�
					int numRun = jobMgr.getNumRunJob();
					int numExec = numWait;
					if ( numWait > MAX_NUM_JOB - numRun ) {
						numExec = MAX_NUM_JOB - numRun;
					}
					// �W���u���s
					BatchSearchWorker[] thread = new BatchSearchWorker[numExec];
					for ( int i = 0; i < numExec; i++ ) {
						JobInfo jobInfo = jobList.get(i);
						String jobId = jobInfo.getJobId();
						thread[i] = new BatchSearchWorker(jobInfo);
						thread[i].start();
						this.thList.add(thread[i]);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					return;
				}
			} while(true);

			jobMgr.end();
		}

		/**
		 * �I���t���O���Z�b�g����
		 */
		public void setTerminate() {
			isTerminated = true;
		}
	}
}
