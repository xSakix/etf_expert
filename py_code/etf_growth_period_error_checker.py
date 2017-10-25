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


def print_sim_report(sum, r_c, shares, cash, price):
    print('total investment:' + str(sum))
    print('r_hold:' + str(r_c[0]))
    print('r_buy:' + str(r_c[1]))
    print('r_sell:' + str(r_c[2]))
    print('num of shares:' + str(shares))
    print('available cash:' + str(cash))
    print('total:' + str((shares * price + cash)))


def plot(data, hist_c, r_c, hist_cash, hist_shares, num_hold, num_buy, num_sell):
    plt.figure(1)
    x = range(len(data))
    plt.subplot(3, 3, 1)
    line1, = plt.plot(x[0:40], hist_c[0][0:40], 'r', label='hist hold')
    plt.legend([line1], ['hist hold, r=' + str(r_c[0])])
    plt.subplot(3, 3, 2)
    line2, = plt.plot(x[0:40], hist_c[1][0:40], 'g', label='hist buy')
    plt.legend([line2], ['hist buy, r=' + str(r_c[1])])
    plt.subplot(3, 3, 3)
    line3, = plt.plot(x[0:40], hist_c[2][0:40], 'b', label='hist sell')
    plt.legend([line3], ['hist sell, r=' + str(r_c[2])])
    plt.subplot(3, 3, 4)
    line4, = plt.plot(x, hist_cash, 'g', label='cash')
    plt.legend([line4], ['cash'])
    plt.subplot(3, 3, 5)
    line5, = plt.plot(x, hist_shares, 'r', label='shares')
    plt.legend([line5], ['shares'])
    plt.subplot(3, 3, 6)
    line6, = plt.plot(x, data, 'y', label='spy')
    plt.legend([line6], ['spy'])
    plt.subplot(3, 3, 7)
    line7 = plt.bar([1, 2, 3], [num_hold, num_buy, num_sell], label='hold,buy,sell operations')
    plt.legend([line7], ['hold,buy,sell operations'])
    plt.show()


def read_etf(etf_name):
    data = []
    dates = []
    num_rows = 0
    # nacitame data
    with open(etf_name, 'r') as csvfile:
        spamreader = csv.reader(csvfile, delimiter=',')
        for row in spamreader:
            num_rows += 1
            # if int(row[0]) > 1483228800:
            data.append(float(row[1]))
            dates.append(str(row[0]))

    return data, num_rows, dates


def compute_choice(choice_min=0., choice_max=1., isConstant=False):
    if (isConstant):
        return choice_max

    return np.random.uniform(choice_min, choice_max)


def main():
    # grow factor pre hold, buy, sell
    # best spy
    r_c = [3.5558226003907123, 3.965694510786344, 3.6113613485193024]
    rc_list = [
        [3.4363722651454927, 3.8957059902210607, 3.688627244823083],
        [3.6386754520214817, 3.8833704321563216, 3.9727648714906167],
        [5.747453482384317, 3.7284941116606305, 3.6725701016651855],
        [3.294807710597407, 3.725440072471869, 3.7787457550696546],
        [2.999214812613804, 3.72581319890074, 3.6709767732351968],
        [2.839118170428752, 3.99620693976436, 3.7522930764190785],
        [3.6733174286673616, 3.7809759390588114, 3.6937943179672583],
        [3.8856334664505887, 3.8253458331648207, 3.6808476165396566],
        [3.2510371781661873, 3.8262661183664903, 3.6611946519828145],
        [1.4681416794322608, 3.7626733637135015, 3.683968207962119],
        [11.406277538430825, 3.7403393118525954, 3.6809644691290897]
    ]
    ticket = 'SPY'

    data, l, dates = read_etf('c:\\WORK folders\\etf_expert\\py_code\\etf_data\\' + ticket + '.csv')

    cash, hist_c, hist_cash, hist_shares, num_buy, num_hold, num_sell, shares, sum = simulate(r_c, data, l,
                                                                                              dates)  # ak je max vacsi nez limit, skonci

    cumulative = 0.
    price = data[len(data) - 1]
    expected = shares * price + cash
    print('expected total=' + str(expected))
    print('---------------------')
    for r_cc in rc_list:
        index = rc_list.index(r_cc)
        cash2, hist_c2, hist_cash2, hist_shares2, num_buy2, num_hold2, num_sell2, shares2, sum2 = simulate(r_cc, data,
                                                                                                           l,
                                                                                                           dates)  # ak je max vacsi nez limit, skonci
        actual = shares2 * price + cash2
        print('actual total=' + str(actual) + ' for case = ' + str(index))
        part = np.power((expected - actual), 2.)
        cumulative += part
        lse = 0.5 * part
        print('LSE(%d)=%.3f' % (int(index), float(lse)))
        rmse = np.sqrt(lse)
        print('RMSE(%d)=%.3f' % (int(index), float(rmse)))
        print('---------------------')

    lse = 0.5 * cumulative
    print('LSE(ALL)=%.3f' % float(lse))
    rmse = np.sqrt(lse)
    print('RMSE(ALL)=%.3f' % float(rmse))
    print('---------------------')


