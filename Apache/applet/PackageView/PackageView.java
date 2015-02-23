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
 * PackageView �N���X
 *
 * ver 1.0.8 2011.08.10
 *
 ******************************************************************************/

import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import massbank.GetConfig;

/**
 * PackageView �N���X
 */
@SuppressWarnings("serial")
public class PackageView extends JApplet {

	public static String baseUrl = "";

	public static final int MAX_DISPLAY_NUM = 20;		// �ő�\���\����
	
	private PackageViewPanel pkgView = null;			// PackageViewPanel�R���|�[�l���g

	public static AppletContext context = null;		// �A�v���b�g�R���e�L�X�g
	public static int initAppletWidth = 0;			// �A�v���b�g������ʃT�C�Y(��)
	public static int initAppletHight = 0;			// �A�v���b�g������ʃT�C�Y(����)
	
	private int seaqCompound = 0;						// �����������������p
	private int seaqId = 0;							// ID���������p
	
	/**
	 * ���C���v���O����
	 */
	public void init() {
		
		// �A�v���b�g�R���e�L�X�g�擾
		context = getAppletContext();
		
		// �A�v���b�g������ʃT�C�Y�擾
		initAppletWidth = getWidth();
		initAppletHight = getHeight();

		// ���ݒ�t�@�C������A�g�T�C�g��URL���擾
		String confPath = getCodeBase().toString();
		confPath = confPath.replaceAll("/jsp", "");
		GetConfig conf = new GetConfig(confPath);
		baseUrl = conf.getServerUrl();
		
		
		// �c�[���`�b�v�}�l�[�W���[�ݒ�
		ToolTipManager ttm = ToolTipManager.sharedInstance();
		ttm.setInitialDelay(50);
		ttm.setDismissDelay(8000);
		
		
		setLayout(new BorderLayout());
		
		// ���C���p�l��
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		Border border = BorderFactory.createCompoundBorder(
				BorderFactory.createEtchedBorder(),	new EmptyBorder(0, 5, 0, 5));
		mainPanel.setBorder(border);
		
		// PackageView�����y�сA������
		pkgView = new PackageViewPanel();
		pkgView.initAllRecInfo();

		mainPanel.add(new HeaderPane(), BorderLayout.NORTH);
		mainPanel.add(pkgView, BorderLayout.CENTER);
		mainPanel.add(new FooterPane(), BorderLayout.SOUTH);
		
		add(mainPanel);
		
		
		// ���[�U�[�t�@�C���Ǎ���
		if (getParameter("file") != null) {
			loadFile(getParameter("file"));
		}
	}
	
