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
 * Multiple Spectra Display�p MolViewPane �N���X
 *
 * ver 1.0.0 2009.12.14
 *
 ******************************************************************************/

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import metabolic.MolFigure;
import canvas.DrawPane;

@SuppressWarnings("serial")
public class MolViewPane extends DrawPane
{
	private boolean   alreadyRead;
	private draw2d.MOLformat mft;
	private MolFigure mf;
	Dimension size;

	/**
	 * �V����MolViewPane���\�z���܂��B
	 */
	public MolViewPane()
	{
		super(null, null);
		mft = new draw2d.MOLformat();
		alreadyRead = false;
		size = new Dimension(-1, -1);
	}

	/**
	 * ���������܂��B
	 */
	public void init()
	{
		util.MolMass.init();
		prepareMenusForPopups(false);
	}

	/**
	 * �N���A���܂��B
	 */
	public void clear()
	{
		if(mf != null)
			mf.clear();
	}

	/**
	 * �^����ꂽID�A���O�A�f�[�^���Z�b�g���܂��B
	 * @param id ID
	 * @param name ���O
	 * @param data mol�f�[�^
	 */
	public void read(String id, String name, String data)
	{
		mft.read(data);
		set(id, name);
		alreadyRead = true;
		size = getPaperSize();
		size.setSize(size.getWidth()-26, size.getHeight()-35);
		unselectAllSymbols();
	}

	/**
	 * �^����ꂽID�A���O�A�f�[�^���Z�b�g���܂��B
	 * @param id ID
	 * @param name ���O
	 * @param file mol�t�@�C��
	 */
	public void read(String id, String name, File file)
		throws FileNotFoundException, IOException
	{
		java.io.BufferedReader br = new java.io.BufferedReader(new InputStreamReader(new FileInputStream(file)));
		mft.read(br);
		set(id, name);
		alreadyRead = true;
		size = getPaperSize();
		size.setSize(size.getWidth()-26, size.getHeight()-35);
		unselectAllSymbols();
	}

	/**
	 * �V���{���̑I�����͂����܂��B
	 */
	public void unselectedSymbols()
	{
		unselectAllSymbols();
	}

	/**
	 * ���ɓǂݍ��ݍς݂ł��邩�𒲂ׂ܂��B
	 * @return ���ɓǂݍ��ݍς݂Ȃ�true�A�܂��Ȃ�false��Ԃ��܂��B
	 */
	public boolean isAlreadyRead()
	{
		return alreadyRead;
	}

	/**
	 * �T�C�Y��ݒ肵�܂��B
	 * @param w ��
	 * @param h ����
	 */
	public void setRectBound(float w, float h)
	{
		mf.setRectBound(w, h);
	}

	/**
	 * �X�P�[����ݒ肵�܂��B
	 * @param s �X�P�[��
	 */
	public void setScale(float s)
	{
		mf.setScale(s);
	}

	/**
	 * ���݂̃X�P�[�����擾���܂��B
	 * @return �X�P�[���l
	 */
	public float getScale()
	{
		return mf.getScale();
	}

	/**
	 * �V���{���̃T�C�Y���擾���܂��B
	 * @return �T�C�Y
	 */
	public Dimension getSymbolSize()
	{
		return size;
	}

	/**
	 * mol�t�@�C�����Z�b�g���A�`�悵�܂��B
	 */
	private void set(String id, String name)
	{
		mf = new MolFigure(id, name, mft);
		mf.initialization(this, new Point2D.Float(0, 0), 20);
		mf.setRectBound();
		newDraw(id, true);
		getLayer().addNew(mf, new Point2D.Float(5, 5), 0); // �ʒu
		Dimension d = getPictureSize();
		setPaperSize(d);
		setBackground(Color.WHITE);
		super.repaint();
	}
}