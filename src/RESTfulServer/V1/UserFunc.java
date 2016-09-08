package RESTfulServer.V1;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
public class UserFunc {
    MongoJDBC m;
    MssqlJDBC ms;
    public UserFunc() throws Exception {
        m = new MongoJDBC();
    }
    @PUT
    @Path("")
    @Produces("application/json; charset=UTF-8")
    public Response updateUserinfo(String input) throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            JSONObject jinput = new JSONObject(input);
            //查詢符合使用者id資訊
            DBCollection col = m.db.getCollection("UserList");
            BasicDBObject search = new BasicDBObject();
            search.put("_id", jinput.getString("uid"));
            DBCursor searchR = col.find(search);
            //取得目前時間
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String Time = sdFormat.format(new Date());
            //檢查是否有符合條件之使用者資訊
            BasicDBObject agerange = new BasicDBObject();
            if(jinput.getJSONObject("agerange").isNull("min") == false) {
                agerange.put("min", jinput.getJSONObject("agerange").getInt("min"));
            }
            if(jinput.getJSONObject("agerange").isNull("max") == false) {
                agerange.put("max", jinput.getJSONObject("agerange").getInt("max"));
            }
            if(searchR.count() == 0) {
                search.put("name", jinput.getString("name"));
                search.put("agerange", agerange);
                search.put("email", jinput.getString("email"));
                search.put("createtime", Time);
                search.put("updatetime", Time);
                col.insert(search);
                output.put("status", "200-1");
                output.put("message", "新增成功");
            } else {
                BasicDBObject updateItem = new BasicDBObject();
                updateItem.put("name", jinput.getString("name"));
                updateItem.put("agerange", agerange);
                updateItem.put("email", jinput.getString("email"));
                updateItem.put("updatetime", Time);
                BasicDBObject updateCommand = new BasicDBObject();
                updateCommand.put("$set", updateItem);
                col.update(search, updateCommand);
                output.put("status", "200-2");
                output.put("message", "更新成功");
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
}