	/**
	 * �t�@�C���ǂݍ��ݏ���
	 * @param fileName �t�@�C����
	 */
	private void loadFile(String fileName) {
		seaqCompound = 0;
		seaqId = 0;
		String reqUrl = baseUrl + "jsp/PackageView.jsp?file=" + fileName;
		ArrayList<String> lineList = new ArrayList<String>();
		
		try {
			
			URL url = new URL(reqUrl);
			URLConnection con = url.openConnection();

			// ���X�|���X�擾
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line = "";
			while ((line = in.readLine()) != null) {
				lineList.add(line);
			}
			in.close();
			
			// 1�s���ǂݍ��߂Ȃ������ꍇ
			if (lineList.size() == 0) {
				// ERROR�F�t�@�C��������܂���
				JOptionPane.showMessageDialog(null, "No file.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		catch (MalformedURLException mue) {			// URL��������
			mue.printStackTrace();
			// ERROR�F�T�[�o�[�G���[
			JOptionPane.showMessageDialog(null, "Server error.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (IOException ie) {					// ���o�͗�O
			ie.printStackTrace();
			// ERROR�F�T�[�o�[�G���[
			JOptionPane.showMessageDialog(null, "Server error.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// PackageView���R�[�h��񐶐�
		String line = "";
		String peaksLine = "";
		PackageRecData recData = null;
		int recNum = 0;
		try {
			for (int i=0; i<lineList.size(); i++) {
				
				line = lineList.get(i);
				
				// �R�����g�s�ǂݔ�΂�
				if (line.trim().startsWith("//")) {
					continue;
				}
				
				// ���R�[�h���擾����
				if (line.trim().indexOf(":") == -1 && line.trim().length() != 0) {
					if (recData == null) {
						recData = new PackageRecData();
					}
					if (line.lastIndexOf(";") != -1) {
						peaksLine += line.trim();						
					}
					else {
						peaksLine += line.trim() + ";";
					}
				}
				else if (line.trim().startsWith("Name:")) {
					if (recData == null) {
						recData = new PackageRecData();
					}
					recData.setName(line.substring(5).trim());
				}
				else if (line.trim().startsWith("Num Peaks:")) {
					// Num Peaks:�͖���
				}
				else if (line.trim().startsWith("Precursor:")) {
					if (recData == null) {
						recData = new PackageRecData();
					}
					recData.setPrecursor(line.substring(10).trim());
				}
				else if (line.trim().startsWith("ID:")) {
					if (recData == null) {
						recData = new PackageRecData();
					}
					recData.setId(line.substring(3).trim());
				}
				
				
				// ���R�[�h���ǉ�����
				if (line.trim().length() == 0 || i == lineList.size()-1) {
					
					if (recData != null) {
						
						recNum++;
						if (recNum > MAX_DISPLAY_NUM) {
							// WARNING�F�ő�20�X�y�N�g���܂ł̕\��
							JOptionPane.showMessageDialog(
									null,
									"Display of up to " + MAX_DISPLAY_NUM + " spectra.",
									"Warning",
									JOptionPane.WARNING_MESSAGE);
							break;
						}
						
						// == �N�G���[���R�[�h�t���O ===
						recData.setQueryRecord(false);
						
						// === ID ===
						if (recData.getId().equals("")) {
							recData.setId(createId());
						}
						
						// === �������� ===
						if (recData.getName().equals("")) {
							recData.setName(createName());
						}
						
						if (peaksLine.length() != 0) {
							
							// �s�[�N�����H(m/z������m/z�Ƌ��x�̑g�ݍ��킹)
							double max = 0d;
							ArrayList<String> peakList = new ArrayList<String>(Arrays.asList(peaksLine.split(";")));
							for (int j = 0; j < peakList.size(); j++) {
								peakList.set(j, peakList.get(j).replaceAll("^ +", ""));
								peakList.set(j, peakList.get(j).replaceAll(" +", "\t"));
								
								// �ő勭�x�ێ�
								if (max < Double.parseDouble(peakList.get(j).split("\t")[1])) {
									max = Double.parseDouble(peakList.get(j).split("\t")[1]);
								}
							}
							Collections.sort(peakList, new PeakComparator());
							
							// �����I�ɋ��x�𑊑΋��x�ɕϊ�
							for (int j = 0; j < peakList.size(); j++) {
								
								// m/z�ޔ�
								String tmpMz = peakList.get(j).split("\t")[0];
								
								// ���̋��x
								String beforeVal = peakList.get(j).split("\t")[1];
								
								// ���΋��x
								long tmpVal = Math.round(Double.parseDouble(beforeVal) / max * 999d);
								if (tmpVal > 999) { 
									tmpVal = 999;
								}
								if (tmpVal < 1) {
									tmpVal = 1;
								}
								String afterVal = String.valueOf(tmpVal);
								
								peakList.set(j, tmpMz + "\t" + afterVal);
							}
							
							// === �s�[�N�� ===
							int num = peakList.size();
							if (num == 1) {
								if (peakList.get(0).split("\t")[0].equals("0") && peakList.get(0).split("\t")[1].equals("0")) {
									num = 0;
								}
							}
							recData.setPeakNum( num );
							
							for (int j=0; j<recData.getPeakNum(); j++ ) {
								// === m/z ===
								recData.setMz( j, peakList.get(j).split("\t")[0] );
								
								// === ���x ===
								recData.setIntensity(j, peakList.get(j).split("\t")[1] );
							}
						}
						
						// === �s�[�N�F ===
						recData.setPeakColorType(PackageRecData.COLOR_TYPE_BLACK);
						
						// ���R�[�h���ǉ�
						pkgView.addRecInfo(recData);
					}
					
					recData = null;
					peaksLine = "";
				}
			}
		}
		catch (Exception e) {
			System.out.println("Illegal file format.");
			e.printStackTrace();
			// WARNING�F�t�@�C���t�H�[�}�b�g���s���ł�
			JOptionPane.showMessageDialog(null, "Illegal file format.", "Warning",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		// ���R�[�h���ǉ��㏈��
		pkgView.addRecInfoAfter(PackageSpecData.SORT_KEY_NAME);
	}

	/**
	 * ID����
	 * ID�������������ԋp����
	 * @return ��������
	 */
	private String createId() {
		String tmpId = "US";
		
		synchronized (this) {
			if (seaqId <= MAX_DISPLAY_NUM) {
				seaqId++;
			} else {
				seaqId = 0;
			}
		}
		DecimalFormat df = new DecimalFormat("000000");
		return tmpId + df.format(seaqId);
	}

	/**
	 * ������������
	 * ���������������������ԋp����
	 * @return ��������
	 */
	private String createName() {
		String tmpName = "Compound_";

		synchronized (this) {
			if (seaqCompound <= MAX_DISPLAY_NUM) {
				seaqCompound++;
			} else {
				seaqCompound = 0;
			}
		}
		DecimalFormat df = new DecimalFormat("00");
		return tmpName + df.format(seaqCompound);
	}
	
	/**
	 * �w�b�_�[�y�C��
	 * PackageView�̃C���i�[�N���X
	 */
	class HeaderPane extends JPanel {
		
		/**
		 * �R���X�g���N�^
		 */
		public HeaderPane() {

			JLabel title = new JLabel();
			title.setText(" Spectral Browser    ver. 1.07 ");
			title.setPreferredSize(new Dimension(0, 18));
			
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc;
			gbc = new GridBagConstraints();						// ���C�A�E�g���񏉊���
			gbc.fill = GridBagConstraints.HORIZONTAL;			// �����T�C�Y�̕ύX������
			gbc.weightx = 1;									// �]���̐����X�y�[�X�𕪔z
			gbc.weighty = 0;									// �]���̐����X�y�[�X�𕪔z���Ȃ�
			gbc.gridwidth = GridBagConstraints.REMAINDER;		// ��Ō�̃R���|�[�l���g�Ɏw��
			gbc.insets = new Insets(5, 0, 5, 0);
			gbl.setConstraints(title, gbc);
			
			setLayout(gbl);
			add(title);
		}
	}
	
	/**
	 * �t�b�^�[�y�C��
	 * PackageView�̃C���i�[�N���X
	 */
	class FooterPane extends JPanel {
		
		/**
		 * �R���X�g���N�^
		 */
		public FooterPane() {
			
			JLabel copyRighit = new JLabel();
			copyRighit.setText("Copyright (C) 2006 MassBank Project");
			copyRighit.setPreferredSize(new Dimension(0, 16));
			copyRighit.setHorizontalAlignment(SwingConstants.RIGHT);
			
			GridBagLayout gbl = new GridBagLayout();
			GridBagConstraints gbc;
			gbc = new GridBagConstraints();						// ���C�A�E�g���񏉊���
			gbc.fill = GridBagConstraints.HORIZONTAL;			// �����T�C�Y�̕ύX������
			gbc.weightx = 1;									// �]���̐����X�y�[�X�𕪔z
			gbc.weighty = 0;									// �]���̐����X�y�[�X�𕪔z���Ȃ�
			gbc.gridwidth = GridBagConstraints.REMAINDER;		// ��Ō�̃R���|�[�l���g�Ɏw��
			gbc.insets = new Insets(2, 0, 3, 1);
			gbl.setConstraints(copyRighit, gbc);
			
			setLayout(gbl);
			add(copyRighit);
		}
	}
	
	/**
	 * �s�[�N�R���p���[�^
	 * PackageView�̃C���i�[�N���X�B
	 * m/z�̏����\�[�g���s���B
	 */
	class PeakComparator implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			String mz1 = String.valueOf(o1).split("\t")[0];
			String mz2 = String.valueOf(o2).split("\t")[0];
			return Float.valueOf(mz1).compareTo(Float.valueOf(mz2));
		}
	}
}
