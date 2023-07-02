
# ! 2 entities in this scheme, User and Tally

import random
import math
import numpy as np
import hashlib
import time

from utils import *

COEF_MIN = 1e5
COEF_MAX = 3e5

C_MIN = 1e2
C_MAX = 9e2


class User():
    def __init__(self, _id, group: CyclicGroup, epsilon, delta, T):
        self.x = group.rand()
        self.y = group.pow(group.g, self.x)
        # add the group object to the user
        self.group = group
        # hyperparameters
        self.epsilon = epsilon
        self.delta = delta
        self.T = T
        self.id = _id

        self.p = group.rand()
        # record the state of the user
        self.s = 0

    def get_y(self):
        return self.y

    def get_b(self):
        return self.b

    def construct_sketch(self, I):
        self.d = math.ceil(math.log(self.T)/self.delta)
        self.w = math.ceil(math.e/self.epsilon)

        self._gen_hash_params()

        X_matrix = np.zeros((self.d, self.w))

        # each data point
        for i in I:
            # each row
            for j in range(self.d):
                # calculate the j-th row's hash value
                h_j_i = self._cal_hash(index=j, x=i)
                X_matrix[j][h_j_i] = X_matrix[j][h_j_i] + self._random_c()

        # flatten the matrix to a vector, length L = d * w
        self.X = X_matrix.flatten()
        # A often used length
        self.L = self.d * self.w

    def encrypt_sketch(self, y_dict: dict):
        k = []
        for l in range(1, self.L + 1):
            k_l = 0
            for _id, y_id in y_dict.items():
                if self.id == _id:
                    continue

                temp = self._cal_H(
                    str(self.group.pow(y_id, self.x)) + str(l) + str(self.s)
                ) * (-1 if self.id > _id else 1) % self.group.q
                k_l += temp
            k.append(k_l)

        self.k = k
        # vector addition
        self.b = self.X + self.k

    def _cal_H(self, x):
        hash_obj = hashlib.sha256(str(x).encode('utf-8'))
        hex_256 = hash_obj.hexdigest()
        return int(hex_256, base=16) % self.p

    def _cal_hash(self, index, x):
        return ((self.hash_params[index][0] * x + self.hash_params[index][1]) % self.p) % self.w

    def _random_c(self):
        return random.randint(C_MIN, C_MAX)

    def _gen_hash_params(self):
        # only save the parameters of a and b
        hash_params = []
        for i in range(self.d):
            hash_params.append(
                (random.randint(COEF_MIN, COEF_MAX),
                 random.randint(COEF_MIN, COEF_MAX))
            )
        self.hash_params = hash_params


class Tally():
    def __init__(self):
        pass

    def aggregate(self, b_dict: dict):
        C = 0
        for k, v in b_dict.items():
            C += v
        self.C = C % 2 ** 32
        return self.C


if __name__ == '__main__':
    # Some args

    sec_params = 32
    epsilon = 0.01
    delta = 0.01
    T = 20
    N = 10

    I = [2023 * T]
    y_dict = {}
    b_dict = {}
    users = []

    measure_encryps = []
    measure_aggregate = 0

    group = CyclicGroup(sec_params)
    # generate N users
    for i in range(N):
        user = User(i + 1, group, epsilon, delta, T)
        y_dict[i + 1] = user.get_y()
        users.append(user)

    # each user perform CM Sketch construction & encrypt the Sketch
    for i in range(N):
        user = users[i]
        user.construct_sketch(I)
        print(i, 'cons')
        # * measure the encryption time
        tmp = time.time()
        user.encrypt_sketch(y_dict)
        measure_encryps.append(time.time() - tmp)
        print(i, 'enc')
        b_dict[i + 1] = user.get_b()

    # Tally perform aggregation
    tally = Tally()
    # * measure the aggregation time
    tmp = time.time()
    C = tally.aggregate(b_dict)
    measure_aggregate = time.time() - tmp

    print(C)
    print(measure_encryps)
    print(measure_aggregate)