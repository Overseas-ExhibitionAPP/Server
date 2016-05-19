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
@Path("/V1/Questionnaire")
public class QuestionnaireFunc {
    MongoJDBC m;
    public QuestionnaireFunc() throws Exception{
        m = new MongoJDBC();
    }
    //取得該國家之各展點攤位圖
    @GET
    @Path("/{year}/{country}")
    @Produces("application/json; charset=UTF-8")
    public Response getQuestionnaire(@PathParam("country") String country, @PathParam("year") int year) throws Exception{
        //尚未過濾未到系統開放時間不開放填答
        try{
            DBCollection col = m.db.getCollection("Questionnaire");
            BasicDBObject search = new BasicDBObject();
            search.put("country", country);
            search.put("year", year);
            DBCursor searchR = col.find(search);
            JSONObject output = new JSONObject(searchR.next().toString());
            //移除不需要資訊
            output.remove("_id");
            output.remove("eid");
            output.remove("openingtime");
            output.remove("closingtime");
            output.put("status", 200);
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
