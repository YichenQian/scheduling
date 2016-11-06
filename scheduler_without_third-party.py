map = [[1, 1, 0, 0, 1, 0, 1], [1, 0, 1, 1, 0, 0, 1], [0, 1/3, 0, 0, 0, 1/3, 0], [2/3, 0, 0, 0, 2/3, 0, 0], [4, 2, 5, 10, 8, 7, 7]]
#map = [[1, 1, 0, 0, 1, 0, 1], [1, 0, 1, 1, 0, 0, 1], [0, 1, 0, 0, 0, 1, 0], [4, 5, 5, 10, 8, 7, 7]]

def scheduling():
    return

def mapping(topology):
    return

def waterFilling(map):
    rate = [0 for i in range(len(map)-3)]
    for i in range(len(map)):
        map[i].append(i)
    while len(map) > 3:
        # Find the bottleneck
        tMin = min(filter(lambda x: x > 0, map[-1]))
        index = map[-1].index(tMin)
        # Update the mapping
        temp = []
        for i in range(len(map) - 3):
            if map[i][index] != 0:
                rate[map[i][-1]] = map[-1][index] * map[i][index]
                temp.append(i)
        # Count deleted num and update num
        num = [0 for i in range(len(map[1]) - 1)]
        for i in range(len(map[1]) - 1):
            for j in temp:
                num[i] += map[j][i]
            map[-2][i] -= num[i]
        # Delete the row
        offset = 0
        for i in temp:
            del map[i - offset]
            offset += 1
        # Update bandwidth and tao
        for i in range(0, len(map[1]) - 1):
            map[-3][i] -= num[i] * tMin
            if map[-2][i] ==0:
                map[-1][i] = 0
            else:
                map[-1][i] = map[-3][i] / map[-2][i]
        # Delete redundant row and column
        for i in range(len(map)):
            del map[i][index]
        temp = []
        for i in range(len(map[1])):
            if map[-3][i] == 0:
                temp.append(i)
        for i in temp:
            for j in range(len(map)):
                del map[j][i]
    return rate

def update(map):
    num = [0 for i in range(len(map[1]))]
    tao = [0 for i in range(len(map[1]))]
    for i in range (len(map[1])):
        for j in range (len(map)-1):
            num[i] += map[j][i]
        if num[i] == 0:
            tao[i] = 0
        else:
            tao[i] = map[-1][i] / num[i]
    map.append(num)
    map.append(tao)
    rate = waterFilling(map)
    return rate

print (update(map))