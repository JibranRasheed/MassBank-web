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
 * BrowsPage ���C���N���X
 *
 * ver 2.0.6 2011.02.10
 *
 ******************************************************************************/

import java.awt.*;
import java.net.*;

import javax.swing.*;
import javax.swing.tree.*;
import massbank.GetConfig;

/**
 * BrowsePage ���C���N���X
 */
@SuppressWarnings("serial")
public class BrowsePage extends JApplet {
	static public String baseUrl = "";
	public static ImageIcon MassIcon;
	public static ImageIcon EmptyIcon;
	public static ImageIcon OpenIcon;
	public static ImageIcon CloseIcon;
	public static JTree tree;
	public static DefaultTreeModel treeModel;
	public static BrowsePage applet;
	public static String site = "0";
	public static String[] mode = null;
	
	public void init() {
		// ���ݒ�t�@�C������A�g�T�C�g��URL���擾
		String confPath = getCodeBase().toString();
		GetConfig conf = new GetConfig(confPath);
		String severUrl = conf.getServerUrl();
		baseUrl = severUrl + "jsp/";
		
		// �c���[�̃��x�����uMassBank / Keio Unive...�v�̂悤�ɏȗ������̂����
		// MyRenderer()���ĂԑO�ɃC���[�W�A�C�R���̏��������s���K�v������
		String imageUrl = severUrl + "/image";
		URL url = null;
		try {
			url = new URL(imageUrl + "/data.png");
			MassIcon = new ImageIcon(url);
			url = new URL(imageUrl + "/none.png");
			EmptyIcon = new ImageIcon(url);
			url = new URL(imageUrl + "/plus.png");
			CloseIcon = new ImageIcon(url);
			url = new URL(imageUrl + "/minus.png");
			OpenIcon = new ImageIcon(url);
		} catch (MalformedURLException e) {
			// ������URL�̏ꍇ�̓A�C�R�����\������Ȃ������ŁA�ȍ~�̏����͍s�����Ƃ���
			e.printStackTrace();
		}
		
		if (getParameter("site") != null && !getParameter("site").equals("")) {
			site = getParameter("site");
		}
		if (getParameter("mode") != null && !getParameter("mode").equals("")) {
			mode = getParameter("mode").split(",");
		}
		
		InterNode dummy_root = new InterNode(MyTreeNode.DUMMY_ROOT, "", 1);
		dummy_root.loadSon();
		
		if (dummy_root.getChildCount() == 0) {
			// �A�C�R���\����ύX���邽�߃��[�g��EmptyNode�Ƃ���
			String [] siteName = conf.getSiteName();
			EmptyNode rootNode = new EmptyNode(MyTreeNode.DUMMY_ROOT, "MassBank / " + siteName[Integer.parseInt(site)]);
			
			treeModel = new DefaultTreeModel(rootNode);
			tree = new JTree(treeModel);
			tree.setCellRenderer(new MyRenderer());
			tree.setRootVisible(true);
		}
		else{
			treeModel = new DefaultTreeModel(dummy_root);
			tree = new JTree(treeModel);
				
			// �����\���̃c���[�W�J�Ή�
			InterNode rootNode = (InterNode)dummy_root.getFirstChild();
			rootNode.loadSon();
			tree.expandPath(new TreePath( rootNode.getPath()));
			// Mass�̉E�N���b�N�Ή�
			tree.addMouseListener(new ShowMass());
			// �c���[�̃N���b�N�񐔎w��
			tree.setToggleClickCount(1);
	
			tree.addTreeWillExpandListener(new MyTreeListener());
			tree.setCellRenderer(new MyRenderer());
			tree.setRootVisible(false);
		}
		
		JScrollPane sc = new JScrollPane(tree);
		JPanel c = new JPanel();
		c.setLayout(new BorderLayout());
		c.add(sc, BorderLayout.CENTER);
		add(c);
		applet = this;
	}
}
