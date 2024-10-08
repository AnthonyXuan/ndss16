import java.math.BigInteger;
import java.security.SecureRandom;

// ! Define the group used in the NDSS16 implementation

public class CyclicGroup {
    private BigInteger q;
    private BigInteger g;

    public CyclicGroup(int lambda) {
        SecureRandom rand = new SecureRandom();
        BigInteger q = BigInteger.probablePrime(lambda, rand);
        while (!isPrime(q)) {
            q = BigInteger.probablePrime(lambda, rand);
        }
        this.q = q;
        this.g = randBetween(BigInteger.valueOf(2), this.q.subtract(BigInteger.ONE));
    }

    public boolean isPrime(BigInteger n, int k) {
        if (n.equals(BigInteger.valueOf(2)) || n.equals(BigInteger.valueOf(3))) {
            return true;
        }
        if (n.compareTo(BigInteger.valueOf(2)) < 0 || n.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
            return false;
        }

        int r = 0;
        BigInteger d = n.subtract(BigInteger.ONE);
        while (d.mod(BigInteger.valueOf(2)).equals(BigInteger.ZERO)) {
            r++;
            d = d.divide(BigInteger.valueOf(2));
        }

        for (int i = 0; i < k; i++) {
            BigInteger a = randBetween(BigInteger.valueOf(2), n.subtract(BigInteger.valueOf(2)));
            BigInteger x = a.modPow(d, n);
            if (x.equals(BigInteger.ONE) || x.equals(n.subtract(BigInteger.ONE))) {
                continue;
            }
            boolean composite = true;
            for (int j = 0; j < r - 1; j++) {
                x = x.modPow(BigInteger.valueOf(2), n);
                if (x.equals(n.subtract(BigInteger.ONE))) {
                    composite = false;
                    break;
                }
            }
            if (composite) {
                return false;
            }
        }

        return true;
    }

    public boolean isPrime(BigInteger n) {
        return isPrime(n, 5);
    }

    public BigInteger mul(BigInteger a, BigInteger b) {
        return a.multiply(b).mod(this.q);
    }

    public BigInteger add(BigInteger a, BigInteger b) {
        return a.add(b).mod(this.q);
    }

    public BigInteger pow(BigInteger a, BigInteger n) {
        return a.modPow(n, this.q);
    }

    public BigInteger randBetween(BigInteger a, BigInteger b) {
        SecureRandom rand = new SecureRandom();
        BigInteger r = new BigInteger(b.bitLength(), rand);
        while (r.compareTo(a) < 0 || r.compareTo(b) > 0) {
            r = new BigInteger(b.bitLength(), rand);
        }
        return r;
    }

    // * generate random number in field Z_q
    public BigInteger rand() {
        return randBetween(BigInteger.ONE, this.q.subtract(BigInteger.ONE));
    }

    public BigInteger getG() {
        return this.g;
    }
}