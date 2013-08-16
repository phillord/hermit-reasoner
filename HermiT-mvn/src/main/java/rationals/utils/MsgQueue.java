package rationals.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * @author bailly
 * @version $Id: MsgQueue.java 2 2006-08-24 14:41:48Z oqube $
 */
public class MsgQueue implements Serializable {

	/** private list for queueing */
	private LinkedList list = new LinkedList();

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
	public Object dequeue() {
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
	public void addAll(Collection coll) {
		Iterator it = coll.iterator();
		while (it.hasNext()) {
			enqueue((Object) it.next());
		}
	}

	/**
	 * Enqueue message at end
	 *
	 * @param msg message to enqueue
	 */
	public void enqueue(Object msg) {
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
	public Object[] dequeueAll() {
		Object[] ary = new Object[0];
		synchronized (lock) {
			ary = (Object[]) list.toArray(ary);
			list.clear();
		}
		return ary;
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
