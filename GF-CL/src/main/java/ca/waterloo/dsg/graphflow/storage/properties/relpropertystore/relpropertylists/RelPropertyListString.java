package ca.waterloo.dsg.graphflow.storage.properties.relpropertystore.relpropertylists;

import ca.waterloo.dsg.graphflow.storage.BucketOffsetManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class RelPropertyListString extends RelPropertyList {

    byte[][][][] buckets;

    private RelPropertyListString(int label, int srcVertexType, byte[][][][] buckets) {
        super(label, srcVertexType);
        this.buckets = buckets;
    }

    public RelPropertyListString(int[][] numElementsInEachBucket, int label, int srcVertexType) {
        super(label, srcVertexType);
        buckets = new byte[numElementsInEachBucket.length][][][];
        for (var bucketSlotID = 0; bucketSlotID < numElementsInEachBucket.length; bucketSlotID++) {
            var bucketSlot = numElementsInEachBucket[bucketSlotID];
            buckets[bucketSlotID] = new byte[bucketSlot.length][][];
            for (var bucketSlotOffset = 0; bucketSlotOffset < bucketSlot.length; bucketSlotOffset++) {
                buckets[bucketSlotID][bucketSlotOffset] = new byte[bucketSlot[bucketSlotOffset]][];
            }
        }
    }

    public String getProperty(long nodeOffset, int bucketOffset) {
        var bucketID = BucketOffsetManager.getBucketID(nodeOffset);
        var bucketSlotID = BucketOffsetManager.getBucketSlotID(bucketID);
        var bucketSlotOffset = BucketOffsetManager.getBucketSlotOffset(bucketID);
        return (bucketSlotID >= buckets.length || bucketSlotOffset >= buckets[bucketSlotID].length ||
            bucketOffset >= buckets[bucketSlotID][bucketSlotOffset].length ||
            buckets[bucketSlotID][bucketSlotOffset][bucketOffset] == null) ? null :
            new String(buckets[bucketSlotID][bucketSlotOffset][bucketOffset]);
    }

    public void setProperty(long nodeOffset, int bucketOffset, byte[] value) {
        var bucketID = BucketOffsetManager.getBucketID(nodeOffset);
        var bucketSlotID = BucketOffsetManager.getBucketSlotID(bucketID);
        var bucketSlotOffset = BucketOffsetManager.getBucketSlotOffset(bucketID);
        ensureBucketCapacity(bucketSlotID, bucketSlotOffset, bucketOffset);
        buckets[bucketSlotID][bucketSlotOffset][bucketOffset] = value;
    }

    public void ensureBucketCapacity(int bucketSlotID, int bucketSlotOffset, int bucketOffset) {
        if (bucketSlotID >= buckets.length) {
            var newArray = new byte[bucketSlotID + 1][][][];
            System.arraycopy(buckets, 0, newArray, 0, buckets.length);
            buckets = newArray;
        }
        if (null == buckets[bucketSlotID] || bucketSlotOffset >= buckets[bucketSlotID].length) {
            var newArray = new byte[bucketSlotOffset + 1][][];
            if (null != buckets[bucketSlotID]) {
                System.arraycopy(buckets[bucketSlotID], 0, newArray, 0, buckets[bucketSlotID].length);
            }
            buckets[bucketSlotID] = newArray;
        }
        var bucket = buckets[bucketSlotID][bucketSlotOffset];
        if (null == bucket || bucketOffset >= bucket.length) {
            var oldLength = null == bucket ? 0 : bucket.length;
            var newLength = ((bucketOffset / BucketOffsetManager.BUCKET_SIZE) + 1)
                * BucketOffsetManager.BUCKET_SIZE;
            var newBucket = new byte[newLength][];
            if (null != bucket) {
                System.arraycopy(bucket, 0, newBucket, 0, oldLength);
            }
            buckets[bucketSlotID][bucketSlotOffset] = newBucket;
        }
    }

    public void serialize(String directory) throws IOException {
        var outputStream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
            directory)));
        outputStream.writeInt(relLabel);
        outputStream.writeInt(nodeType);
        outputStream.writeObject(buckets);
        outputStream.close();
    }

    public static RelPropertyListString deserialize(String directory) throws IOException,
        ClassNotFoundException {
        var inputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(
            directory)));
        return new RelPropertyListString(inputStream.readInt() /*edgeLabel*/,
            inputStream.readInt() /*srcVertexType*/,
            (byte[][][][]) inputStream.readObject() /*buckets*/);
    }
}
