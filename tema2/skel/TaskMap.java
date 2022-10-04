// clasa in care retin datele taskurilor ce vor fi atribuite workerilor(threadurilor) pt Map
public class TaskMap {

    String inName;
    int offset;
    int dim;
    String buffer;

    public TaskMap(String inName, int offset, int dim, String buffer) {
        this.inName = inName;
        this.offset = offset;
        this.dim = dim;
        this.buffer = buffer;
    }
}
