
public class Job {
    String type;
    int subTime;
    int id;
    int runTime;
    int coreCount;
    int memory;
    int disk;

    //Constructor that uses parsing to input job data into the job class
    public Job(String[] jobData) {
        this(
        		jobData[0],
                Integer.parseInt(jobData[1]),
                Integer.parseInt(jobData[2]),
                Integer.parseInt(jobData[3]),
                Integer.parseInt(jobData[4]),
                Integer.parseInt(jobData[5]),
                Integer.parseInt(jobData[6])
        );

    }

    //Normal constructor for the job class
    public Job(String type, int subTime, int id, int runTime, int coreCount, int memory, int disk) {
        this.type = type; 
        this.subTime = subTime;
        this.id = id;
        this.runTime = runTime;
        this.coreCount = coreCount;
        this.memory = memory;
        this.disk = disk;
    }

    //Get methods to return appropriate fields
    public int getSubTime() {
        return subTime;
    }

    public int getId() {
        return id;
    }

    public int getrunTime() {
        return runTime;
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

    public String GET() {
        return coreCount + " " + memory + " " + disk;
    }
}