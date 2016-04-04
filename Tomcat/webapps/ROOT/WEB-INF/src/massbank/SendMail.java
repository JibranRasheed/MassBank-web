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
 * ver 1.0.1 2013.11.11
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
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;


/**
 * ���[�����M���ʃN���X
 */
public class SendMail {

	private static String host = "";
	private static String port = "";
	private static String user = "";
	private static String pass = "";

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

		String[] smtpItems = info.getSmtp().split(",");
		if ( smtpItems.length >= 1 ) {
			host = smtpItems[0].trim();
		}
		if ( smtpItems.length >= 2 ) {
			port = smtpItems[1].trim();
		}
		if ( smtpItems.length >= 3 ) {
			user = smtpItems[2].trim();
		}
		if ( smtpItems.length >= 4 ) {
			pass = smtpItems[3].trim();
		}
		if ( port.equals("") ) {
			port = "25";
		}
		System.out.println( host + "/" + port + "/" + user + "/" + pass);

		try {
			// SMTP�T�[�o�[�̃A�h���X��ݒ�
			Properties props = new Properties();
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);

			if ( port.equals("465") || port.equals("587") ) {
				props.put("mail.smtp.ssl.trust", host);
				if ( port.equals("465") ) {
					props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
					props.put("mail.smtp.socketFactory.fallback", "false");
				}
				else if ( port.equals("587") ) {
					props.put("mail.smtp.starttls.enable", "true");
				}
			}
			Session session = null;
			if ( user.equals("") || pass.equals("") ) {
				props.put("mail.smtp.auth", "false");
				session = Session.getDefaultInstance(props, null);
			}
			else {
				props.put("mail.smtp.auth", "true");
				session = Session.getInstance(props, new Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(user, pass);
						}
					}
				);
			}

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
