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
 * �X�y�N�g���\���p�l���N���X
 *
 * ver 1.0.2 2011.08.10
 *
 ******************************************************************************/
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import massbank.MassBankCommon;

public class PlotPane extends JPanel implements MouseListener, MouseMotionListener
{
	private static final int MARGIN = 15;
	private static final int MIN_RANGE = 5;
	private static final int INTENSITY_MAX = 1000;
	private static final Color[] colorTbl = {
		new Color(0xD2,0x69,0x48), new Color(0x22,0x8B,0x22),
		new Color(0x41,0x69,0xE1), new Color(0xBD,0x00,0x8B),
		new Color(0x80,0x80,0x00), new Color(0x8B,0x45,0x13),
		new Color(0x9A,0xCD,0x32)
	};

	private final double TOLERANCE = 0.01d;

	private double massStart = 0;
	private double massRange = 0;
	private double massMax = 0;
	private int hitNum = 0;
	private boolean isMZFlag = false;
	private String[] diffMzs = null;
	private int intensityRange = INTENSITY_MAX;

	private Peak peaks1 = null;
	private String precursor = "";

	private long lastClickedTime = 0;		// �Ō�ɃN���b�N��������
	private Timer timer = null;

	private boolean underDrag = false;
	private Point fromPos = null;
	private Point toPos = null;
	private double xscale = 0;
	private double yscale = 0;

	private String reqType = "";
	private ArrayList<Double>[] hitPeaks1 = null;
	private ArrayList<Double>[] hitPeaks2 = null;
	private ArrayList<Integer>[] barColors = null;
	private int diffMargin = 0;
	private ArrayList<String> formulas = null;

	private Graphics g = null;
	private int panelWidth = 0;
	private int panelHeight = 0;

	private ArrayList<Double> mz1Ary = null;
	private ArrayList<Double> mz2Ary = null;
	private ArrayList<Integer> colorAry = null;

	private int hitMz1Cnt = -1;
	private int hitMz2Cnt = -1;
	private double prevHitMz1 = 0;
	private double prevHitMz2 = 0;

	private Point cursorPoint = null;		// �}�E�X�J�[�\���|�C���g
	private final Color onCursorColor = Color.blue;	// �J�[�\����F
	private final Color selectColor = Color.cyan.darker();	// �I���s�[�N�F
	
	private ArrayList<String> selectPeakList = null;
	
	private JPopupMenu selectPopup = null;			// �s�[�N�I���|�b�v�A�b�v���j���[
	private JPopupMenu contextPopup = null;		// �R���e�L�X�g�|�b�v�A�b�v���j��
	
