package RESTfulServer.V1;
import java.util.Calendar;
import java.util.NoSuchElementException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
@Path("/V1/exhibitions")
public class TrafficInfoFunc {
    MongoJDBC m;
    public TrafficInfoFunc() throws Exception{
        m = new MongoJDBC();
    }
    //取得該舉辦地區之交通資訊與相關資訊
    @GET
    @Path("/traffic/{country}")
    @Produces("application/json; charset=UTF-8")
    public Response getTrafficInfo(@PathParam("country") String country) throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //取得Server side目前時間之年分
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            DBCollection col = m.db.getCollection("Exhibition");
            BasicDBObject search = new BasicDBObject();
            search.put("country", country);
            search.put("year", year);
            DBCursor searchR = col.find(search);
            JSONArray tmpArr = (new JSONObject(searchR.next().toString())).getJSONArray("subExhib");
            JSONArray outputArr = new JSONArray();
            for(int i = 0; i < tmpArr.length(); i++) {
                //去除不需要資訊
                JSONObject tmp = tmpArr.getJSONObject(i);
                tmp.remove("school");
                tmp.remove("layout");
                outputArr.put(tmp);
            }
            output.put("status", "200");
            output.put("traffic_list", outputArr);
        } catch(JSONException err) {
        	output = new JSONObject();
        	output.put("status", "400");
        	output.put("message","Request格式或資料錯誤");
        } catch(NoSuchElementException err) {
        	output = new JSONObject();
            output.put("status", "400");
            output.put("message","Request格式或資料錯誤");
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
