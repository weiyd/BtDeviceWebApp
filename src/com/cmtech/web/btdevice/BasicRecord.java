package com.cmtech.web.btdevice;

import static com.cmtech.web.MyConstant.DEFAULT_VER;
import static com.cmtech.web.MyConstant.INVALID_ID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONObject;

import com.cmtech.web.dbUtil.DbUtil;

public abstract class BasicRecord implements IDbOperation, IJsonable{
	private static final String[] BASIC_PROPERTIES = {"ver", "creatorPlat", "creatorId", "note", "recordSecond"};
	private static final String[] INSERT_PROPERTIES = {"createTime", "devAddress", "ver", "creatorPlat", "creatorId", "note", "recordSecond"};
    private String ver; // record version
    private final RecordType type;
    private final long createTime; //
    private final String devAddress; //
    private String creatorPlat;
    private String creatorId;
    private String note;
    private int recordSecond;

    protected BasicRecord(RecordType type, long createTime, String devAddress) {
    	ver = DEFAULT_VER;
    	this.type = type;
        this.createTime = createTime;
        this.devAddress = devAddress;
        creatorPlat = "";
        creatorId = "";
        note = "";
        recordSecond = 0;
    }    
    
    public RecordType getType() {
		return type;
	}

    public String getVer() {
    	return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public long getCreateTime() {
        return createTime;
    }

    public String getDevAddress() {
        return devAddress;
    }

    public String getRecordName() {
        return createTime + devAddress;
    }

    public String getCreatorPlat() {
        return creatorPlat;
    }

    public String getCreatorId() {
    	return creatorId;
    }

    public void setCreator(Account creator) {
        this.creatorPlat = creator.getPlatName();
        this.creatorId = creator.getPlatId();
    }

    public String getNote() {
    	return note;
    }

    public void setNote(String note) {
    	this.note = note;
    }

    public int getRecordSecond() {
		return recordSecond;
	}

	public void setRecordSecond(int recordSecond) {
		this.recordSecond = recordSecond;
	}
	
	public abstract String[] getProperties();
    
	@Override
    public void fromJson(JSONObject json) {
    	if(json.has("ver")) {
			ver = json.getString("ver");			
    	} else {
    		ver = DEFAULT_VER;
    	}
		creatorPlat = json.getString("creatorPlat");
		creatorId = json.getString("creatorId");
		note = json.getString("note");
		recordSecond = json.getInt("recordSecond");
    }
    
    @Override
	public JSONObject toJson() {    	
		return basicToJson();
	}
    
    public JSONObject basicToJson() {
    	JSONObject json = new JSONObject();
		json.put("recordTypeCode", type.getCode());
		json.put("createTime", createTime);
		json.put("devAddress", devAddress);
    	json.put("ver", ver);
		json.put("creatorPlat", creatorPlat);
		json.put("creatorId", creatorId);
		json.put("note", note);
		json.put("recordSecond", recordSecond);
		return json;
    }

	public void getPropertiesFromResultSet(ResultSet rs) throws SQLException {
		getBasicPropertiesFromResultSet(rs);
	}
	
	public int setPropertiesToPreparedStatement(PreparedStatement ps) throws SQLException {
		int begin = 1;
		ps.setLong(begin++, createTime);
		ps.setString(begin++, devAddress);
		ps.setString(begin++, ver);
		ps.setString(begin++, creatorPlat);
		ps.setString(begin++, creatorId);
		ps.setString(begin++, note);
		ps.setInt(begin++, recordSecond);
		return begin;
	}

	@Override
    public String toString() {
        return type + "-" + createTime + "-" + devAddress + "-" + creatorPlat + "-" + creatorId + "-" + note;
    }

    @Override
    public boolean equals(Object otherObject) {
        if(this == otherObject) return true;
        if(otherObject == null) return false;
        if(getClass() != otherObject.getClass()) return false;
        BasicRecord other = (BasicRecord) otherObject;
        return getRecordName().equals(other.getRecordName());
    }

    @Override
    public int hashCode() {
        return getRecordName().hashCode();
    }

    @Override
    public final int getId() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return INVALID_ID;
    	
		Connection conn = DbUtil.connect();
		if(conn == null) return INVALID_ID;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id from " + tableName + " where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, createTime);
			ps.setString(2, devAddress);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getInt("id");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return INVALID_ID;	
	}
    
    @Override
	public boolean retrieve() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select " + getPropertiesString() + " from " + tableName + " where devAddress = ? and createTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getDevAddress());
			ps.setLong(2, getCreateTime());
			rs = ps.executeQuery();
			if(rs.next()) {
				getPropertiesFromResultSet(rs);
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;
	}
    
	// UPDATE NOTE
    @Override
	public final boolean update() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "update " + tableName + " set note = ? where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, note);
			ps.setLong(2, createTime);
			ps.setString(3, devAddress);			
			if(ps.executeUpdate() != 0) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
    
    // INSERT
    @Override
	public final boolean insert() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		int id = getId();
		if(id != INVALID_ID) return false;
		
		Connection conn = DbUtil.connect();
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "insert into " + tableName + " (" + getInsertPropertiesString() + ") values (" + getInsertQuestionMark() + ")";
		try {
			ps = conn.prepareStatement(sql);
			setPropertiesToPreparedStatement(ps);
			if(ps.executeUpdate() != 0)
				return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
    
	// DELETE
    @Override
	public boolean delete() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		String sql = "delete from " + tableName + " where createTime = ? and devAddress = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setLong(1, createTime);
			ps.setString(2, devAddress);
			if(ps.executeUpdate() != 0)
				return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(null, ps, conn);
		}
		return false;
	}
    
    public boolean retrieveBasicInfo() {
    	String tableName = type.getTableName();
    	if("".equals(tableName)) return false;
    	
		Connection conn = DbUtil.connect();		
		if(conn == null) return false;
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select " + getBasicPropertiesString() + " from " + tableName + " where devAddress = ? and createTime = ?";
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, getDevAddress());
			ps.setLong(2, getCreateTime());
			rs = ps.executeQuery();
			if(rs.next()) {
				getBasicPropertiesFromResultSet(rs);
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return false;
	}
	
	private void getBasicPropertiesFromResultSet(ResultSet rs) throws SQLException {
		ver = rs.getString("ver");
		creatorPlat = rs.getString("creatorPlat");
		creatorId = rs.getString("creatorId");
		note = rs.getString("note");
		recordSecond = rs.getInt("recordSecond");
	}
    
    private String getBasicPropertiesString() {
    	StringBuilder builder = new StringBuilder();
    	int len = BASIC_PROPERTIES.length;
    	for(int i = 0; i < len-1; i++) {
    		builder.append(BASIC_PROPERTIES[i]).append(',');
    	}
    	builder.append(BASIC_PROPERTIES[len-1]);
    	return builder.toString();
    }

    private String getPropertiesString() {
    	StringBuilder builder = new StringBuilder(getBasicPropertiesString());
    	builder.append(',');
    	String[] properties = getProperties();
    	int len = properties.length;
    	for(int i = 0; i < len-1; i++) {
    		builder.append(properties[i]).append(',');
    	}
    	builder.append(properties[len-1]);
    	return builder.toString();
    }
    
    private String getInsertPropertiesString() {
    	StringBuilder builder = new StringBuilder();
    	for(String str : INSERT_PROPERTIES) {
    		builder.append(str).append(',');
    	}
    	String[] properties = getProperties();
    	int len = properties.length;
    	for(int i = 0; i < len-1; i++) {
    		builder.append(properties[i]).append(',');
    	}
    	builder.append(properties[len-1]);
    	return builder.toString();
    }
    
    private String getInsertQuestionMark() {
    	int num = INSERT_PROPERTIES.length + getProperties().length;
    	StringBuilder builder = new StringBuilder();
		for(int i = 0; i < num-1; i++) {
			builder.append('?').append(',');
		}
		builder.append('?');
		return builder.toString();
    }
    
    public static List<BasicRecord> findRecord(RecordType type, String creatorPlat, String creatorId, long fromTime, String noteSearchStr, int num) {
    	if(num <= 0) return null;
		
		List<RecordType> types = new ArrayList<>();
		if(type == RecordType.ALL) {
			for(RecordType t : RecordType.values()) {
				if(t != RecordType.ALL && t != RecordType.TH) {
					types.add(t);
				}
			}
		}
		else
			types.add(type);
		
		List<BasicRecord> found = new ArrayList<>();
		for(RecordType t : types) {
			List<BasicRecord> tmp = BasicRecord.searchRecordOfType(t, creatorPlat, creatorId, fromTime, noteSearchStr, num);
			if(tmp != null && !tmp.isEmpty())
				found.addAll(tmp);
		}
		Collections.sort(found, new Comparator<BasicRecord>() {
		    @Override
		    public int compare(BasicRecord o1, BasicRecord o2) {
		    	int rlt = 0;
		        if(o2.getCreateTime() > o1.getCreateTime()) rlt = 1;
		        else if(o2.getCreateTime() < o1.getCreateTime()) rlt = -1;
		        return rlt;
		    }
		});

		int N = Math.min(num, found.size());			
		for(int i = 0; i < N; i++) {
			found.get(i).retrieveBasicInfo();
		}
		
		return found.subList(0, N);
    }

	private static List<BasicRecord> searchRecordOfType(RecordType type, String creatorPlat, String creatorId, long fromTime, String noteSearchStr, int num) {
		if(num <= 0) return null;
		String tableName = type.getTableName();
		if("".equals(tableName)) return null;
		
		Connection conn = DbUtil.connect();		
		if(conn == null) return null;		
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = "select createTime, devAddress from " + tableName;
			String where = " where ";
			if(!"".equals(creatorPlat)) {
				where += "creatorPlat = ? and ";
			}
			if(!"".equals(creatorId)) {
				where += "creatorId = ? and "; 
			}
			if(!"".equals(noteSearchStr)) {
				where += "note REGEXP ? and ";
			}
			where += "createTime < ? order by createTime desc limit ?";
			sql += where;
			
			ps = conn.prepareStatement(sql);
			int i = 1;
			if(!"".equals(creatorPlat)) {
				ps.setString(i++, creatorPlat);
			}
			if(!"".equals(creatorId)) {
				ps.setString(i++, creatorId);
			}
			if(!"".equals(noteSearchStr)) {
				ps.setString(i++, noteSearchStr);
			}
			ps.setLong(i++, fromTime);
			ps.setInt(i++, num);
			rs = ps.executeQuery();

			List<BasicRecord> found = new ArrayList<>();
			while(rs.next()) {
				long createTime = rs.getLong("createTime");
				String devAddress = rs.getString("devAddress");
				found.add(RecordFactory.create(type, createTime, devAddress));
			}			
			return found;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbUtil.close(rs, ps, conn);
		}
		return null;
	}
}
