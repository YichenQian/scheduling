import numpy as np
import pulp

y = pulp.LpVariable("y", 0, 10)
x = pulp.LpVariable("x", 0, 1)

#map = np.matrix([[1, 1, 0, 0, 1, 0, 1], [1, 0, 1, 1, 0, 0, 1], [0, 1, 0, 0, 0, 1, 0], [4, 5, 5, 10, 8, 7, 7]])
#map = np.matrix([[1, 1, 0, 0, 1, 0, 1], [1, 0, 1, 1, 0, 0, 1], [0, 1/3, 0, 0, 0, 1/3, 0], [2/3, 0, 0, 0, 2/3, 0, 0], [4, 2, 5, 10, 8, 7, 7]])
map = np.matrix([[1, 1, 0, 0, 1, 0, 1], [1, 0, 1, 1, 0, 0, 1], [0, 1/2, 0, 0, 0, 1/2, 0], [1/2, 0, 0, 0, 1/2, 0, 0], [4, 2, 5, 10, 8, 7, 7]])
#map = np.matrix([[1, 1, 0, 0, 1, 0, 1], [1, 0, 1, 1, 0, 0, 1], [0, x, 0, 0, 0, x, 0], [1-x, 0, 0, 0, 1-x, 0, 0], [4, 2, 5, 10, 8, 7, 7]])

def scheduling():
    return

def mapping(topology, ongoing):
    return

def waterFilling(map):
    rate = np.zeros(map.shape[0]-3)
    order = np.arange(0, map.shape[0], 1)
    map = np.insert(map, 0, order, axis=1)
    while map.shape[0] > 3:
        # Find the bottleneck
        tMin = map[-1, 1:].min(1)[0, 0]
        index = map[-1, 1:].argmin(1)[0, 0]
        index += 1
        # Update the rate
        temp = []
        for i in range(map.shape[0]-3):
            if map[i, index] != 0:
                rate[int(map[i, 0])] = map[-1, index] * map[i, index]
                temp.append(i)
        # Update num and delete the redundant row
        num = np.sum(map[temp], axis=0)
        map[-2] = map[-2] - num
        map = np.delete(map, temp, 0)
        # Update bandwidth and tao
        map[-3] -= num * tMin
        for i in range(1, map.shape[1]):
            map[-1, i] = map[-2, i] and map[-3, i] / map[-2, i] or 0
        # Delete redundant row and column
        map = np.delete(map, index, 1)
        temp = []
        for i in range(1, map.shape[1]):
            if map[-3, i] == 0:
                temp.append(i)
        map = np.delete(map, temp, 1)
    return rate

def caculatePartition(map):
    prob = pulp.LpProblem("Max min problem", pulp.LpMinimize)
    prob += y
    for i in range (map.shape[1]):
        if map[-1, i].__class__ == pulp.LpAffineExpression:
            prob += map[-2, i] * y - map[-1, i] >=0
    prob.solve()
    return prob.variables()[0].varValue

def update(map):
    symbolFlag = 0
    num = np.sum(map[0 : -1], axis = 0)
    tao = np.matrix([[]])
    for i in range(num.size):
        if num[0, i].__class__ == pulp.LpAffineExpression:
            symbolFlag = 1
    map = np.append(map, num, 0)
    if symbolFlag == 1:
        xp = caculatePartition(map)
        # Substitute the variables in matrix
        map[2, 1] = xp
        map[2, 5] = xp
        map[3, 0] = 1 - xp
        map[3, 4] = 1 - xp
        map[-1] = np.sum(map[0: -2], axis=0)
    for i in range(num.size):
        if map[-1, i] == 0:
            tao = np.append(tao, [[0]], 1)
        else:
            tao = np.append(tao, [[map[-2, i] / map[-1, i]]], 1)
    map = np.append(map, tao, 0)
    rate = waterFilling(map)
    return rate

print (update(map))