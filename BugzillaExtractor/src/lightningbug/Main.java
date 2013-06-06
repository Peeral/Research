/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lightningbug;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;

/**
 *
 * @author Administrator
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    private Connection connection;
    private XmlRpcClient rpcClient;
    private List<MyBug> bugList = null;
    PrintWriter pw;
    

    public Connection getConn() {
        return connection;
    }

    public Main() throws ClassNotFoundException, SQLException, MalformedURLException, XmlRpcException, IOException {
        Properties props = getProperties("bugzilla.properties");
        //setupDBConn(props);
        setupRPCClient(props);
        loginBugzilla(props);
        pw = new PrintWriter(new FileWriter("General.txt", true));
    }

    private Properties getProperties(String propsFileName) throws IOException {
        Properties props = new Properties(System.getProperties());
        props.load(new FileInputStream(propsFileName));
        return props;
    }

    private void loginBugzilla(Properties props) throws XmlRpcException {
        // map of the login data
    	System.out.println("LOGIN");
        Map loginMap = new HashMap();
        loginMap.put("login", props.getProperty("bugzilla.wsuser"));
        loginMap.put("password", props.getProperty("bugzilla.wspw"));
        loginMap.put("rememberlogin", "Bugzilla_remember");
        // login to bugzilla
        Object loginResult = rpcClient.execute("User.login", new Object[]{loginMap});
        System.out.println("LOGIN SUCCESSFUL");
    }

    private void setupDBConn(Properties props) throws IOException, ClassNotFoundException, SQLException {
        System.out.println("getting database connection...");
        String driverName = props.getProperty("bugzilla.driverName");
        Class.forName(driverName);
        String url = props.getProperty("bugzilla.dburl");
        String username = props.getProperty("bugzilla.dbuser");
        String password = props.getProperty("bugzilla.dbpw");
        connection = DriverManager.getConnection(url, username, password);
    }

    private void setupRPCClient(Properties props) throws MalformedURLException {
        System.out.println("getting bugzilla connection...");
        HttpClient httpClient = new HttpClient();
        rpcClient = new XmlRpcClient();
        XmlRpcCommonsTransportFactory factory = new XmlRpcCommonsTransportFactory(rpcClient);
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

        factory.setHttpClient(httpClient);
        rpcClient.setTransportFactory(factory);
        String wsurl = props.getProperty("bugzilla.wsurl");
        config.setServerURL(new URL(wsurl));

        rpcClient.setConfig(config);
    }

    public static void main(String[] args) throws Exception {
    	System.out.println("START");
        Main m = new Main();
        //Statement st = m.getConn().createStatement();
        System.out.println("searching bugzilla...");
        Map bugSearch = new HashMap();
        
        // Apache products
        //Object[] products = {"Apache httpd-2"};//{"Ant", "Apache httpd-1.3", "Apache httpd-2"};
        
        // Eclipse products
        //Object[] products = {"Linux Tools"};//{"BIRT", "JDT", "Mylyn"};
       
        
        // Wireshark products
        //Object[] products = /*{"Web sites",*/ {"Wireshark"};
        
        // Mindrot - OpenSSH
        //Object[] products = {"Portable OpenSSH"};//{"Portable OpenSSH"};
        
        // Mozilla
        //Object[] products = {"SeaMonkey"};
        
        // Kernel
        //Object[] products = {"Tools"};
        
        // Apache OpenOffice
        Object[] products = {"General"};
        
        //Object[] components = {"All", "Build", "Core"}; //list all component. ommit to include all components
        Object[] status = {"CLOSED"};
        Object[] resolutions = {"FIXED"};
        //Object[] resolutions = {"CODE_FIX"};
        bugSearch.put("product", products);
        //bugSearch.put("component", components);  //comment this line to include all component
        bugSearch.put("status", status);
        bugSearch.put("resolution", resolutions);
      
        m.bugList = m.searchBugs(bugSearch, products);
//        List<Integer> idList = new ArrayList();
//        List<Timestamp> dateTimeList = new ArrayList();
//        List<String> versionList = new ArrayList();
        List<Integer> idList = new ArrayList();
        List<Timestamp> creationDateList = new ArrayList();
        List<String> versionList = new ArrayList();
        System.out.println("building arraylist...");
        
        //build array of ids for retrieving history details for each bug id
        for (MyBug mb : m.bugList) {
            idList.add(mb.getId());
            creationDateList.add(mb.getReported());
            versionList.add(mb.getVersion());
        }

        int totalPatches = 0;
        
        Object[] ids = idList.toArray();
