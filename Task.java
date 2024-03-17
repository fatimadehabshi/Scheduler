public class Task {
    String name, type, state;
    int duration;
    int[] requirements = new int[2];
    int waitingStartTime;
    MainThread.Processor processor;

    public Task(String name, String type, int duration){
        this.name = name;
        this.type = type;
        this.duration = duration;
        this.state = "Ready";
        this.processor = null;
        switch (type) {
            case "X":
                this.requirements[0] = 0;
                this.requirements[1] = 1;
                break;
            case "Y":
                this.requirements[0] = 1;
                this.requirements[1] = 2;
                break;
            case "Z":
                this.requirements[0] = 0;
                this.requirements[1] = 2;
                break;
        }
    }
}