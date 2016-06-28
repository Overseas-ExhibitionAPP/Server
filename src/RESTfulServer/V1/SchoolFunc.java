package RESTfulServer.V1;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    MongoJDBC m;
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
        m = new MongoJDBC();
    }
    @PUT
    @Path("/schname/search")
    @Consumes("application/json; charset=UTF-8")
    @Produces("application/json; charset=UTF-8")
    public Response searchSchoolname(String input) throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try {
            JSONObject jinput = new JSONObject(input);
            String schName = jinput.getString("schname");
            ms.connectionServer("forschool");
            String sql = "select school.schoolcode,school.chineseName from school where ";
            sql += "school.chineseName like '%" + schName + "%' ";
            sql += "order by school.schoolcode asc";
            ms.executeQueryCommand(sql);
            JSONArray tmpArr = new JSONArray();
            while(ms.rs.next()) {
                JSONObject tmp = new JSONObject();
                tmp.put("schoolnum", ms.rs.getString(1));
                tmp.put("schoolname", ms.rs.getString(2));
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
            JSONArray tmpArr = new JSONArray();
            while(ms.rs.next()) {
                JSONObject tmp = new JSONObject();
                tmp.put("schoolnum", ms.rs.getString(1));
                tmp.put("schoolname", ms.rs.getString(2));
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
    public Response getSchoolInfo(@PathParam("sch_id")String schid,@QueryParam("country")@DefaultValue("") String country) throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try {
            MssqlJDBC ms = new MssqlJDBC();
            //取得符合條件之學校對於學系與學群對應的資料
            ms.connectionServer("forschool");
            String sql = "select depart.deptname, depart.homepage, deptgroup.type from depart, deptgroup ";
            sql += "where deptgroup.code = depart.deptgroup and ";
            sql += "depart.schoolcode = '" + schid + "'";
            sql += "order by deptgroup asc";
            ms.executeQueryCommand(sql);
            JSONArray deptGList =new JSONArray(); //儲存所有學群與學系的分類資料
            JSONArray deptList = new JSONArray(); //儲存單一學群與學系的資料
            JSONObject deptObj;//匯整單一學系資料，含學系名與對應系網址
            boolean check = false;
            String deptG="", pre_deptG="";
            while(ms.rs.next()) {
                 deptG = ms.rs.getString(3);
                 if(check == false) {
                     pre_deptG = deptG;
                     check = true;
                 }
                 if(deptG.equals(pre_deptG) == false){
                     deptObj = new JSONObject();
                     deptObj.put("deptGroup", pre_deptG+"學群");
                     deptObj.put("deptList",deptList);
                     deptGList.put(deptObj);
                     deptList = new JSONArray();
                     pre_deptG = deptG;
                 }
                 JSONObject tmp = new JSONObject();
                 tmp.put("deptName", ms.rs.getString(1));
                 tmp.put("deptURL", ms.rs.getString(2));
                 deptList.put(tmp);
            }
            String schoolCName = "";
            String schoolEName = "";
            sql = "select chineseName,englishName from school where schoolcode='" + schid+ "'";
            ms.executeQueryCommand(sql);
            while(ms.rs.next()) {
                schoolCName = ms.rs.getString(1);
                schoolEName = ms.rs.getString(2);
            }
            ms.closeConnection();
            ms = new MssqlJDBC();
            //取得符合條件學校之學校相關資訊
            ms.connectionServer("oversea");
            sql = "select schoolinfo.attractions,schoolinfo.zcode,schoolinfo.address,"
                    + "schoolinfo.tel,schoolinfo.fax,schoolinfo.website,schoolinfo.introduction,"
                    + "schoolinfo.ctitle1,schoolinfo.ctitle2,schoolinfo.ctitle3,schoolinfo.ctitle4,"
                    + "schoolinfo.ctitle5,schoolinfo.characteristic1,schoolinfo.characteristic2,"
                    + "schoolinfo.characteristic3,schoolinfo.characteristic4,"
                    + "schoolinfo.characteristic5,schoolinfo.alumnus from schoolinfo,school "
                    + "where school.id = schoolinfo.sch_id and "
                    + "school.schoolcode = '"+ schid + "'";
            ms.executeQueryCommand(sql);
            boolean infoCheck = false;
            JSONObject schoolinfo = new JSONObject();//儲存學校資訊
            while(ms.rs.next()) {
                schoolinfo.put("attractions", ms.rs.getString(1));
                schoolinfo.put("zcode", ms.rs.getString(2));
                schoolinfo.put("address", ms.rs.getString(3));
                schoolinfo.put("tel", ms.rs.getString(4));
                schoolinfo.put("fax", ms.rs.getString(5));
                schoolinfo.put("website", ms.rs.getString(6));
                schoolinfo.put("introduction", ms.rs.getString(7));
                JSONArray cList = new JSONArray();
                JSONObject c1 =new JSONObject();
                c1.put("ctitle", ms.rs.getString(8));
                c1.put("characteristic",ms.rs.getString(13));
                JSONObject c2 =new JSONObject();
                c2.put("ctitle", ms.rs.getString(9));
                c2.put("characteristic",ms.rs.getString(14));
                JSONObject c3 =new JSONObject();
                c3.put("ctitle", ms.rs.getString(10));
                c3.put("characteristic",ms.rs.getString(15));
                JSONObject c4 =new JSONObject();
                c4.put("ctitle", ms.rs.getString(11));
                c4.put("characteristic",ms.rs.getString(16));
                JSONObject c5 =new JSONObject();
                c5.put("ctitle", ms.rs.getString(12));
                c5.put("characteristic",ms.rs.getString(17));
                cList.put(c1);
                cList.put(c2);
                cList.put(c3);
                cList.put(c4);
                cList.put(c5);
                schoolinfo.put("cList", cList);
                schoolinfo.put("alumnus", ms.rs.getString(18));
                infoCheck = true;
            }
            DBCollection col = m.db.getCollection("School-Logo");
            BasicDBObject search = new BasicDBObject();
            search.put("_id", schid);
            String pic = col.find(search).next().get("picture").toString();
            boolean layoutcheck = false;
            JSONArray layoutList = new JSONArray();//儲存符合舉辦國家之各地區之攤位號碼
            //取得符合學校資料之攤位資料
            if(country.equals("") == true) {
                output.put("status", "400");
                output.put("message", "未提供欲查詢的國家代碼");
            } else {
                
                //取得Server side目前時間之年分
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                sql = "select showcase,lid from layout,school where " 
                        + "yy = '" + year +"' and " 
                        + "country='" + country.toUpperCase() + "' and "
                        + "layout.sch_id = school.id and "
                        + "school.schoolcode = '" + schid + "' "
                        + "order by lid asc";
                ms.executeQueryCommand(sql);
                col = m.db.getCollection("Exhibition");
                search = new BasicDBObject();
                search.put("year", year);
                search.put("country", country);
                JSONArray layoutSet = (new JSONObject(col.find(search).next().toString())).getJSONArray("subExhib");
                while(ms.rs.next()) {
                    JSONObject tmp = new JSONObject();
                    tmp.put("layoutNum", ms.rs.getString(1));
                    tmp.put("exhibName", layoutSet.getJSONObject(Integer.parseInt(ms.rs.getString(2))).getString("area"));
                    layoutList.put(tmp);
                    layoutcheck = true;
                }
                m.mClient.close();
                
                /*
                 * 檢查是否有取得學校資訊與攤位資訊，則以狀態碼提醒
                 * 1.皆有取得學校資訊與攤位資訊:200-1
                 * 2.有取得學校資訊，無取得攤位資訊:200-2
                 * 3.無取得學校資訊，有取得攤位資訊:200-3
                 * 4.無取得學校資訊，無取得攤位資訊:200-4
                 */
                
                if(infoCheck == true) {
                    if(layoutcheck == true) {
                        output.put("status", "200-1");
                        output.put("layoutList", layoutList);
                        output.put("schoolinfo", schoolinfo);
                    } else {
                        output.put("status", "200-2");
                        output.put("schoolinfo", schoolinfo);
                    }
                } else {
                    if(layoutcheck == true) {
                        output.put("status", "200-3");
                        output.put("layoutList", layoutList);
                    } else {
                        output.put("status", "200-4");
                    }
                }
                output.put("picture", pic);
                output.put("schoolnum", schid);
                output.put("deptGList", deptGList);
                output.put("chineseName", schoolCName);
                output.put("englishName", schoolEName);
            }
        }catch(JSONException err) {
            output = new JSONObject();
            output.put("status", "400");
            output.put("message","Request格式或資料錯誤");
        }catch(SQLException sqle) {
            output = new JSONObject();
            output.put("status", "502");
            output.put("message",sqle.getMessage());
            output.put("message","資料庫錯誤");
        } catch(Exception err) {
            output = new JSONObject();
            output.put("status", "500");
            output.put("message", "tst");
            output.put("message","伺服器錯誤");
        } finally {
            re.setResponse(output.toString());
            ms.closeConnection();
        }
        return re.builder.build();
    }
}