	/**
	 * �R���X�g���N�^
	 * @
	 */
	public PlotPane(String reqType, Peak p, ColorInfo colorInfo, String precursor) {
		this.selectPeakList = new ArrayList<String>();
		this.peaks1 = p;
		this.precursor = precursor;

		//---------------------------------------------
		// m/z�̍ő�l���Z�b�g����
		//---------------------------------------------
		double maxMz = 0;
		double massMax = 0;

		//(1) �s�[�N�f�[�^������ꍇ
		if ( this.peaks1.getCount() > 0 ) {
			maxMz = this.peaks1.compMaxMzPrecusor(precursor);

			// m/z�̍ő�l�𐮐���2�Ő؂�グ���l�������W�̍ő�l�Ƃ���
			massMax = new BigDecimal(maxMz).setScale(-2, BigDecimal.ROUND_UP).intValue();
		}
		//(2) �s�[�N�f�[�^���Ȃ��ꍇ
		else {
			if ( !precursor.equals("") ) {
				massMax = Double.parseDouble(precursor);
			}
		}
		this.massMax = massMax;
		
		// maxMz��100�Ŋ���؂��ꍇ��+100�̗]�T������
		if (maxMz != 0d && (maxMz % 100.0d) == 0d) {
			maxMz += 100.0d;
		}
		
		// massRange��100�P�ʂɂ��낦��
		this.massRange = (double)Math.ceil(maxMz / 100.0) * 100.0d;
		
		//---------------------------------------------
		// �s�[�N�̐F�Â��ɕK�v�ȏ����Z�b�g����
		//---------------------------------------------
		this.reqType = reqType;
		if ( colorInfo != null ) {
			this.hitPeaks1  = colorInfo.getHitPeaks1();
			this.hitPeaks2  = colorInfo.getHitPeaks2();
			this.barColors  = colorInfo.getBarColors();
			this.diffMzs    = colorInfo.getDiffMzs();
			this.diffMargin = colorInfo.getDiffMargin();
			this.formulas   = colorInfo.getFormulas();
		}

		cursorPoint = new Point();
		
		// ���X�i�[�ǉ�
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/**
	 * 
	 */
	int setStep(int range)
	{
		if (range < 20)  return 2;
		if (range < 50)  return 5;
		if (range < 100) return 10;
		if (range < 250) return 25;
		if (range < 500) return 50;
		return 100;
	}

	/**
	 * �s�[�N�̕`�揈��
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		this.g = g;

		//---------------------------------------------
		// �t���[����`�悷��
		//---------------------------------------------
		drawChartFrame();

		//---------------------------------------------
		// �s�[�N��`�悷��
		//---------------------------------------------
		if ( this.peaks1 == null || this.peaks1.getCount() == 0 ) {
			// �s�[�N���Ȃ��ꍇ�̕\��
			drawNoPeak();
		}
		else {
			// �s�[�N�o�[��`��
			drawPeakBar();

			// �s�[�N�������̃q�b�g�ʒu��\��
			if ( this.reqType.equals("diff") ) {
				drawHitDiffPos();
			}
			else if ( this.reqType.equals("nloss") ) {
				drawHitNlossPos();
			}
		}

		//---------------------------------------------
		// �v���J�[�T�[m/z�Ƀ}�[�N�t��
		//---------------------------------------------
		drawPrecursorMark();

		//---------------------------------------------
		// �}�E�X�Ńh���b�O�����̈�����F�����ň͂�
		//---------------------------------------------
		if ( underDrag ) {
			fillRectRange();
		}
	}

	/**
	 * �t���[����`�悷��
	 */
	private void drawChartFrame()
	{
		//---------------------------------------------
		// �㕔�]���̃T�C�Y���Z�b�g
		//---------------------------------------------
	 	int marginTop = 0;
		if ( this.reqType.equals("diff") ) {
			marginTop = MARGIN + 10 + 12 * this.diffMargin;
		}
		else if ( this.reqType.equals("product") ) {
			marginTop = MARGIN + 10 + 20;
		}
		else if ( this.reqType.equals("nloss") ) {
			marginTop = MARGIN + 10 + 60;
		}
		else {
			marginTop = MARGIN;
		}

		this.panelWidth = getWidth();
		this.panelHeight = getHeight();
		this.xscale = (this.panelWidth - 2.0d * MARGIN) / massRange;
		this.yscale = (this.panelHeight - (double)(MARGIN + marginTop) ) / this.intensityRange;

		//---------------------------------------------
		// �w�i�F�𔒂œh��Ԃ�
		//---------------------------------------------
		g.setColor(Color.white);
		g.fillRect(0, 0, this.panelWidth, this.panelHeight);

		g.setFont(g.getFont().deriveFont(9.0f));
		g.setColor(Color.lightGray);

		int x = MARGIN;
		int y = this.panelHeight - MARGIN;
		g.drawLine(x, marginTop, x, y);
		g.drawLine(x, y, this.panelWidth - MARGIN, y);

		//---------------------------------------------
		// x��
		//---------------------------------------------
		int step = setStep((int)massRange);
		int start = (step - (int)massStart % step) % step;
		y = this.panelHeight - MARGIN;
		for (int i = start; i < (int)massRange; i += step) {
			x = MARGIN + (int)(i * xscale);
			g.drawLine(x, y, x, y + 3);

			// �����_�ȉ��̌�������
			String mzStr = formatMass(i + massStart, true);
			g.drawString(mzStr, x - 5, this.panelHeight - 1);
		}

		//---------------------------------------------
		// y��
		//---------------------------------------------
		for (int i = 0; i <= this.intensityRange; i += this.intensityRange / 5) {
			y = this.panelHeight - MARGIN - (int)(i * yscale);
			g.drawLine(MARGIN - 2, y, MARGIN, y);
			g.drawString(String.valueOf(i), 0, y);
		}
	}


	/**
	 * �s�[�N�o�[��`�悷��(Peak Search�̏ꍇ)
	 */
	private void drawPeakBar()
	{
		if ( this.reqType.equals("peak") || this.reqType.equals("diff") ) {
			this.mz1Ary = this.hitPeaks1[hitNum];
			this.mz2Ary = this.hitPeaks2[hitNum];
			this.colorAry = this.barColors[hitNum];
		}
		else if ( this.reqType.equals("product") || this.reqType.equals("nloss") ) {
			this.mz1Ary = this.hitPeaks1[0];
			this.mz2Ary = this.hitPeaks2[0];
		}

		boolean isOnPeak;		// �J�[�\���s�[�N��t���O
		boolean isSelectPeak;	// �I���ς݃s�[�N�t���O
		int start = this.peaks1.getIndex(massStart);
		int end = this.peaks1.getIndex(massStart + massRange);

		for ( int i = start; i < end; i++ ) {
			isOnPeak = false;
			isSelectPeak = this.peaks1.isSelectPeakFlag(i);
			double mz = this.peaks1.getMz(i);
			int its = this.peaks1.getIntensity(i);
			int w = (int)(xscale / 8);
			int h = (int)(its * yscale);
			int x = MARGIN + (int) ((mz - massStart) * xscale) - (int) Math.floor(xscale / 8);
			int y = this.panelHeight - MARGIN - h;
			
			// �`��p�����[�^�i�����A�ʒu�j����
			if (h == 0) {
				y -= 1;
				h = 1;
			}
			// �`��p�����[�^�i���j����
			if ( w < 2 ) {
				w = 2;
			} else if (w < 3) {
				w = 3;
			}
			
			// y����荶���ɂ͕`�悵�Ȃ��悤�ɒ���
			if (MARGIN >= x) {
				w = (w - (MARGIN - x) > 0) ? (w - (MARGIN - x)) : 1;
				x = MARGIN + 1;
			}
			
			// �J�[�\���s�[�N�㔻��
			if (x <= cursorPoint.getX() 
					&& cursorPoint.getX() <= (x + w)
					&& y <= cursorPoint.getY() 
					&& cursorPoint.getY() <= (y + h)) {
				
				isOnPeak = true;
			}
			
			//---------------------------------------------
			// �q�b�g�����s�[�N�ɐF�Â�����
			//---------------------------------------------
			Color color = Color.black;
			//(1) Peak Search�̏ꍇ
			if ( this.reqType.equals("peak") || this.reqType.equals("diff") ) {
				color = getColorForPeak(mz);
			}
			//(2) Quick Search by Peak�̏ꍇ
			else if ( this.reqType.equals("qpeak") ) {
				color = getColorForQuick(mz);
			}
			//(3) Product Ion�����̏ꍇ
			else if ( this.reqType.equals("product") ) {
				color = drawHitProduct(mz, its);
			}
			//(4) Neutral Loss�����̏ꍇ
			else if ( this.reqType.equals("nloss") ) {
				color = getColorForNloss(mz);
			}

			boolean isHit = true;
			if ( color == Color.black ) {
				isHit = false;
			}
			
			//---------------------------------------------
			// �s�[�N�o�[�`��
			//---------------------------------------------
			if ( isOnPeak ) {
				color = onCursorColor;
				if ( isSelectPeak ) {
					color = selectColor;
				}
			}
			else if ( isSelectPeak ) {
				color = selectColor;
			}
			//** �`��F���Z�b�g **
			g.setColor(color);
			g.fill3DRect(x, y, w, h, true);

			//----------------------------------------------------
			// m/z�l��`��
			// 1) "all m/z"�{�^��ON�ŁAintensity��400�ȏ�́u�ԐF�v
			// 2) "all m/z"�{�^��OFF�ŁA�q�b�g�s�[�N�̓o�[�Ɠ��F
			// 3) ����ȊO�͍��F
			//----------------------------------------------------
			if ( its > this.intensityRange * 0.4 || isMZFlag || isHit || isOnPeak || isSelectPeak ) {
				float fontSize = 9.0f;
				if ( isOnPeak ) {
					color = onCursorColor;
					fontSize = 14.0f;
					if ( isSelectPeak ) {
						color = selectColor;
					}
				}
				else if ( isSelectPeak ) {
					color = selectColor;
				}
				else if ( isMZFlag && its > this.intensityRange * 0.4 ) {
					color = Color.red;
				}
				g.setFont(g.getFont().deriveFont(fontSize));
				g.setColor(color);
				
				// �����_�ȉ��̌�������
				String mzStr = formatMass(mz, false);
				g.drawString(mzStr, x, y);
			}
			
			//----------------------------------------------------
			// ���x�l��`��
			//----------------------------------------------------
			if ( isOnPeak || isSelectPeak ) {
				// ���x�ڐ���`��
				if ( isOnPeak ) {
					g.setColor(onCursorColor);
				}
				if ( isSelectPeak ) {
					g.setColor(selectColor);
				}
				g.drawLine(MARGIN + 4, y, MARGIN - 4, y);
				
				// ���x�l�`��
				g.setColor(Color.lightGray);
				g.setFont(g.getFont().deriveFont(9.0f));
				if ( isOnPeak && isSelectPeak ) {
					g.setColor(Color.gray);
				}
				g.drawString(String.valueOf(its), MARGIN + 7, y + 1);
			}
		}
	}

	/**
	 * �`��F���擾����(�ʏ�Peak Search�̏ꍇ)
	 */
	private Color getColorForPeak(double mz)
	{
		int num = 1;
		boolean isHit = false;

		//(1) �q�b�g�s�[�Nmz1�ƈ�v���Ă��邩
		for ( int j = 0; j < this.mz1Ary.size(); j++ ) {
			double hitMz1 = this.mz1Ary.get(j);
			if ( mz == hitMz1 ) {
				isHit = true;
				if ( hitMz1 - this.prevHitMz1 >= 1 ) {
					this.hitMz1Cnt++;
				}
				if ( this.colorAry.get(j) != null ) {
					num = this.colorAry.get(j);
				}
				else {
					num = this.hitMz1Cnt - (this.hitMz1Cnt / colorTbl.length) * colorTbl.length;
					this.barColors[hitNum].set( j, num );
				}
				this.prevHitMz1 = hitMz1;
				break;
			}
		}

		//(2) mz1�ƕs��v�̏ꍇ�A�q�b�g�s�[�Nmz2�ƈ�v���Ă��邩
		if ( !isHit ) {
			for ( int j = 0; j < this.mz2Ary.size(); j++ ) {
				double hitMz2 = this.mz2Ary.get(j);
				if ( mz == hitMz2 ) {
					isHit = true;
					if ( colorAry.get(j) != null ) {
						num = colorAry.get(j);
					}
					else if ( hitMz2 - this.prevHitMz2 >= 1 ) {
						num = this.hitMz2Cnt - (this.hitMz2Cnt / colorTbl.length) * colorTbl.length;
						this.barColors[hitNum].set( j, num );
					}
					else {}
					this.hitMz2Cnt++;
					this.prevHitMz2 = hitMz2;
					break;
				}
			}
		}

		if ( isHit ) {
			return colorTbl[num];
		}
		return Color.black;
	}

	/**
	 * �`��F���擾����(Quick Search by Peak�̏ꍇ)
	 */
	private Color getColorForQuick(double mz)
	{
		double hitMz = 0;
		boolean isQhit = false;
		Color color = Color.black;

		// [���S��v] - �ԐF���Z�b�g
		if ( this.hitPeaks1.length == 2 ) {
			ArrayList<Double> hitMzAry = this.hitPeaks1[1];
			for ( int j = 0; j < hitMzAry.size(); j++ ) {
				hitMz = hitMzAry.get(j);
				if ( mz == hitMz ) {
					isQhit = true;
					color = Color.RED;
					break;
				}
			}
		}
		// [�g�������X���ɓ����Ă���] - �}�[���^�F���Z�b�g
		if ( !isQhit ) {
			ArrayList<Double> tolMzAry = this.hitPeaks1[0];
			for ( int j = 0; j < tolMzAry.size(); j++ ) {
				hitMz = tolMzAry.get(j);
				if ( mz == hitMz ) {
					isQhit = true;
					color = Color.MAGENTA;
					break;
				}
			}
		}

		//** �`��F���Z�b�g **
		if ( isQhit ) {
			return color;
		}
		return Color.black;
	}


	/**
	 * �`��F���擾����(Neutral Loss�����̏ꍇ)
	 */
	private Color getColorForNloss(double mz)
	{
		int num = 0;
		String prevFormula = "";
		double hitMz = 0;
		for ( int i = 0; i < this.mz1Ary.size(); i++ ) {
			double mz1 = this.mz1Ary.get(i);
			double mz2 = this.mz2Ary.get(i);
			if ( mz >= mz1 - TOLERANCE && mz <= mz1 + TOLERANCE ) {
				hitMz = mz1;
				break;
			}
			else if ( mz >= mz2 - TOLERANCE && mz <= mz2 + TOLERANCE ) {
				hitMz = mz2;
				break;
			}
			String formula = this.formulas.get(i);
			if ( !formula.equals(prevFormula) ) {
				num++;
			}
			prevFormula = formula;
		}

		ArrayList<Double> mzAry = new ArrayList<Double>();
		if ( hitMz > 0 ) {
//  		System.out.println("hitMz:" + String.valueOf(hitMz));
			for ( int i = 0; i < peaks1.getCount(); i++ ) {
				double getMz = this.peaks1.getMz(i);
				if ( getMz >= hitMz - TOLERANCE && getMz <= hitMz + TOLERANCE ) {
					mzAry.add( getMz );
				}
			}

			int maxInte = 0;
			int maxInteIndex = 0;
			for ( int i = 0; i < mzAry.size(); i++ ){
				int index = this.peaks1.getIndex( mzAry.get(i) );
				int inte = this.peaks1.getIntensity(index);
				if ( inte > maxInte ) {
					maxInte = inte;
					maxInteIndex = index;
				}
			}

			if ( maxInte > 0 && mz == this.peaks1.getMz(maxInteIndex) ) {
				return Color.MAGENTA;
			}
		}
		return Color.black;
	}

	/**
	 * ���q����`�悵�A�`��F��Ԃ�(Product Ion�����̏ꍇ)
	 */
	private Color drawHitProduct(double mz, int its)
	{
		for ( int i = 0; i < this.mz1Ary.size(); i++ ) {
			double mz1 = this.mz1Ary.get(i);
			if ( mz >= mz1 - TOLERANCE && mz <= mz1 + TOLERANCE ) {
				ArrayList<Double> mzAry = new ArrayList<Double>();
				for ( int j = 0; j < peaks1.getCount(); j++ ) {
					double getMz = this.peaks1.getMz(j);
					if ( getMz >= mz1 - TOLERANCE && getMz <= mz1 + TOLERANCE ) {
						mzAry.add( getMz );
					}
				}

				int maxInte = 0;
				int maxInteIndex = 0;
				for ( int j = 0; j < mzAry.size(); j++ ){
					int index = this.peaks1.getIndex( mzAry.get(j) );
					int inte = this.peaks1.getIntensity(index);
					if ( inte > maxInte ) {
						maxInte = inte;
						maxInteIndex = index;
					}
				}
				if ( maxInte > 0 && mz == this.peaks1.getMz(maxInteIndex) ) {
					String formula = this.formulas.get(i);
					int barWidth = (int)Math.floor(this.xscale / 8);
					int x = MARGIN + (int)((mz1 - this.massStart) * this.xscale) - barWidth / 2;
					int y = this.panelHeight - MARGIN - (int)(its * yscale) - 10;
					int xm = (int)(formula.length() * 5) + 10;

					g.setColor( Color.MAGENTA );
					g.fillRect( x - 1, y - 9, xm, 11 );
					g.setColor( Color.WHITE );
					g.drawString( formula, x, y );
					return Color.MAGENTA;
				}
			}
		}
		return Color.black;
	}

	/**
	 * �s�[�N�f�[�^�Ȃ���\������
	 */
	private void drawNoPeak()
	{
		g.setFont( new Font("Arial", Font.ITALIC, 24) );
		g.setColor( Color.LIGHT_GRAY );
		g.drawString( "No peak was observed.", this.panelWidth / 2 - 110, this.panelHeight / 2 );
	}

	/**
	 * �s�[�N�������̃q�b�g�ӏ���\������
	 */
	private void drawHitDiffPos()
	{
		String diffmz = diffMzs[hitNum];
		int pos = diffmz.indexOf(".");
		if ( pos > 0 ) {
			BigDecimal bgMzZDiff = new BigDecimal( diffmz );
			diffmz = (bgMzZDiff.setScale(1, BigDecimal.ROUND_DOWN)).toString(); 
		}

		double mz1Prev = 0;
		int hitCnt = 0;
		for ( int j = 0; j < this.mz1Ary.size(); j++ ) {
			double mz1 = this.mz1Ary.get(j);
			double mz2 = this.mz2Ary.get(j);
			if ( mz1 - mz1Prev >= 1 ) {
				g.setColor(Color.GRAY);

				/* �s�[�N�o�[�̃��C���� */
				int barWidth = (int)Math.floor(xscale / 8);
				/* �������̊J�n�ʒu */
				int x1 = MARGIN + (int)((mz1 - massStart) * xscale) - barWidth / 2;
				/* �����E�̊J�n�ʒu */
				int x2 = MARGIN + (int)((mz2 - massStart) * xscale) - barWidth / 2;
				/* �����E�̊J�n�ʒu */
				int xc = x1 + (x2 - x1) / 2 - 12;
				/* �x���W */
				int y = this.panelHeight - ( MARGIN + (int)((INTENSITY_MAX + MARGIN*2) * yscale) + 5 + (++hitCnt * 12) );
				/* ������ */
				int xm = (int)(diffmz.length() * 5) + 4;

				int padding = 5;

				// �����`��
				g.drawLine( x1, y , xc, y );
				g.drawLine( xc + xm + padding, y, x2, y );

				// �c���`��
				g.drawLine( x1, y, x1, y + 4 );
				g.drawLine( x2, y, x2, y + 4 );

				// �s�[�N���̒l��`��
				int num = colorAry.get(j);
				g.setColor( colorTbl[num] );
				g.fillRect( xc, y - padding, (xc + xm + padding) - xc, padding * 2 );
				g.setColor( Color.WHITE );
				g.drawString( diffmz, xc + padding , y + 3 );
			}
			mz1Prev = mz1;
		}
	}

	/**
	 * �j���[�g�������X�������̃q�b�g�ӏ���\������
	 */
	private void drawHitNlossPos()
	{
		this.mz1Ary = this.hitPeaks1[0];
		this.mz2Ary = this.hitPeaks2[0];
		String prevFormula = "";
		int prevX2 = 0;
		int colorNum = -1;
		int num = 0;
		for ( int j = 0; j < this.mz1Ary.size(); j++ ) {
			double mz1 = this.mz1Ary.get(j);
			double mz2 = this.mz2Ary.get(j);
			String formula = this.formulas.get(j);
			g.setColor(Color.GRAY);

			/* �s�[�N�o�[�̃��C���� */
			int barWidth = (int)Math.floor(xscale / 8);
			/* �������̊J�n�ʒu */
			int x1 = MARGIN + (int)((mz1 - massStart) * xscale) - barWidth / 2;
			/* �����E�̊J�n�ʒu */
			int x2 = MARGIN + (int)((mz2 - massStart) * xscale) - barWidth / 2;
			/* �x���W */
			if ( x1 < prevX2 ) {
				num++;
			}
			int y = this.panelHeight - ( MARGIN + (int)((INTENSITY_MAX + MARGIN*2) * yscale) + 12 + num * 13 );
			/* ������ */
			int xm = (int)(formula.length() * 5) + 4;

			int padding = 5;

			// �����`��
			g.drawLine( x1, y , x2, y );

			// �c���`��
			g.drawLine( x1, y, x1, y + 4 );
			g.drawLine( x2, y, x2, y + 4 );

			// �s�[�N���̒l��`��
			if ( !formula.equals(prevFormula) ) {
				colorNum++;
			}
			g.setColor( colorTbl[colorNum] );

			int width = xm + padding * 2;
			int hight = padding * 2 - 1;
			int x = x1 + (x2 - x1 - width) / 2;
			if ( width > x2 - x1 ) {
				x = x1;
			}

			// �h��Ԃ����l�p�ɕ��q���̕�����`��
			g.fillRect( x, y - padding - 3, width - 1, hight );
			g.setColor( Color.WHITE );
			g.drawString( formula, x + (padding / 2) + 1, y );
			prevFormula = formula;
			prevX2 = x2;
		}
	}

	/**
	 * �v���J�[�T�[m/z�Ƀ}�[�N�t����
	 */
	private void drawPrecursorMark()
	{
		if ( !this.precursor.equals("") ) {
			int pre = Integer.parseInt(this.precursor);
			int xPre = MARGIN + (int)((pre - massStart) * xscale) - (int)Math.floor(xscale / 8);
			int yPre = this.panelHeight - MARGIN;
			// �v���J�[�T�[m/z���O���t���̏ꍇ�̂ݕ`�悷��
			if (xPre >= MARGIN && xPre <= this.panelWidth - MARGIN) {
				int [] xp = { xPre, xPre + 6, xPre - 6 };
				int [] yp = { yPre, yPre + 6, yPre + 6 };
				g.setColor( Color.RED );
				g.fillPolygon( xp, yp, xp.length );
			}
		}
	}

	/**
	 * �}�E�X�Ńh���b�O�����̈�����F�����ň͂�
	 */
	private void fillRectRange()
	{
		int xpos = Math.min(fromPos.x, toPos.x);
		int width = Math.abs(fromPos.x - toPos.x);
		g.setXORMode(Color.white);
		g.setColor(Color.yellow);
		g.fillRect(xpos, 0, width, this.panelHeight - MARGIN);
		g.setPaintMode();
	}

	/**
	 * m/z�̕\���p�t�H�[�}�b�g
	 * ��ʕ\���p��m/z�̌��������킹�ĕԋp����
	 * @param mass �t�H�[�}�b�g�Ώۂ�m/z
	 * @param isForce ������������t���O�itrue:0���߂Ɛ؎̂Ă��s���Afalse:�؎̂Ă̂ݍs���j
	 * @return �t�H�[�}�b�g���m/z
	 */
	private String formatMass(double mass, boolean isForce) {
		final int ZERO_DIGIT = 4;
		String massStr = String.valueOf(mass);
		if (isForce) {
			// �����I�ɑS�Ă̌��𓝈ꂷ��i0���߂Ɛ؎̂Ă��s���j
			if (massStr.indexOf(".") == -1) {
				massStr += ".0000";
			}
			else {
				if (massStr.indexOf(".") != -1) {
					String [] tmpMzStr = massStr.split("\\.");
					if (tmpMzStr[1].length() <= ZERO_DIGIT) {
						int addZeroCnt = ZERO_DIGIT - tmpMzStr[1].length();
						for (int j=0; j<addZeroCnt; j++) {
							massStr += "0";
						}
					}
					else {
						if (tmpMzStr[1].length() > ZERO_DIGIT) {
							massStr = tmpMzStr[0] + "." + tmpMzStr[1].substring(0, ZERO_DIGIT);
						}
					}
				}
			}
		}
		else {
			// ���𒴂���ꍇ�̂݌��𓝈ꂷ��i�؎̂Ă̂ݍs���j
			if (massStr.indexOf(".") != -1) {
				String [] tmpMzStr = massStr.split("\\.");
				if (tmpMzStr[1].length() > ZERO_DIGIT) {
					massStr = tmpMzStr[0] + "." + tmpMzStr[1].substring(0, ZERO_DIGIT);
				}
			}
		}
		return massStr;
	}
	
	/**
	 * massStart�l���Z�b�g
	 */
	public void setMassStart(double val)
	{
		this.massStart = val;
	}

	/**
	 * massMax�l���Z�b�g
	 */
	public void setMassMax(double val)
	{
		this.massMax = val;
	}

	/**
	 * isMZFlag�l���Z�b�g
	 */
	public void setIsMZFlag(boolean val)
	{
		this.isMZFlag = val;
	}

	/**
	 * hitNum�l���Z�b�g
	 */
	public void setHitNum(int val)
	{
		this.hitNum = val;
	}


	/**
	 * massStart�l���擾
	 */
	public double getMassStart()
	{
		return this.massStart;
	}

	/**
	 * massRange�l���擾
	 */
	public double getMassRange()
	{
		return this.massRange;
	}

	/**
	 * massMax�l���擾
	 */
	public double getMassMax()
	{
		return this.massMax;
	}


	/**
	 * �}�E�X�v���X�C�x���g
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		if ( SwingUtilities.isLeftMouseButton(e) ) {
			if (timer != null && timer.isRunning()) {
				return;
			}
			fromPos = toPos = e.getPoint();
		}
	}

	/**
	 * �}�E�X�h���b�O�C�x���g
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		if ( SwingUtilities.isLeftMouseButton(e) ) {
			if (timer != null && timer.isRunning()) {
				return;
			}
			this.underDrag = true;
			toPos = e.getPoint();
			repaint();
		}
	}

	/**
	 * �}�E�X�����[�X�C�x���g
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		// �������[�X�̏ꍇ
		if ( SwingUtilities.isLeftMouseButton(e) ) {
			if (!underDrag || (timer != null && timer.isRunning())) {
				return;
			}
			underDrag = false;
			if ((fromPos != null) && (toPos != null)) {
				if (Math.min(fromPos.x, toPos.x) < 0) {
					massStart = Math.max(0, massStart - massRange / 3);
				}
				else if (Math.max(fromPos.x, toPos.x) > getWidth()) {
					massStart = Math.min(massMax - massRange, massStart + massRange / 3);
				}
				else {
					if ( this.peaks1 != null ) {
						timer = new Timer(30,
								new AnimationTimer(Math.abs(fromPos.x - toPos.x),
										Math.min(fromPos.x, toPos.x)));
						timer.start();
					} else {
						fromPos = toPos = null;
						repaint();
					}
				}
			}
		}
		// �E�����[�X�̏ꍇ
		else if (SwingUtilities.isRightMouseButton(e)) {
			
			if (timer != null && timer.isRunning()) {
				return;
			}
			
			contextPopup = new JPopupMenu();
			
			JMenuItem item1 = null;
			item1 = new JMenuItem("Peak Search");
			item1.setActionCommand("search");
			item1.addActionListener(new ContextPopupListener());
			item1.setEnabled(false);
			contextPopup.add(item1);
			
			JMenuItem item2 = null;
			item2 = new JMenuItem("Select Reset");
			item2.setActionCommand("reset");
			item2.addActionListener(new ContextPopupListener());
			item2.setEnabled(false);
			contextPopup.add(item2);
			
			if (peaks1 != null) {
				if (selectPeakList.size() != 0) {
					item1.setEnabled(true);
					item2.setEnabled(true);
				}
			}
			
			// �|�b�v�A�b�v���j���[�\��
			contextPopup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * �}�E�X�N���b�N�C�x���g
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		if (timer != null && timer.isRunning()) {
			return;
		}
		
		// ���N���b�N�̏ꍇ
		if ( SwingUtilities.isLeftMouseButton(e) ) {
			
			// �N���b�N�Ԋu�Z�o
			long interSec = (e.getWhen() - lastClickedTime);
			lastClickedTime = e.getWhen();
			
			// �_�u���N���b�N�̏ꍇ�i�N���b�N�Ԋu280�~���b�ȓ��j
			if(interSec <= 280){
				fromPos = toPos = null;
				initMass();
			}
			// �V���O���N���b�N�̏ꍇ
			else {
				// �}�E�X�N���b�N�|�C���g
				Point p = e.getPoint();
				
				ArrayList<Integer> tmpClickPeakList = new ArrayList<Integer>();
				
				int height = getHeight();
				float yscale = (height - 2.0f * MARGIN) / intensityRange;
				int start, end, its, tmpX, tmpY, tmpWidth, tmpHight;
				double mz;
				start = peaks1.getIndex(massStart);
				end = peaks1.getIndex(massStart + massRange);

				for (int i = start; i < end; i++) {

					mz = peaks1.getMz(i);
					its = peaks1.getIntensity(i);
					tmpX = MARGIN + (int) ((mz - massStart) * xscale)
							- (int) Math.floor(xscale / 8); // Peak�`��n�_�iX���W�j
					tmpY = height - MARGIN - (int) (its * yscale); // Peak�`��n�_�iY���W�j
					tmpWidth = (int) (xscale / 8); // �n�_����̕�
					tmpHight = (int) (its * yscale); // �n�_����̍���

					if (MARGIN > tmpX) {
						tmpWidth = tmpWidth - (MARGIN - tmpX);
						tmpX = MARGIN;
					}

					if (tmpWidth < 2) {
						tmpWidth = 2;
					} else if (tmpWidth < 3) {
						tmpWidth = 3;
					}

					// �}�E�X�_�E�������ꏊ�iX/Y���W�j��Peak�̕`��G���A�Ɋ܂܂�Ă��邩�𔻒�
					if (tmpX <= p.getX() && p.getX() <= (tmpX + tmpWidth)
							&& tmpY <= p.getY()
							&& p.getY() <= (tmpY + tmpHight)) {

						tmpClickPeakList.add(i);
					}
				}

				// �}�E�X�_�E���|�C���g��Peak��1����ꍇ�A
				// �}�E�X�N���b�N�Ɠ�����Peak�̐F��ύX����
				if (tmpClickPeakList.size() == 1) {

					int index = tmpClickPeakList.get(0);

					if (!peaks1.isSelectPeakFlag(index)) {
						if (peaks1.getSelectPeakNum() < MassBankCommon.PEAK_SEARCH_PARAM_NUM) {
							// �I����Ԃ�ݒ�
							selectPeakList.add(String.valueOf(peaks1
									.getMz(index)));
							peaks1.setSelectPeakFlag(index, true);
						} else {
							JOptionPane.showMessageDialog(PlotPane.this,
									" m/z of " + MassBankCommon.PEAK_SEARCH_PARAM_NUM + " peak or more cannot be selected. ",
									"Warning",
									JOptionPane.WARNING_MESSAGE);
							cursorPoint = new Point();
						}
					} else if (peaks1.isSelectPeakFlag(index)) {

						// �I����Ԃ�����
						selectPeakList.remove(String.valueOf(peaks1
								.getMz(index)));
						peaks1.setSelectPeakFlag(index, false);
					}
					PlotPane.this.repaint();
				}
				// �}�E�X�_�E���|�C���g��Peak��2�ȏ゠��ꍇ�A
				// �}�E�X�N���b�N�Ɠ����Ƀ|�b�v�A�b�v���j���[��\������
				else if (tmpClickPeakList.size() >= 2) {

					// �|�b�v�A�b�v���j���[�C���X�^���X����
					selectPopup = new JPopupMenu();
					JMenuItem item = null;
					int index = -1;

					// �|�b�v�A�b�v���j���[�ǉ�
					for (int i = 0; i < tmpClickPeakList.size(); i++) {

						index = tmpClickPeakList.get(i);
						item = new JMenuItem(String.valueOf(peaks1.getMz(index)));
						selectPopup.add(item);
						item.addActionListener(new SelectMZPopupListener(index));

						if (peaks1.getSelectPeakNum() >= MassBankCommon.PEAK_SEARCH_PARAM_NUM
								&& !peaks1.isSelectPeakFlag(index)) {

							// Peak�I�𐔂�MAX�̏ꍇ�A�I���ς�Peak�ȊO�͑I��s��ݒ�
							item.setEnabled(false);
						}
					}

					// �|�b�v�A�b�v���j���[�\��
					selectPopup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		}
	}

	/**
	 * ������
	 */
	public void initMass()
	{
		massRange = this.peaks1.compMaxMzPrecusor(this.precursor);
		
		// massRange��100�Ŋ���؂��ꍇ��+100�̗]�T������
		if (massRange != 0d && (massRange % 100.0d) == 0d) {
			massRange += 100.0d;
		}
		
		// massRange��100�P�ʂɂ��낦��
		massRange = (double) Math.ceil(massRange / 100.0) * 100.0d;
		massStart = 0;
		this.intensityRange = INTENSITY_MAX;
		repaint();
	}

	/**
	 * �}�E�X�G���^�[�C�x���g
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}
	
	/**
	 * �}�E�X�C�O�W�b�g�C�x���g
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}
	
	/**
	 * �}�E�X���[�u�C�x���g
	 * @see java.awt.event.MouseMotionListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		// �|�b�v�A�b�v���\������Ă���ꍇ
		if ((selectPopup != null && selectPopup.isVisible())
				|| contextPopup != null && contextPopup.isVisible()) {
			
			return;
		}
		cursorPoint = e.getPoint();
		PlotPane.this.repaint();
	}

	/**
	 * �g�又�����A�j���[�V����������N���X
	 */
	class AnimationTimer implements ActionListener
	{
		final int LOOP = 15;
		int loopCoef;
		int minx;
		int width;
		double tmpMassStart;
		double tmpMassRange;
		int tmpIntensityRange;
		int movex;

		public AnimationTimer(int w, int x)
		{
			loopCoef = 0;
			minx = x;
			width = w;
			movex = 0 + MARGIN;
			// �ړI�g�嗦���Z�o
			double xs = (getWidth() - 2.0f * MARGIN) / massRange;
			tmpMassStart = massStart
					+ ((minx - MARGIN) / xs);
			tmpMassRange = 10 * (width / (10 * xs));
			if (tmpMassRange < MIN_RANGE) {
				tmpMassRange = MIN_RANGE;
			}

			// Intensity�̃����W��ݒ�
			if ((peaks1 != null) && (massRange <= massMax)) {
				// �ő�l�����o�B
				int max = 0;
				double start = Math.max(tmpMassStart, 0.0d);
				max = peaks1.getMaxIntensity(start, start + tmpMassRange);
				// 50�P�ʂɕϊ����ăX�P�[��������
				tmpIntensityRange = (int)((1.0f + max / 50.0f) * 50.0f);
				if (tmpIntensityRange > INTENSITY_MAX) {
					tmpIntensityRange = INTENSITY_MAX;
				}
			}
		}

		public void actionPerformed(ActionEvent e)
		{
			xscale = (getWidth() - 2.0f * MARGIN) / massRange;
			double yscale = (getHeight() - 2.0f * MARGIN) / intensityRange;
			int xpos = (movex + minx) / 2;
			if (Math.abs(massStart - tmpMassStart) <= 2
			 && Math.abs(massRange - tmpMassRange) <= 2)
			{
				xpos = minx;
				massStart = tmpMassStart;
				massRange = tmpMassRange;
				timer.stop();
			} else {
				loopCoef++;
				massStart += (((tmpMassStart + massStart) / 2 - massStart) * loopCoef / LOOP);
				massRange += (((tmpMassRange + massRange) / 2 - massRange) * loopCoef / LOOP);
				intensityRange += (((tmpIntensityRange + intensityRange) / 2 - intensityRange) * loopCoef / LOOP);
				if (loopCoef >= LOOP) {
					movex = xpos;
					loopCoef = 0;
				}
			}
			repaint();
		}
	}
	
	/**
	 * �s�[�N�I���|�b�v�A�b�v���j���[���X�i�[�N���X
	 * PlotPane�̃C���i�[�N���X
	 */
	class SelectMZPopupListener implements ActionListener {

		/** �C���f�b�N�X */
		private int index = -1;
		
		/**
		 * �R���X�g���N�^
		 * @param index �C���f�b�N�X
		 */
		public SelectMZPopupListener(int index) {
			this.index = index;
		}

		/**
		 * �A�N�V�����C�x���g
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {

			if (!peaks1.isSelectPeakFlag(index)
					&& peaks1.getSelectPeakNum() < MassBankCommon.PEAK_SEARCH_PARAM_NUM) {
				// �I����Ԃ�ݒ�
				selectPeakList.add(String.valueOf(peaks1.getMz(index)));
				peaks1.setSelectPeakFlag(index, true);
			} else if (peaks1.isSelectPeakFlag(index)) {
				// �I����Ԃ�����
				selectPeakList.remove(String.valueOf(peaks1.getMz(index)));
				peaks1.setSelectPeakFlag(index, false);
			}

			cursorPoint = new Point();
			PlotPane.this.repaint();
		}
	}
	
	/**
	 * �R���e�L�X�g�|�b�v�A�b�v���j���[���X�i�[�N���X
	 * PlotPane�̃C���i�[�N���X
	 */
	class ContextPopupListener implements ActionListener {
		
		/**
		 * �R���X�g���N�^
		 */
		public ContextPopupListener() {
		}

		/**
		 * �A�N�V�����C�x���g
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {

			String com = e.getActionCommand();
			
			if (com.equals("search")) {
				// URL�p�����[�^����
				StringBuffer urlParam = new StringBuffer();

				String typeName = MassBankCommon.CGI_TBL[MassBankCommon.CGI_TBL_NUM_TYPE][MassBankCommon.CGI_TBL_TYPE_PEAK];

				urlParam.append("?type=" + typeName);							// type�Fpeak
				urlParam.append("&num=" + peaks1.getSelectPeakNum());			// num �F
				urlParam.append("&tol=0");										// tol �F0
				urlParam.append("&int=5");										// int �F5
				
				for (int i = 0; i < peaks1.getSelectPeakNum(); i++) {
					if (i != 0) {
						urlParam.append("&op" + i + "=and");					// op �Fand
					} else {
						urlParam.append("&op" + i + "=or");						// op �For
					}
					urlParam.append("&mz" + i + "=" + selectPeakList.get(i));	// mz �F
				}
				urlParam.append("&sortKey=name&sortAction=1&pageNo=1&exec=&inst=all");
				
				// JSP�Ăяo��
				String reqUrl = Display.baseUrl + "jsp/Result.jsp"
						+ urlParam.toString();
				try {
					Display.context.showDocument(new URL(reqUrl), "_blank");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else if (com.equals("reset")) {
				if (peaks1 != null) {
					selectPeakList = new ArrayList<String>();
					peaks1.initSelectPeakFlag();
				}
			}
			
			cursorPoint = new Point();
			PlotPane.this.repaint();
		}
	}
}
