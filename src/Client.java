//Group 16 Vanilla Client DS-SIM
//Note: Testing

import java.net.Socket;

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
    
    
    //Global Variables for BF Algorithm
    
    private final int INT_MAX = 2147483647;
    private int bfCore = INT_MAX; 
    private int bfTime = INT_MAX; 


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
                        
                        if(algo == "bf") {
                        	//function call for best fit
                      
                        	bfAlgo("noRead");
                        }
                        sendToServer("OK");
                    }
                    
                    if(bfCore == INT_MAX && algo == "bf") {
                  
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
    		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse("/home/joshua/Downloads/ds-sim/system.xml"); 
        	doc.getDocumentElement().normalize();
        	
        	systemXML = doc.getElementsByTagName("server");
        	
        	
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		return systemXML;
    	}
    	
   
    }
    
    public void bfAlgo(String x) {
    	if(x == "readXML") {
    	try {
    		NodeList xml = readFile(); 
        	
        	for(int i =0 ; i < xml.getLength(); i++) {
        		serverType = xml.item(i).getAttributes().item(6).getNodeValue(); 
        		
        		serverID = 0; 
        		serverCpuCores = Integer.parseInt(xml.item(i).getAttributes().item(1).getNodeValue());
        		serverMemory = Integer.parseInt(xml.item(i).getAttributes().item(4).getNodeValue());
        		serverDisk = Integer.parseInt(xml.item(i).getAttributes().item(2).getNodeValue());

        		 if(jobCpuCores <= serverCpuCores && jobDisk <= serverDisk && jobMemory <= serverMemory ) {
             		if((serverCpuCores == bfCore && serverTime < bfTime)|| serverCpuCores < bfCore  ) {
             			finalServer = serverType; 
             			finalServerID = serverID; 
             			bfCore = serverCpuCores; 
             			bfTime = serverTime; 
             		}
             	}
        	
        	}
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	} else {
    		if(jobCpuCores <= serverCpuCores && jobDisk <= serverDisk && jobMemory <= serverMemory ) {
         		if(serverCpuCores < bfCore || (serverCpuCores == bfCore && serverTime < bfTime)) {
         			finalServer = serverType; 
         			finalServerID = serverID; 
         			bfCore = serverCpuCores; 
         			bfTime = serverTime; 
         		}
         	}
    	}
    	
       
    }
    
   
    

    public static void main(String[] args) {
    	
    	String algo = "bf"; 
    	
    	if(args.length > 1 && args[0] == "-a") {
    		algo = args[1]; 
    	}

        Client client = new Client( algo, "127.0.0.1", 50000);
    }
}

