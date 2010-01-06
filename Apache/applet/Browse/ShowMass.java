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
 * �E�N���b�N�C�x���g���� �N���X
 *
 * ver 2.0.8 2010.01.06
 *
 ******************************************************************************/



import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import massbank.MassBankCommon;

/**
 * �E�N���b�N�C�x���g�����N���X
 */
public class ShowMass implements MouseListener{

	/**
	 * mouseClicked����
	 * @param e mouseClicked�C�x���g
	 */
	public void mouseClicked(MouseEvent e) {
		
		// ���N���b�N�̏ꍇ
		if ( SwingUtilities.isLeftMouseButton(e) ) {
			
			// �_�u���N���b�N�̏ꍇ
		    if (e.getClickCount() >= 2){
		    	
				// �I�����ꂽ�S�Ă̒l�̃p�X���擾
				TreePath [] paths = BrowsePage.tree.getSelectionPaths();
				if ( paths == null || paths.length > 1 ) {
					return;
				}
				
				// �I�����ꂽ�X�y�N�g�����̎擾
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[0].getLastPathComponent();
				if ( node instanceof LeafNode ) {
					LeafNode leaf = (LeafNode) node;
					String accs = (leaf.acc != null) ? leaf.acc : "";
					
					try {
						String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISP];
						String reqStr = BrowsePage.baseUrl + MassBankCommon.DISPATCHER_NAME + "?type=" + typeName + "&id=" + accs;
						reqStr += "&site=" + BrowsePage.site;
						URL url = new URL( reqStr );
						BrowsePage.applet.getAppletContext().showDocument( url, "_blank" );
					} catch ( Exception ex ) {
						ex.printStackTrace();
					}
				}
		    }
		
		// �E�N���b�N�̏ꍇ
		} else if ( SwingUtilities.isRightMouseButton(e) ) {
			
			// �I�����ꂽ�S�Ă̒l�̃p�X���擾
			TreePath [] paths = BrowsePage.tree.getSelectionPaths();
			if ( paths == null ) {
				return;
			}
			
			// �I�����ꂽ�X�y�N�g�����̎擾
			int l = paths.length;
			ArrayList<String> accs = new ArrayList<String>();
			ArrayList<String> names = new ArrayList<String>();
			int num = 0;
			for ( int i = 0; i < l; i ++ ) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
				if ( node instanceof LeafNode ) {
					LeafNode leaf = (LeafNode) node;
					names.add(leaf.name);
					accs.add(leaf.acc);
					num ++;
				}
			}
			
			// �t�m�[�h��1���I������Ă��Ȃ��ꍇ
			if ( num == 0) {
				return;
			}
			
			// �|�b�v�A�b�v���j���[�쐬
			JPopupMenu popup = new JPopupMenu();
			JMenuItem item1 = new JMenuItem( "Show Record" );
			JMenuItem item2 = new JMenuItem( "Multiple Display" );
			popup.add( item1 );
			popup.add( item2 );
			
			// �I���s��1�s�̏ꍇ
			if ( num == 1 ) {
				item1.addActionListener( new PopupEntryListener(accs.get(0)) );
				item1.setEnabled( true );
				item2.setEnabled( false );
			}
			// �I���s�������̏ꍇ
			else if ( num > 1 ) {
				item2.addActionListener( 
						new PopupMultipleDisplayListener(
								(String[])accs.toArray(new String[accs.size()]), 
								(String[])names.toArray(new String[names.size()])) );
				item1.setEnabled( false );
				item2.setEnabled( true );
			}
			
			// �|�b�v�A�b�v���j���[�\��
			if ( num > 0 ) {
				popup.show( e.getComponent(), e.getX(), e.getY() );
			}
		}
	}
	
	/**
	 * mousePressed����
	 * @param e mousePressed�C�x���g
	 */
	public void mousePressed(MouseEvent e) {
	}
	
	/**
	 * mouseReleased����
	 * @param e mouseReleased�C�x���g
	 */
	public void mouseReleased(MouseEvent e) {
	}
	
	/**
	 * mouseEntered����
	 * @param e mouseEntered�C�x���g
	 */
	public void mouseEntered(MouseEvent e) {
	}
	
	/**
	 * mouseExited����
	 * @param e mouseExited�C�x���g
	 */
	public void mouseExited(MouseEvent e) {
	}
	
	/**
	 * �|�b�v�A�b�v���j���[ "Show Record" ����
	 */
	private class PopupEntryListener implements ActionListener {
		
		/** ACCESSION */
		private String acc = "";
		
		/**
		 * �R���X�g���N�^
		 * @param acc ACCESSION
		 */
		public PopupEntryListener(String acc) {
			this.acc = acc;
		}
		
		/**
		 * �C�x���g����
		 * @param e �A�N�V�����C�x���g
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_DISP];
				String reqStr = BrowsePage.baseUrl + MassBankCommon.DISPATCHER_NAME + "?type=" + typeName + "&id=" + acc;
				reqStr += "&site=" + BrowsePage.site;
				URL url = new URL( reqStr );
				BrowsePage.applet.getAppletContext().showDocument( url, "_blank" );
			} catch ( Exception ex ) {
				ex.printStackTrace();
			}
		}

	}
	
	/**
	 * �|�b�v�A�b�v���j���[ "Multiple Display" ����
	 */
	private class PopupMultipleDisplayListener implements ActionListener {
		
		/** ACCESSION�z�� */
		private String[] accs = null;
		/** RECORD_TITLE�z�� */
		private String[] names = null;
		
		/**
		 * �R���X�g���N�^
		 * @param accs ACCESSION�z��
		 * @param names RECORD_TITLE�z��
		 */
		public PopupMultipleDisplayListener(String[] accs, String[] names) {
			this.accs = accs;
			this.names = names;
		}
		
		/**
		 * �C�x���g����
		 * @param e �A�N�V�����C�x���g
		 */
		public void actionPerformed(ActionEvent e) {

			String reqUrl = BrowsePage.baseUrl + "Display.jsp";
			String param = "";
			for ( int i = 0; i < accs.length; i++ ) {
				String name = names[i];
				String id = accs[i];
				String formula = "";
				String mass = "";
				String site = BrowsePage.site;
				param += "id=" + name + "\t" + id + "\t" + formula + "\t" + mass + "\t" + site + "&";
			}
			param = param.substring( 0, param.length() -1 );

			try {
				URL url = new URL( reqUrl );
				URLConnection con = url.openConnection();
				con.setDoOutput(true);
				PrintStream out = new PrintStream( con.getOutputStream() );
				out.print( param );
				out.close();
				String line;
				String filename = "";
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				boolean isStartSpace = true;
				while ( (line = in.readLine() ) != null ) {
					// �擪�X�y�[�X��ǂݔ�΂�����
					if ( line.equals("") ) {
						if ( isStartSpace ) {
							continue;
						}
						else {
							break;
						}
					}
					else {
						isStartSpace = false;
					}
					filename += line;
				}
				in.close();

				reqUrl += "?type=Multiple Display&" + "name=" + filename;
				BrowsePage.applet.getAppletContext().showDocument( new URL(reqUrl), "_blank" );

			} catch ( Exception ex ) {
				ex.printStackTrace();
			}
		}
	}
}
