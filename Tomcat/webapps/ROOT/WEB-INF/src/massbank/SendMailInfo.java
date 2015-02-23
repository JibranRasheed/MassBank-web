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
 * ���[�����M���N���X
 *
 * ver 1.0.0 2010.04.05
 *
 ******************************************************************************/
package massbank;

import java.io.File;

/**
 * ���[�����M���N���X
 * 
 */
public class SendMailInfo {

	/** SMTP�A�h���X */
	private String smtp = "";
	
	/** ���M�Җ� */
	private String fromName = "";
	
	/** From���[���A�h���X */
	private String from = "";
	
	/** To���[���A�h���X�i�J���}��؂�ŕ����ݒ�j */
	private String to = "";
	
	/** CC���[���A�h���X�i�J���}��؂�ŕ����ݒ�j */
	private String cc = "";

	/** BCC���[���A�h���X�i�J���}��؂�ŕ����ݒ�j */
	private String bcc = "";
	
	/** �^�C�g�� */
	private String subject = "";
	
	/** �{�� */
	private String contents = "";
	
	/** �Y�t�t�@�C�� */
	private File[] files = null;
	
	/**
	 * �R���X�g���N�^
	 * @param smtp SMTP�A�h���X
	 * @param from ���M�����[���A�h���X
	 * @param to ���M�惁�[���A�h���X
	 */
	public SendMailInfo(String smtp, String from, String to) {
		if (smtp != null) { this.smtp = smtp.trim(); }
		if (from != null) { this.from = from.trim(); }
		if (to != null) { this.to = to.trim(); }
	}

	/**
	 * ���[�����M���K�{�`�F�b�N
	 * �K�v�Œ���̏�񂪐ݒ肳��Ă��邩���`�F�b�N
	 * @return ����
	 */
	public boolean isCheck() {
		boolean ret = true;
		
		if (smtp.equals("")) { ret = false; }
		if (from.equals("")) { ret = false; }
		if (to.equals("")) { ret = false; }
		
		return ret;
	}
	
	public String getSmtp() {
		return smtp;
	}

	public void setSmtp(String smtp) {
		if (smtp != null) { this.smtp = smtp.trim(); }
	}
	
	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		if (fromName != null) { this.fromName = fromName.trim(); }
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		if (from != null) { this.from = from.trim(); }
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		if (to != null) { this.to = to.trim(); }
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		if (cc != null) { this.cc = cc.trim(); }
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		if (bcc != null) { this.bcc = bcc.trim(); }
	}
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		if (subject != null) { this.subject = subject.trim(); }
	}

	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		if (contents != null) { this.contents = contents; }
	}

	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}
}
