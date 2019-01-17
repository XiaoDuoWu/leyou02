import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test {


    public static void main(String[] args) {

        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(1);
        list.add(3);
        for (int i = 0; i < list.size(); i++) {
            int count = 0;
            for (int i1 = 0; i1 < list.size(); i1++) {
                if (list.get(i) == list.get(i1)) {
                    count++;
                }
            }
            if (count > 1) {
                list.remove(i);
            }
        }
        System.out.println(list);
    }
}
