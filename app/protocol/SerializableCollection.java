package protocol;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SerializableCollection <T> extends ConnectionProtocol.SerializableJsonObject implements Iterable<T> {

    private List<T> items = new ArrayList<>();

    @JsonCreator
    public SerializableCollection(@JsonProperty("items") List<T> items) {
        this.items = items;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public SerializableCollection add(T item) {
        items.add(item);
        return this;
    }

    public void addAll(Collection<T> list) {
        this.items.addAll(list);
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }

    @Override
    public String toString() {
        return "SerializableCollection{" +
                "items=" + items +
                '}';
    }
}

