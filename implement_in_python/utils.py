import random
import math

class CyclicGroup:
    def __init__(self, _lambda=128):
        # Generate a prime number q of 256 bits in length
        q = random.getrandbits(_lambda)
        while not self.is_prime(q):
            q = random.getrandbits(_lambda)
        self.q = q
        
        # Choose a random generator g of the group
        self.g = random.randint(2, self.q-1)
    
    def is_prime(self, n, k=5):
        # Miller-Rabin primality test
        if n == 2 or n == 3:
            return True
        if n <= 1 or n % 2 == 0:
            return False
        
        r, s = 0, n-1
        while s % 2 == 0:
            r += 1
            s //= 2
        for _ in range(k):
            a = random.randrange(2, n-1)
            x = pow(a, s, n)
            if x == 1 or x == n-1:
                continue
            for _ in range(r-1):
                x = pow(x, 2, n)
                if x == n-1:
                    break
            else:
                return False
        return True
    
    def mul(self, a, b):
        return (a * b) % self.q
    
    def add(self, a, b):
        return (a + b) % self.q
    
    def pow(self, a, n):
        return pow(a, n, self.q)
    
    def rand(self):
        return random.randint(1, self.q-1)
    