
# ! 2 entities in this scheme, User and Tally

import random

class User:
    def __init__(self, g, q, epsilon, delta):
        self.x = random.randint(1e10, 9e10)
        self.y = g ** self.x % q
        self.epsilon = epsilon
        self.delta = delta
        
    def construct_sketch(self, I):
        L = 