//        int index = 0;
//        for (int i = 0; i <100; i++) {
//        	Object id[] = new Object [ids.length/100];
//        	for (int j = 0; j < id.length; j ++) {
//        		id[j] = ids[index++];
//        	}
        
//        for (int i = 0; i < ids.length; i ++) {
//        	Map bugMap = new HashMap();
//        	bugMap.put("ids", ids[i]);
//        	//System.out.println("Call getPatches(): " + i);
//        	totalPatches += m.getPatches(bugMap);
//        }
//        
//        System.out.println("Total Patches for this Project: " + totalPatches);
        
        Map bugMap = new HashMap();
        bugMap.put("ids", ids);
        System.out.println("Call getPatches()");
        m.getPatches(bugMap, creationDateList, versionList, idList);
        

        
//        System.out.println("processing bugs...");
//        m.processHistory(bugMap);
//        String sql = "INSERT INTO httpdbugs (bug_id, product, component, status, resolution, reported, closed, summary, assignee) "
//                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        PreparedStatement ps = m.getConn().prepareStatement(sql);
//        System.out.println("building batch update statment...");
//        
//        for (MyBug mb : m.bugList) {
//            ps.setLong(1, mb.getId());
//            ps.setString(2, mb.getProduct());
//            ps.setString(3, mb.getComponent());
//            ps.setString(4, mb.getStatus());
//            ps.setString(5, mb.getResolution());
//            ps.setTimestamp(6, mb.getReported());
//            ps.setTimestamp(7, mb.getClosed());
//            ps.setString(8, mb.getSummary());
//            ps.setString(9, mb.getAssignee());
//            ps.addBatch();
//        }
//
//        System.out.println("executing batch update...");
//        ps.executeBatch();
//        System.out.println("done...");
    }

    public Timestamp returnCreationDate (int id, List<Timestamp> creationDateList, List<Integer> idList) {
    	int i = idList.indexOf(id);
    	return creationDateList.get(i);
    }
    
    public String returnVersion (int id, List<String> versionList, List<Integer> idList) {
    	int i = idList.indexOf(id);
    	return versionList.get(i);
    }
    
    
    public int getPatches(Map bugMap, List<Timestamp> creationDateList, List<String> versionList, List<Integer> idList) throws XmlRpcException {
    	Map result = (Map) rpcClient.execute("Bug.attachments", new Object[]{bugMap});
    	System.out.println("Getting Patches...");
    	HashMap bugs = (HashMap) result.get("bugs");
    	Set s = bugs.keySet();
    	Iterator i = s.iterator();
    	int countPatches = 0;
    	long avgFixTime = 0;
    	int totalBugsWithPatches = 0;
    	
//    	System.out.println("BugID \t BugVersion(ReleaseDate) \t BugReportDate" +
//    			" \t PatchDate \t FixTime");
    	pw.println("BugID \t BugVersion(ReleaseDate) \t BugReportDate" +
    			" \t PatchDate \t FixTime");
    	while(i.hasNext()) {
    		String bug = (String) i.next();
    		int bugId = Integer.parseInt (bug);
    		//System.out.println(bug);
    		Object[] attachments = (Object[]) bugs.get(bug); 
//    		Integer bugId = (Integer) bug.get("id");
//    		Collection values = (Collection) bug.values();
    		int count = 0;
//    		Object[] attachments = values.toArray();

    		if (attachments == null) {
    			continue;
    		}
    		///System.out.println("Total Attachments: " + attachments.length);
    		
    		Long fixTime = (long) 0.0;
    		for (int j = 0; j < attachments.length; j ++) {
    			HashMap attachment = (HashMap) attachments[j];
    			//System.out.println(attachment.get("is_patch"));
    			if ((Integer) attachment.get("is_patch") == 1) {
    				if (count == 0) {
    					count += 1;
    				}
    				//System.out.println(attachment.get("data"));
    				countPatches ++;
    				Timestamp reportDate = returnCreationDate(bugId, creationDateList, idList);
    				Timestamp patchDate = new Timestamp(((java.util.Date) attachment.get("creation_time")).getTime());
    				fixTime = (Long) (patchDate.getTime() - reportDate.getTime())/ (1000 *60 *60 *24);
//    				System.out.println(bugId + "\t"
//    						//+ idList.get(index) + "\t"
//    						+ returnVersion(bugId, versionList, idList) + "\t" 
//    						+ reportDate + "\t"
//    						+ patchDate + "\t" 
//    						+ fixTime + " days");
    				pw.println(bugId + "\t"
    						//+ idList.get(index) + "\t"
    						+ returnVersion(bugId, versionList, idList) + "\t" 
    						+ reportDate + "\t"
    						+ patchDate + "\t" 
    						+ fixTime + " days");
    			}
    		}
    		
    		// This will add only the latest patch to calculate the average
    		avgFixTime += fixTime;
    		totalBugsWithPatches += count;
    		//System.out.println("For Bug " + bugId + ", No of patches: " + count);
    	}
    	System.out.println("Total Patches: " + countPatches);
    	System.out.println("Average Fix Time: " + (avgFixTime/totalBugsWithPatches) + " days");
    	pw.println("Total Patches: " + countPatches);
    	pw.println("Average Fix Time: " + (avgFixTime/totalBugsWithPatches) + " days");
    	pw.close();
    	return countPatches;
    	
    }
    
    
    private List<MyBug> searchBugs(Map bugSearch, Object[] products) throws XmlRpcException {
        Map searchResult = (Map) rpcClient.execute("Bug.search", new Object[]{bugSearch});
        Object[] bugs = (Object[]) searchResult.get("bugs");
        List<MyBug> myBugList = new ArrayList();
        MyBug myBug;

        int count[] = new int[products.length];
        
        for (int i = 0; i < bugs.length; i++) {

            HashMap bug = (HashMap) bugs[i];
            myBug = new MyBug();
            Integer bugId = (Integer) bug.get("id");
            String component = (String) bug.get("component");
            String product = (String) bug.get("product");
            String resolution = (String) bug.get("resolution");
            String status = (String) bug.get("status");
            Long reportedlt = ((java.util.Date) bug.get("creation_time")).getTime();
            String summary = (String)bug.get("summary");
            String assignee = (String)bug.get("assigned_to");
            String version = (String)bug.get("version");
            
            
            myBug.setReported(new Timestamp(reportedlt));
            myBug.setProduct(product);
            myBug.setComponent(component);
            myBug.setResolution(resolution);
            myBug.setStatus(status);
            myBug.setId(bugId);
            myBug.setAssignee(assignee);
            myBug.setSummary(summary);
            myBug.setVerison(version);
            myBugList.add(myBug);
            
//            for (int i1 = 0; i1 < products.length; i1 ++) {
//            	if (products[i1].equals(product)) {
//            		count[i1] ++;
//            		break;
//            	}
//            }
        }
        
//        for (int i = 0; i < products.length; i ++) {
//        	System.out.println(products[i] + " bugs: " + count[i]);
//        }
        
        System.out.println("Total Number of bugs returned: " + bugs.length);
        return myBugList;
    }

    public void processHistory(Map bugMap) throws XmlRpcException {
        Map historyResult = (Map) rpcClient.execute("Bug.history", new Object[]{bugMap});
        Object[] bugs = (Object[]) historyResult.get("bugs");
        BugComparator bugComparator = new BugComparator();
        Collections.sort(this.bugList, bugComparator);

        for (int i = 0; i < bugs.length; i++) {
            MyBug myBug = getMyBug(bugs[i]);
            int index = Collections.binarySearch(this.bugList, myBug, bugComparator);
            this.bugList.get(index).setClosed(myBug.getClosed());
        }
    }

    public MyBug getMyBug(Object o) {
        //Map[] history = (HashMap[]) map.get("history
        MyBug myBug = new MyBug();
        HashMap bug = (HashMap) o;
        Integer id = null;
        boolean closed = false;
        String status = null;
        Timestamp timestamp = null;
        //System.out.println(bug.get("id"));
        Object[] history = (Object[]) bug.get("history");
        for (int j = 0; j < history.length; j++) {
            HashMap h = (HashMap) history[j];
            Object[] changes = (Object[]) h.get("changes");

            for (int k = 0; k < changes.length; k++) {
                HashMap c = (HashMap) changes[k];
                //System.out.println(c.get("field_name"));
                if ("bug_status".equalsIgnoreCase(c.get("field_name").toString())) {
                    if ("closed".equalsIgnoreCase(c.get("added").toString())) {
                        //System.out.println(h.get("when"));
                        closed = true;
                        id = (Integer) bug.get("id");
                        status = (String) c.get("added");
                        Long longtime = ((java.util.Date) h.get("when")).getTime();
                        // System.out.println(longtime);
                        timestamp = new Timestamp(longtime);
                        // System.out.println(timestamp);                       
                        //break;
                    } else if ("reopened".equalsIgnoreCase(c.get("added").toString())) {
                        closed = false;
                        id = null;
                        status = null;
                        timestamp = null;
                    }
                }

            }


        }
        if (closed) {
            //to handle for cases where a bug is reopened.
            myBug.setId(id);
            myBug.setStatus(status);
            myBug.setClosed(timestamp);
        }

        return myBug;
    }
}