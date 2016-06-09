package RESTfulServer.V1;
import javax.ws.rs.Path;

import java.util.Calendar;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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
import MongoConnection.MongoJDBC;
@Path("/V1/news")
public class NewsFunc {
    MongoJDBC m;
    public NewsFunc() throws Exception {
        m = new MongoJDBC();
    }
    @GET
    @Path("/{country}")
    @Produces("application/json; charset=UTF-8")
    public Response getUserCBox(@PathParam("country") String country){
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //查詢符合地區之最新消息
            DBCollection col = m.db.getCollection("NewsList");
            BasicDBObject search = new BasicDBObject();
            search.put("country", country);
            BasicDBObject sort = new BasicDBObject();
            sort.put("timestamp", -1);
            DBCursor searchR = col.find(search).sort(sort);
            if(searchR.count() == 0) {
                output.put("status", "403");
                output.put("message", "尚未有任何講座時間");
            } else {
                JSONArray tmpArr = new JSONArray();
                JSONObject tmp = new JSONObject();
                while(searchR.hasNext()) {
                    tmp = new JSONObject(searchR.next().toString());
                    tmp.remove("_id");
                    tmp.remove("country");
                    tmp.remove("year");
                    tmpArr.put(tmp);
                }
                output.put("country", country);
                output.put("news_set", tmpArr);
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
        } finally {
            re.setResponse(output.toString());
            m.mClient.close();
        }
        return re.builder.build();
    }
}
