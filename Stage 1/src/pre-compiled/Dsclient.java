import java.io.*;
import java.net.*;
import java.lang.Object.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;
import java.util.*;

class Dsclient{

	// Global Variables  
	public static String HELO = "HELO";
	public static String REDY = "REDY";
	public static String GETS = "GETS Avail ";
	public static String OK = "OK";
	public static String QUIT = "QUIT";
	public static String SCHD = "SCHD ";
	public static String gap = " ";
	public static String AUTH = "AUTH ";
 	public static String[][] jobAttributes = null;

	public static boolean receivednone = false;
	public static int[][] serverslist;
	public static String[] servertypes;
	public static int largestServerIndex;
	public static String[] message;
	public static DataOutputStream dout = null;
	public static Socket s = null;
	public static BufferedReader din = null;
	public static int[][] servers = null; 

	// Constructor
   	 public Dsclient(String address, int port) throws Exception {
		s = new Socket(address, port);

		// Receive buffer from server
		din= new BufferedReader(new InputStreamReader(s.getInputStream()));

		// Sends output to the socket
		dout= new DataOutputStream(s.getOutputStream());
	    }

	 // Helper method to notify Client
	 public static void notifyClient(String message){
	 	System.out.println(message);
	 }

	/*Function to perform authentication
	Returns true upon successful authentication
	false otherwise.*/
	public static boolean performAuth(){	
		String msg = "";
		try{
			// Auth information
			String user = System.getProperty("user.name");
			String auth = AUTH +user;
			// Input and Output streams
			//dout = new DataOutputStream(s.getOutputStream());
			// Sending HELO to server
			dout.write(HELO.getBytes());
			msg = getMsg();
			//Sending Auth info
			notifyClient("Performing Authentication \n");
			messageToServer(auth);
			receivednone = true;
			return true;
		}
		catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
	}

