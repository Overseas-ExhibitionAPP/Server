package RESTfulServer.V1;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoSocketReadTimeoutException;

import DBConnection.MongoJDBC;
import DBConnection.TestingJDBC;
@Path("/V1/exhibitions")
public class ExhibitionFunc {
    MongoJDBC m;
    TestingJDBC t;
    public ExhibitionFunc() throws Exception {
        m = new MongoJDBC();
        t = new TestingJDBC();
    }
    @GET
    @Path("/list")
    public Response getExhibList() throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //取得所有教育展資料，並過濾欄位，僅保留ename、year、openingtime、closingtime、country、subExhib陣列中的area
            DBCollection col = m.db.getCollection("Exhibition");
            BasicDBObject search = new BasicDBObject();
            BasicDBObject limit = new BasicDBObject();
            limit.put("ename", 1);
            limit.put("year", 1);
            limit.put("openingtime", 1);
            limit.put("closingtime", 1);
            limit.put("country", 1);
            limit.put("subExhib.area", 1);
            List<DBObject> result = col.find(search,limit).toArray();
            JSONArray exhibSet = new JSONArray();
            for(int i = 0; i < result.size(); i++) {
                JSONObject tmp = new JSONObject(result.get(i).toString());
                exhibSet.put(tmp);
            }
            output.put("status", "200");
            output.put("exhibitionset", exhibSet);
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
        } finally {
            re.setResponse(output.toString());
            m.mClient.close();
        }
        return re.builder.build();
    }
    @GET
    @Path("/id/{exhibid}")
    public Response getExhibinfo(@PathParam("exhibid") String eid) throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //查詢符合eid的教育展資訊
            DBCollection col = m.db.getCollection("Exhibition");
            BasicDBObject search = new BasicDBObject();
            search.put("_id", new ObjectId(eid));
            DBCursor resultSet = col.find(search);
            if(resultSet.count() == 0) {
                output.put("status", "403");
                output.put("message","無符合資料");
            } else {
	            DBObject result = resultSet.next();
	            output = new JSONObject(result.toString());
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
        }
        return re.builder.build();
    }
    @DELETE
    @Path("/{exhibid}")
    public Response deleteExhibinfo(@PathParam("exhibid") String eid) throws Exception{
        //先連接測試DB(2016.08.17)
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //查詢符合eid的教育展資訊，並刪除
            DBCollection col = t.db.getCollection("Exhibition");
            BasicDBObject search = new BasicDBObject();
            search.put("_id", new ObjectId(eid));
            col.remove(search);
            output.put("status", "200");
            output.put("message", "已成功刪除");
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
        } finally {
            re.setResponse(output.toString());
            m.mClient.close();
            t.mClient.close();//關閉測試資料庫連線
        }
        return re.builder.build();
    }
    @POST
    public Response insertExhibinfo(String input) throws Exception{
        //先連接測試DB(2016.08.17)
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //取得Server side目前時間之年分
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            DBCollection col = t.db.getCollection("Exhibition");
            JSONObject jinput = new JSONObject(input);
            BasicDBObject insertCommand = new BasicDBObject();
            insertCommand.put("ename", jinput.getString("ename"));
            insertCommand.put("year" , year);
            insertCommand.put("openingtime", jinput.getString("openingtime"));
            insertCommand.put("closingtime", jinput.getString("closingtime"));
            insertCommand.put("status", "Y");//暫無使用，先保留
            insertCommand.put("country", jinput.getString("country"));
            insertCommand.put("exhib_info" , jinput.getString("exhib_info"));
            ArrayList<BasicDBObject> tmpArr = new ArrayList<BasicDBObject>();
            JSONArray jinputArr = jinput.getJSONArray("subExhib");
            BasicDBObject tmp = new BasicDBObject();
            BasicDBObject tmp2 = new BasicDBObject();
            for(int i = 0; i < jinputArr.length(); i++) {
                JSONObject jtmp = jinputArr.getJSONObject(i);
                tmp.put("area", jtmp.getString("area"));
                tmp.put("name", jtmp.getString("name"));
                tmp.put("btn_name", jtmp.getString("btn_name"));
                tmp.put("address", jtmp.getString("address"));
                tmp.put("position", new BasicDBObject().append("x_pos", jtmp.getJSONObject("position").getString("x_pos"))
                        .append("y_pos", jtmp.getJSONObject("position").getString("y_pos")));
                tmp.put("layout", jtmp.getString("layout"));
                tmp.put("starttime", jtmp.getString("starttime"));
                tmp.put("endtime", jtmp.getString("endtime"));
                tmp2 = tmp;
                tmpArr.add(tmp2);
                tmp = new BasicDBObject();
            }
            insertCommand.put("subExhib" , tmpArr);
            col.insert(insertCommand);
            output.put("status", "200");
            output.put("message", "已成功新增");
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
        } finally {
            re.setResponse(output.toString());
            m.mClient.close();
            t.mClient.close();//關閉測試資料庫
        }
        return re.builder.build();
    }
    @PUT
    @Path("/{exhibid}")
    public Response updateExhibinfo(@PathParam("exhibid") String eid, String input) throws Exception{
        //先連接測試DB(2016.08.17)
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            DBCollection col = t.db.getCollection("Exhibition");
            JSONObject jinput = new JSONObject(input);
            BasicDBObject search = new BasicDBObject();
            search.put("_id", new ObjectId(eid));
            BasicDBObject updateCommand = new BasicDBObject();
            BasicDBObject updateItem = new BasicDBObject();
            updateItem.put("ename", jinput.getString("ename"));
            updateItem.put("openingtime", jinput.getString("openingtime"));
            updateItem.put("closingtime", jinput.getString("closingtime"));
            updateItem.put("status", "Y");//暫無使用，先保留
            updateItem.put("country", jinput.getString("country"));
            updateItem.put("exhib_info" , jinput.getString("exhib_info"));
            ArrayList<BasicDBObject> tmpArr = new ArrayList<BasicDBObject>();
            JSONArray jinputArr = jinput.getJSONArray("subExhib");
            BasicDBObject tmp = new BasicDBObject();
            BasicDBObject tmp2 = new BasicDBObject();
            for(int i = 0; i < jinputArr.length(); i++) {
                JSONObject jtmp = jinputArr.getJSONObject(i);
                tmp.put("area", jtmp.getString("area"));
                tmp.put("name", jtmp.getString("name"));
                tmp.put("btn_name", jtmp.getString("btn_name"));
                tmp.put("address", jtmp.getString("address"));
                tmp.put("position", new BasicDBObject().append("x_pos", jtmp.getJSONObject("position").getString("x_pos"))
                        .append("y_pos", jtmp.getJSONObject("position").getString("y_pos")));
                tmp.put("layout", jtmp.getString("layout"));
                tmp.put("starttime", jtmp.getString("starttime"));
                tmp.put("endtime", jtmp.getString("endtime"));
                tmp2 = tmp;
                tmpArr.add(tmp2);
                tmp = new BasicDBObject();
            }
            updateItem.put("subExhib" , tmpArr);
            updateCommand.put("$set", updateItem);
            col.update(search, updateCommand);
            output.put("status", "200");
            output.put("message", "已成功修改");
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
        } finally {
            re.setResponse(output.toString());
            m.mClient.close();
            t.mClient.close();//關閉測試資料庫
        }
        return re.builder.build();
    }
}
