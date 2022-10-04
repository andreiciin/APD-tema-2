import java.util.List;
import java.util.Map;

public class ReduceThread extends Thread {

    List<TaskReduce> w_tasks; // worker's tasks

    public ReduceThread(List<TaskReduce> w_tasks) {
        this.w_tasks = w_tasks;
    }

    @Override
    public void run() {

        for (int i = 0; i < this.w_tasks.size(); i++) {
            synchronized (ReduceThread.class) {

                int nr_cuv = 0;
                float sum = 0;
                float rang = 0;
                float fibbo[] = new float[]{0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181,
                        6765, 10946, 17711, 28657, 46368, 75025, 121393, 196418, 317811};

                Map<Integer, Integer> auxMap = this.w_tasks.get(i).aparMap; // extrag dictionarul partial creat la Map
                for (Integer key : auxMap.keySet()) {
                    // pe baza dictionarului partial
                    // calculez numarul de cuvinte, suma cu formula bazata pe Fibonacci
                    if (key != 0) {
                        nr_cuv += (float) auxMap.get(key);
                        sum += fibbo[key + 1] * (float) auxMap.get(key);
                    }
                }
                // calculez rangul si salvez rangurile sortate
                rang = sum / (float) nr_cuv;
                Tema2.rangSort.add(rang);
                // combin datele din dictionarele partiale pentru a crea dictionarul rangurilor
                Tema2.rangs.put(rang, this.w_tasks.get(i).inName);
            }
        }
    }
}
