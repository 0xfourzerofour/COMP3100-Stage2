//Group 16 Vanilla Client DS-SIM
//Note: Testing2

import java.net.Socket;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Client {
    private Socket socket            = null;
    private PrintWriter out = null;
    private BufferedReader input = null;
    private String input1 = "";
 
    private int jobCpuCores, jobMemory, jobDisk, jobSub, jobID, jobTime;
    private String serverType;
    private int serverTime, serverState, serverCpuCores, serverMemory, serverDisk;
    private int serverID;
    private int jobCount = 0;
    private int finalServerID = 0;
    private String finalServer = "";
    
    //Global Variables for ff Algorithm
    private int first = 0;
    private ArrayList<String[]> servers = new ArrayList<String[]>();
    
    
    //Global Variables for BF Algorithm
    
    private final int INT_MAX = 2147483647;
    private int bfCore = INT_MAX;
    private int bfTime = INT_MAX;

    //Global Variables for WF Algorithm
    private final int INT_MIN = 0;
    private int altn = INT_MIN;
    private int wf = INT_MIN;
    private boolean worstFound = false;


    public Client(String algo ,String address, int port) {
        try {
            
             try {
                 socket = new Socket(address, port);
                 out = new PrintWriter(socket.getOutputStream());
                 input = new BufferedReader( new InputStreamReader(socket.getInputStream()));
                 sendToServer("HELO");
             } catch(IOException io) {
                 System.out.println(io);
             }
 
            if(newStatus("OK")) {
                sendToServer("AUTH " + System.getProperty("user.name"));
            }
           
            while (!newStatus("NONE")){
                if(currentStatus("OK")) {
                    sendToServer("REDY");
                } else if (input1.startsWith("JOBN")) {
                    jobRecieve();
                    sendToServer("RESC All");
                    
                    if(newStatus("DATA")) {
                        sendToServer("OK");
                    }

                    while (!newStatus(".")) {
                        serverRecieve();
                        
                        if(algo.equals("bf")) {
                            //function call for best fit
                            bfAlgo("noRead");
                        }  if(algo.equals("ff")){
                        	if(first == 0) {
                            	ff();
                        	}
                            
                        }
                       
                        sendToServer("OK");
                    }
                    
                    if(bfCore == INT_MAX && algo.equals("bf")) {
                  
                        bfAlgo("readXML");
                    }  
                 
                    
                    sendToServer("SCHD " + jobCount + " " + finalServer + " " + finalServerID);

                  jobCount++;
                }
            }

            closeConnection();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void jobRecieve() {
        String[] jobInput = input1.split("\\s+");
        jobSub = Integer.parseInt(jobInput[1]);
        jobID = Integer.parseInt(jobInput[2]);
        jobTime = Integer.parseInt(jobInput[3]);
        jobCpuCores = Integer.parseInt(jobInput[4]);
        jobMemory = Integer.parseInt(jobInput[5]);
        jobDisk = Integer.parseInt(jobInput[6]);
        bfCore = INT_MAX;
        bfTime = INT_MAX;
        wf = INT_MIN;
        altn = INT_MIN;
        worstFound = false;
    }

    public void serverRecieve() {
        String[] serverInput = input1.split("\\s+");
        serverType = serverInput[0];
        serverID = Integer.parseInt(serverInput[1]);
        serverState = Integer.parseInt(serverInput[2]);
        serverTime = Integer.parseInt(serverInput[3]);
        serverCpuCores = Integer.parseInt(serverInput[4]);
        serverMemory = Integer.parseInt(serverInput[5]);
        serverDisk = Integer.parseInt(serverInput[6]);
    }

    public void closeConnection() {
        try {
            sendToServer("QUIT");
            input.close();
            out.close();
            socket.close();
        } catch(IOException io) {
            System.out.println(io);
        }
    }

   
    public void sendToServer(String x) {
        out.write(x + "\n");
        out.flush();
    }


    public boolean currentStatus(String x) {
        try {
            if(input1.equals(x)){
                return true;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public boolean newStatus(String x) {
        
        try {
            input1 = input.readLine();
            
            if(input1.equals(x)){
                return true;
            }
            
            
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }
    
    @SuppressWarnings("finally")
    public NodeList readFile() {
        
        NodeList systemXML = null;
        
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("/home/peter/Desktop/ds-sim/system.xml");
            doc.getDocumentElement().normalize();
            
            systemXML = doc.getElementsByTagName("server");
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return systemXML;
        }
        
   
    }
    
    
    // do this for ff algorithm
    public void ff() {    	
    	ArrayList<String[]> temp = new ArrayList<String[]>();
        for (int i = 0; i < servers.size(); i++)  {
            String[] temp2 = servers.get(i);
                temp.add(temp2); 
        }
        System.out.println("Hello");

        servers.clear();
        servers.addAll(temp);
    
        for (int i = 0; i < servers.size(); i++)  {
            String[] temp2 = servers.get(i);
            serverType = temp2[0];
            serverID = Integer.parseInt(temp2[1]);
            serverCpuCores = Integer.parseInt(temp2[4]);
            serverMemory = Integer.parseInt(temp2[5]);
            serverDisk = Integer.parseInt(temp2[6]);
            System.out.println(serverType);
 
            if(jobCpuCores <= serverCpuCores && jobMemory <= serverMemory && jobDisk <= serverDisk) {
                finalServer = serverType;
                finalServerID = serverID;
                System.out.println("First fit");
                return;
            }
            
            }
 
        String[] ffSplit = input1.split("\\s+");
        servers.add(ffSplit);
    }
    //
    
    public void algoXML(String xmlAlgo) {
    	NodeList xml = readFile(); 
    	
    	if(xmlAlgo == "bf") {
    		for(int i = 0; i < xml.getLength(); i++) {
    			serverType = xml.item(i).getAttributes().item(6).getNodeValue();
                
                serverID = 0;
                serverCpuCores = Integer.parseInt(xml.item(i).getAttributes().item(1).getNodeValue());
                serverMemory = Integer.parseInt(xml.item(i).getAttributes().item(4).getNodeValue());
                serverDisk = Integer.parseInt(xml.item(i).getAttributes().item(2).getNodeValue());
    		}
    		
    	}
 	
    }
    
    
    
    public void bfAlgo(String x) {
        if(x == "readXML") {
        algoXML("bf"); 
        
        } 
            
        if(jobCpuCores <= serverCpuCores && jobDisk <= serverDisk && jobMemory <= serverMemory ) {
                 if(serverCpuCores < bfCore || (serverCpuCores == bfCore && serverTime < bfTime)) {
                     finalServer = serverType;
                     finalServerID = serverID;
                     bfCore = serverCpuCores;
                     bfTime = serverTime;
                 }
             }
        }
        
       
    
    

   
    

    public static void main(String[] args) {
        
        String algo = "bf";

        if (args.length == 2 && args[0].equals("-a")) {
            algo = args[1]; 
        }       
        System.out.println(algo);
        
        Client client = new Client( algo, "127.0.0.1", 50000);
    }
}