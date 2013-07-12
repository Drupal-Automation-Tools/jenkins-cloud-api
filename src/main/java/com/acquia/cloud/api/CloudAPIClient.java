package com.acquia.cloud.api;

import flexjson.JSONDeserializer;
import org.apache.http.HttpEntity;
import java.net.URI;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpGet;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;

//@TODO: think about this. 
import hudson.model.BuildListener;


/**
 *
 * @author mark.sakurada
 */
public class CloudAPIClient {
  
  private static CloudAPIResource_Client entity;  
  
  public CloudAPIClient(String username, String password) {
    entity = new CloudAPIResource_Client(username,  password);
  
      }  
  
  public String getSites (BuildListener listener) {
    String sitesstr = entity.getSite(); 
    String sites= new String();
    try{
      sites = new JSONDeserializer<ArrayList<String>>().deserialize(sitesstr).get(0).toString();
    } catch (Exception e) {
      sites ="failure";
      listener.getLogger().println(sites);
    }
    return sites;   
  }
  
 // public HashMap getProps(String sitename, String env, String dbs, String dbackup, String proptype) {
  public HashMap getProps(String sitename) {
    String path = "/sites/" + sitename  + ".json";
    return getParseJson(path);
  }

  public HashMap getProps(String sitename, String envname) {
    String path = "/sites/" + sitename + "/envs/" + envname + ".json";
    return getParseJson(path);
  }
  
  public HashMap getProps(String sitename, String envname, String dbname) {
    String path = "/sites/" + sitename  + "/envs/" + envname + "/dbs/" + dbname + "/backups.json";
    return getParseJson(path);
  }
  
  public HashMap getProps(String sitename, String envname, String dbname, String backupid) {
    String path = "/sites/" + sitename  + "/envs/" + envname + "/dbs/" + dbname + "/backups/" + backupid + ".json";
    return getParseJson(path);
  }  
    
  public HashMap getParseJson(String path) {
    String jsonstr = entity.getInfo(path);
    HashMap map = new HashMap();
    try {
    map = new JSONDeserializer<HashMap>().deserialize(jsonstr); 
    } catch (Exception e) {
      System.out.println( e + ": " + jsonstr);
    }
    return map;  
  }
  
  public HashMap postParseJson(String path) {
    String jsonstr = entity.postInfo(path);
    HashMap map = new HashMap();
    try {
    map = new JSONDeserializer<HashMap>().deserialize(jsonstr); 
    } catch (Exception e) {
      System.out.println( e + ": " + jsonstr);
    }
    return map;  
  }
  
  public HashMap delParseJson(String path) {
    String jsonstr = entity.deleteInfo(path);
    HashMap map = new HashMap();
    try {
    map = new JSONDeserializer<HashMap>().deserialize(jsonstr); 
    } catch (Exception e) {
      System.out.println( e + ": " + jsonstr);
    }
    return map;  
  }
  
  public ArrayList<HashMap> parseJsonArray(String path) {
    String jsonstr = entity.getInfo(path);
    ArrayList<HashMap> map = new ArrayList<HashMap>();
    try {
    map = new JSONDeserializer<ArrayList<HashMap>>().deserialize(jsonstr); 
    } catch (Exception e) {
      System.out.println( e + ": " + jsonstr);
    }
    return map;  
  }
 
  public ArrayList<HashMap> getSubs(String classType, String sitename) {
    String path = new String();
    if ("Env".equals(classType)) {path = "/sites/" + sitename  + "/envs.json";} 
    if ("Task".equals(classType)) {path = "/sites/" + sitename  + "/tasks.json";} 
    return parseJsonArray(path);   
  }

  public ArrayList<HashMap> getSubs(String classType, String sitename, String envname) {
    String path = "/sites/" + sitename  + "/envs/" + envname + "/dbs.json";
    return parseJsonArray(path);   
  }

  public ArrayList<HashMap> getSubs(String classType, String sitename, String envname, String dbname) {
    String path = "/sites/" + sitename  + "/envs/" + envname + "/dbs/" + dbname + "/backups.json"; 
    return parseJsonArray(path);   
  }

  public HashMap postEnvDbsBackups(String sitename, String envname, String dbname) {
    String path = "/sites/" + sitename  + "/envs/" + envname + "/dbs/" + dbname + "/backups.json";
    return postParseJson(path);
  }
  
