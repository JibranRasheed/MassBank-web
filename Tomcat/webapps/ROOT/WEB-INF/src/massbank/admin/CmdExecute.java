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
 * �R�}���h���s�N���X
 *
 * ver 1.0.3 2009.06.15
 *
 ******************************************************************************/
package massbank.admin;

import java.lang.InterruptedException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class CmdExecute {
	
	// �R�}���h���s���t���O
	private boolean isRunning = false;
	
	// �R�}���h���s�^�C���A�E�g�t���O
	private boolean isTimeout = false;
	
	// �^�C���A�E�g���ԁi�~���b�P�ʁj
	private final long timout = 60000L;
	
	/**
	 * �R�}���h�����s
	 *
	 * @param cmdArray �R�}���h
	 * @return ���s����
	 */
	public CmdResult exec(final String[] cmd) {
		CmdResult res = new CmdResult();
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			isRunning = true;
			
			// �R�}���h���s�Ď��X���b�h
			WatchDog wd = new WatchDog(process);
			
			// �W���o�́A�G���[�o�͂̃n���h��
			CmdOutputHandler stdoutHandler =
					new CmdOutputHandler(process.getInputStream());
			CmdOutputHandler stderrHandler =
					new CmdOutputHandler(process.getErrorStream());
			
			// �X���b�h�J�n
			stdoutHandler.start();
			stderrHandler.start();
			wd.start();
			
			// �v���Z�X�I���҂��A�X�e�[�^�X�Z�b�g
			res.setStatus(process.waitFor());
			isRunning = false;
			
			// ���荞�ݏ���
			if ( !stdoutHandler.isInterrupted() ) {
				stdoutHandler.interrupt();
			}
			if ( !stderrHandler.isInterrupted() ) {
				stderrHandler.interrupt();
			}
			if ( !wd.isInterrupted() ) {
				wd.interrupt();
			}
			// �W���o�́A�G���[�o�͂̓��e���Z�b�g
			res.setStdout(stdoutHandler.getCmdOutput());
			if (isTimeout) {
				res.setStderr(cmd[0] + " command was timeout. (" + timout + "msec.)");
			}
			else {
				res.setStderr(stderrHandler.getCmdOutput());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * �R�}���h�o�̓n���h���N���X
	 */
	private final class CmdOutputHandler extends Thread {
		// �i�[�o�b�t�@
		private ByteArrayOutputStream buf = new ByteArrayOutputStream();
		
		// �V�F���R�}���h����̏o�͂��󂯎����̓X�g���|��
		private InputStream in;
		
		/**
		 * �f�t�H���g�R���X�g���N�^�̖�����
		 */
		private CmdOutputHandler() {
			super();
		}
		
		/**
		 * �R���X�g���N�^
		 * @param argIn �V�F���R�}���h����̏o�͂��󂯎��InputStream
		 */
		private CmdOutputHandler(final InputStream argIn) {
			super();
			in = argIn;
		}
		
		/**
		 * �R�}���h����̏o�͂��擾���܂�
		 * @return
		 */
		public String getCmdOutput() {
			// �O�̂��ߍŌ�Ɉ��ǂݏo��
			storeBuf();
			try {
				in.close();
			}
			catch (IOException e) {
				e = null;
			}
			return buf.toString();
		}
		
		/**
		 * �R�}���h����̏o�͂�ǂݎ��܂�
		 */
		public void run() {
			// �o�b�t�@�ǂݎ��Ԋu(ms)
			final long sleepTime = 100L;
			
			while (isRunning) {
				// �o�b�t�@�̒��g���擾����
				storeBuf();
				
				// ��莞�ԃX���[�v����
				try {
					sleep(sleepTime);
				}
				catch (InterruptedException ignoreEx) {
					storeBuf();
				}
			}
		}
		
		/**
		 * ���̓X�g���|���̓��e���o�b�t�@�Ɋi�[���܂�
		 */
		private void storeBuf() {
			try {
				int size = in.available();
				if ( size > 0 ) {
					byte[] cmdout = new byte[size];
					in.read(cmdout);
					buf.write(cmdout);
				}
			}
			catch (IOException ignoreEx) {
				ignoreEx = null;
			}
		}
	}
	
	/**
	 * �R�}���h���s�Ď��X���b�h
	 */
	private final class WatchDog extends Thread {
		// ���s���̃v���Z�X
		private Process process = null;
		
		/**
		 * Creates a new WatchDog object.
		 *
		 * @param param ���s�\���Process
		 */
		public WatchDog(final Process param) {
			process = param;
		}
		
		/**
		 * �f�t�H���g�R���X�g���N�^�̖�����
		 */
		@SuppressWarnings("unused")
		private WatchDog() {
			super();
		}
		
		/**
		 * �^�C���A�E�g����
		 */
		public synchronized void run() {
			long until = System.currentTimeMillis() + timout;
			long now = 0;			
			// ��莞�ԑҋ@����
			while ( isRunning && (until > (now = System.currentTimeMillis())) ) {
				try {
					wait( until - now );
				}
				catch ( InterruptedException ignoreEx ) {
					ignoreEx = null;
				}
			}
			// ��莞�Ԍo���Ă����s���̏ꍇ�A�v���Z�X�������I������
			if ( isRunning ) {
				process.destroy();
				isTimeout = true;
			}
		}
	}
}
