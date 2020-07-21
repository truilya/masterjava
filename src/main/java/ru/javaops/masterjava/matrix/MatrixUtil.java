package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    // TODO implement parallel multiplication matrixA*matrixB
    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor, final int threadNumber) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        final int[][] matrixBT = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrixBT[i][j] = matrixB[j][i];
            }
        }
        class PartMatrixMultiplier implements Callable<Integer> {
            private int lowBound;
            private int upperBound;

            public PartMatrixMultiplier(int lowBound, int upperBound) {
                this.lowBound = lowBound;
                this.upperBound = upperBound;
            }

            @Override
            public Integer call() throws Exception {
                for (int i = lowBound; i < upperBound; i++) {

                    for (int j = 0; j < matrixSize; j++) {
                        int sum = 0;
                        int[] row = matrixA[i];
                        int[] column = matrixBT[j];
                        for (int k = 0; k < matrixSize; k++) {
                            sum += row[k] * column[k];
                        }
                        matrixC[i][j] = sum;
                    }

                }
                return 1;
            }
        }
        final int delta = matrixSize / threadNumber;
        List<PartMatrixMultiplier> list = new ArrayList<>();
        for (int i = 0; i < threadNumber; i++) {
            int lowBound = i * delta;
            int upperBound;
            if (i == (threadNumber - 1)) {
                upperBound = matrixSize;
            } else {
                upperBound = (i + 1) * delta;
            }
            list.add(new PartMatrixMultiplier(lowBound, upperBound));
        }
        CompletionService<Integer> completionService = new ExecutorCompletionService<>(executor);
        List<Future<Integer>> futures = new ArrayList<>();
        for (PartMatrixMultiplier p : list) {
            futures.add(completionService.submit(p));
        }
        while (!futures.isEmpty()) {
            Future<Integer> future = completionService.poll();
            futures.remove(future);
        }
        return matrixC;
    }

    // TODO optimize by https://habrahabr.ru/post/114797/
    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];
        final int[][] matrixBT = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrixBT[i][j] = matrixB[j][i];
            }
        }
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int[] thatRow = matrixA[i];
                int[] thatColumn = matrixBT[j];
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += thatRow[k] * thatColumn[k];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] oldSingleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * matrixB[k][j];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
