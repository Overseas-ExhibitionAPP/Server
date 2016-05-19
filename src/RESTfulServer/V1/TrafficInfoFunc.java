package RESTfulServer.V1;
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
import MongoConnection.MongoJDBC;
@Path("/V1/exhibitions/{year}/traffic")
public class TrafficInfoFunc {
    MongoJDBC m;
    public TrafficInfoFunc() throws Exception{
        m = new MongoJDBC();
    }
    //取得該舉辦地區之交通資訊與相關資訊
    @GET
    @Path("/{country}")
    @Produces("application/json; charset=UTF-8")
    public Response getTrafficInfo(@PathParam("country") String country, @PathParam("year") int year) throws Exception{
        try{
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
            JSONObject output = new JSONObject();
            output.put("status", 200);
            output.put("traffic_list", outputArr);
            NewResponse re = new NewResponse();
            re.setResponse(output.toString());
            return re.builder.build();
        } catch(JSONException err) {
            NewResponse re = new NewResponse();
            JSONObject content = new JSONObject();
            content.put("status", "400");
            content.put("message","Request格式或資料錯誤");
            re.setResponse(content.toString());
            return re.builder.build();
        } catch(NoSuchElementException err) {
            NewResponse re = new NewResponse();
            JSONObject content = new JSONObject();
            content.put("status", "400");
            content.put("message","Request格式或資料錯誤");
            re.setResponse(content.toString());
            return re.builder.build();
        } catch(Exception err) {
            NewResponse re = new NewResponse();
            JSONObject content = new JSONObject();
            content.put("status", "500");
            content.put("message","伺服器錯誤");
            re.setResponse(content.toString());
            return re.builder.build();
        } finally {
            m.mClient.close();
        }
    }
}
