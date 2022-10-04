import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Tema2 {

    public static int N = 0; // nr_documente
    public static int dimFrag = 0; // dimensiune fragment
    public static ArrayList<String> docName = new ArrayList<>(); // sirul numelor documentelor din <in_file>
    public static Map<String, String> fileData = new HashMap<>(); // textul din documentele de mai sus

    public static List<TaskMap> tasksMap = new ArrayList<>(); // map pt. crearea taskurilor
    public static List<TaskReduce> tasksReduce = new ArrayList<>();
    public static ConcurrentMap<String, List<String>> wordMap = new ConcurrentHashMap<>(); // cuvintele de lungime max
    public static ConcurrentMap<String, Map<Integer, Integer>> aparMap = new ConcurrentHashMap<>(); // nr. aparitii cuvant
    public static ConcurrentMap<String, Integer> maxMap = new ConcurrentHashMap<>(); // maximul pt. fiec. fisier
    public static ConcurrentMap<Float, String> rangs = new ConcurrentHashMap<>(); // rangul fiec fisier
    public static SortedSet<Float> rangSort = new TreeSet<>(); // lista creata pentru a afisarea documentelor sortate

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: Tema2 <workers> <in_file> <out_file>");
            return;
        }

        // citire in_file: dimFragment, nThreads, documentsNames
        int k_citire = 0;
        try {
            File myFile = new File(args[1]);
            Scanner myReader = new Scanner(myFile);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();

                if (k_citire == 0) {
                    dimFrag = Integer.parseInt(data);
                }
                if (k_citire == 1) {
                    N = Integer.parseInt(data);
                }
                if (k_citire > 1) {
                    docName.add(data);
                }
                k_citire++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("A READ error occurred.");
            e.printStackTrace();
        }

        // citesc bufferul din documentele: documentsNames
        for (int i = 0; i < docName.size(); i++) {
            wordMap.put(docName.get(i), new ArrayList<>()); // pt fiec. document salvez cuvintele de lungime maxima
            aparMap.put(docName.get(i), new HashMap<>()); // pt fiec. document salvez nr. aparitii al cuvintelor
            maxMap.put(docName.get(i), -1); // pt. fiec document salvez lungimea maxima a cuvintelor

            // citesc din fiecare document si creez taskurile de tip Map
            int offset = 0, dim = dimFrag, currDim, start = 0, end = dimFrag;
            String data;
            try {
                File fileOne = new File(docName.get(i));
                Scanner readerOne = new Scanner(fileOne);

                readerOne.useDelimiter("\\Z");
                data = readerOne.next();

                fileData.put(docName.get(i), data);
                currDim = data.length();
                // creez Taskurile pt Map
                while (currDim > dimFrag) {
                    String buffer = data.substring(start, end);
                    tasksMap.add(new TaskMap(docName.get(i), offset, dim, buffer));
                    offset += dim;
                    currDim -= dim;
                    start = end;
                    end += dimFrag;
                }
                if (currDim < dimFrag) {
                    end -=  (dimFrag - currDim);
                    String buffer = data.substring(start, end);
                    tasksMap.add(new TaskMap(docName.get(i), offset, currDim, buffer));
                }
                readerOne.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }

        // creez threaduri pt Map
        N = Integer.parseInt(args[0]); // N devine nr_threaduri
        Map <Integer, List<TaskMap>> wo_map = new HashMap<>();
        for (int i = 0; i < N; i++) {
            wo_map.put(i, new ArrayList<>());
        }
        // in workers_map salvez repartizarea taskurilor pt fiecare thread
        for (int i = 0; i < tasksMap.size(); i++) {
            List<TaskMap> l_aux = new ArrayList<>();
            l_aux.addAll(wo_map.get(i % N));
            l_aux.add(tasksMap.get(i));
            wo_map.put(i % N, l_aux);
        }

        // pornire threaduri Map
        Thread[] t = new Thread[N];
        for (int i = 0; i < N; ++i) {
            t[i] = new MapThread(wo_map.get(i));
            t[i].start();
        }
        for (int i = 0; i < N; ++i) {
            try {
                t[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // creez tasksReduce
        for (int i = 0; i < docName.size(); i++) {
            tasksReduce.add(new TaskReduce(docName.get(i), wordMap.get(docName.get(i)),
                    aparMap.get(docName.get(i))));
        }
        // in workers_reduce salvez repartizarea taskurilor pt fiecare thread
        Map <Integer, List<TaskReduce>> wo_reduce = new HashMap<>();
        for (int i = 0; i < N; i++) {
            wo_reduce.put(i, new ArrayList<>());
        }
        for (int i = 0; i < tasksReduce.size(); i++) {
            List<TaskReduce> l_aux = new ArrayList<>();
            l_aux.addAll(wo_reduce.get(i % N));
            l_aux.add(tasksReduce.get(i));
            wo_reduce.put(i % N, l_aux);
        }

        // pornire threaduri Reduce
        for (int i = 0; i < N; ++i) {
            t[i] = new ReduceThread(wo_reduce.get(i));
            t[i].start();
        }
        for (int i = 0; i < N; ++i) {
            try {
                t[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // afisare
        File outFile = new File(args[2]);
        // sir cu rangurile sortate
        Object[] rangArr = rangSort.toArray();
        try {
            FileWriter myWriter = new FileWriter(outFile);
            // parcurgem rangurile sortate in ordine descrescatoare
            for (int i = rangArr.length-1; i >=0 ; i--) {
                String out = rangs.get(rangArr[i]).substring(12);
                int len = maxMap.get(rangs.get(rangArr[i]));
                String str = String.format("%.2f", rangArr[i]);
                myWriter.write(out + "," + String.format("%.2f", rangArr[i]) + "," +
                        len + "," +
                        aparMap.get(rangs.get(rangArr[i])).get(len) + "\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("A WRITEFILE error occurred.");
            e.printStackTrace();
        }
    }
}
