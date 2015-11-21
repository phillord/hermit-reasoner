package rationals.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * @param <T> type
 * @author bailly
 * @version $Id: MsgQueue.java 2 2006-08-24 14:41:48Z oqube $
 */
public class MsgQueue<T> implements Serializable {

    /** private list for queueing */
    private LinkedList<T> list = new LinkedList<>();

    /** counter of enqueued objects */
    private int count = 0;

    /** lock object */
    private transient Object lock = new Object();

    /////////////////////////////////////////////////
    // CONSTRUCTOR
    //////////////////////////////////////////////////

    //////////////////////////////////////////////:
    // PUBLIC METHODS
    ///////////////////////////////////////////////

    /**
     * Dequeu first message
     * 
     * this method is blocking
     *
     * @return first message in list or null
     */
    public T dequeue() {
        try {
            synchronized (lock) {
                return list.removeFirst();
            }
        } catch (NoSuchElementException nsex) {
            return null;
        }

    }

    /**
     * Adds all elements of collection in order of 
     * iterator for collection
     *
     * @param col a Collection
     */
    public void addAll(Collection<T> coll) {
        Iterator<T> it = coll.iterator();
        while (it.hasNext()) {
            enqueue(it.next());
        }
    }

    /**
     * Enqueue message at end
     *
     * @param msg message to enqueue
     */
    public void enqueue(T msg) {
        synchronized (lock) {
            list.addLast(msg);
        }
        count++;
    }

    /**
     * Get all messages in the list as an array of messages
     *
     * @return an array of Object objects or null
     */
    public T[] dequeueAll(T[] ary) {
        synchronized (lock) {
            ary = list.toArray(ary);
            list.clear();
        return ary;
        }
    }

    /**
     * get number of messages in queue
     *
     * @return number of messages
     */
    public int getSize() {
        synchronized(lock) {
        return list.size();
        }
    }

    /**
     * Get number of messages which have been enqueued
     * @return total number of messages of queue
     */
    public int getCount() {
        return count;
    }

    /**
     * removes all messages m queue
     */
    public void flush() {
        synchronized (lock) {
            list.clear();
        }
    }

    /**
     * ReadObject implementation 
     */
    private void readObject(java.io.ObjectInputStream stream)
        throws java.io.IOException, ClassNotFoundException {
        try {
            // first, call default serialization mechanism
            stream.defaultReadObject();
            lock = new Object();
        } catch (java.io.NotActiveException ex) {
        }
    }

}
