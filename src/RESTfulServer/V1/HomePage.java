package RESTfulServer.V1;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
@Path("/V1")
public class HomePage {
    @GET
    @Produces("text/HTML;charset=UTF-8")
    public String sayHtmlHello() {
        String s = "<html>";
        s += "<title> Exhibition RESTful Server</title>"; 
        s += "<body>";
        s += "<h1>Exhibition RESTful Server-Version 1 API</h1>";
        s += "<h2> Base URL: http://163.22.17.174:8080/V1</h2>";
        //Token相關API
        s += "<h2> User相關API:</h2>";
        s += "<ul>";
        s += "<li>" + "新增/更新使用者fb資訊(PUT): /users" + "</li>";
        s += "</ul>";
        s += "<br/>";
        s += "<h2> 會展相關API:</h2>";
        s += "<ul>";/*
        s += "<li>" + "新增一筆新會展(POST): /exhibitions" + "</li>";
        s += "<li>" + "列出會展列表(GET): /exhibitions" + "</li>";
        s += "<li>" + "<B>(尚未實作)</B>列出參加此會展的出席者清單(GET): /exhibitions/{eid}/attendees" + "</li>";
        s += "<li>" + "User報名參加此會展(GET): /exhibitions/{eid}/attendees/{username}/signup" + "</li>";
        s += "<li>" + "User走訪廠商(POST): /exhibitions/visit" + "</li>";
        s += "<li>" + "查詢符合廠商id之所有使用者走訪記錄(GET): /exhibitions/{eid}/visitrecord/{manfid}" + "</li>";
        s += "<li>" + "列出使用者參加的會展列表(GET): /exhibitions/{username}/joinlist" + "</li>";*/
        s += "<li>" + "取得某該舉辦地區之各展點攤位圖(GET): /exhibitions/layout/{:country}" + "</li>";
        s += "<li>" + "取得某該舉辦地區家之行動問卷(GET): /questionnaire/{:country}?userid={:userid}" + "</li>";
        s += "<li>" + "回傳某User之行動問卷填答結果(POST): /questionnaire/{:country}" + "</li>";
        s += "<li>" + "取得某該舉辦地區之展覽資訊與各展點交通資訊(GET): /exhibitions/traffic/{:country}" + "</li>";
        s += "</ul>";
        s += "<br/>";
        s += "<h2> 主題活動相關API:</h2>";
        s += "<ul>";
        s += "<li>" + "取得某user之集點簿(GET): /exhibitions/activity/{:userid}/{:country}/collectionbox" + "</li>";
        s += "<li>" + "某user進行集章(PUT): /exhibitions/activity/collectionbox" + "</li>";
        s += "<li>" + "某user進行兌換(GET): /exhibitions/activity/{:userid}/{:country}/collectionbox/exchange" + "</li>";
        s += "</ul>";
        s += "<h2> 講座時間相關API:</h2>";
        s += "<ul>";
        s += "<li>" + "取得某舉辦國家之所有講座時間(GET): /exhibitions/lectures/{:country}" + "</li>";
        s += "</ul>";
        s += "<h2> 最新消息相關API:</h2>";
        s += "<ul>";
        s += "<li>" + "取得某舉辦國家之所有最新消息(GET): /news/{:country}" + "</li>";
        s += "</ul>";
        s += "<h2> 參展學校相關API:</h2>";
        s += "<ul>";
        s += "<li>" + "依照地區與學群條件搜尋，取得符合條件之學校清單(PUT): /school/search" + "</li>";
        s += "<li>" + "依照校名搜尋，取得符合條件之學校清單(PUT): /school/schname/search" + "</li>";
        s += "<li>" + "取得符合學校代碼之學校資訊(GET): /school/{:schoolid}?country={:country}" + "</li>";
        s += "</ul>";
        s += "<h2> 我的最愛相關API:</h2>";
        s += "<ul>";
        s += "<li>" + "取得符合條件之我的最愛清單(GET): /users/{:userid}/favoritelist?country={:country}" + "</li>";
        s += "<li>" + "新增/取消追蹤我的最愛(PUT): /users/{:userid}/favoritelist" + "</li>";
        s += "</ul>";
        s += "<h2> AppDataCenter專用:</h2>";
        s += "<ul>";
        s += "<li>" + "取得教育展列表(GET): /exhibitions/list" + "</li>";
        s += "<li>" + "取得單一教育展資訊(GET): /exhibitions/id/{:exhibition_id}" + "</li>";
        s += "<li>" + "新增單一教育展(POST): /exhibitions" + "</li>";
        s += "<li>" + "刪除單一教育展(DELETE): /exhibitions/{:exhibition_id}" + "</li>";
        s += "<li>" + "修改單一教育展(PUT): /exhibitions/{:exhibition_id}" + "</li>";
        s += "<br/>";
        s += "<li>" + "取得最新消息列表(GET): /news/list" + "</li>";
        s += "<li>" + "取得單一最新消息資訊(GET): /news/id/{:news_id}" + "</li>";
        s += "<li>" + "新增單一最新消息(POST): /news" + "</li>";
        s += "<li>" + "刪除單一最新消息(DELETE): /news/{:news_id}" + "</li>";
        s += "<li>" + "修改單一最新消息(PUT): /news/{:news_id}" + "</li>";
        s += "</ul>";
        s += "</body></html>";
        return s;
    }
}
