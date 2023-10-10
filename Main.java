package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;


public class Main {
    public static void main(String[] args) {
        String[][] cake = new String[][]{
                {".", "o", ".", ".", ".", ".", ".", "."},
                {".", ".", ".", ".", "o", ".", ".", "."},
                {".", ".", ".", ".", ".", ".", ".", "."},
                {".", ".", "o", ".", "o", ".", ".", "."}
        };


        List<CakePiece> initial = new ArrayList<>();

        CakePiece cakePiece = new CakePiece(cake);
        verticalCut(cakePiece, initial);
        horizontalCut(cakePiece, initial);

        List<CakePiece> combinedList = new ArrayList<>();

        for (CakePiece piece : initial) {
            List<CakePiece> visited = new ArrayList<>();
            List<CakePiece> tmp = new ArrayList<>();

            exploreAndCutRaisinPieces(piece, visited, tmp);

            deleteDuplicates(visited);
            deleteDuplicates(tmp);

            combinedList.addAll(tmp);
            combinedList.addAll(visited);
        }

        deleteCakesWithManyRaisins(combinedList);
        deleteDuplicates(combinedList);

        Comparator<CakePiece> cakePieceComparator = Comparator.comparingInt(cp -> cp.piece.length);
        combinedList.sort(cakePieceComparator);

        List<CakePiece> allVariants = findSuitableCakePieces(combinedList, cakePiece);
        List<CakePiece> widerPieces = findWiderPieces(allVariants);

        String[][] initialCake = copyArray(cake);
        List<CakePiece> result = finalCut(allVariants, widerPieces, cake, initialCake);
        if (result.size() < raisinsAmount(initialCake)) {
            result = finalCut(allVariants, allVariants, cake, initialCake);
        }

        System.out.println("\nInitial cake:");
        printCake(initialCake);
        System.out.println("\n");
        printResultPieces(result);
    }

    private static void printResultPieces(List<CakePiece> result) {
        int i = 1;
        for (CakePiece p : result) {
            System.out.println("------------------------\n" + (i++) + ") Piece: ");
            printCake(p.piece);
            System.out.println("\nS = " + p.area + "\n------------------------");
        }
    }


    private static List<CakePiece> finalCut(List<CakePiece> allVariants, List<CakePiece> widerPieces, String[][] cake, String[][] cake2) {
        List<CakePiece> result = new ArrayList<>();

        String[][] copyCake = copyArray(cake);
        for (CakePiece widerVar : widerPieces) {
            if (removeSubArray(copyCake, widerVar.piece)) {
                removeSubArray(cake, widerVar.piece);
                for (int j = 0; j < allVariants.size(); j++) {
                    CakePiece allVariant = allVariants.get(j);
                    result.add(allVariant);
                    boolean flag = removeSubArray(copyCake, allVariant.piece);
                    removeSubArray(cake, allVariant.piece);
                    if (!flag || !findCombination(copyCake, cake, widerVar, allVariant, allVariants, result)) {
                        result.clear();
                        result.add(widerVar);
                        copyCake = copyArray(cake2);
                        cake = copyArray(cake2);
                        removeSubArray(copyCake, widerVar.piece);
                        removeSubArray(cake, widerVar.piece);
                    } else {
                        return result;
                    }
                }
            }
            copyCake = copyArray(cake2);
            cake = copyArray(cake2);
        }
        return result;
    }


    private static String[][] copyArray(String[][] source) {
        int rows = source.length;
        int cols = source[0].length;
        String[][] destination = new String[rows][cols];

        for (int i = 0; i < rows; i++) {
            System.arraycopy(source[i], 0, destination[i], 0, cols);
        }

        return destination;
    }


    private static boolean findCombination(String[][] cake, String[][] initCake, CakePiece widerPiece, CakePiece allVariant, List<CakePiece> allVariants, List<CakePiece> result) {
        if (allElementsAreDash(cake)) {
            return true;
        }

        for (int j = 0; j < allVariants.size(); j++) {
            CakePiece variant = allVariants.get(j);
            if (removeSubArray(cake, variant.piece)) {
                result.add(variant);
                if (findCombination(cake, initCake, widerPiece, allVariant, allVariants, result)) {
                    return true;
                }
                result.remove(variant);

                int[] position = findPiecePosition(initCake, variant.piece);
                if (position != null) {
                    int rowOffset = position[0];
                    int colOffset = position[1];
                    insertPiece(cake, variant.piece, rowOffset, colOffset);
                }
            }
        }

        return false;
    }


