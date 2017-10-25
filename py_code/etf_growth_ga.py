import matplotlib.pyplot as plt
import numpy as np
import csv


def compute_yorke(c, r_c, hist_c):
    c[0] = c[0] * r_c[0] * (1. - c[0])
    hist_c[0].append(c[0])
    c[1] = c[1] * r_c[1] * (1. - c[1])
    hist_c[1].append(c[1])
    c[2] = c[2] * r_c[2] * (1. - c[2])
    hist_c[2].append(c[2])


def compute_may_feigenbaum(c, r_c, hist_c):
    c_pow = np.power(c, 2.)
    c[0] = r_c[0] * (c[0] - c_pow[0])
    hist_c[0].append(c[0])
    c[1] = r_c[1] * (c[1] - c_pow[1])
    hist_c[1].append(c[1])
    c[2] = r_c[2] * (c[2] - c_pow[2])
    hist_c[2].append(c[2])


def compute_feigenbaum(c, r_c, hist_c):
    c[0] = r_c[0] * np.sin(np.pi * c[0])
    hist_c[0].append(c[0])
    c[1] = r_c[1] * np.sin(np.pi * c[1])
    hist_c[1].append(c[1])
    c[2] = r_c[2] * np.sin(np.pi * c[2])
    hist_c[2].append(c[2])


def print_sim_report(individual):
    print('total investment:' + str(individual.sum))
    print('r_hold:' + str(individual.r_c[0]))
    print('r_buy:' + str(individual.r_c[1]))
    print('r_sell:' + str(individual.r_c[2]))
    print('num of shares:' + str(individual.shares))
    print('available cash:' + str(individual.cash))
    print('total:' + str(individual.total()))


def plot(data, individual, hist_totals, ticket):
    plt.figure(1)
    x = range(len(data))

    plt.subplot(3, 3, 1)
    line1, = plt.plot(x[0:40], individual.hist_c[0][0:40], 'r', label='hist hold')
    plt.legend([line1], ['hist hold, r=' + str(individual.r_c[0])])

    plt.subplot(3, 3, 2)
    line2, = plt.plot(x[0:40], individual.hist_c[1][0:40], 'g', label='hist buy')
    plt.legend([line2], ['hist buy, r=' + str(individual.r_c[1])])

    plt.subplot(3, 3, 3)
    line3, = plt.plot(x[0:40], individual.hist_c[2][0:40], 'b', label='hist sell')
    plt.legend([line3], ['hist sell, r=' + str(individual.r_c[2])])

    plt.subplot(3, 3, 4)
    line4, = plt.plot(x, individual.hist_cash, 'g', label='cash')
    plt.legend([line4], ['cash'])

    plt.subplot(3, 3, 5)
    line5, = plt.plot(x, individual.hist_shares, 'r', label='shares')
    plt.legend([line5], ['shares'])

    plt.subplot(3, 3, 6)
    line6, = plt.plot(x, data, 'y', label=ticket)
    plt.legend([line6], [ticket])

    plt.subplot(3, 3, 7)
    line7 = plt.bar([1, 2, 3], [individual.num_hold, individual.num_buy, individual.num_sell],
                    label='hold,buy,sell operations')
    plt.legend([line7], ['hold,buy,sell operations'])

    plt.subplot(3, 3, 8)
    xx = range(len(hist_totals))
    line8, = plt.plot(xx, hist_totals, 'r', label='GA convergence')
    plt.legend([line8], ['GA convergence'])

    plt.show()


def read_etf(etf_path):
    data = []

    # nacitame data
    with open(etf_path, 'r') as csvfile:
        etf_reader = csv.reader(csvfile, delimiter=',')
        for row in etf_reader:
            # if int(row[0]) > 1483228800:
            data.append(float(row[1]))

    return data


def compute_choice(choice_min=0., choice_max=1., isConstant=False):
    if (isConstant):
        return choice_max

    return np.random.uniform(choice_min, choice_max)


class Individual:
    def __init__(self, data):
        self.data = data
        self.r_c = [np.random.uniform(3., 4.), np.random.uniform(3., 4.), np.random.uniform(3., 4.)]
        # self.r_c = [np.random.uniform(0.7,1.),np.random.uniform(0.7,1.),np.random.uniform(0.7,1.)]
        self.c = [0.01, 0.01, 0.01]
        self.cash = 300.
        self.sum = self.cash
        self.shares = 0
        self.hist_c = [[] for i in range(len(data))]
        self.hist_shares = []
        self.hist_cash = []
        self.num_hold = 0
        self.num_buy = 0
        self.num_sell = 0

    def initial_heat(self):
        for i in range(20):
            # inicializuj growth factor
            compute_yorke(self.c, self.r_c, self.hist_c)

    # compute_may_feigenbaum(c,r_c,hist_c)
    # compute_feigenbaum(self.c,self.r_c,self.hist_c)

    def simulate(self):
        # zakladny loop pre jeden vyber parametrov
        for i in range(len(self.data)):
            price = self.data[i]
            # uloz do historie shares a hodnotu aktiv
            self.hist_shares.append(self.shares)
            self.hist_cash.append(self.cash + price * self.shares)

            # vypocitaj growth factor
            compute_yorke(self.c, self.r_c, self.hist_c)
            # compute_may_feigenbaum(c,r_c,hist_c)
            # compute_feigenbaum(self.c,self.r_c,self.hist_c)

            # kazdy mesiac (30dni) investujeme 300
            if (i % 30 == 0):
                self.cash += 300.
                self.sum += 300.

                # nahodny vyber
            choice = compute_choice(choice_max=0.9, isConstant=True)
            action_performed = False
            # hold akcia
            if (self.c[0] > choice):
                self.num_hold += 1
                action_performed = True
                continue
                # buy akcia
            if (self.c[1] > choice):
                self.num_buy += 1
                num_shares = np.rint(self.cash / price)
                self.cash -= price * num_shares
                self.shares += num_shares
                action_performed = True
                # sell akcia
            if (self.c[2] > choice):
                self.num_sell += 1
                self.cash += price * self.shares
                self.shares = 0
                action_performed = True

                # no-action = hold action
            if (not action_performed):
                self.num_hold += 1

    def total(self):
        return self.cash + self.data[len(self.data) - 1] * self.shares


