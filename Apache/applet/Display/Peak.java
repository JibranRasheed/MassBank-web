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
 * �s�[�N�f�[�^�N���X
 *
 * ver 2.0.3 2009.12.07
 *
 ******************************************************************************/
import java.util.Vector;

public class Peak
{
	/** �s�[�N�� */
	private int peakNum = 0;
	
	/** m/z */
	private double[] mz;
	
	/** ���x */
	private int[] intensity;

	/** �s�[�N�I���t���O */
	private boolean[] selectPeakFlag;
	
	/**
	 * �R���X�g���N�^1
	 * @parama data �s�[�N�f�[�^
	 */
	public Peak(String[] data)
	{
		clear();
		
		peakNum = data.length;
		if (data.length == 1) {
			if (data[0].split(" ")[0].equals("0") && data[0].split(" ")[1].equals("0")) {
				peakNum = 0;
			}
		}
		mz = new double[peakNum];
		intensity = new int[peakNum];
		selectPeakFlag = new boolean[peakNum];
		String[] words;
		for(int i=0; i<peakNum; i++){
			words = data[i].split("	");
			mz[i] = Double.parseDouble(words[0]);
			intensity[i] = Integer.parseInt(words[1]);
		}
	}

	/**
	 * �R���X�g���N�^2
	 * @parama data �s�[�N�f�[�^
	 */
	public Peak(Vector<String> data)
	{
		clear();
		
		peakNum = data.size();
		if (data.size() == 1) {
			if (data.get(0).split("\t")[0].equals("0") && data.get(0).split("\t")[1].equals("0")) {
				peakNum = 0;
			}
		}
		mz = new double[peakNum];
		intensity = new int[peakNum];
		selectPeakFlag = new boolean[peakNum];
		int i = 0;
		String[] words;
		while ( data.size() > 0 ) {
			words = data.remove(0).split("\t");
			mz[i] = Double.parseDouble(words[0]);
			intensity[i] = Integer.parseInt(words[1]);
			i++;
		}
	}

	/**
	 * ������
	 * @
	 */
	public void clear()
	{
		mz = null;
		intensity = null;
		selectPeakFlag = null;
	}

	/**
	 * m/z���擾����
	 * @return m/z�l
	 */
	public double[] getMZ()
	{
		return mz;
	}

	/**
	 * intensity���擾����
	 * @return intensity�l
	 */
	public int[] getIntensity()
	{
		return intensity;
	}

	/**
	 * �ő�m/z�ƃv���J�[�T�[�̔�r
	 * @param �v���J�[�T�[
	 * @return �ő�m/z�ƃv���J�[�T�[�̑傫����
	 */
	public double compMaxMzPrecusor(String precursor) {
		double mzMax;
		if (mz.length == 0) {
			mzMax = 0f;
		}
		else {
			mzMax = mz[mz.length-1];
		}
		try {
			Float.parseFloat(precursor);
		} catch (Exception e) {
			return mzMax;
		}
		
		return Math.max(mzMax, Double.parseDouble(precursor));
	}
	
	/**
	 * intensity�̍ő�l���擾����
	 * @param start m/z�͈̔�1
	 * @param end   m/z�͈̔�2
	 * @return intensity�̍ő�l
	 */
	public int getMaxIntensity(double start, double end)
	{
		int max = 0;
		for ( int i = 0; i < intensity.length; i++ ) {
			if ( mz[i] > end ) {
				break;
			}
			if ( start <= mz[i] ) {
				if ( max < intensity[i] ) {
					max = intensity[i];
				}
			}
		}
		return max;
	}

	/**
	 * �s�[�N�����擾����
	 * @return �s�[�N��
	 */
	public int getCount()
	{
		return peakNum;
	}

	/**
	 * �w�肳�ꂽ�C���f�b�N�X��m/z���擾����
	 * @param index �C���f�b�N�X
	 * @return m/z�l
	 */
	public double getMz(int index)
	{
		if ( index < 0 || index >= peakNum ) {
			return -1.0f;
		}
		return mz[index];
	}

	/**
	 * �w�肳�ꂽ�C���f�b�N�X��intensity���擾����
	 * @param index �C���f�b�N�X
	 * @return intensity�l
	 */
	public int getIntensity(int index)
	{
		if ( index < 0 || index >= peakNum ) {
			return -1;
		}
		return intensity[index];
	}

	/**
	 * �w�肳�ꂽm/z�̃C���f�b�N�X���擾����
	 * @param target m/z�l
	 * @return �C���f�b�N�X
	 */
	public int getIndex(double target)
	{
		int i;
		for ( i = 0; i < peakNum; i++ ) {
			if ( mz[i] >= target ) {
				break;
			}
		}
		return i;
	}
	
	/**
	 * �s�[�N�I����Ԏ擾
	 * @param index �C���f�b�N�X
	 * @return �I����ԁitrue�F�I����, false�F���I���j
	 */
	public boolean isSelectPeakFlag(int index) {
		return selectPeakFlag[index];
	}

	/**
	 * �s�[�N�I����Ԑݒ�
	 * @param index �C���f�b�N�X
	 * @param flag �I����ԁitrue�F�I����, false�F���I���j
	 */
	public void setSelectPeakFlag(int index, boolean flag) {
		this.selectPeakFlag[index] = flag;
	}

	/**
	 * �s�[�N�I����ԏ�����
	 */
	public void initSelectPeakFlag() {
		this.selectPeakFlag = new boolean[peakNum];
	}
	/**
	 * �I����ԃs�[�N���擾 
	 * @return int �I����ԃs�[�N��
	 */
	public int getSelectPeakNum() {
		int num = 0;
		for (int i = 0; i < peakNum; i++) {
			if (selectPeakFlag[i]) {
				num++;
			}
		}
		return num;
	}
}
