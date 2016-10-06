package RESTfulServer.V1;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
@Path("/V1/questionnaire")
public class QuestionnaireFunc {
    MongoJDBC m;
    public QuestionnaireFunc() throws Exception{
        m = new MongoJDBC();
    }
    //取得該舉辦地區專屬的行動問卷
    @GET
    @Path("/{country}")
    @Produces("application/json; charset=UTF-8")
    public Response getQuestionnaire(@PathParam("country") String country,@QueryParam("userid") String uid) throws Exception{
        //尚未過濾未到系統開放時間不開放填答
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //取得Server side目前時間之年分
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            //檢查是否有填答過的紀錄
            DBCollection col = m.db.getCollection("UserQuestionnaire");
            BasicDBObject search = new BasicDBObject();
            search.put("country", country);
            search.put("year", year);
            search.put("uid", uid);
            int count = col.find(search).count();
            if(count > 0) {
                output.put("status", "403");
                output.put("message", "已有問卷答題紀錄");
            } else {
                col = m.db.getCollection("Questionnaire");
                search.remove("uid");
                DBCursor searchR = col.find(search);
                output = new JSONObject(searchR.next().toString());
                //移除不需要資訊
                output.remove("_id");
                output.remove("eid");
                output.remove("openingtime");
                output.remove("closingtime");
                output.put("status", "200");
            }
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
    @POST
    @Path("/{country}")
    @Consumes("application/json; charset=UTF-8")
    @Produces("application/json; charset=UTF-8")
    public Response insertUserQuestionnaire(@PathParam("country") String country, String input) throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //取得Server side目前時間之年分
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            JSONObject jinput = new JSONObject(input);
            DBCollection col = m.db.getCollection("UserQuestionnaire");
            BasicDBObject insertSet = new BasicDBObject();
            insertSet.put("country", country);
            insertSet.put("year", year);
            insertSet.put("uid", jinput.getString("userid"));
            insertSet.put("uans", convertArr(jinput.getJSONArray("userAnsList")));
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            insertSet.put("createtime", sdFormat.format(new Date()));
            col.insert(insertSet);
            output.put("status", "200");
            output.put("message", "已成功送出");

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
            err.printStackTrace();
        } finally {
            re.setResponse(output.toString());
            m.mClient.close();
        }
        return re.builder.build();
    }
    //JSONArray轉換為List
    public List<BasicDBObject> convertArr(JSONArray input) {
        List<BasicDBObject> output = new ArrayList<>();
        for(int i = 0; i < input.length(); i++) {
            JSONArray tmp = input.getJSONObject(i).getJSONArray("options");//單題
            List<String> op = new ArrayList<>();
            for(int j = 0; j < tmp.length(); j++) {
                op.add(tmp.getString(j));
            }
            BasicDBObject item = new BasicDBObject();
            item.put("opitons", op);
            output.add(item);
        }
        System.out.println(output);
        return output;
    }
}
