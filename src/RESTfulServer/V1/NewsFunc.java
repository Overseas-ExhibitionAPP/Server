package RESTfulServer.V1;
import javax.ws.rs.Path;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoSocketReadTimeoutException;

import DBConnection.MongoJDBC;
import DBConnection.TestingJDBC;
@Path("/V1/news")
public class NewsFunc {
	TestingJDBC t;
    MongoJDBC m;
    public NewsFunc() throws Exception {
        m = new MongoJDBC();
        t = new TestingJDBC();
    }
    @GET
    @Path("/{country}")
    @Produces("application/json; charset=UTF-8")
    public Response getSingleCountryNewsSet(@PathParam("country") String country) throws Exception{
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
                output.put("message", "尚未有任何最新消息");
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
    @GET
    @Path("/list")
    @Produces("application/json; charset=UTF-8")
    public Response getNewsList() throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //查詢所有最新消息，並封裝成陣列
            DBCollection col = m.db.getCollection("NewsList");
            BasicDBObject search = new BasicDBObject();
            BasicDBObject limit = new BasicDBObject();
            limit.put("date", 1);
            limit.put("country", 1);
            limit.put("type", 1);
            limit.put("title", 1);
            limit.put("content", 1);
            BasicDBObject sort = new BasicDBObject();
            sort.put("timestamp", -1);
            DBCursor searchR = col.find(search,limit).sort(sort);
            if(searchR.count() == 0) {
                output.put("status", "403");
                output.put("message", "尚未有任何最新消息");
            } else {
                JSONArray tmpArr = new JSONArray();
                JSONObject tmp = new JSONObject();
                while(searchR.hasNext()) {
                    tmp = new JSONObject(searchR.next().toString());
                    tmpArr.put(tmp);
                }
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
    @DELETE
    @Path("/{newsid}")
    public Response deleteNews(@PathParam("newsid") String nid) throws Exception{
        //先連接測試DB(2016.08.22)
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //查詢符合nid的最新消息資訊，並刪除
            DBCollection col = t.db.getCollection("NewsList");
            BasicDBObject search = new BasicDBObject();
            search.put("_id", new ObjectId(nid));
            col.remove(search);
            output.put("status", "200");
            output.put("message", "已成功刪除");
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
            t.mClient.close();//關閉測試資料庫連線
        }
        return re.builder.build();
    }
    @POST
    public Response insertNews(String input) throws Exception{
        //先連接測試DB(2016.08.17)
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            DBCollection col = t.db.getCollection("NewsList");
            JSONObject jinput = new JSONObject(input);
            //取得Server side目前時間之年分
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            BasicDBObject insertCommand = new BasicDBObject();
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");;
            String date = sdFormat.format(new Date());
            insertCommand.put("year", year);
            insertCommand.put("date", date);
            insertCommand.put("country", jinput.getString("country"));
            insertCommand.put("type", jinput.getString("type"));
            insertCommand.put("title", jinput.getString("title"));
            insertCommand.put("content", jinput.getString("content"));
            SimpleDateFormat sdFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");;
            String timestamp = sdFormat2.format(new Date());
            insertCommand.put("timestamp",timestamp);
            col.insert(insertCommand);
            output.put("status", "200");
            output.put("message", "已成功新增");
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
            t.mClient.close();//關閉測試資料庫
        }
        return re.builder.build();
    }
    @GET
    @Path("/id/{newsid}")
    public Response getSingleNews(@PathParam("newsid") String nid) throws Exception{
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //查詢符合nid的最新消息資訊
            DBCollection col = m.db.getCollection("NewsList");
            BasicDBObject search = new BasicDBObject();
            search.put("_id", new ObjectId(nid));
            BasicDBObject limit = new BasicDBObject();
            limit.put("timestamp", 0);
            DBObject result = col.find(search,limit).next();
            output = new JSONObject(result.toString());
            output.put("status", "200");
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
