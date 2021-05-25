---
layout:     post
title:      "leetcode-旋转图像"
subtitle:   "https://leetcode-cn.com/problems/rotate-image/"
date:       2021-08-23 12:00:00
author:     "gdream"
header-img: "img/leetcode/post-rotate.jpeg"
catalog: true
tags:
    - leetcode
    - 算法
---

> “leetcode-旋转图像 找规律”

```java
/**
 * 给定一个 n × n 的二维矩阵 matrix 表示一个图像。请你将图像顺时针旋转 90 度。
 * 
 * 你必须在 原地 旋转图像，这意味着你需要直接修改输入的二维矩阵。请不要 使用另一个矩阵来旋转图像。
 * 
 * 来源：力扣（LeetCode） 链接：https://leetcode-cn.com/problems/rotate-image
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 */
class RotateImage {
    /**
     * m[0,0] --> m[0, n-1] --> m[n-1, n-1] --> m[n-1, 0] --> m[0, 0] m[0,1] -->
     * m[1, n-1] --> m[n-1, n-2] --> m[n-2, 0] --> m[0, 1] m[i, j] --> m[j, n-i-1]
     * --> matrix[n - i - 1][n - j - 1] --> matrix[n - j - 1][i] --> matrix[i][j]
     * 
     * @param matrix
     */
    public void rotate(int[][] matrix) {
        int n = matrix.length;
        for (int i = 0; i < n / 2; ++i) {
            for (int j = 0; j < (n + 1) / 2; ++j) {
                int temp = matrix[i][j];
                matrix[i][j] = matrix[n - j - 1][i];
                matrix[n - j - 1][i] = matrix[n - i - 1][n - j - 1];
                matrix[n - i - 1][n - j - 1] = matrix[j][n - i - 1];
                matrix[j][n - i - 1] = temp;
            }
        }
    }

    public static void main(String[] args) {
        int n = 4;
        int[][] matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = i * n + j + 1;
            }
        }
        new RotateImage().rotate(matrix);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                System.out.println(matrix[i][j]);
            }
        }
    }
}
```