def simulate(r_c, data, l, dates):
    # zakladne vlastnosti hold, buy, sell
    c = [0.01, 0.01, 0.01]
    # zakladny cash
    cash = 300.
    # referecna suma
    sum = cash
    # pociatocny pocet share spy
    shares = 0
    # inicializacia historie vlastnosti, shares, cash a poctu operacii
    hist_c = [[] for i in range(len(data))]
    hist_shares = []
    hist_cash = []
    num_hold = 0
    num_buy = 0
    num_sell = 0
    actions = []
    for i in range(20):
        # inicializuj growth factor
        compute_yorke(c, r_c, hist_c)
    # compute_may_feigenbaum(c,r_c,hist_c)
    # compute_feigenbaum(c,r_c,hist_c)
    # print('total number of rows:' + str(l))
    # print('num of data:' + str(len(data)))
    for preheat in range(l - len(data)):
        compute_yorke(c, r_c, hist_c)

    # zakladny loop pre jeden vyber parametrov
    for i in range(len(data)):
        price = data[i]
        # uloz do historie shares a hodnotu aktiv
        hist_shares.append(shares)
        hist_cash.append(cash + price * shares)

        # vypocitaj growth factor
        compute_yorke(c, r_c, hist_c)
        # compute_may_feigenbaum(c,r_c,hist_c)
        # compute_feigenbaum(c,r_c,hist_c)

        # kazdy mesiac (30dni) investujeme 300
        if (i % 30 == 0):
            cash += 300.
            sum += 300.

        # nahodny vyber
        choice = compute_choice(choice_max=0.9, isConstant=True)
        action_performed = False
        # hold akcia
        if (c[0] > choice):
            num_hold += 1
            action_performed = True
            actions.append([dates[i], c[0], c[1], c[2], price / 1000., 'H', price * shares + cash, shares])
            continue
        # buy akcia
        if c[1] > choice:
            num_buy += 1
            num_shares = cash / price
            cash -= price * num_shares
            shares += num_shares
            action_performed = True
            if num_shares > 0:
                actions.append([dates[i], c[0], c[1], c[2], price / 1000., 'B', price * shares + cash, shares])
            else:
                actions.append([dates[i], c[0], c[1], c[2], price / 1000., 'H', price * shares + cash, shares])
        # sell akcia
        if c[2] > choice:
            num_sell += 1
            cash += price * shares
            if shares > 0:
                actions.append([dates[i], c[0], c[1], c[2], price / 1000., 'S', cash, 0])
            else:
                actions.append([dates[i], c[0], c[1], c[2], price / 1000., 'H', price * shares + cash, shares])
            shares = 0
            action_performed = True

        # no-action = hold action
        if not action_performed:
            num_hold += 1
            actions.append([dates[i], c[0], c[1], c[2], price / 1000., 'H', price * shares + cash, shares])

    return cash, hist_c, hist_cash, hist_shares, num_buy, num_hold, num_sell, shares, sum


if __name__ == "__main__":
    main()
