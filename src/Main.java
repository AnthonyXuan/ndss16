import java.util.*;
import java.math.*;
import java.security.*;
import java.math.BigInteger;

public class Main {
    private static final double COEF_MIN = 1e5;
    private static final double COEF_MAX = 3e5;
    private static final double C_MIN = 1e2;
    private static final double C_MAX = 9e2;

    public static void main(String[] args) {
        int secParams = 32;
        double epsilon = 0.01;
        double delta = 0.01;
        int T = 20;
        int N = 10;
        List<Integer> I = new ArrayList<Integer>();
        I.add(2023 * T);
        Map<Integer, BigInteger> yDict = new HashMap<Integer, BigInteger>();
        Map<Integer, BigInteger[]> bDict = new HashMap<Integer, BigInteger[]>();
        List<User> users = new ArrayList<User>();
        List<Double> measureEncryps = new ArrayList<Double>();
        double measureAggregate = 0;

        CyclicGroup group = new CyclicGroup(secParams);
        for (int i = 0; i < N; i++) {
            User user = new User(i + 1, group, epsilon, delta, T);
            yDict.put(i + 1, user.getY());
            users.add(user);
        }

        for (int i = 0; i < N; i++) {
            User user = users.get(i);
            user.constructSketch(I);
            System.out.println(i + " cons");
            long startTime = System.nanoTime();
            user.encryptSketch(yDict);
            measureEncryps.add((double) (System.nanoTime() - startTime) / 1e9);
            System.out.println(i + " enc");
            bDict.put(i + 1, user.getB());
        }

        Tally tally = new Tally();
        long startTime = System.nanoTime();
        BigInteger C = tally.aggregate(bDict);
        measureAggregate = (double) (System.nanoTime() - startTime) / 1e9;

        System.out.println(C);
        System.out.println(measureEncryps);
        System.out.println(measureAggregate);

    }

    public static class User {
        private int id;
        private CyclicGroup group;
        private double epsilon;
        private double delta;
        private int T;
        private BigInteger x;
        private BigInteger y;
        private BigInteger p;
        private int s;
        // * d and w should be int, since they are indices in the dictionary
        private int d;
        private int w;
        private int[][] X;
        private List<BigInteger> k;
        private BigInteger[] b;
        private List<Pair<Double, Double>> hashParams;

        public User(int id, CyclicGroup group, double epsilon, double delta, int T) {
            this.id = id;
            this.group = group;
            this.epsilon = epsilon;
            this.delta = delta;
            this.T = T;
            this.x = group.rand();
            this.y = group.pow(group.getG(), this.x);
            this.p = group.rand();
            this.s = 0;
        }

        public BigInteger getY() {
            return this.y;
        }

        public BigInteger[] getB() {
            return this.b;
        }

        public void constructSketch(List<Integer> I) {
            this.d = (int) Math.ceil(Math.log(this.T) / this.delta);
            this.w = (int) Math.ceil(Math.E / this.epsilon);
            this.genHashParams();

            int[][] XMatrix = new int[this.d][this.w];
            for (int i : I) {
                for (int j = 0; j < this.d; j++) {
                    int h_j_i = this.calHash(j, i);
                    XMatrix[j][h_j_i] += this.randomC();
                }
            }

            this.X = new int[this.d * this.w][1];
            for (int j = 0; j < this.d; j++) {
                for (int k = 0; k < this.w; k++) {
                    this.X[j * this.w + k][0] = XMatrix[j][k];
                }
            }
        }

        public void encryptSketch(Map<Integer, BigInteger> yDict) {
            List<BigInteger> k = new ArrayList<BigInteger>();
            for (int l = 1; l <= this.d * this.w; l++) {
                BigInteger k_l = BigInteger.valueOf(0);
                for (Map.Entry<Integer, BigInteger> entry : yDict.entrySet()) {
                    int id = entry.getKey();
                    BigInteger y_id = entry.getValue();
                    if (this.id == id) {
                        continue;
                    }
                    String str = this.group.pow(y_id, this.x) + "" + l + "" + this.s;
                    BigInteger temp = this.calH(str).multiply(this.id > id ? BigInteger.valueOf(-1) : BigInteger.ONE);
                    k_l = k_l.add(temp);
                }
                k.add(k_l);
            }

            this.k = k;
            this.b = new BigInteger[this.d * this.w];
            for (int i = 0; i < this.d * this.w; i++) {
                this.b[i] = BigInteger.valueOf(this.X[i][0]).add(this.k.get(i));
            }
        }

        private double randomC() {
            return Math.floor(Math.random() * (C_MAX - C_MIN + 1) + C_MIN);
        }

        private void genHashParams() {
            this.hashParams = new ArrayList<Pair<Double, Double>>();
            for (int i = 0; i < this.d; i++) {
                double a = Math.floor(Math.random() * (COEF_MAX - COEF_MIN + 1) + COEF_MIN);
                double b = Math.floor(Math.random() * (COEF_MAX - COEF_MIN + 1) + COEF_MIN);
                this.hashParams.add(new Pair<Double, Double>(a, b));
            }
        }

        private int calHash(int index, int x) {
            double a = this.hashParams.get(index).first;
            double b = this.hashParams.get(index).second;
            return (int) ((a * x + b) % this.p.doubleValue() % this.w);
        }

        private BigInteger calH(String x) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(x.getBytes("UTF-8"));
                BigInteger bigInt = new BigInteger(1, digest);
                return bigInt.mod(this.p);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return BigInteger.valueOf(-1);
        }
    }

    public static class Tally {
        public BigInteger aggregate(Map<Integer, BigInteger[]> bDict) {
            BigInteger C = BigInteger.valueOf(0);
            for (BigInteger[] b : bDict.values()) {
                for (BigInteger x : b) {
                    C = C.add(x);
                }
            }
            return C;
        }
    }

    public static class Pair<T1, T2> {
        public T1 first;
        public T2 second;

        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    }
}