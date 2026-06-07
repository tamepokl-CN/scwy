package tamepokl.scwy.qwp.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QwpInfo {
    public String name;
    public String id;
    public Set<QwpItem> items = new HashSet<QwpItem>();

    public QwpInfo(String name, String id) {
        this.name = name;
        this.id = id;
    }
    public void addItem(QwpItem item){
        items.add(item);
    }
}
