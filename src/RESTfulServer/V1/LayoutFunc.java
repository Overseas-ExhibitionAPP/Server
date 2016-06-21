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
import org.json.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoSocketReadTimeoutException;

import DBConnection.MongoJDBC;
@Path("/V1/exhibitions")
public class LayoutFunc {
    MongoJDBC m;
    public LayoutFunc() throws Exception{
        m = new MongoJDBC();
    }
    //取得該舉辦地區之各展點攤位圖
    @GET
    @Path("/{year}/layout/{country}")
    @Produces("application/json; charset=UTF-8")
    public Response getLayout(@PathParam("country") String country, @PathParam("year") int year) throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
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
                tmp.remove("position");
                tmp.remove("school");
                tmp.remove("address");
                tmp.remove("starttime");
                tmp.remove("endtime");
                outputArr.put(tmp);
            }
            output.put("status", "200");
            output.put("layout_list", outputArr);
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