    private static int[] findPiecePosition(String[][] cake, String[][] piece) {
        for (int i = 0; i <= cake.length - piece.length; i++) {
            for (int j = 0; j <= cake[i].length - piece[0].length; j++) {
                boolean found = true;
                for (int row = 0; row < piece.length; row++) {
                    for (int col = 0; col < piece[0].length; col++) {
                        if (!piece[row][col].equals(cake[i + row][j + col]) && !piece[row][col].equals(".")) {
                            found = false;
                            break;
                        }
                    }
                    if (!found) {
                        break;
                    }
                }
                if (found) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }


    private static void insertPiece(String[][] cakeUpdated, String[][] piece, int rowOffset, int colOffset) {
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (!piece[i][j].equals("-")) {
                    cakeUpdated[i + rowOffset][j + colOffset] = piece[i][j];
                }
            }
        }
    }


    private static boolean allElementsAreDash(String[][] array) {
        for (String[] strings : array) {
            for (int j = 0; j < array[0].length; j++) {
                if (!strings[j].equals("-")) {
                    return false;
                }
            }
        }
        return true;
    }


    private static List<CakePiece> findWiderPieces(List<CakePiece> all) {
        List<CakePiece> pieces = new ArrayList<>();
        int max = 0;
        int column;
        CakePiece cakePiece;
        for (CakePiece value : all) {
            cakePiece = value;
            column = cakePiece.piece[0].length;
            if (column > max) {
                max = column;
            }
        }

        for (CakePiece piece : all) {
            cakePiece = piece;
            column = cakePiece.piece[0].length;
            if (column == max) {
                pieces.add(cakePiece);
            }
        }
        return pieces;
    }


    private static boolean removeSubArray(String[][] first, String[][] second) {
        boolean isChanged = false;
        int rowFirst = first.length;
        int colFirst = first[0].length;
        int rowSecond = second.length;
        int colSecond = second[0].length;

        for (int i = 0; i <= rowFirst - rowSecond; i++) {
            for (int j = 0; j <= colFirst - colSecond; j++) {
                boolean match = true;
                for (int r = 0; r < rowSecond; r++) {
                    for (int c = 0; c < colSecond; c++) {
                        if (!first[i + r][j + c].equals(second[r][c])) {
                            match = false;
                            break;
                        }
                    }
                    if (!match) {
                        break;
                    }
                }
                if (match) {
                    isChanged = true;
                    for (int r = 0; r < rowSecond; r++) {
                        for (int c = 0; c < colSecond; c++) {
                            first[i + r][j + c] = "-";
                        }
                    }
                    return isChanged; //added this line
                }
            }
        }
        return isChanged;
    }


    private static List<CakePiece> findSuitableCakePieces(List<CakePiece> allPieces, CakePiece initialCake) {
        List<CakePiece> allVariants = new ArrayList<>();
        int quantityOfRaisins = raisinsAmount(initialCake.piece);
        double initArea = initialCake.piece.length * initialCake.piece[0].length;
        double onePieceArea = initArea / quantityOfRaisins;
        for (CakePiece cakePiece : allPieces) {
            if (cakePiece.area == onePieceArea) {
                allVariants.add(cakePiece);
            }
        }
        return allVariants;
    }


    private static void exploreAndCutRaisinPieces(CakePiece initialPiece, List<CakePiece> visitedPieces, List<CakePiece> allVerticalPieces) {
        Stack<CakePiece> stack = new Stack<>();
        stack.push(initialPiece);

        while (!stack.isEmpty()) {
            CakePiece cakePiece = stack.pop();
            if (cakePiece.area > 2 && raisinsAmount(cakePiece.piece) > 0) {
                visitedPieces.add(cakePiece);
                allVerticalPieces.remove(cakePiece);

                List<CakePiece> newPieces = new ArrayList<>();
                horizontalCut(cakePiece, newPieces);
                verticalCut(cakePiece, newPieces);

                for (CakePiece newPiece : newPieces) {
                    if (raisinsAmount(newPiece.piece) > 0) {
                        stack.push(newPiece);
                    }
                }
            }
        }
    }


    private static void deleteDuplicates(List<CakePiece> allVerticalPieces) {
        Set<String> uniquePieces = new HashSet<>();
        List<CakePiece> nonDuplicatePieces = new ArrayList<>();

        for (CakePiece piece : allVerticalPieces) {
            String pieceString = Arrays.deepToString(piece.piece);
            if (uniquePieces.add(pieceString)) {
                nonDuplicatePieces.add(piece);
            }
        }

        allVerticalPieces.clear();
        allVerticalPieces.addAll(nonDuplicatePieces);
    }


