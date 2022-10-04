import java.util.*;

public class MapThread extends Thread {

    List<TaskMap> w_tasks; // worker's tasks

    public MapThread(List<TaskMap> w_tasks) {
        this.w_tasks = w_tasks;
    }

    boolean charValid(Character ch) { // pt. parsare verifica daca un caracter poate forma un cuvant
        return (ch != null) && (Character.isDigit(ch) || Character.isLetter(ch));
    }

    @Override
    public void run() {

        // fiecare thread intra si isi face taskurile
        for (int i = 0; i < this.w_tasks.size(); i++) {
            // cuvant mijloc
            // compar textul din in012 daca la sf continua cuvant => iau tot cuvantul
            // daca la inceput e jumate de cuvant => sterg cuvantul
            String buff = this.w_tasks.get(i).buffer;
            String data = Tema2.fileData.get(this.w_tasks.get(i).inName);
            // stergere primele litere daca e in mijloc
            if (this.w_tasks.get(i).offset > 0) {
                if (charValid(buff.charAt(0)) &&
                   charValid(data.charAt(this.w_tasks.get(i).offset - 1))) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(buff);
                        for (int j = 0; j < buff.length(); j++) {
                            if (charValid(buff.charAt(j))) {
                                sb.deleteCharAt(0);
                            } else break;
                        }
                        this.w_tasks.get(i).buffer = sb.toString();
                }
            }
            // adaugare la ultimele litere ale cuvantului
            int pos = this.w_tasks.get(i).offset + this.w_tasks.get(i).dim;
            if (charValid(buff.charAt(buff.length()-1))) {
                if (pos < data.length()) { // sa nu fie mai mare ca tot textul din fisier
                    if (charValid(data.charAt(pos))) {

                        int k = 0;
                        while (pos+k < data.length() && charValid(data.charAt(pos + k))) {
                            this.w_tasks.get(i).buffer += data.charAt(pos + k);
                            k++;
                        }
                    }
                }
            }

            // dictionare partiale in care salvam pt. secventa curenta lungimile cuvintelor si aparitiile
            Map<Integer, List<String>> cuvLen = new HashMap<>();
            Map<Integer, Integer> cuvAp = new HashMap<>();
            Integer maxL = -1;

            // scoatem cuvintele din string
            String s = this.w_tasks.get(i).buffer;

            List<String> words1 = new ArrayList<>();
            String delim = "[\\;\\:\\?\\~\\.\\,\\>\\<\\`\\[\\]\\{\\}\\(\\)\\!\\@\\#\\$\\%\\^\\&\\-\\_\\+\\'\\=\\*\\\\\"\\|\\ \\\t\\\r\\\n]+";
            StringTokenizer tokenizer = new StringTokenizer(s, delim);
            while (tokenizer.hasMoreTokens()) {
                words1.add(tokenizer.nextToken());
            }

            synchronized (MapThread.class) {
                for (int j = 0; j < words1.size(); j++) {

                    if (maxL < words1.get(j).length()) maxL = words1.get(j).length();
                    // creez map partial cu lenght + aparitii de cate ori
                    var aux = cuvAp.get(words1.get(j).length());
                    if (aux != null) {
                        var val = cuvAp.get(words1.get(j).length());
                        val++;
                        cuvAp.put(words1.get(j).length(), val);
                    } else {
                        cuvAp.put(words1.get(j).length(), 1);
                    }

                    // creez map partial cu length + toate cuv cu acel length
                    var aux1 = cuvLen.get(words1.get(j).length());
                    if (aux1 != null) {
                        aux1.add(words1.get(j));
                        cuvLen.put(words1.get(j).length(), aux1);
                    } else {
                        List<String> newList = new ArrayList<>();
                        newList.add(words1.get(j));
                        cuvLen.put(words1.get(j).length(), newList);
                    }
                }
            }

            // map pentru cuvintele maxime
            if (maxL > 0) {
                List<String> outList = Tema2.wordMap.get(this.w_tasks.get(i).inName);
                outList.addAll(cuvLen.get(maxL));
                Tema2.wordMap.put(this.w_tasks.get(i).inName, outList);

                var lungime = Tema2.maxMap.get(this.w_tasks.get(i).inName);

                if (maxL > lungime) {
                    Tema2.maxMap.put(this.w_tasks.get(i).inName, maxL);
                }
            }
            // map pt nr de aparitii al cuvintelor
            for (Integer key : cuvAp.keySet()) {
                Map<Integer, Integer> auxMap = Tema2.aparMap.get(this.w_tasks.get(i).inName);
                var aux = cuvAp.get(key);
                var val = auxMap.get(key);
                if (val != null) {
                    val += aux;
                    auxMap.put(key, val);
                } else {
                    auxMap.put(key, aux);
                }
            }
            // am terminat prelucrarille
        }
        // inafara forului pt taskuri
    }
}
