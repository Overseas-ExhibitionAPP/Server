package RESTfulServer.V1;

import java.util.Calendar;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoSocketReadTimeoutException;

import DBConnection.MongoJDBC;
@Path("/V1/exhibitions")
public class ExhibitionFunc {
    MongoJDBC m;
    public ExhibitionFunc() throws Exception {
        m = new MongoJDBC();
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
}
