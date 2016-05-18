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
        s += "<h2> Login相關API:</h2>";
        s += "<ul>";
        s += "<li>" + "login(POST): /tokens" + "</li>";
        s += "</ul>";
        s += "<br/>";
        //測驗相關API
        s += "<h2> 會展相關API:</h2>";
        s += "<ul>";
        s += "<li>" + "新增一筆新會展(POST): /exhibitions" + "</li>";
        s += "<li>" + "列出會展列表(GET): /exhibitions" + "</li>";
        s += "<li>" + "<B>(尚未實作)</B>列出參加此會展的出席者清單(GET): /exhibitions/{eid}/attendees" + "</li>";
        s += "<li>" + "User報名參加此會展(GET): /exhibitions/{eid}/attendees/{username}/signup" + "</li>";
        s += "<li>" + "User走訪廠商(POST): /exhibitions/visit" + "</li>";
        s += "<li>" + "查詢符合廠商id之所有使用者走訪記錄(GET): /exhibitions/{eid}/visitrecord/{manfid}" + "</li>";
        s += "<li>" + "列出使用者參加的會展列表(GET): /exhibitions/{username}/joinlist" + "</li>";
        s += "</ul>";
        s += "<br/>";
        //User相關API
        s += "<h2> User相關API:</h2>";
        s += "<ul>";
        s += "<li>" + "註冊一名使用者資訊(POST): /users" + "</li>";
        s += "<li>" + "<B>(尚未實作)</B>更新使用者資訊(PUT): /users/{uid}" + "</li>";
        s += "</ul>";
        //User相關API
        s += "<h2> 廣告相關API:</h2>";
        s += "<ul>";
        s += "<li>" + "新增一新廣告(POST): /advertisements" + "</li>";
        s += "<li>" + "查詢廣告列表(GET): /advertisements" + "</li>";
        s += "</ul>";
        s += "</body></html>";
        return s;
    }
}
