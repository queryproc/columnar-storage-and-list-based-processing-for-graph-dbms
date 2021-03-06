package ca.waterloo.dsg.graphflow.storage.graph.adjlistindex.defaultadjlistindex.defaultadjlist;

import ca.waterloo.dsg.graphflow.storage.graph.adjlistindex.defaultadjlistindex.iterators.DefaultAdjListSlice;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public abstract class DefaultAdjListIndex implements Serializable {

    public abstract void fillAdjList(DefaultAdjListSlice slice, long vertexOffset);

    protected abstract int getNumGroups();

    @SuppressWarnings("rawtypes")
    public void serialize(String directory) throws IOException {
        var numGroups = getNumGroups();
        var partitionOutputStream = new ObjectOutputStream(new BufferedOutputStream(
            new FileOutputStream(directory)));
        partitionOutputStream.writeInt(numGroups);
        for (var i = 0; i < numGroups; i++) {
            serializeFurther(partitionOutputStream, i);
        }
        partitionOutputStream.close();
    }

    protected abstract void serializeFurther(ObjectOutputStream outputStream, int groupId)
        throws IOException;
}
