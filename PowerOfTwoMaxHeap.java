import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * A max heap whose branching factor is always a power of two.
 *
 * The constructor parameter is the exponent used for the child count:
 * childCount = 2 ^ childPower. A childPower of 0 creates a unary heap, 1 creates
 * a binary heap, 2 creates a 4-ary heap, and so on.
 */
public final class PowerOfTwoMaxHeap {
    private static final int DEFAULT_CAPACITY = 16;
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private final int childPower;
    private final int childCount;
    private int[] elements;
    private int size;

    public PowerOfTwoMaxHeap(int childPower) {
        this(childPower, DEFAULT_CAPACITY);
    }

    public PowerOfTwoMaxHeap(int childPower, int initialCapacity) {
        if (childPower < 0 || childPower > 30) {
            throw new IllegalArgumentException("childPower must be between 0 and 30");
        }
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity must not be negative");
        }

        this.childPower = childPower;
        this.childCount = 1 << childPower;
        this.elements = new int[initialCapacity];
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int childPower() {
        return childPower;
    }

    public int childCount() {
        return childCount;
    }

    public void insert(int value) {
        ensureCapacity(size + 1);

        int index = size++;
        while (index > 0) {
            int parentIndex = parentIndex(index);
            int parentValue = elements[parentIndex];
            if (parentValue >= value) {
                break;
            }

            elements[index] = parentValue;
            index = parentIndex;
        }
        elements[index] = value;
    }

    public int peekMax() {
        if (size == 0) {
            throw new NoSuchElementException("heap is empty");
        }
        return elements[0];
    }

    public int popMax() {
        if (size == 0) {
            throw new NoSuchElementException("heap is empty");
        }

        int max = elements[0];
        int lastValue = elements[--size];
        if (size > 0) {
            siftDown(0, lastValue);
        }
        return max;
    }

    private void siftDown(int index, int value) {
        int lastParentIndex = parentIndex(size - 1);

        while (index <= lastParentIndex) {
            int firstChildIndex = (index << childPower) + 1;
            int childLimit = childLimit(firstChildIndex);

            int bestChildIndex = firstChildIndex;
            int bestChildValue = elements[firstChildIndex];
            for (int childIndex = firstChildIndex + 1; childIndex < childLimit; childIndex++) {
                int childValue = elements[childIndex];
                if (childValue > bestChildValue) {
                    bestChildValue = childValue;
                    bestChildIndex = childIndex;
                }
            }

            if (bestChildValue <= value) {
                break;
            }

            elements[index] = bestChildValue;
            index = bestChildIndex;
        }

        elements[index] = value;
    }

    private int childLimit(int firstChildIndex) {
        int remainingElements = size - firstChildIndex;
        if (childCount >= remainingElements) {
            return size;
        }
        return firstChildIndex + childCount;
    }

    private int parentIndex(int childIndex) {
        return (childIndex - 1) >>> childPower;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= elements.length) {
            return;
        }

        int oldCapacity = elements.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1) + 1;
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }
        if (newCapacity > MAX_ARRAY_SIZE) {
            newCapacity = hugeCapacity(minCapacity);
        }
        elements = Arrays.copyOf(elements, newCapacity);
    }

    private int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError("heap size overflow");
        }
        return minCapacity > MAX_ARRAY_SIZE ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }
}
