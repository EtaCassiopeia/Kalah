package util;

import java.util.List;

public class Util {
    public static  <T> boolean compareLists(List<T> list1, List<T> list2) {
        if (null == list1 || null == list2 || list1.size() != list2.size())
            return false;
        for (int i = 0; i < list1.size(); i++)
            if (list1.get(i) != list2.get(i))
                return false;
        return true;
    }
}