  public HashMap deleteEnvDbsBackups(String sitename, String envname, String dbname, String backupid) {
    String path = "/sites/" + sitename  + "/envs/" + envname + "/dbs/" + dbname + "/backups/" + backupid + ".json";
    return delParseJson(path);
  }   
 
  public HashMap restoreEnvDbsBackups(String sitename, String envname, String dbname, String backupid) {
    String path = "/sites/" + sitename  + "/envs/" + envname + "/dbs/" + dbname + "/backups/" + backupid + "/restore.json";
    return postParseJson(path);   
  }   
  
  public HashMap codeDeployEnvEnv(String sitename, String envsrcname, String envtgtname) {
    String path = "/sites/" + sitename  + "/code-deploy/" + envsrcname + "/" + envtgtname + ".json";
    return postParseJson(path);
  }
  
  public HashMap dbCopyEnvEnv(String sitename, String envsrcname, String envtgtname, String dbname) {
    String path = "/sites/" + sitename  + "/dbs/" + dbname + "/db-copy/" + envsrcname + "/" + envtgtname + ".json";
    return postParseJson(path);
  }
  
  public HashMap vcsDeployVcsEnv(String sitename, String envname, String tagname) {
    String path = "/sites/" + sitename  + "/envs/" + envname + "/code-deploy.json?path=" + tagname;
    return postParseJson(path);
  }
  
  public HashMap fileCopyEnvEnv(String sitename, String envsrcname, String envtgtname) {
    String path = "/sites/" + sitename  + "/files-copy/" + envsrcname + "/" + envtgtname + ".json";
    return postParseJson(path);
  }
   
  public void close() {
      entity.close();
  } 
  
private static class CloudAPIResource_Client  {
  DefaultHttpClient httpclient = new DefaultHttpClient();
  private URI uri;
  private HttpEntity entity;
  private String rtnval = "";
  
        CloudAPIResource_Client(String username, String password) {
            httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope("cloudapi.acquia.com", 443),
                    new UsernamePasswordCredentials(username, password));           
        }

        public String getSite() {
             try {
                HashMap<Integer, String> rtrnmap = API_GetCall("/sites.json"); 
                rtnval = rtrnmap.get(200).toString(); 
             } catch (Exception e) {
                System.out.println(e);
              } 
             return rtnval;
        }

        public String getInfo(String path) {
          HashMap<Integer, String> rtrnmap = new HashMap<Integer, String>();
             try {
                rtrnmap = API_GetCall(path); 
                rtnval = rtrnmap.get(200).toString();                
             } catch (Exception e) {
               System.out.println(rtrnmap.toString());
               System.out.println(e);
              } 
             return rtnval;
        }
        
        public String postInfo(String path) {
             try {
                rtnval=EntityUtils.toString(API_PostCall(path));
             } catch (Exception e) {
                System.out.println(e);
              } 
             return rtnval;
        }
        
        public String deleteInfo(String path) {
             try {
                rtnval=EntityUtils.toString(API_DeleteCall(path));
             } catch (Exception e) {
                System.out.println(e);
              } 
             return rtnval;
        }
                      

        public HashMap<Integer, String> API_GetCall(String uripath) {
              HashMap<Integer, String> rtrnMap=new HashMap<Integer, String>();  
              try{                  
                 HttpResponse response = httpclient.execute(new HttpGet(API_formaturi(uripath)));
                 Integer statusCode = response.getStatusLine().getStatusCode();
                 entity=response.getEntity();
                  rtrnMap.put(statusCode,EntityUtils.toString(entity));
                } catch (Exception e) {
                    System.out.println(e);
                } 
                return rtrnMap;
        }
        
        public HttpEntity API_PostCall(String uripath) {
                try{
                  entity = httpclient.execute(new HttpPost(API_formaturi(uripath))).getEntity();
                } catch (Exception e) {
                    System.out.println(e);
                } 
                return entity;
        }

        public HttpEntity API_DeleteCall(String uripath) {
                try{
                  entity = httpclient.execute(new HttpDelete(API_formaturi(uripath))).getEntity();
                } catch (Exception e) {
                    System.out.println(e);
                } 
                return entity;
        }
        
        
        public void close() {
            httpclient.getConnectionManager().shutdown();
        }
    }

    public static URI API_formaturi(String path) throws Exception {
    URI uri = URIUtils.createURI("https", "cloudapi.acquia.com/v1", -1, path, null, null);
    return uri;
    }
}