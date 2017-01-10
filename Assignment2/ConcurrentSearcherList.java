package concurrent;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentSearcherList<T> {

	/*
     * Three kinds of threads share access to a singly-linked list:
	 * searchers, inserters and deleters. Searchers merely examine the list;
	 * hence they can execute concurrently with each other. Inserters add
	 * new items to the front of the list; insertions must be mutually exclusive
	 * to preclude two inserters from inserting new items at about
	 * the same time. However, one insert can proceed in parallel with
	 * any number of searches. Finally, deleters remove items from anywhere
	 * in the list. At most one deleter process can access the list at
	 * a time, and deletion must also be mutually exclusive with searches
	 * and insertions.
	 * 
	 * Make sure that there are no data races between concurrent inserters and searchers!
	 */

    /**
     * Invariants
     * <p>
     * There is exactly one (last) Node with a null next reference,
     * which is CASed when enqueueing.  This last Node can be
     * reached in O(1) time from tail, but tail is merely an
     * optimization - it can always be reached in O(N) time from
     * head as well.
     */


    Semaphore searchKey = new Semaphore(1);
    Semaphore insertKey = new Semaphore(1);

    Semaphore insertSem = new Semaphore(1);

    volatile AtomicInteger concurrenSearches = new AtomicInteger(0);
    volatile AtomicInteger concurrentInserts = new AtomicInteger(0);

    private static class Node<T> {
        final T item;
        Node<T> next;

        Node(T item, Node<T> next) {
            this.item = item;
            this.next = next;
        }
    }


    private Node<T> first;


    public ConcurrentSearcherList() {
        first = null;
    }

    /**
     * Inserts the given item into the list.
     * <p>
     * Precondition:  item != null
     *
     * @param item
     * @throws InterruptedException
     */
    public void insert(T item) throws InterruptedException {
        assert item != null : "Error in ConcurrentSearcherList insert:  Attempt to insert null";
        start_insert();
        try {
            first = new Node<T>(item, first);
        } finally {
            end_insert();
        }
    }

    /**
     * Determines whether or not the given item is in the list
     * <p>
     * Precondition:  item != null
     *
     * @param item
     * @return true if item is in the list, false otherwise.
     * @throws InterruptedException
     */
    public boolean search(T item) throws InterruptedException {
        assert item != null : "Error in ConcurrentSearcherList insert:  Attempt to search for null";
        start_search();
        try {
            for (Node<T> curr = first; curr != null; curr = curr.next) {
                if (item.equals(curr.item)) return true;
            }
            return false;
        } finally {
            end_search();
        }
    }

    /**
     * Removes the given item from the list if it exists.  Otherwise the list is not modified.
     * The return value indicates whether or not the item was removed.
     * <p>
     * Precondition:  item != null.
     *
     * @param item
     * @return whether or not item was removed from the list.
     * @throws InterruptedException
     */
    public boolean remove(T item) throws InterruptedException {
        assert item != null : "Error in ConcurrentSearcherList insert:  Attempt to remove null";
        start_remove();
        try {
            if (first == null) return false;
            if (item.equals(first.item)) {
                first = first.next;
                return true;
            }
            for (Node<T> curr = first; curr.next != null; curr = curr.next) {
                if (item.equals(curr.next.item)) {
                    curr.next = curr.next.next;
                    return true;
                }
            }
            return false;
        } finally {
            end_remove();
        }
    }

    private void start_insert() throws InterruptedException {
        if (concurrentInserts.incrementAndGet() == 1) {
            insertKey.acquire();
        }
        insertSem.acquire();
    }

    private void end_insert() {
        if (concurrentInserts.decrementAndGet() == 0) {
            insertKey.release();
        }
        insertSem.release();
    }

    private void start_search() throws InterruptedException {
        if (concurrenSearches.incrementAndGet() == 1) {
            searchKey.acquire();
        }
    }

    private void end_search() {
        if (concurrenSearches.decrementAndGet() == 0) {
            searchKey.release();
        }
    }

    private void start_remove() throws InterruptedException {
        insertKey.acquire();
        searchKey.acquire();
    }

    private void end_remove() {
        insertKey.release();
        searchKey.release();
    }
}
