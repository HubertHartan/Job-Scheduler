import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;



public class Dsclient {
	private String message = "";
	private DataOutputStream dout;
    private BufferedReader din;
    private Socket sock;
    private String address;
    private int port;
    String readMsg = "";
    public boolean receivedNone = false;

	//The main method where the necessary methods are called
	//to run the scheduler
	public static void main(String[] args) throws Exception{
		Dsclient dsclient = new Dsclient();
		dsclient.serverConnect();
		dsclient.performAuth();
		dsclient.jobScheduler();	
	}

	//Constructor that assigns the necessary IP address
	//and port number
	public Dsclient(){
		this.address = "localhost";
        this.port = 50000;
	}


	//Connects to the "server" by setting up the socket
	//and input reader and output stream
	public void serverConnect() throws Exception {
		sock = new Socket(address, port);
        din= new BufferedReader(new InputStreamReader(sock.getInputStream()));
        dout= new DataOutputStream(sock.getOutputStream());
    }

	//Sends a message to server
	public void messageToServer(String message) throws Exception{
        dout.write((message+"\n").getBytes());
		dout.flush();
    }

	//Get a message from the server and reads it
	public String getMsg() throws Exception {   
        readMsg = din.readLine();
        receivedNone = readMsg.equals("NONE");
        return readMsg;
    }

	//Creates a list of servers 
	public ArrayList<Server> getCapableServers(int numServers) throws Exception{
        ArrayList<Server> serverList = new ArrayList<Server>();
        try {
            for (int i = 0; i < numServers; i++) {
            	readMsg = getMsg();
                String[] splitMsg = readMsg.split(" ");

				Server newServer = parseToServer(splitMsg); //calls the parsing method, see below

                serverList.add(newServer);
                receivedNone = readMsg.equals("NONE");
            }
            return serverList;

        } catch (IOException exception) {
            System.out.println("ERROR");
            return null;
        }
    }



	//Separates the server information and parses it into a Server object
	public Server parseToServer(String [] arrayStrings){
		return new Server(
			arrayStrings[0],					//The server type
			Integer.parseInt(arrayStrings[1]),  //The server id
			arrayStrings[2],					//The state the server is in
			Integer.parseInt(arrayStrings[3]),	//The boot up time for the server
			Integer.parseInt(arrayStrings[4]),	//The core count of the server
			Integer.parseInt(arrayStrings[5]),	//The server's memory
			Integer.parseInt(arrayStrings[6]),	//The server's disk
			Integer.parseInt(arrayStrings[7]),	//The estimated wait time for the server
			Integer.parseInt(arrayStrings[8])	//The estimated run time for the server
		);
	}

	//Performs the handshake protocol with the server
	public void performAuth() throws Exception{
		try{
			messageToServer("HELO");
			message = getMsg();
			messageToServer("AUTH " + System.getProperty("user.name"));
			message = getMsg();
		}catch (IOException e) {
            e.printStackTrace();
        }
	}

	//the actual job scheduler that runs everything
	//based on the string received from the server 
	public void jobScheduler() throws Exception{
		try{
			messageToServer("REDY");
			String[] arrayMessage = null;

			while(!receivedNone){

				message = getMsg();
				arrayMessage = message.split(" ");

				switch(arrayMessage[0]){
					case "JOBN": //empty so it trickles down to JOBP
					case "JOBP":
						Job job = new Job(arrayMessage);
						messageToServer("GETS Capable " + job.GET());

						message = getMsg();
                        int numServers = Integer.parseInt(message.split(" ")[1]);
                        messageToServer("OK");       
                        
                     
                        ArrayList<Server> serverList = getCapableServers(numServers);
                        messageToServer("OK");
                        message = getMsg();
                        
                     
                        messageToServer(newAlgo(serverList, job));
                        
                        break;
                    
                    case "JCPL":
                    case "OK":
						messageToServer("REDY");
                        break;
                    default:
                        break;

				}
			}
			//Quits the processes and sends a message to the server
			messageToServer("QUIT");
        	message = getMsg();
        
        	if (message.equals("QUIT")) {        
            	din.close();
        		dout.close();
        		sock.close();
        	}
		}catch (IOException e) {
            e.printStackTrace();
        }
	}


	//The new scheduling algorithm
	//Based on finding the minimum wait time of a server
	//and prioritising the jobs there
	public String newAlgo(ArrayList<Server> servers, Job job){
		Server minTime = servers.get(0);

	        for (int i = 0; i < servers.size(); i++) {
 	            if (minTime.getwait()> servers.get(i).getwait()) {                           
 	                minTime = servers.get(i); 
 	            }
 	        }
	        
 	    return "SCHD "+ job.getId()+" "+minTime.getType()+" "+minTime.getId(); 
	}

}
