package RESTfulServer.V1;
import javax.ws.rs.Path;
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
import MongoConnection.MongoJDBC;
@Path("/V1/exhibitions/lectures")
public class LectureFunc {
    MongoJDBC m;
    public LectureFunc() throws Exception {
        m = new MongoJDBC();
    }
    @GET
    @Path("/{country}")
    @Produces("application/json; charset=UTF-8")
    public Response getUserCBox(@PathParam("country") String country){
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //取得Server side目前時間之年分
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            //查詢符合使用者id與年分的集章簿資料
            DBCollection col = m.db.getCollection("LectureTimes");
            BasicDBObject search = new BasicDBObject();
            search.put("country", country);
            search.put("year", year);
            DBCursor searchR = col.find(search);
            //檢查是否有符合條件之集章簿資料
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
                output.put("area_set", tmpArr);
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
