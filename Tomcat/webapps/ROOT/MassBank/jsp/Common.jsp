<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%
/*******************************************************************************
 *
 * Copyright (C) 2009 JST-BIRD MassBank
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
 * ����JSP�i�ÓI�C���N���[�h�p�j
 *
 * ver 1.0.0 2009.12.09
 *
 ******************************************************************************/
%>
<%
	//-------------------------------------
	// �Q�Ɛ�x�[�XURL
	//-------------------------------------
	/* �ʏ�͌c��T�[�o���Q�Ƃ��� */
	String refBaseUrl = "http://www.massbank.jp/";
//	String refReqUrl = request.getRequestURL().toString();
//	String refBaseUrl = refReqUrl.substring( 0, (refReqUrl.indexOf("/jsp")+1) );
	
	//-------------------------------------
	// �u���E�U�D�挾��ɂ�錾�ꔻ��
	//-------------------------------------
	String browserLang = (request.getHeader("accept-language") != null) ? request.getHeader("accept-language") : "";
	boolean isJp = false;
	if ( browserLang.startsWith("ja") || browserLang.equals("") ) {
		isJp = true;
	}
	
	//-------------------------------------
	// �eURL�ݒ�
	//-------------------------------------
	String MANUAL_URL = refBaseUrl + "manuals/UserManual_ja.pdf";
	String SAMPLE_URL = refBaseUrl + "sample/sample1_ja.txt";
	String SAMPLE_ZIP_URL = refBaseUrl + "sample/sample.zip";
	if ( !isJp ) {
		MANUAL_URL = refBaseUrl + "manuals/UserManual_en.pdf";
		SAMPLE_URL = refBaseUrl + "sample/sample1_en.txt";
	}
%>
