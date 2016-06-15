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

@Path("/V1/exhibitions/activity")
public class ActivityFunc {
    MongoJDBC m;
    public ActivityFunc() throws Exception {
        m = new MongoJDBC();
    }
    @GET
    @Path("/{userid}/{country}/collectionbox")
    @Produces("application/json; charset=UTF-8")
    public Response getUserCBox(@PathParam("userid") String uid, @PathParam("country") String country){
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
            //檢查是否有符合條件之集章簿資料
            if(searchR.count() == 0) {
                output.put("status", "403");
                output.put("message", "尚未有任何集章紀錄");
            } else {
                output = new JSONObject(searchR.next().toString());
                output.remove("_id");
                output.remove("country");
                output.remove("year");
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
    @PUT
    @Path("/collectionbox")
    @Consumes("application/json; charset=UTF-8")
    @Produces("application/json; charset=UTF-8")
    public Response updateUserCBox(String input){
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            JSONObject jinput = new JSONObject(input);
            //取得集章時間
            SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            String vTime = sdFormat.format(new Date());
            //取得符合學校代碼的logo(base64編碼)
            DBCollection scol = m.db.getCollection("School-Logo");
            BasicDBObject search = new BasicDBObject();
            search.put("_id", jinput.getString("schoolnum"));
            JSONObject tmp = new JSONObject(scol.find(search).next().toString());
            String schoolpic = tmp.get("picture").toString();
            String schoolnum = tmp.get("_id").toString();
            DBCollection col = m.db.getCollection("CollectionBox");
            //取得Server side目前時間之年分
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            //設定搜尋條件,找符合的集章簿
            BasicDBObject updateQuery = new BasicDBObject();
            updateQuery.put("uid", jinput.getString("userid"));
            updateQuery.put("country", jinput.getString("country"));
            updateQuery.put("year", year);
            //設定欲新增之集章記錄，內含圖片之base64編碼與集章時戳
            BasicDBObject item = new BasicDBObject();
            item.put("schoolnum", schoolnum);
            item.put("picture", schoolpic);
            item.put("timestamp", vTime);
            //若資料庫中未有這筆資料，則新增之，若有則更新集章簿記錄
            DBCursor searchR = col.find(updateQuery);
            int count = searchR.count();
            if(count == 0) {
                ArrayList<BasicDBObject> cbox = new ArrayList<BasicDBObject>();
                cbox.add(item);
                updateQuery.put("collectionbox", cbox);
                updateQuery.put("box_status", 0);
                col.insert(updateQuery);
                output.put("status", "201");
                output.put("message", "新增集章簿與集章紀錄成功");
            } else {
                //檢查是否處於已兌換狀態(boxS為1)，若為兌換狀態，則不新增任何集章紀錄
                JSONObject tmpCbox = new JSONObject(searchR.next().toString());
                int boxS = Integer.parseInt(tmpCbox.get("box_status").toString());
                int boxCount = tmpCbox.getJSONArray("collectionbox").length();
                if(boxS == 1) {
                    output.put("status", "403");
                    output.put("message", "集章簿已兌換贈品，不再進行集章紀錄");
                } else {
                    if(boxCount == 30) {
                        output.put("status", "403");
                        output.put("message", "集章簿已滿，不再進行集章紀錄");
                    } else {
                        BasicDBObject itemSet = new BasicDBObject();
                        itemSet.put("collectionbox", item);
                        BasicDBObject updateCommand = new BasicDBObject();
                        updateCommand.put("$push", itemSet);
                        col.update(updateQuery,updateCommand);
                        output.put("status", "200");
                        output.put("message", "新增集章紀錄成功");
                    }
                }
            }
        } catch(JSONException err) {
            output = new JSONObject();
            output.put("status", "400");
            output.put("message","Request格式或資料錯誤");
        }catch(MongoSocketReadTimeoutException msrt) {
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
    @Path("/{userid}/{country}/collectionbox/exchange")
    @Produces("application/json; charset=UTF-8")
    public Response exchangeUserCBox(@PathParam("userid") String uid, @PathParam("country") String country){
        NewResponse re = new NewResponse();
        JSONObject output = new JSONObject();
        try{
            //取得Server side目前時間之年分
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            //設定搜尋條件,找符合的集章簿
            DBCollection col = m.db.getCollection("CollectionBox");
            BasicDBObject search = new BasicDBObject();
            search.put("uid", uid);
            search.put("country", country);
            search.put("year", year);
            //檢查是否有符合的集章簿
            DBCursor searchR = col.find(search);
            int count = searchR.count();
            if(count == 0) {
                output.put("status", "403");
                output.put("message", "尚未有集章紀錄，不得兌換");
            } else {
                JSONObject tmp = new JSONObject(searchR.next().toString());
                //檢查是否處於已兌換狀態(boxS為1)/未兌換狀態(box為0)
                int boxS = tmp.getInt("box_status");
                if(boxS == 1) {
                    output.put("status", "403-1");
                    output.put("message", "集章簿已兌換贈品，不可重複兌換");
                } else {
                    JSONArray tmpArr = tmp.getJSONArray("collectionbox");
                    //檢查集章簿是否已集滿
                    if(tmpArr.length() == 30) {
                        BasicDBObject itemSet = new BasicDBObject();
                        itemSet.put("box_status", 1);
                        BasicDBObject updateCommand = new BasicDBObject();
                        updateCommand.put("$set", itemSet);
                        col.update(search,updateCommand);
                        output.put("status", "200");
                        output.put("message", "成功兌換");
                    } else {
                        output.put("status", "403-2");
                        output.put("message", "尚未到達兌換條件");
                    }
                }
            }
        } catch(JSONException err) {
            output = new JSONObject();
            output.put("status", "400");
            output.put("message","Request格式或資料錯誤");
        }catch(MongoSocketReadTimeoutException msrt) {
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
