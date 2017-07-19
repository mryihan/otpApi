package test1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;

import java.util.HashMap;
import spark.servlet.SparkApplication;
import java.util.Map;
import static spark.Spark.get;
import static spark.Spark.post;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author yhuang
 */
public class userController implements SparkApplication {
//public class userController {
    public static String dataToJson(Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, data);
            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException("IOException from a StringWriter?");
        }
    }

    //public static void main(String[] args) {
    @Override
    public void init(){
            //UserService a = new UserService();
            //List<String> token = null;
            Map<String,String> display =new HashMap<>();
        //ask for a userid as a input
        get("/gen", (request, response) -> {
            display.clear();
            //asking for /hello?userid=blahblah
            String name = request.queryParams("userid");
            testMain a = new testMain();
            response.type("application/json");
            response.status(200);
            display.put("Username", name );
            display.put("Token", a.genToken(name));
            display.put("Time created",testMain.time.toString());
            display.put("Message", testMain.message);
            return dataToJson(display);
            
        });
        
        //test method
         get("/", (request, response) -> "Hello 123");
        
        //validate user with given username n token
        get("/validate", (request, response) -> {
            display.clear();
            //asking for /hello?userid=blahblah
            String name = request.queryParams("userid");
            String token =request.queryParams("token");
            testMain a = new testMain();
            response.type("application/json");
            display.put("Username", name);
            display.put("Token", token);
            String testS="testing";
            //Timestamp b= 
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            //Date testD=sdf.parse(a.validateToken(name, token));
            //Timestamp ts=Timestamp.valueOf(a.validateToken(name, token));
            
            display.put("Last Token Time", a.validateToken(name, token));
            display.put("Message", testMain.message);
            return dataToJson(display);
            
        });
        
        post("/hello", (request, response) -> {
            String val = request.queryParams("userid");
            testMain a = new testMain();
            response.type("application/json");
            
            return dataToJson(a.genToken(val));
        });
        //get all current token
        get("/token", (request, response) -> {
            //asking for /hello?userid=blahblah
            //String name = request.queryParams("userid");
            testMain a = new testMain();
            response.type("application/json");
            return dataToJson(a.getAllToken());
            
        });

    }

}
