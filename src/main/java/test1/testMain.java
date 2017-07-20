/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test1;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import org.hibernate.Criteria;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;

/**
 *
 * @author yhuang
 */
public class testMain {

    Session session = null;

    //set a "global time" so that all the program are in sync
    public static Date time = new Date();
    public static String message = "No message";

    public testMain() {
        //open a "current" session that is bound to the life cycle of a transaction and will be flush and close when transaction end(commit or roll back)
        this.session = HibernateUtil.getSessionFactory().getCurrentSession();

    }

    public String genToken(String username) throws NullPointerException {
        String token = null;

        //check if user exists by querying database  with a select statement
        //need to throw exception
        Transaction tx = session.beginTransaction();
        try {

            //boolean exists = session.createQuery("select token from Info where username=:username").setParameter("username", username).uniqueResult() != null;
            Query q1=session.createQuery("select secretkey from Info where username=:username");
            q1.setParameter("username", username);
            boolean exists=q1.uniqueResult()!=null;
            //retrieving secretkey
            
            
            //System.out.println("Exists is: " + exists);
            if (exists == true) {
                //update token
                try {
                    //System.out.println("im doing shyt");
                    String key=q1.getSingleResult().toString();
                    System.out.println("my skey is:"+key);
                    TOTP otp = new TOTP();
                    String newToken = otp.OTP(key);
                    
                    token = newToken;
                    //refresh the global time
                    Date tempDate = new Date();
                    //test
                    testMain.time = tempDate;
                    
                    message = "Updating Token for past user";
                    Query q = session.createQuery("update Info set token = :token,created_on =:date where username =:username");
                    q.setParameter("date", testMain.time);
                    q.setParameter("token", newToken);
                    q.setParameter("username", username);
                    
                    q.executeUpdate();
                    tx.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    tx.rollback();
                    token = null;
                    message = "Could not execute statement";
                }
            } else {
                //set the token
                try {
                    Info s = new Info();
                    TOTP otp = new TOTP();
                    s.setUsername(username);
                    token = otp.OTP();
                    s.setToken(token);
                    String skey=otp.getSecretkey();
                    System.out.println("key is:"+skey);
                    s.setSecretkey(skey);
                    Date tempDate = new Date();
                    testMain.time = tempDate;
                    System.out.println(testMain.time);
                    s.setCreatedOn(testMain.time);
                    message = "Creating new Token for new user";

                    session.save(s);

                    //session.getTransaction().commit();
                    tx.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    tx.rollback();
                    token = null;
                    message = "Could not execute statement";
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

        }
        if (tx.isActive()) {
            tx.commit();
        }
        
        return token;

    }

    public List getToken(String username) {
        List<String> token = null;
        //String token="";
        try {
            Transaction tx = session.beginTransaction();
            Query q = session.createQuery("select token from Info where username=:input");
            q.setParameter("input", username);
            token = q.list();
            
            //Info info = new Info(usernam  e,token,date);
            //session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }

    //return the column of token
    public List getAllToken() {
        List<String> token = null;
        try {
            org.hibernate.Transaction tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(Info.class);
            //set specific property to token where 'token'= variable in Info.class
            criteria.setProjection(Projections.property("token"));
            token = criteria.list();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }

    /*public String validateToken(String username, String token) throws ParseException {
        String LastTokentime = null;
        message = null;
        //List<Info> test = null;
        try {
            org.hibernate.Transaction tx = session.beginTransaction();
            long now = System.currentTimeMillis();
            long nowMinus1Minutes = now - 1 * 60 * 1000;
            Timestamp a = new Timestamp(nowMinus1Minutes);
            //check if token exists, if exists check if expired
            Query q1 = session.createQuery("select createdOn from Info where username=:username and token=:token");
            q1.setParameter("token", token);
            q1.setParameter("username", username);
            //if token exists
            if (q1.uniqueResult() != null) {
                Query q = session.createQuery("select createdOn from Info where username=:username and token=:token and createdOn>:timelimit");
                q.setParameter("token", token);
                q.setParameter("username", username);
                q.setParameter("timelimit", a);
                
                //if criteria matched
                if (q.uniqueResult() != null) {

                    message = "Token validated sucessfully";
                } else {
                    message = "Token Expired";
                }
                //converting query result to a string without []
                String LastTokentimeinS = q.getSingleResult().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                LastTokentime = sdf.parse(LastTokentimeinS).toString();
                //LastTokentime =q.list().toString();

            } //if token dont exists
            else {
                message = "Token not found";
            }

            if (tx.isActive()) {
                tx.commit();
            }
        } catch (Exception e) {
                message="Error executing statement";
        }

        return LastTokentime;
    }
    */
    
  public String val(String username, String token){
      String auth=null;
      message=null;
      String dbtoken=null;
      //first check if user exists
      //if no return error message of user not found, and null token
      //if yes, call gentoken
      //retrieve token call database
      //compare userinput token n db token
      // if same, return message correct 
      //if wrong return token expire
      System.out.println("User Token: "+token);
      Transaction tx2 = session.beginTransaction();
      try {
        Query qx = session.createQuery("select secretkey from Info where username=:username");
        qx.setParameter("username", username);
        
        //see if user exists
        //if dont exists
        if (qx.uniqueResult()==null){
            System.out.println("No user found");
            message="Invalid User! Please generate your token first";
            auth="No";
        }
        //if user exists, start comparing token by generating one first and compare ith user input
        else {
            //set and update token
            
            try {
                    //System.out.println("im doing shyt");
                    String key=qx.getSingleResult().toString();
                    System.out.println("my skey is:"+key);
                    TOTP otp = new TOTP();
                    //generate new token using skey retrieve from database
                    String newToken = otp.OTP(key);                   
                    dbtoken = newToken;
                    
                    Date tempDate = new Date();
                    testMain.time = tempDate;

                    Query q = session.createQuery("update Info set token = :token,created_on =:date where username =:username");
                    q.setParameter("date", testMain.time);
                    q.setParameter("token", newToken);
                    q.setParameter("username", username);
                    
                    q.executeUpdate();
                    tx2.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    tx2.rollback();
                    dbtoken = null;
                    message = "Could not execute statement";
                }
            
            System.out.println("System generated token: "+dbtoken);
            if(dbtoken.equals(token)){
                message="Token validated sucessfully";
                auth="Yes";
            }
            else {
                message = "Token Expired";
                auth="No";
            }
            
        }
        if (tx2.isActive()) {
                tx2.commit();
            }
      }catch(Exception e){
          e.printStackTrace();
        
      }
      
      return auth;
  }
}
