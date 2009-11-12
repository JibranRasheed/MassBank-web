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
 * TreeListener �N���X
 *
 * ver 2.0.5 2008.12.05
 *
 ******************************************************************************/

import javax.swing.tree.*;
import javax.swing.event.*;

/**
 * TreeListener �N���X
 */
public class MyTreeListener implements TreeWillExpandListener {
	
	/**
	 * �m�[�h�W�J
	 */
	public void treeWillExpand(TreeExpansionEvent e) {
		TreePath path = e.getPath();
		InterNode node = (InterNode) path.getLastPathComponent();
		if ( node.loaded ) {
			return;
		}
		node.loadSon();
		BrowsePage.treeModel.reload(node);
		
		// ���[�g�m�[�h����̋������擾
		int lebel = node.getLevel();
		
		// �T�C�g�ɂ���ăc���[�\���̕��@��؂�ւ���
		for (int i=0; i<BrowsePage.mode.length; i++) {
			if (lebel == Integer.parseInt(BrowsePage.mode[i]) && !node.isLeaf()) {
				// �q�m�[�h�W�J
				childNodeExpand(node);
			}
		}
	}

	/**
	 * �m�[�h���[
	 */
	public void treeWillCollapse(TreeExpansionEvent e) {
	}
	
	/**
	 * �����Ŏ󂯎�����m�[�h�̎q�m�[�h��W�J
	 * @param node ���ƂȂ�m�[�h
	 */
	private void childNodeExpand(InterNode node) {
		
		// 1�߂̎q�m�[�h���t�m�[�h�̏ꍇ�͓W�J�������s��Ȃ�
		if (node.getFirstChild() instanceof InterNode) {
			
			// 1�߂̎q�m�[�h�擾
			InterNode childNode = (InterNode)node.getFirstChild();
			
			for (int i=0; i<node.getChildCount(); i++) {
				
				// �q�m�[�h�܂ł�W�J
				BrowsePage.tree.expandPath(new TreePath(childNode.getPath()));
			
				// ���̎q�m�[�h�擾
				childNode = (InterNode)node.getChildAfter(childNode);
			}
		}
	}
}
