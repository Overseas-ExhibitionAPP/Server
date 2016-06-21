package RESTfulServer.V1;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoSocketReadTimeoutException;

import DBConnection.MongoJDBC;
import DBConnection.MssqlJDBC;

@Path("/V1/school")
public class SchoolFunc {
    MssqlJDBC ms;
    //台灣五大地區所包含的縣市名稱
    String[][] school_areaList ={
            {"台北","臺北","新北","基隆","桃園","新竹","苗栗"},
            {"台中","臺中","彰化","南投","雲林"},
            {"嘉義","台南","臺南","高雄","屏東"},
            {"宜蘭","花蓮","台東","臺東"},
            {"澎湖","金門"}
        };
    public SchoolFunc() throws Exception{
        ms = new MssqlJDBC();
    }
    @PUT
    @Path("/search")
    @Consumes("application/json; charset=UTF-8")
    @Produces("application/json; charset=UTF-8")
    public Response searchSchoolList(String input) throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try {
            JSONObject jinput = new JSONObject(input);
            JSONArray deptGroup = jinput.getJSONArray("DepartList");
            JSONArray area = jinput.getJSONArray("AreaList");
            ms.connectionServer("forschool");
            String sql = "select depart.schoolcode,school.chineseName from depart,school where ";
            if(deptGroup.length() != 0 && deptGroup.length() < 18) {
                /*
                 * 須過濾兩種狀況
                 * 1.無任何學群被勾選即是全學群
                 * 2.勾選學群數若為18，則為全學群
                 */
                sql += "(";
                for(int i = 0; i < deptGroup.length(); i++) {
                    sql += ("deptgroup = " + deptGroup.getString(i));
                    if(i < (deptGroup.length() - 1)) {
                        sql += " or ";
                    }
                }
                sql += ") and ";
            }
            if(area.length() != 0 && area.length() < 5) {
                /*
                 * 須過濾兩種狀況
                 * 1.無任何地區被勾選即是全地區
                 * 2.勾選地區若為五區，則為全地區
                 */
                sql += "(";
                for(int i = 0; i < area.length(); i++) {
                    int areaIndex = area.getInt(i);
                    for(int j = 0; j < school_areaList[areaIndex].length; j++) {
                        if(j > 0 && j < school_areaList[areaIndex].length) {
                            sql+=" or ";
                        }
                        sql += ("school.address like '%" + school_areaList[areaIndex][j] + "%'");
                    }
                    if(i < (area.length() - 1)) {
                        sql += " or ";
                    }
                }
                sql += ") and ";
            }
                    
            sql += "school.schoolcode = depart.schoolcode and ";
            sql += "school.survey = '"+"Y' ";
            sql += "group by depart.schoolcode,school.chineseName";
            ms.executeQueryCommand(sql);
            ResultSet result = ms.rs;
            JSONArray tmpArr = new JSONArray();
            while(result.next()) {
                JSONObject tmp = new JSONObject();
                tmp.put("schoolnum", result.getString(1));
                tmp.put("schoolname", result.getString(2));
                tmpArr.put(tmp);
            }
            output.put("status", "200");
            output.put("searchList", tmpArr);
        } catch(JSONException err) {
            output = new JSONObject();
            output.put("status", "400");
            output.put("message","Request格式或資料錯誤");
        }catch(SQLException sqle) {
            output = new JSONObject();
            output.put("status", "502");
            output.put("message","資料庫查詢錯誤");
        } catch(Exception err) {
            output = new JSONObject();
            output.put("status", "500");
            output.put("message","伺服器錯誤");
        } finally {
            re.setResponse(output.toString());
            ms.closeConnection();
        }
        return re.builder.build();
    }
    @GET
    @Path("/{sch_id}")
    @Produces("application/json; charset=UTF-8")
    public Response getSchoolInfo(@PathParam("sch_id")String input) throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try {
            JSONObject jinput = new JSONObject(input);
            JSONArray deptGroup = jinput.getJSONArray("DepartList");
            JSONArray area = jinput.getJSONArray("AreaList");
            ms.connectionServer("forschool");
            String sql = "select depart.schoolcode,school.chineseName from depart,school where ";
            if(deptGroup.length() != 0 && deptGroup.length() < 18) {
                /*
                 * 須過濾兩種狀況
                 * 1.無任何學群被勾選即是全學群
                 * 2.勾選學群數若為18，則為全學群
                 */
                sql += "(";
                for(int i = 0; i < deptGroup.length(); i++) {
                    sql += ("deptgroup = " + deptGroup.getString(i));
                    if(i < (deptGroup.length() - 1)) {
                        sql += " or ";
                    }
                }
                sql += ") and ";
            }
            if(area.length() != 0 && area.length() < 5) {
                /*
                 * 須過濾兩種狀況
                 * 1.無任何地區被勾選即是全地區
                 * 2.勾選地區若為五區，則為全地區
                 */
                sql += "(";
                for(int i = 0; i < area.length(); i++) {
                    int areaIndex = area.getInt(i);
                    for(int j = 0; j < school_areaList[areaIndex].length; j++) {
                        if(j > 0 && j < school_areaList[areaIndex].length) {
                            sql+=" or ";
                        }
                        sql += ("school.address like '%" + school_areaList[areaIndex][j] + "%'");
                    }
                    if(i < (area.length() - 1)) {
                        sql += " or ";
                    }
                }
                sql += ") and ";
            }
                    
            sql += "school.schoolcode = depart.schoolcode and ";
            sql += "school.survey = '"+"Y' ";
            sql += "group by depart.schoolcode,school.chineseName";
            ms.executeQueryCommand(sql);
            ResultSet result = ms.rs;
            JSONArray tmpArr = new JSONArray();
            while(result.next()) {
                JSONObject tmp = new JSONObject();
                tmp.put("schoolnum", result.getString(1));
                tmp.put("schoolname", result.getString(2));
                tmpArr.put(tmp);
            }
            output.put("status", "200");
            output.put("searchList", tmpArr);
        } catch(JSONException err) {
            output = new JSONObject();
            output.put("status", "400");
            output.put("message","Request格式或資料錯誤");
        }catch(SQLException sqle) {
            output = new JSONObject();
            output.put("status", "502");
            output.put("message","資料庫查詢錯誤");
        } catch(Exception err) {
            output = new JSONObject();
            output.put("status", "500");
            output.put("message","伺服器錯誤");
        } finally {
            re.setResponse(output.toString());
            ms.closeConnection();
        }
        return re.builder.build();
    }
}
