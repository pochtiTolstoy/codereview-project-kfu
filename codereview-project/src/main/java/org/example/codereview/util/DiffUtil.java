package org.example.codereview.util;

import org.example.codereview.model.DiffLine;
import org.example.codereview.model.DiffType;

import java.util.ArrayList;
import java.util.List;

public class DiffUtil {

    public static List<DiffLine> diff(List<String> baseLines, List<String> reviewLines) {
        int n = baseLines.size();
        int m = reviewLines.size();

        int[][] dp = new int[n + 1][m + 1];

        
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                if (baseLines.get(i).equals(reviewLines.get(j))) {
                    dp[i][j] = dp[i + 1][j + 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j + 1]);
                }
            }
        }

        List<DiffLine> result = new ArrayList<>();
        int i = 0, j = 0;
        int leftNum = 1, rightNum = 1;

        while (i < n && j < m) {
            String left = baseLines.get(i);
            String right = reviewLines.get(j);

            if (left.equals(right)) {
                
                DiffLine line = new DiffLine();
                line.setType(DiffType.SAME);
                line.setLeftLineNumber(leftNum);
                line.setRightLineNumber(rightNum);
                line.setLeftText(left);
                line.setRightText(right);
                result.add(line);

                i++; j++;
                leftNum++; rightNum++;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                
                DiffLine line = new DiffLine();
                line.setType(DiffType.REMOVED);
                line.setLeftLineNumber(leftNum);
                line.setLeftText(left);
                line.setRightLineNumber(null);
                line.setRightText(null);
                result.add(line);

                i++;
                leftNum++;
            } else {
                
                DiffLine line = new DiffLine();
                line.setType(DiffType.ADDED);
                line.setRightLineNumber(rightNum);
                line.setRightText(right);
                line.setLeftLineNumber(null);
                line.setLeftText(null);
                result.add(line);

                j++;
                rightNum++;
            }
        }

        
        while (i < n) {
            String left = baseLines.get(i);
            DiffLine line = new DiffLine();
            line.setType(DiffType.REMOVED);
            line.setLeftLineNumber(leftNum);
            line.setLeftText(left);
            line.setRightLineNumber(null);
            line.setRightText(null);
            result.add(line);

            i++;
            leftNum++;
        }

        while (j < m) {
            String right = reviewLines.get(j);
            DiffLine line = new DiffLine();
            line.setType(DiffType.ADDED);
            line.setRightLineNumber(rightNum);
            line.setRightText(right);
            line.setLeftLineNumber(null);
            line.setLeftText(null);
            result.add(line);

            j++;
            rightNum++;
        }

        return result;
    }
}