	/* Function to which parses the ds-system.xml and store Server details in a 
	 2-Dimensional Array such as 
	    _______________________________
	    |Index|Limit|Cores|Memory|Disk |
	    |_____|_____|_____|______|____ |
	    |  0  |  2	|  4  |	4000 |16000|
	    |_____|_____|_____|______|____ |

	  Consequently the server_types is stored in a String Array.
	  Therefore Server_type[0] will have its details in server_list[0][_]

	 returns a 2-D int array of the server details 
	 */
	public static int[][] readXML(){
		serverslist = new int[0][0];
		try {
			File systemXML = new File("./ds-system.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(systemXML);
			doc.getDocumentElement().normalize();
			NodeList servers = doc.getElementsByTagName("server");
			serverslist = new int[servers.getLength()][servers.getLength()*4];
			servertypes = new String[servers.getLength()];
			for (int i = 0; i < servers.getLength(); i++) {
				Element server = (Element) servers.item(i);
				String t = server.getAttribute("type");
				int l = Integer.parseInt(server.getAttribute("limit"));
				int b = Integer.parseInt(server.getAttribute("bootupTime"));
				float hr = Float.parseFloat(server.getAttribute("hourlyRate"));
				int c = Integer.parseInt(server.getAttribute("coreCount"));
				int m = Integer.parseInt(server.getAttribute("memory"));
				int d = Integer.parseInt(server.getAttribute("disk"));
				serverslist[i][0] = l;
				serverslist[i][1] = c;
				serverslist[i][2] = m;
				serverslist[i][3] = d;
				servertypes[i] = t;	
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return serverslist;	
       	}

        /* Function to recieve Message from the server
	 * returns a String 
	 * */
	public static String getMsg() throws Exception {
        StringBuilder msg = new StringBuilder();
        while (msg.length() < 1) {
            while (din.ready()) {
                msg.append((char) din.read());
				//notifyClient("Receiving Message from Server");
            }
        }
		receivednone = true;
        String inputMsg = msg.toString();
        return inputMsg;
    }

	/*Function which performs the algorithm to find the largest
	 * server. 
	 * The largest server is determined by the core count. Therefore
	 * this function compares the second value of every server entry 
	 * which contains the core count and stores the index of the largest 
	 * server.
	 *
	 * returns none
	 * */
	public static void allToLargest(int[][] servers, String[] types){
		int max = servers[0][1]; // comparing it with cores
		int index = 0;
		int limit =0;
		//notifyClient("Performing All_To_Largest Algorithm \n");
		for(int i = 0; i<servers.length;i++){
			if(servers[i][1] > max){
				max = servers[i][1];
				index = i;
				limit = servers[i][0];
			}
		}
		/*
		notifyClient("***** Largest Server Details *****");
		notifyClient("Server Index : "+ index);
		notifyClient("Cores : "+servers[index][1]);
		notifyClient("Memory :"+ servers[index][2]);
		notifyClient("Disk Space :"+ servers[index][3]);
		notifyClient("***Running All_To_Largest Algorithm is completed****");
		*/
		largestServerIndex = index;	
	}

	/* Function to perform the GETS AVAIL command with the job information
	 * returns a string 
	 * */
	public static String getsInform(String[] jobs, BufferedReader din){
		String sendjobs = "";
		try{
			if(jobs.length > 6 || jobs.length == 6) {
			String commandType = jobs[0]; // JOBN , JOBP
			String sbtTime = jobs[1];
			String jobID = jobs[2];
			String estRunTime = jobs[3];
			String cores = jobs[4];
			String memory = jobs[5];
			String disk = jobs[6];
			sendjobs = GETS+cores+gap+memory+gap+disk+"\n";
			dout.flush();
			din =  new BufferedReader(new InputStreamReader(s.getInputStream()));
			}
			else {
				return "insufficent arguments";
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return sendjobs;
	}
	

	/* Function to schedule a Job to the largest server, by send the SCHD command
	 * returns a string
	 * */
	public static String scheduleJobs(int index, String[] types, String[] jobs){
		String schedule = "";
		int serverLimitValue = 0;
		String limit_value = serverLimitValue + "";
		try{
			String jobID = jobs[2]; //jobs[2] contains the index of the Job
			//if(serverLimitValue < servers[index][0]){
			schedule = SCHD+ jobID+ gap+ types[index] + gap+ limit_value;
			//notifyClient("Scheduling Job "+jobID + " Now");
			//}
			//else{
			//	return "Assigning Job to the next Largest server"; 
			//} 
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return schedule;	
	}

        /* Function to send a message to the server
	 * returns a void
	 * */
	public static void messageToServer(String message) throws IOException {
		try{
			dout.write(message.getBytes());
			dout.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/* Main Function 
	 * Simulates the Job scheduling algorithm
	 * */
	public static void main(String[] args){
		try{
			// Connect to the server
		 	Dsclient client = new Dsclient("localhost", 50000);
			String msg = ""; // message from server
			boolean firstJobAllocation = true;
			boolean authSuccessful = performAuth();
			String[] resourceInfo = null;
			String[] Data = null;
			if(authSuccessful){
				servers = readXML();
				// Sending a notification to server if READY
				allToLargest(servers,servertypes);

				while (receivednone){
					if(firstJobAllocation){
						getMsg(); // SENT OK
						messageToServer(REDY);
					}

					firstJobAllocation = false;
					String msgTracker = "";
					String[] jobs;
					String[] inputFromServer = getMsg().split(" ");
					String check = inputFromServer[0]; //JOBN or JCPL or OK
				    String temp;
					//notifyClient("RCVD from Server => "+ check);
					switch(check){
						case "JCPL"+"\n":
						case "JCPL": 
							messageToServer(REDY);
							firstJobAllocation = false;
							break;
						
						case "JOBP"+"\n":
						case "JOBP":
						//Not used in the current version of the implementation
						//Trickles down into JOBN
						
						case "JOBN"+"\n":
						case "JOBN":
							String sendjobs = getsInform(inputFromServer,din);
							messageToServer(sendjobs); // GETS Avail
							Data = getMsg().split(" "); // Data 
							messageToServer(OK); // send OK
							resourceInfo = getMsg().split(" "); // get resource info
							messageToServer(OK);
							temp = getMsg();
							if(temp.equals("."+"\n") || temp.equals(".")){
								String sendMessage = scheduleJobs(largestServerIndex, servertypes,inputFromServer);
								messageToServer(sendMessage);
							}
				            temp = getMsg();
							if(temp.equals(OK)||temp.equals("OK")){
								messageToServer(REDY);
							}
					
							break;

						case "OK":
						case "OK"+"\n":
							messageToServer(REDY);
							break;

				        case "NONE":
						case "NONE"+"\n":
						    //notifyClient("RCVD from Server => NONE");
							notifyClient("-----TERMININATING PROGRAM------");
							messageToServer(QUIT);
							receivednone= false;
							break;					
					
						default:
						    //notifyClient("Inside default"); 
							msgTracker = getMsg();
							if(msgTracker.startsWith("JOBN") || msgTracker.startsWith("JOBN"+"\n")){
								receivednone = true;
							}
							break;	
					}
			
				}
			}
			dout.flush();		
			dout.close();
			din.close();	
		}catch(Exception e){
			System.out.println(e);
		}
	}


}
