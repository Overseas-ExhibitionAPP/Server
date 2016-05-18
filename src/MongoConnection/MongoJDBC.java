/*
 *  File name : MongoJDBC.java
 *  Author : Nai-Jia Chen
 *  Since : 2015/05/08
 *  
 *  Finished:
 *  1.MongoDB Connection
 *  2.show All Collection method
 *  3.drop collection method
 *  4.print all document from one collection(find All method)
 */
package MongoConnection;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import java.util.*;
public class MongoJDBC {
	public MongoClient mClient;
	public DB db;
	public MongoJDBC() throws Exception{
        try{
            MongoCredential credential = MongoCredential.createCredential("account", "DB_Name", "password".toCharArray());
            mClient = new MongoClient(new ServerAddress("IP"), Arrays.asList(credential));
            db = mClient.getDB("DB Name");
        }catch(Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }
    /*
     * Method name : ShowAllCollection
     * Description : show all collection name from DB
     */
    public void showAllCollection() {
        Set<String> colls = db.getCollectionNames();
        for(String s: colls) {
            System.out.println(s);
        }
    }
    /*
     * Method name : findAll
     * Description : print all document from collection 
     */
    public void findAll(String colname) {
        DBCollection testcol = db.getCollection(colname);
        DBCursor cur = testcol.find();
        int count = cur.count();
        System.out.println(count);
        while(cur.hasNext()) {
            System.out.println(cur.next());
        }
    }
    /*
     * Method name : deleteCollection
     * Description : drop a collection
     */
    public void deleteCollection(String colname) {
        db.getCollection(colname).drop();
        System.out.println("Drop " + colname + " collection");
    }
    public static void main(String[] argv) throws Exception {
        new MongoJDBC();
    }
}