def cross(f1, f2):
    i1 = int(f1 * np.power(10., 16))
    i2 = int(f2 * np.power(10., 16))
    b1 = np.binary_repr(i1, 57)
    b2 = np.binary_repr(i2, 57)
    index = int(np.random.uniform(0, 57))
    b3 = b1[:index] + b2[index:]
    i3 = int(b3, 2)

    return float(i3 / np.power(10., 16))


def mutate(f1):
    i1 = int(f1 * np.power(10., 16))
    b1 = np.binary_repr(i1, 57)
    index = int(np.random.uniform(0, 57))
    b2 = None
    if b1[index] == '0':
        b2 = b1[:index - 1] + '1' + b1[index + 1:]
    else:
        b2 = b1[:index - 1] + '0' + b1[index + 1:]

    i3 = int(b2, 2)

    return float(i3 / np.power(10., 16))


def create_child(parent1, parent2, data):
    child = Individual(data)

    child.r_c[0] = cross(parent1.r_c[0], parent2.r_c[0])
    child.r_c[1] = cross(parent1.r_c[1], parent2.r_c[1])
    child.r_c[2] = cross(parent1.r_c[2], parent2.r_c[2])

    return child


def mutate_child(child):
    child.r_c[0] = mutate(child.r_c[0])
    child.r_c[1] = mutate(child.r_c[1])
    child.r_c[2] = mutate(child.r_c[2])


def find_best(individuals):
    max = 0.
    best = None

    for i in range(len(individuals)):
        if (individuals[i].total() > max):
            max = individuals[i].total()
            best = individuals[i]

    return max, best


def reinitialize(individual, data):
    individual.cash = 300.
    individual.sum = individual.cash
    individual.shares = 0
    individual.c = [0.01, 0.01, 0.01]
    individual.hist_c = [[] for i in range(len(data))]
    individual.hist_shares = []
    individual.hist_cash = []
    individual.num_hold = 0
    individual.num_buy = 0
    individual.num_sell = 0


def create_copy(individual, data):
    copy = Individual(data)

    copy.cash = individual.cash
    copy.sum = individual.sum
    copy.shares = individual.shares
    copy.hist_c = individual.hist_c
    copy.hist_shares = individual.hist_shares
    copy.hist_cash = individual.hist_cash
    copy.num_hold = individual.num_hold
    copy.num_buy = individual.num_buy
    copy.num_sell = individual.num_sell
    copy.r_c = individual.r_c
    copy.c = individual.c

    return copy


def main():
    ticket = 'SPY'
    data = read_etf('c:\\downloaded_data\\USD\\' + ticket + '.csv')

    print('Original length: %d' % int(len(data)))

    data = data[:int(71*len(data) / 72)]

    print('New length: %d' % int(len(data)))

    individuals = []

    hist_totals = []

    iterations = 0

    best_of_all = None

    population_max = 100

    for i in range(population_max):
        individuals.append(Individual(data))

    while (True):

        for i in range(len(individuals)):
            individuals[i].initial_heat()
            individuals[i].simulate()

        max, best = find_best(individuals)
        hist_totals.append(max)
        print(str(iterations) + '=' + str(max))
        print()

        if best_of_all is None or best_of_all.total() < max:
            best_of_all = create_copy(best, data)
            print('best of all:' + str(best_of_all.total()))

            # if max > 200000.:
        if iterations > 1000:
            print_sim_report(best_of_all)
            plot(data, best_of_all, hist_totals, ticket)
            break

        individuals.remove(best)
        _, second_best = find_best(individuals)
        individuals.remove(second_best)

        individuals2 = []
        reinitialize(best, data)
        reinitialize(second_best, data)

        individuals2.append(best)
        individuals2.append(second_best)

        print(best.r_c)
        print(second_best.r_c)

        while len(individuals2) < population_max:
            child1 = create_child(best, second_best, data)
            if np.random.uniform(0., 1.) < 0.05:
                mutate_child(child1)

            child2 = create_child(second_best, best, data)
            if np.random.uniform(0., 1.) < 0.05:
                mutate_child(child2)

            individuals2.append(child1)
            individuals2.append(child2)
            _, best = find_best(individuals)
            individuals.remove(best)
            _, second_best = find_best(individuals)
            individuals.remove(second_best)

        individuals = individuals2

        print('----------------------------------------')
        iterations += 1

if __name__ == "__main__":
    main()
