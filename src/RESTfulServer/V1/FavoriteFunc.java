package RESTfulServer.V1;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoSocketReadTimeoutException;

import DBConnection.MongoJDBC;
import DBConnection.MssqlJDBC;
@Path("/V1/users")
public class FavoriteFunc {
    MongoJDBC m;
    MssqlJDBC ms;
    public FavoriteFunc() throws Exception {
        m = new MongoJDBC();
        ms = new MssqlJDBC();
    }
    @GET
    @Path("/{userid}/favoritelist")
    @Produces("application/json; charset=UTF-8")
    public Response getUserFavoriteList(@PathParam("userid") String uid, @QueryParam("country") String country) throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //查詢符合使用者id的我的最愛列表
            DBCollection col = m.db.getCollection("UserFavoriteList");
            BasicDBObject search = new BasicDBObject();
            search.put("uid", uid);
            DBCursor searchR = col.find(search);
            //檢查是否有符合條件之我的最愛列表
            if(searchR.count() == 0) {
                output.put("status", "403");
                output.put("message", "尚未有任何我的最愛紀錄");
            } else {
                //取得目前user的我的最愛列表
                JSONObject tmpUser = new JSONObject(searchR.next().toString());
                //取得Server side目前時間之年分
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                //取得展場地區列表
                DBCollection lcol = m.db.getCollection("Exhibition");
                BasicDBObject searchA = new BasicDBObject();
                searchA.put("year", year);
                searchA.put("country", country);
                BasicDBList layoutSet = (BasicDBList) lcol.find(searchA).next().get("subExhib");
                ms.connectionServer("oversea");
                //取出目前的favoriteList
                JSONArray fList = tmpUser.getJSONArray("favoriteList");
                for(int i = 0; i < fList.length(); i++) {
                    JSONObject tmpL = fList.getJSONObject(i);
                    if(tmpL.isNull("layoutList") == false) {
                        tmpL.remove("layoutList");
                    }
                    String schoolnum = tmpL.getString("schoolnum");
                    //插入目前的我的最愛列表清單之攤位位置
                    JSONArray layoutList = new JSONArray();
                    //搜尋對應學校的各展攤位號碼
                    String sql = "select showcase,lid from layout,school where " 
                            + "yy = '" + year +"' and " 
                            + "country='" + country.toUpperCase() + "' and "
                            + "layout.sch_id = school.id and "
                            + "school.schoolcode = '" + schoolnum+ "' "
                            + "order by lid asc";
                    ms.executeQueryCommand(sql);
                    while(ms.rs.next()) {
                        JSONObject tmp = new JSONObject();
                        tmp.put("layoutNum", ms.rs.getString(1));
                        JSONObject layoutItem = new JSONObject(layoutSet.get(Integer.parseInt(ms.rs.getString(2))).toString());
                        tmp.put("exhibName", layoutItem.getString("area"));
                        layoutList.put(tmp);
                    }
                    tmpL.put("layoutList", layoutList);
                    fList.put(i, tmpL);
                }
                output = tmpUser;
                output.remove("_id");
                output.remove("favoriteList");
                output.put("favoriteList", fList);
                output.put("status", "200");
            }
        } catch(JSONException err) {
            output = new JSONObject();
            output.put("status", "400");
            output.put("message","Request格式/資料錯誤");
        } catch(MongoSocketReadTimeoutException msrt) {
            output = new JSONObject();
            output.put("status", "502");
            output.put("message","連線逾時");
        } catch(Exception err) {
            output = new JSONObject();
            output.put("status", "500");
            output.put("message","伺服器錯誤");
            err.printStackTrace();
        } finally {
            re.setResponse(output.toString());
            m.mClient.close();
            ms.closeConnection();
        }
        return re.builder.build();
    }
    @PUT
    @Path("/{userid}/favoritelist")
    @Consumes("application/json; charset=UTF-8")
    @Produces("application/json; charset=UTF-8")
    public Response updateUserFavoriteList(@PathParam("userid") String uid, String input) throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            JSONObject jinput = new JSONObject(input);
            //檢查是否已有此學校，若是則取消我的最愛紀錄
            DBCollection col = m.db.getCollection("UserFavoriteList");
            BasicDBObject citem = new BasicDBObject();
            citem.put("uid", uid);
            citem.put("favoriteList.schoolnum", jinput.getString("schoolnum"));
            BasicDBObject searchCommand = new BasicDBObject();
            searchCommand.put("favoriteList.$", 1);
            int check = col.find(citem,searchCommand).count();
            if(check != 0) {
                //移除我的最愛資料
                BasicDBObject match = new BasicDBObject("uid", uid);
                BasicDBObject update = new BasicDBObject("favoriteList", new BasicDBObject("schoolnum", jinput.getString("schoolnum")));
                col.update(match, new BasicDBObject("$pull", update));
                output.put("status", "403");
                output.put("message", "已取消追蹤");
            } else {
                //設定搜尋條件,找符合的我的最愛列表
                BasicDBObject updateQuery = new BasicDBObject();
                updateQuery.put("uid", uid);
                //設定欲新增之學校記錄，內含學校代碼、學校名稱
                BasicDBObject item = new BasicDBObject();
                item.put("schoolnum", jinput.getString("schoolnum"));
                item.put("schName", jinput.getString("schoolname"));
                //若資料庫中未有這筆資料，則新增之，若有則更新我的最愛記錄
                DBCursor searchR = col.find(updateQuery);
                int count = searchR.count();
                if(count == 0) {
                    ArrayList<BasicDBObject> fbox = new ArrayList<BasicDBObject>();
                    fbox.add(item);
                    updateQuery.put("favoriteList", fbox);
                    col.insert(updateQuery);
                    output.put("status", "201");
                    output.put("message", "新增我的最愛列表與學校記錄成功");
                } else {
                    BasicDBObject itemSet = new BasicDBObject();
                    itemSet.put("favoriteList", item);
                    BasicDBObject updateCommand = new BasicDBObject();
                    updateCommand.put("$push", itemSet);
                    col.update(updateQuery,updateCommand);
                    output.put("status", "200");
                    output.put("message", "新增我的最愛紀錄成功");
                }
            }
        } catch(JSONException err) {
            output = new JSONObject();
            output.put("status", "400");
            output.put("message","Request格式或資料錯誤");
        }catch(MongoSocketReadTimeoutException msrt) {
            output = new JSONObject();
            output.put("status", "502");
            output.put("message","連線逾時");
        } catch(Exception err) {
            output = new JSONObject();
            output.put("status", "500");
            output.put("message","伺服器錯誤");
        } finally {
            re.setResponse(output.toString());
            m.mClient.close();
            ms.closeConnection();
        }
        return re.builder.build();
    }
}
