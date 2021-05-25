
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * n = 3, edges = [[0,1,100],[1,2,100],[0,2,500]] src = 0, dst = 2, k = 1 输出:
 * 200
 */
class CheapestFlights {
    /**
     * 递归解法一般都超时
     */
    public int findCheapestPrice(int n, int[][] flights, int src, int dst, int k) {
        Map<Integer, List<Integer>> m1 = new HashMap<>();
        Map<Integer, List<Integer>> m2 = new HashMap<>();
        for (int i = 0; i < flights.length; i++) {
            List<Integer> l1 = m1.getOrDefault(flights[i][0], new ArrayList<>());
            l1.add(flights[i][1]);
            m1.put(flights[i][0], l1);
            List<Integer> l2 = m2.getOrDefault(flights[i][0], new ArrayList<>());
            l2.add(flights[i][2]);
            m2.put(flights[i][0], l2);
        }
        return helper(m1, m2, -1, 0, src, dst, k);
    }

    public int helper(Map<Integer, List<Integer>> m1, Map<Integer, List<Integer>> m2, int depth, int price, int src,
            int dst, int k) {
        if (depth > k) {
            return -1;
        }
        if (src == dst) {
            return price;
        }
        List<Integer> l1 = m1.getOrDefault(src, new ArrayList<>());
        List<Integer> p1 = m2.getOrDefault(src, new ArrayList<>());
        int maxPrices = Integer.MAX_VALUE;
        for (int i = 0; i < l1.size(); i++) {
            int p = helper(m1, m2, depth + 1, price + p1.get(i), l1.get(i), dst, k);
            if (p < maxPrices && p >= 0) {
                maxPrices = p;
            }
        }
        return maxPrices == Integer.MAX_VALUE ? -1 : maxPrices;
    }

    /**
     * 动态规划可以解决问题，但是还可以继续优化，使用状态压缩降低内存的占用
     */
    public int d1(int n, int[][] flights, int src, int dst, int k) {
        int INF = 10000000;
        int[][] f = new int[k + 2][n];
        for (int t = 0; t <= k + 1; t++) {
            Arrays.fill(f[t], INF);
            if (t == 0) {
                f[0][src] = 0;
                continue;
            }
            for (int[] flight : flights) {
                int i = flight[0], j = flight[1], cost = flight[2];
                f[t][j] = Math.min(f[t][j], f[t - 1][i] + cost);
            }
        }
        int ans = INF;
        for (int t = 1; t <= k + 1; ++t) {
            ans = Math.min(ans, f[t][dst]);
        }
        return ans == INF ? -1 : ans;
    }

    public int d2(int n, int[][] flights, int src, int dst, int k) {
        int INF = 10000000;
        int[] f = new int[k + 2];
        Arrays.fill(f, INF);
        f[src] = 0;
        int ans = INF;
        for (int t = 1; t <= k + 1; t++) {
            int[] g = new int[n];
            Arrays.fill(g, INF);
            for (int[] flight : flights) {
                int i = flight[0], j = flight[1], cost = flight[2];
                g[j] = Math.min(g[j], f[i] + cost);
            }
            f = g;
            ans = Math.min(ans, f[dst]);
        }
        return ans == INF ? -1 : ans;
    }

    public static void main(String[] args) {
        int[][] flights = { { 0, 1, 100 }, { 1, 2, 200 }, { 0, 2, 500 } };
        CheapestFlights cheapestFlights = new CheapestFlights();
        System.out.println(cheapestFlights.findCheapestPrice(3, flights, 0, 2, 1));
        System.out.println(cheapestFlights.d1(3, flights, 0, 2, 1));
        System.out.println(cheapestFlights.d2(3, flights, 0, 2, 1));
    }
}