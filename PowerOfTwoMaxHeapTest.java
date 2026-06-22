import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Random;

public final class PowerOfTwoMaxHeapTest {
    public static void main(String[] args) {
        testConstructorValidation();
        testEmptyHeap();
        testSingleElement();
        testDuplicatesAndNegativeValues();
        testAscendingAndDescendingInputs();
        testVerySmallChildPower();
        testVeryLargeChildPower();
        randomizedAgainstPriorityQueue();
        stressLargeHeap();
        System.out.println("All PowerOfTwoMaxHeap tests passed.");
    }

    private static void testConstructorValidation() {
        expectThrows(IllegalArgumentException.class, () -> new PowerOfTwoMaxHeap(-1));
        expectThrows(IllegalArgumentException.class, () -> new PowerOfTwoMaxHeap(31));
        expectThrows(IllegalArgumentException.class, () -> new PowerOfTwoMaxHeap(1, -1));

        PowerOfTwoMaxHeap heap = new PowerOfTwoMaxHeap(30, 0);
        assertEquals(1 << 30, heap.childCount(), "large child count");
        assertEquals(30, heap.childPower(), "child power");
    }

    private static void testEmptyHeap() {
        PowerOfTwoMaxHeap heap = new PowerOfTwoMaxHeap(1);
        assertTrue(heap.isEmpty(), "new heap should be empty");
        assertEquals(0, heap.size(), "new heap size");
        expectThrows(NoSuchElementException.class, heap::peekMax);
        expectThrows(NoSuchElementException.class, heap::popMax);
    }

    private static void testSingleElement() {
        PowerOfTwoMaxHeap heap = new PowerOfTwoMaxHeap(2, 0);
        heap.insert(42);
        assertEquals(1, heap.size(), "single element size");
        assertEquals(42, heap.peekMax(), "single element peek");
        assertEquals(42, heap.popMax(), "single element pop");
        assertTrue(heap.isEmpty(), "single element heap should be empty after pop");
    }

    private static void testDuplicatesAndNegativeValues() {
        PowerOfTwoMaxHeap heap = new PowerOfTwoMaxHeap(3);
        int[] input = {5, -1, 5, Integer.MIN_VALUE, 0, Integer.MAX_VALUE, -1, 5};
        for (int value : input) {
            heap.insert(value);
        }

        Arrays.sort(input);
        for (int i = input.length - 1; i >= 0; i--) {
            assertEquals(input[i], heap.popMax(), "duplicates and negatives");
        }
        assertTrue(heap.isEmpty(), "duplicates heap should be empty");
    }

    private static void testAscendingAndDescendingInputs() {
        for (int childPower = 0; childPower <= 10; childPower++) {
            PowerOfTwoMaxHeap ascendingHeap = new PowerOfTwoMaxHeap(childPower);
            for (int value = 0; value < 1_000; value++) {
                ascendingHeap.insert(value);
            }
            assertPopsDescending(ascendingHeap, 999, 0, "ascending input childPower=" + childPower);

            PowerOfTwoMaxHeap descendingHeap = new PowerOfTwoMaxHeap(childPower);
            for (int value = 999; value >= 0; value--) {
                descendingHeap.insert(value);
            }
            assertPopsDescending(descendingHeap, 999, 0, "descending input childPower=" + childPower);
        }
    }

    private static void testVerySmallChildPower() {
        PowerOfTwoMaxHeap heap = new PowerOfTwoMaxHeap(0);
        int[] input = {3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5};
        for (int value : input) {
            heap.insert(value);
        }

        int[] expected = {9, 6, 5, 5, 5, 4, 3, 3, 2, 1, 1};
        for (int value : expected) {
            assertEquals(value, heap.popMax(), "unary heap order");
        }
    }

    private static void testVeryLargeChildPower() {
        PowerOfTwoMaxHeap heap = new PowerOfTwoMaxHeap(30, 0);
        int[] input = {7, 11, -8, 25, 25, 0, 13, Integer.MIN_VALUE, Integer.MAX_VALUE};
        for (int value : input) {
            heap.insert(value);
        }

        int[] expected = input.clone();
        Arrays.sort(expected);
        for (int i = expected.length - 1; i >= 0; i--) {
            assertEquals(expected[i], heap.popMax(), "huge childPower order");
        }
    }

    private static void randomizedAgainstPriorityQueue() {
        Random random = new Random(0xC0FFEE);
        for (int childPower : new int[] {0, 1, 2, 5, 10, 20, 30}) {
            PowerOfTwoMaxHeap heap = new PowerOfTwoMaxHeap(childPower, 0);
            PriorityQueue<Integer> reference = new PriorityQueue<>((left, right) -> Integer.compare(right, left));

            for (int step = 0; step < 20_000; step++) {
                boolean shouldInsert = reference.isEmpty() || random.nextInt(100) < 65;
                if (shouldInsert) {
                    int value = random.nextInt();
                    heap.insert(value);
                    reference.add(value);
                    assertEquals(reference.peek(), heap.peekMax(), "random peek childPower=" + childPower);
                } else {
                    assertEquals(reference.remove(), heap.popMax(), "random pop childPower=" + childPower);
                }
                assertEquals(reference.size(), heap.size(), "random size childPower=" + childPower);
            }

            while (!reference.isEmpty()) {
                assertEquals(reference.remove(), heap.popMax(), "random drain childPower=" + childPower);
            }
            assertTrue(heap.isEmpty(), "random heap should be empty childPower=" + childPower);
        }
    }

    private static void stressLargeHeap() {
        PowerOfTwoMaxHeap heap = new PowerOfTwoMaxHeap(4, 1);
        int count = 250_000;
        for (int value = 0; value < count; value++) {
            heap.insert(value ^ 0x5A5A5A5A);
        }

        int previous = Integer.MAX_VALUE;
        while (!heap.isEmpty()) {
            int current = heap.popMax();
            assertTrue(current <= previous, "stress heap must pop in non-increasing order");
            previous = current;
        }
    }

    private static void assertPopsDescending(PowerOfTwoMaxHeap heap, int highInclusive, int lowInclusive, String message) {
        for (int expected = highInclusive; expected >= lowInclusive; expected--) {
            assertEquals(expected, heap.popMax(), message);
        }
        assertTrue(heap.isEmpty(), message + " should be empty after draining");
    }

    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static <T extends Throwable> void expectThrows(Class<T> expectedType, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable actual) {
            if (expectedType.isInstance(actual)) {
                return;
            }
            throw new AssertionError("expected " + expectedType.getSimpleName()
                    + " but got " + actual.getClass().getSimpleName(), actual);
        }
        throw new AssertionError("expected " + expectedType.getSimpleName());
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
