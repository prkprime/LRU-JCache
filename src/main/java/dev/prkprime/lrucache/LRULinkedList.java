package dev.prkprime.lrucache;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LRULinkedList<K, V> implements Serializable {
    @Serial
    private static final long serialVersionUID = -7419952370180484181L;
    @Getter
    transient long size;
    transient LRULinkedListNode<K, V> first;
    transient LRULinkedListNode<K, V> last;

    LRULinkedList() {
        size = 0;
    }

    /**
     * add new node at the start of the linked list
     *
     * @param newNode
     *            new node to be added
     */
    public void addFirst(LRULinkedListNode<K, V> newNode) {
        LRULinkedListNode<K, V> f = first; // keep ref to current head node
        first = newNode; // assign new node to first note (inserting at start of list)
        if (f == null) { // if list was empty (because we always insert from head, so head null means empty list)
            last = newNode; // since this is first and only element, this is also last element
        } else { // if list was not empty
            newNode.setNext(f); // then we need to point newNode->next to the f which was the previous first node
            f.setPrev(newNode); // also set newNode as previous node for 5 since doubly linked list
        }
        ++size; // increase size by one
    }

    /**
     * evict last element from the linked list
     *
     * @return key of the evicted node
     */
    public K evictLast() {
        if (last != null) { // if last is null, that means list is empty. nothing to evict, return
            LRULinkedListNode<K, V> f = last.getPrev(); // get 2nd last element
            K key = last.getKey(); // get key of node to be evicted
            last.setKey(null); // set key of last to null
            last.setValue(null); // set value of last to null
            last.setPrev(null); // set prev value of last to null
            last = f; // set prev node as last
            if (f == null) { // if prev node is null, that means first and last element were same
                first = null; // since first is still referencing to f, set first to null
            } else { // if prev node not null
                f.setNext(null); // set next link of prev to null
            }
            --size;
            return key;
        }
        return null;
    }

    /**
     * move any node from list to the first position
     *
     * @param node
     *            node to be promoted
     */
    public void promoteToFirst(LRULinkedListNode<K, V> node) {
        LRULinkedListNode<K, V> prev = node.getPrev(); // get prev node
        LRULinkedListNode<K, V> next = node.getNext(); // get next node
        if (prev == null) { // if prev node is null, i.e. current node is head, no need for promotion
            return;
        } else { // if prev is non-null
            LRULinkedListNode<K, V> ignored = deleteNode(node);
        }
        addFirst(node); // promote t first
    }

    public LRULinkedListNode<K, V> deleteNode(LRULinkedListNode<K, V> node) {
        LRULinkedListNode<K, V> prev = node.getPrev(); // get prev node
        LRULinkedListNode<K, V> next = node.getNext(); // get next node
        if (prev != null) { // if prev is non-null i.e. not first node
            prev.setNext(next); // set next to prev->next
        }
        if (next != null) { // if next is non-null i.e not last node
            next.setPrev(prev); // set prev next->prev
        }
        node.setPrev(null); // remove all linking of node to be promoted
        node.setNext(null);
        --size; // since add first is going to increase size, we decrease here when we remove node
        return node;
    }

    /**
     * get all nodes in the linked list from first to last
     *
     * @return list of all nodes
     */
    public List<LRULinkedListNode<K, V>> getAll() {
        return getAll(false);
    }

    /**
     * get all nodes in the linked list from last to first if reversed is true, otherwise from first to last
     *
     * @param reversed
     *            weather to get list in reverse order
     *
     * @return list of all nodes
     */
    public List<LRULinkedListNode<K, V>> getAll(boolean reversed) {
        List<LRULinkedListNode<K, V>> list = new ArrayList<>();
        LRULinkedListNode<K, V> curr = reversed ? last : first;
        if (curr != null) {
            do {
                list.add(curr);
                curr = reversed ? curr.getPrev() : curr.getNext();
            } while (curr != null);
        }
        return list;
    }

    /**
     * @param index
     *            index of the node to be fetched (index 0 starts from first/head node)
     *
     * @return node at the given index
     *
     * @throws IndexOutOfBoundsException
     *             index out of bounds
     */
    public LRULinkedListNode<K, V> getByIndex(long index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + size);
        }
        LRULinkedListNode<K, V> node = first;
        for (int i = 0; i < index; i++) {
            node = node.getNext();
        }
        return node;
    }

    public void clear() {
        getAll().forEach(node -> {
            node.setPrev(null);
            node.setNext(null);
            node.setKey(null);
            node.setValue(null);
        });
        first = null;
        last = null;
        size = 0;
    }

    @Override
    public String toString() {
        return getAll().toString();
    }
}
