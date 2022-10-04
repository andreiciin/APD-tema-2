// clasa in care retin datele taskurilor ce vor fi atribuite workerilor(threadurilor) pt Reduce
import java.util.List;
import java.util.Map;

public class TaskReduce {

    String inName;
    List<String> topWords;
    Map<Integer, Integer> aparMap;

    public TaskReduce(String inName, List<String> topWords, Map<Integer, Integer> aparMap) {
        this.inName = inName;
        this.topWords = topWords;
        this.aparMap = aparMap;
    }
}