    private static void deleteCakesWithManyRaisins(List<CakePiece> allVerticalPieces) {
        allVerticalPieces.removeIf(cakePiece -> raisinsAmount(cakePiece.piece) > 1);
    }


    private static void verticalCut(CakePiece cake, List<CakePiece> allVerticalPieces) {
        int colAmount = cake.piece[0].length;
        List<CakePiece> cakePieces = new ArrayList<>();

        for (int i = 0; i < colAmount - 1; i++) {

            for (int j = 0; j + i <= colAmount - 1; j++) {
                String[][] onePiece;
                if (i > 0) {
                    onePiece = getColumn(cake.piece, j);
                    for (int k = j; k < j + i; k++) {
                        onePiece = concatenateArraysHorizontally(onePiece, getColumn(cake.piece, k + 1));
                    }
                } else {
                    onePiece = getColumn(cake.piece, j);
                }

                if (raisinsAmount(onePiece) == 0) {
                    continue;
                }
                double area = onePiece.length * onePiece[0].length;
                cakePieces.add(new CakePiece(onePiece, area));
            }
            allVerticalPieces.addAll(cakePieces);
            cakePieces.clear();
        }
    }


    private static void horizontalCut(CakePiece cake, List<CakePiece> allVerticalPieces) {
        int rowAmount = cake.piece.length;
        List<CakePiece> cakePieces = new ArrayList<>();

        for (int i = 0; i < rowAmount - 1; i++) {
            for (int j = 0; j + i <= rowAmount - 1; j++) {
                String[][] onePiece;
                if (i > 0) {
                    onePiece = getRow(cake.piece, j);
                    for (int k = j; k < j + i; k++) {
                        onePiece = concatenateArraysVertically(onePiece, getRow(cake.piece, k + 1));
                    }
                } else {
                    onePiece = getRow(cake.piece, j);
                }

                if (raisinsAmount(onePiece) == 0) {
                    continue;
                }
                double area = onePiece.length * onePiece[0].length;
                cakePieces.add(new CakePiece(onePiece, area));
            }
            allVerticalPieces.addAll(cakePieces);
            cakePieces.clear();
        }
    }


    private static String[][] concatenateArraysHorizontally(String[][] first, String[][] second) {
        int length = Math.max(second.length, first.length);
        String[][] newPiece = new String[length][first[0].length + second[0].length];
        for (int i = 0; i < first.length; i++) {
            System.arraycopy(first[i], 0, newPiece[i], 0, first[0].length);
        }

        for (int j = 0; j < second[0].length; j++) {
            for (int i = 0; i < second.length; i++) {
                newPiece[i][j + first[0].length] = second[i][j];
            }
        }

        return newPiece;
    }


    private static String[][] concatenateArraysVertically(String[][] first, String[][] second) {
        int length = Math.max(second[0].length, first[0].length);
        String[][] newPiece = new String[first.length + second.length][length];
        for (int i = 0; i < first.length; i++) {
            System.arraycopy(first[i], 0, newPiece[i], 0, first[0].length);
        }

        for (int i = 0; i < second.length; i++) {
            System.arraycopy(second[i], 0, newPiece[i + first.length], 0, second[0].length);
        }

        return newPiece;
    }


    private static String[][] getColumn(String[][] cake, int column) {
        String[][] columnArr = new String[cake.length][1];
        for (int i = 0; i < cake.length; i++) {
            columnArr[i][0] = cake[i][column];
        }
        return columnArr;
    }


    private static String[][] getRow(String[][] cake, int row) {
        if (row < 0 || row >= cake.length) {
            return null;
        }

        int columns = cake[0].length;
        String[][] rowArr = new String[1][columns];
        for (int i = 0; i < columns; i++) {
            if (i < cake[row].length) {
                rowArr[0][i] = cake[row][i];
            } else {
                rowArr[0][i] = "";
            }
        }
        return rowArr;
    }


    private static void printCake(String[][] cake) {
        for (String[] strings : cake) {
            for (int j = 0; j < cake[0].length; j++) {
                System.out.print(strings[j] + " ");
            }
            System.out.println();
        }
    }


    private static int raisinsAmount(String[][] cake) {
        int counter = 0;
        for (String[] strings : cake) {
            for (int j = 0; j < cake[0].length; j++) {
                if (strings[j].equals("o")) {
                    counter++;
                }
            }
        }
        return counter;
    }
}