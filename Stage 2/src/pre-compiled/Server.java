public class Server {

	public int id;
	public String type;
	public int limit;
	public int bootupTime;
	public float rate;
	public int coreCount;
	public int memory;
	public int disk;
	public String state;
	public int waitTime;
	public int runTime;
    
    //Constructor
	Server(String type, int id, String state, int bootupTime, int coreCount, int memory, int disk,int waitTime, int runTime) {
		this.type = type;
		this.id = id;
		this.state = state;
		this.coreCount = coreCount;
		this.memory = memory;
		this.disk = disk;
		this.waitTime = waitTime;
		this.runTime = runTime;
	}
	
    //Get methods to return appropriate fields
	public int getId() {
        return id;
    }
    public String getType() {
        return type;
    }
    public String getState() {
        return state;
    }
    public int getbootupTime() {
        return bootupTime;
    }
    public int getCoreCount() {
        return coreCount;
    }
    public int getMemory() {
        return memory;
    }
    public int getDisk() {
        return disk;
    }
    public int getwait() {
        return waitTime;
    }
	
	
}