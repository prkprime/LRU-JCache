package dev.prkprime.lrucache;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class LinkedListInternalTest {

    @Test
    public void newListTest() {
        LRULinkedList<String, String> linkedList = new LRULinkedList<>();
        assert true;
    }

    @Test
    public void addFirstTest() {
        LRULinkedList<String, String> linkedList = new LRULinkedList<>();
        int size = 5;
        List<LRULinkedListNode<String, String>> nodes = getNodes(size);
        nodes.forEach(linkedList::addFirst);
        assert linkedList.size == size;
    }

    @Test
    public void getAllTest() {
        LRULinkedList<String, String> linkedList = new LRULinkedList<>();
        int size = 5;
        List<LRULinkedListNode<String, String>> nodes = getNodes(size);
        nodes.forEach(linkedList::addFirst);
        assert linkedList.getAll().equals(nodes.reversed());
        assert linkedList.getAll(true).equals(nodes);
    }

    @Test
    public void getByIndexTest() {
        LRULinkedList<String, String> linkedList = new LRULinkedList<>();
        int size = 5;
        List<LRULinkedListNode<String, String>> nodes = getNodes(size);
        nodes.forEach(linkedList::addFirst);
        nodes = nodes.reversed();
        checkAllEqual(size, linkedList, nodes);
    }

    @Test
    public void evictLastTest() {
        LRULinkedList<String, String> linkedList = new LRULinkedList<>();
        int size = 5;
        List<LRULinkedListNode<String, String>> nodes = getNodes(size);
        nodes.forEach(linkedList::addFirst);
        nodes = nodes.reversed();
        for (int i = 0; i < size; i++) {
            linkedList.evictLast();
            nodes.removeLast();
            checkAllEqual(nodes.size(), linkedList, nodes);
        }
    }

    @Test
    public void promoteToFirstTest() {
        LRULinkedList<String, String> linkedList = new LRULinkedList<>();
        int size = 5;
        List<LRULinkedListNode<String, String>> nodes = getNodes(size);
        nodes.forEach(linkedList::addFirst);
        nodes = nodes.reversed();
        System.out.println(linkedList);
        linkedList.promoteToFirst(linkedList.getByIndex(3));
        System.out.println(linkedList);
        assert linkedList.getByIndex(0).equals(nodes.get(3));
        assert linkedList.getByIndex(1).equals(nodes.get(0));
        assert linkedList.getByIndex(2).equals(nodes.get(1));
        assert linkedList.getByIndex(3).equals(nodes.get(2));
        assert linkedList.getByIndex(4).equals(nodes.get(4));

    }

    private static List<LRULinkedListNode<String, String>> getNodes(int size) {
        List<LRULinkedListNode<String, String>> linkedList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            linkedList.add(new LRULinkedListNode<>("key" + (i + 1), "value" + (i + 1)));
        }
        return linkedList;
    }

    private static void checkAllEqual(int size, LRULinkedList<String, String> linkedList,
            List<LRULinkedListNode<String, String>> nodes) {
        for (int i = 0; i < size; i++) {
            assert linkedList.getByIndex(i).equals(nodes.get(i));
        }
    }
}