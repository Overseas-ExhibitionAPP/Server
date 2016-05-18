package RESTfulServer.V1;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
/*
 * 自訂Response格式
 */
public class NewResponse{
    ResponseBuilder builder;
    public void setResponse(String input){
        builder = Response.status(Status.OK);
        builder.header("Access-Control-Allow-Origin", "*");
        builder.header("Access-Control-Allow-Headers", "x-requested-with");
        builder.entity(input);
    }

}
