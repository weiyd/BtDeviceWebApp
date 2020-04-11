package com.cmtech.web.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.cmtech.web.btdevice.BleEcgRecord10;
import com.cmtech.web.btdevice.RecordType;
import com.cmtech.web.dbop.Account;
import com.cmtech.web.util.MySQLUtil;


import static com.cmtech.web.util.MySQLUtil.INVALID_ID;


/**
 * Servlet implementation class RecordUploadServlet
 */
@WebServlet(name="RecordUploadServlet", urlPatterns="/RecordUpload")
public class RecordUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RecordUploadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		MySQLUtil.connect();
		String strRecordTypeCode = request.getParameter("recordTypeCode");
		int recordTypeCode = Integer.parseInt(strRecordTypeCode);
		RecordType type = RecordType.getType(recordTypeCode);
		String strCreateTime = request.getParameter("createTime");
		long createTime = Long.parseLong(strCreateTime);
		String devAddress = request.getParameter("devAddress");
		
		int id = INVALID_ID;
		switch(type) {
		case ECG:
			id = BleEcgRecord10.getId(createTime, devAddress);
			break;
		default:
			break;
		}
		
		JSONObject json = new JSONObject();
		json.put("id", id);
		
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=utf-8");
		PrintWriter out = null;
		try {
			out = response.getWriter();
			out.append(json.toString());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}
		MySQLUtil.disconnect();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		BufferedReader streamReader = null;
		try {
			String charEncoding = request.getCharacterEncoding();
	        if (charEncoding == null) {
	            charEncoding = "UTF-8";
	        }
			streamReader = new BufferedReader( new InputStreamReader(request.getInputStream(), charEncoding));
			StringBuilder strBuilder = new StringBuilder();
			String inputStr;
			while ((inputStr = streamReader.readLine()) != null)
				strBuilder.append(inputStr);
			JSONObject jsonObject = new JSONObject(strBuilder.toString());
			System.out.println(jsonObject.toString());
			
			RecordType type = RecordType.getType(jsonObject.getInt("recordTypeCode"));
			long createTime = jsonObject.getLong("createTime");
			String devAddress = jsonObject.getString("devAddress");
			if(type != RecordType.ECG) return;
			if(BleEcgRecord10.getId(createTime, devAddress) != INVALID_ID) return;
			
			byte[] ver = (byte[]) jsonObject.get("ver");
			String creatorPlat = jsonObject.getString("creatorPlat");
			String creatorId = jsonObject.getString("creatorId");
			int sampleRate = jsonObject.getInt("sampleRate");
			int caliValue = jsonObject.getInt("caliValue");
			int leadTypeCode = jsonObject.getInt("leadTypeCode");
			int recordSecond = jsonObject.getInt("recordSecond");
			String note = jsonObject.getString("note");
			String ecgData = jsonObject.getString("ecgData");

			MySQLUtil.connect();
			BleEcgRecord10 record = new BleEcgRecord10();
			record.setVer(ver);
			record.setCreateTime(createTime);
			record.setDevAddress(devAddress);
			record.setCreator(new Account(creatorPlat, creatorId));
			record.setSampleRate(sampleRate);
			record.setCaliValue(caliValue);
			record.setLeadTypeCode(leadTypeCode);
			record.setRecordSecond(recordSecond);
			record.setNote(note);
			String[] ecgStrArr = ecgData.split(",");
			List<Short> ecgArr = new ArrayList<>();
			for(String str : ecgStrArr) {
				ecgArr.add(Short.parseShort(str));
			}
			record.setEcgData(ecgArr);
			if(record.insert()) {
				System.out.println("插入记录成功,id="+record.getId());
			} else {
				System.out.println("插入记录失败");
			}
			MySQLUtil.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			streamReader.close();
		}
		
		
//		MySQLUtil.connect();
//		BleEcgRecord10 record = new BleEcgRecord10();
//		record.setVer(new byte[] {0x01,0x00});
//		record.setCreateTime(new Date().getTime());
//		record.setDevAddress("12:34:56:78");
//		record.setCreator(new Account("chenm", "ctl080512"));
//		record.setSampleRate(125);
//		record.setCaliValue(164);
//		if(record.insert()) {
//			System.out.println("插入记录成功,id="+record.getId());
//		} else {
//			System.out.println("插入记录失败");
//		}
//		MySQLUtil.disconnect();
	}

}