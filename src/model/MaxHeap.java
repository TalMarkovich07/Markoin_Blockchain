package model;

import java.util.ArrayList;

public class MaxHeap<T extends Comparable<T>> {
    private ArrayList<T> heap;

    public MaxHeap() {
        this.heap = new ArrayList<>();
    }

    public void insert(T item) {
        heap.add(item);
        int current = heap.size() - 1;

        while (current > 0) {
            int parent = (current - 1) / 2;
            if (heap.get(current).compareTo(heap.get(parent)) <= 0) break;
            swap(current, parent);
            current = parent;
        }
    }

    public T extractMax() {
        if (isEmpty()) return null;

        T max = heap.get(0);
        T lastItem = heap.remove(heap.size() - 1);

        if (!isEmpty()) {
            heap.set(0, lastItem);
            heapify(0);
        }

        return max;
    }

    private void heapify(int index) {
        int largest = index;
        int left = 2 * index + 1;
        int right = 2 * index + 2;

        if (left < heap.size() && heap.get(left).compareTo(heap.get(largest)) > 0) {
            largest = left;
        }

        if (right < heap.size() && heap.get(right).compareTo(heap.get(largest)) > 0) {
            largest = right;
        }

        if (largest != index) {
            swap(index, largest);
            heapify(largest);
        }
    }

    private void swap(int i, int j) {
        T temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public int size() {
        return heap.size();
    }
}