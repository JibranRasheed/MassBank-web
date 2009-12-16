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
 * �q�b�g�s�[�N���i�[ �N���X
 *
 * ver 2.0.5 2008.12.16
 *
 ******************************************************************************/

import java.util.ArrayList;

/**
 * �q�b�g�s�[�N���i�[ �N���X
 *-------------------------------------------------
 * [�\��]
 *  ���X�y�N�g��1�̏�� : mzInfoList[0]
 *        �b
 *        �� �q�b�g�s�[�N1�̏�� : MzInfo(0)
 *        �b        �b
 *        �b        �� diffmz         �s�[�N�� 
 *        �b        �� mz1Ary(0..n)   m/z1
 *        �b        �� mz2Ary(0..n)   m/z2
 *        �b        �� barColor(0..n) �`��F
 *        �b  �E
 *        �b  �E
 *        �b
 *        �� �q�b�g�s�[�Nn�̏�� MzInfo(n)
 *
 *
 *  ���X�y�N�g��2�̏�� mzInfoList[1]
 *      �E
 *      �E
 *  ���X�y�N�g��n�̏�� mzInfoList[n]
 *-------------------------------------------------
 */
public class HitPeaks
{
	/* �q�b�g�����s�[�N�̊i�[���X�g */
	public ArrayList<MzInfo>[] mzInfoList = null;
	/* �i�[���X�g�̏��� */
	private int pnum = 1;
	
	/**
	 * �i�[���X�g�̏��Ԃ��Z�b�g����
	 * @param pnum �i�[���X�g�̏���
	 */
	public void setListNum(int pnum) {
		this.pnum = pnum;
	}

	/**
	 * �i�[���X�g�̏��Ԃ��Z�b�g����
	 * @return pnum �i�[���X�g�̏���
	 */
	public int getListNum() {
		return this.pnum ;
	}

	/**
	 * �s�[�N�o�[�̕`��F���Z�b�g����
	 * @param idNum �\������X�y�N�g���̏���
	 * @param �i�[���X�g�̏���
	 */
	public void setBarColor( int idNum, int index, int colotTblNum ) {
		ArrayList<MzInfo> mzInfo = mzInfoList[idNum];
		MzInfo mzs = mzInfo.get( this.pnum - 1 );
		mzs.barColor.set( index, colotTblNum );
	}

	/**
	 * �s�[�N�o�[�̕`��F���擾����
	 * @param  idNum �\������X�y�N�g���̏���
	 * @return �i�[���X�g�̏���
	 */
	public ArrayList<Integer> getBarColor( int idNum ) {
		ArrayList<MzInfo> mzInfo = mzInfoList[idNum];
		MzInfo mzs = mzInfo.get( this.pnum - 1 );
		return mzs.barColor;
	}

	/**
	 * �q�b�g�����s�[�N���i�[����
	  * @param  hitMzInfo cgi����̕Ԃ��ꂽ���e
	  * @param  isDiff ture:�s�[�N������ / false:�s�[�N����
	  * @return �i�[�������X�g
	 */
	public ArrayList<MzInfo> setMz( String[] hitMzInfo, boolean isDiff ) {
		ArrayList<MzInfo> mzInfoList = new ArrayList<MzInfo>();
		MzInfo mzInfo = new MzInfo();
		// �s�[�N�������̏ꍇ
		if ( isDiff ) {
			double diffmz = 0;
			double diffmz2 = 0;
			for ( int i = 0; i < hitMzInfo.length; i++ ) {
				String[] val = hitMzInfo[i].split(",");
				diffmz = Double.parseDouble(val[0]);
				if ( diffmz != diffmz2 ) {
					if ( i > 0 ) {
						mzInfoList.add(mzInfo);
					}
					mzInfo = new MzInfo();
					mzInfo.diffmz = val[0];
				}
				if ( val.length > 1  ) {
					double mz1 = Double.parseDouble(val[1]);
					double mz2 = Double.parseDouble(val[2]);
					mzInfo.mz1Ary.add(mz1);
					mzInfo.mz2Ary.add(mz2);
				}
				mzInfo.barColor.add(null);
				diffmz2 = diffmz;
			}
		}
		// �s�[�N�����̏ꍇ
		else {
			for ( int i = 0; i < hitMzInfo.length; i++ ) {
				double mz1 = Double.parseDouble(hitMzInfo[i]);
				mzInfo.mz1Ary.add(mz1);
				mzInfo.barColor.add(0);
			}
		}
		mzInfoList.add(mzInfo);
		return mzInfoList;
	}

	/**
	 * �s�[�N���i����������m/z�j�̒l���擾����
	  * @return �s�[�N���l�̃��X�g
	 */
	public String[] getDiffMz( int idNum ) {
		String[] diffmzs = new String[ mzInfoList[idNum].size() ]; 
		for ( int i = 0; i < mzInfoList[idNum].size(); i++) {
			MzInfo MzInfo = mzInfoList[idNum].get(i);
			diffmzs[i] = String.valueOf(MzInfo.diffmz);
		}
		return diffmzs;
	}

	/**
	 * �q�b�g�����s�[�N���擾����(1)
	  * @param  idNum �\������X�y�N�g���̏���
	  * @return �q�b�g�����s�[�N�̃��X�g
	 */
	public ArrayList<Double> getMz1( int idNum ) {
		return getMzList( idNum, this.pnum, 1 );
	}

	/**
	 * �q�b�g�����s�[�N���擾����(2)
	 * ���s�[�N�������̏ꍇ�̂ݎg�p
	 * @param  idNum �\������X�y�N�g���̏���
	 * @return �q�b�g�����s�[�N�̃��X�g
	 */
	public ArrayList<Double> getMz2( int idNum ) {
		return getMzList( idNum, this.pnum, 2 );
	}

	/**
	 * �s�[�N�̃��X�g���擾����
	 */
	private ArrayList<Double> getMzList( int idNum, int pnum, int flg ) {
		ArrayList<MzInfo> mzInfo = mzInfoList[idNum];
		MzInfo mzs = mzInfo.get( pnum - 1 );
		ArrayList<Double> mzAry = new ArrayList<Double>();
		if ( flg == 0 || flg == 1 ) {
			mzAry.addAll( mzs.mz1Ary );
		}
		if ( flg == 0 || flg == 2 ) {
			mzAry.addAll( mzs.mz2Ary );
		}
		return mzAry;
	}

	/**
	 * �q�b�g�����s�[�N��m/z���i�[����N���X
	 */
	public class MzInfo 
	{
		String diffmz = "";
		ArrayList<Double> mz1Ary = new ArrayList<Double>();
		ArrayList<Double> mz2Ary = new ArrayList<Double>();
		ArrayList<Integer> barColor = new ArrayList<Integer>();
	}
}

