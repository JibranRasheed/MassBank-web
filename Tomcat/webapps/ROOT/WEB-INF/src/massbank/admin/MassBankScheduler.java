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
 * MassBank�p�X�P�W���[��
 *
 * ver 1.0.0 2010.09.30
 *
 ******************************************************************************/
package massbank.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;



/**
 * MassBank�p�X�P�W���[���N���X
 * 
 * �����F
 * admin.conf�ɋL�q�����X�P�W���[���^�X�N���w�肳�ꂽ�Ԋu�Ŏ��s����B
 * �^�X�N��admin.conf�Ɉȉ��̂悤�ɕ����L�q�ł���B
 * 
 *     schedule=[�^�X�N],[������s����],[���s�Ԋu]
 *     schedule=[�^�X�N],[������s����],[���s�Ԋu]
 *        �F�i�ȍ~�����L�q�j
 * 
 * �⑫�F
 *  [�^�X�N]
 *    ����I�Ɏ��s�������R�}���h���L�q
 *  [������s����]
 *    �T�[�u���b�g�N����Ƀ^�X�N��������s���鎞�Ԃ��L�q�i�b�w��j
 *  [���s�Ԋu]
 *    �O����s�^�X�N�I���ォ�玟����s�^�X�N���J�n���鎞�Ԃ̊Ԋu���L�q�i�b�w��j
 *    0���w�肵���ꍇ�̓^�X�N�͏���̂ݎ��s����i2��ڈȍ~�͎��s���Ȃ��j
 *       
 * �T�[�u���b�g�N����ɁA�w�肳�ꂽ������s���Ԃ�1��ڂ̃^�X�N�����s����B
 * 2��ڈȍ~�̃^�X�N���s�́A�O��̃^�X�N���s���I�����Ă���w�肳�ꂽ���s�Ԋu�̎��Ԃ��o�ߌ�Ɏ��s�����B
 * admin.conf�ɋL�q���ꂽ�����̃^�X�N�́A�^�X�N���ƂɕʃX���b�h�Ŏ��s�����B
 */
public class MassBankScheduler extends HttpServlet {
	
	/** �ő�X�P�W���[���X���b�h�v�[���T�C�Y */
	private static final int MAX_THREAD_POOL_SIZE = 15;
	
	/** �X�P�W���[���I�u�W�F�N�g */
	private ScheduledExecutorService sc = null;
	
	/** �X�P�W���[���^�X�N��ԃI�u�W�F�N�g */
	private ScheduledFuture<?>[] futures = null;
	
	/**
	 * �T�[�u���b�g����������
	 */
	public void init() throws ServletException {
		
		String baseUrl = getInitParameter("baseUrl");
		if ( baseUrl == null ) {
			baseUrl = "http://localhost/MassBank/";
		}
		String realPath = this.getServletContext().getRealPath("/");
		AdminCommon admin = new AdminCommon(baseUrl, realPath);
		ArrayList<String> scheduleList = admin.getSchedule();
		
		int threadPoolSize = (( MAX_THREAD_POOL_SIZE > scheduleList.size() ) ? scheduleList.size() : MAX_THREAD_POOL_SIZE);
		sc = Executors.newScheduledThreadPool(threadPoolSize);
		futures = new ScheduledFuture<?>[scheduleList.size()];
		
		for (int i=0; i<scheduleList.size(); i++) {
			String schedule = scheduleList.get(i);
			boolean isFormatError = false;
			boolean isTimeError = false;
			
			// �X�P�W���[���t�H�[�}�b�g���`�F�b�N
			String taskCmd = "";
			long initial = 0L;
			long delay = 0L;
			try {
				String[] tmp = schedule.split(",");
				if ( tmp.length == 3 ) {
					taskCmd = tmp[0];
					initial = Long.parseLong(tmp[1]);
					delay = Long.parseLong(tmp[2]);
				}
				else {
					isFormatError = true;
				}
				if ( taskCmd.equals("")  ) {
					isFormatError = true;
				}
				if ( initial < 0 || delay < 0 ) {
					isTimeError = true;
				}
			}
			catch (NumberFormatException ne) {
				isTimeError = true;
			}
			
			// �G���[�o�͂ƃX�P�W���[���̎��s
			if ( isFormatError && isTimeError ) {
				Logger.getLogger("global").warning( 
						"<<SCHEDULE_" + i + ">> The format and time of the schedule is wrong. [schedule=" + schedule + "]" );
			}
			else if ( isFormatError ) {
				Logger.getLogger("global").warning( 
						"<<SCHEDULE_" + i + ">> The format of the schedule is wrong. [schedule=" + schedule + "]" );
			}
			else if ( isTimeError ) {
				Logger.getLogger("global").warning( 
						"  <<SCHEDULE_" + i + ">> The time of the schedule is wrong. [schedule=" + schedule + "]" );
			}
			else {
				if ( delay != 0L ) {
					// ����I�Ɏ��s
					futures[i] = sc.scheduleWithFixedDelay(new TaskExec(i, taskCmd, initial, delay), initial, delay, TimeUnit.SECONDS);
				}
				else {
					// 1��̂ݎ��s
					futures[i] = sc.schedule(new TaskExec(i, taskCmd, initial, delay), initial, TimeUnit.SECONDS);
				}
			}
		}
	}
	
	/**
	 * �T�[�u���b�g�I������
	 */
	public void destroy() {
		// �X�P�W���[������S�Ẵ^�X�N�����O���A���s���̑S�Ẵ^�X�N���I��
		if ( futures != null ) {
			for (ScheduledFuture<?> future : futures) {
				if (future != null) {
					future.cancel(true);
				}
			}
		}
		// �X�P�W���[���̏I��
		if ( sc != null ) {
			sc.shutdown();
		}
	}

	/**
	 * �^�X�N���s�N���X
	 */
	private class TaskExec implements Runnable {
		private int tskIndex = -1;
		private String taskCmd;
		private long initial;
		private long delay;
		
		/**
		 * �R���X�g���N�^
		 * @param tskIndex �^�X�N�C���f�b�N�X
		 * @param taskCmd �^�X�N�R�}���h 
		 * @param initial ������s����
		 * @param delay ���s�Ԋu
		 */
		public TaskExec(int tskIndex, String taskCmd, long initial, long delay) {
			this.tskIndex = tskIndex;
			this.taskCmd = taskCmd;
			this.initial = initial;
			this.delay = delay;
		}
		
		public void run() {
			Logger.getLogger("global").info( 
					"  <<SCHEDULE_" + tskIndex + ">> Start schedule task. [schedule=" + taskCmd + "," + initial + "," + delay + "]" );
			
			Process p = null;
			boolean isError = false;
			try {
				p = Runtime.getRuntime().exec(taskCmd);
			}
			catch (IOException e) {
				e.printStackTrace();
				isError = true;
			}
			
			// �^�X�N�����s�ł��Ȃ������ꍇ�̓X�P�W���[�����珜�O
			if ( p == null || isError ) {
				Logger.getLogger("global").severe( 
						"  <<SCHEDULE_" + tskIndex + ">> The task of failing is excluded from the schedule. [schedule=" + taskCmd + "," + initial + "," + delay + "]" );
				futures[tskIndex].cancel(true);
				return;
			}
			
			// �^�X�N�̎��s�Ɏ��Ԃ�������ꍇ�͏I���܂ő҂�
			try {
				p.waitFor();
			} catch (InterruptedException ex) {
				// �T�[�u���b�g�I�����Ɏ��s���̃^�X�N�͋����I��
				Logger.getLogger("global").warning( 
						"  <<SCHEDULE_" + tskIndex + ">> Force-quit a schedule task. [schedule=" + taskCmd + "," + initial + "," + delay + "]" );
				p.destroy();
				return;
			}
			
			Logger.getLogger("global").info( 
					"  <<SCHEDULE_" + tskIndex + ">> End schedule task. [schedule=" + taskCmd + "," + initial + "," + delay + "]" );
		}
	}
}
