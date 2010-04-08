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
 * ���[�����M���ʃN���X
 *
 * ver 1.0.0 2010.04.05
 *
 ******************************************************************************/
package massbank;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * ���[�����M���ʃN���X
 */
public class SendMail {
	
	/**
	 * ���[�����M�֐�
	 * @param info ���[�����M���I�u�W�F�N�g
	 * @return ����
	 */
	public static boolean send(SendMailInfo info) {
		
		// ���[�����M���`�F�b�N
		if (!info.isCheck()) {
			Logger.global.severe( "The mail sending failed.");
			return false;
		}
		
		try {
			// SMTP�T�[�o�[�̃A�h���X��ݒ�
			Properties props = System.getProperties();
			props.put("mail.smtp.host", info.getSmtp());
			
			Session session = Session.getDefaultInstance(props, null);
			MimeMessage mimeMsg = new MimeMessage(session);

			// ���M�����[���A�h���X�Ƒ��M�Җ���ݒ�
			mimeMsg.setFrom(new InternetAddress(info.getFrom(), info.getFromName(), "utf-8"));
			
			// ���M�惁�[���A�h���X��ݒ�
			mimeMsg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(info.getTo()));
			if (!info.getCc().equals("")) {
				mimeMsg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(info.getCc()));
			}
			if (!info.getBcc().equals("")) {
				mimeMsg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(info.getBcc()));
			}
			
			// ���[���^�C�g����ݒ�
			mimeMsg.setSubject(info.getSubject(), "utf-8");
			
			// ���[���{�f�B�p�}���`�p�[�g�I�u�W�F�N�g����
			MimeMultipart mp = new MimeMultipart();
			MimeBodyPart mbp = new MimeBodyPart();
			mbp.setText(info.getContents(), "utf-8" );	// �{��
			mp.addBodyPart(mbp);
			File[] files = info.getFiles();
			if (files != null) {
				for(int i=0; i<files.length; i++){		// �Y�t�t�@�C��
					mbp = new MimeBodyPart();
					FileDataSource fds = new FileDataSource(files[i]);
					mbp.setDataHandler(new DataHandler(fds));
					mbp.setFileName(MimeUtility.encodeWord(fds.getName()));
					mp.addBodyPart(mbp);
				}
			}
			
			// ���[�����e�Ƀ}���`�p�[�g�I�u�W�F�N�g�Ƒ��M���t��ݒ肵�đ��M
			mimeMsg.setContent(mp);
			mimeMsg.setSentDate(new Date());
			Transport.send(mimeMsg);
		}
		catch (Exception e) {
			Logger.global.severe( "The mail sending failed.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
