package RESTfulServer.V1;

import javax.ws.rs.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.NoSuchElementException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.json.JSONException;
import org.json.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoSocketReadTimeoutException;
import MongoConnection.MongoJDBC;

@Path("/V1/exhibitions/activity")
public class ActivityFunc {
    MongoJDBC m;
    public ActivityFunc() throws Exception {
        m = new MongoJDBC();
    }
    @GET
    @Path("/{userid}/{country}/collectionbox")
    public Response getUser(@PathParam("userid") String uid, @PathParam("country") String country){
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //取得Server side目前時間之年分
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            //查詢符合使用者id與年分的集章簿資料
            DBCollection col = m.db.getCollection("CollectionBox");
            BasicDBObject search = new BasicDBObject();
            search.put("country", country);
            search.put("year", year);
            search.put("uid", uid);
            DBCursor searchR = col.find(search);
            output = new JSONObject(searchR.next().toString());
            output.remove("_id");
            output.remove("country");
            output.remove("year");
            output.put("status", 200);
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
