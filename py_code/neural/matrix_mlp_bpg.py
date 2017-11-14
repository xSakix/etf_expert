import numpy as np
import matplotlib.pyplot as pplot
import csv


norm = 1000.

def nonlin(x,deriv=False):
	if(deriv==True):
	    return x*(1.-x)

	return 1./(1.+np.exp(-x))


def read_etf():
    data = []
    # nacitame data
    with open('c:\\downloaded_data\\USD\\SPY.csv', 'r') as csvfile:
        for row in csv.reader(csvfile, delimiter=','):
            data.append(float(row[1]) / norm)

    return data


def main():

    data = read_etf()

    x1 = np.array(data[0:-10])
    x2 = np.array(data[1:-9])
    x3 = np.array(data[2:-8])
    x4 = np.array(data[3:-7])
    x = []
    for i in range(6150):
        x.append(np.array([x1[i], x2[i], x3[i], x4[i]]))

    x = np.array(x)
    y = np.array(data[4:-6])
    y = np.reshape(y, (len(y), 1))

    np.random.seed(1)

    # randomly initialize our weights with mean 0
    syn0 = 2 * np.random.random((4, 9)) - 1
    syn1 = 2 * np.random.random((9, 1)) - 1

    for j in range(60000):

        # Feed forward through layers 0, 1, and 2
        l0 = x
        l1 = nonlin(np.dot(l0, syn0))
        l2 = nonlin(np.dot(l1, syn1))

        # how much did we miss the target value?
        l2_error = np.subtract(y, l2)

        if (j % 10000) == 0:
            print("Error:" + str(np.mean(np.abs(l2_error))))

        l2_delta = l2_error * nonlin(l2, deriv=True)

        l1_error = l2_delta.dot(syn1.T)

        l1_delta = l1_error * nonlin(l1, deriv=True)

        syn1 += l1.T.dot(l2_delta)
        syn0 += l0.T.dot(l1_delta)

    l0 = x
    l1 = nonlin(np.dot(l0, syn0))
    yy = nonlin(np.dot(l1, syn1))

    pplot.plot(y, 'r')
    pplot.plot(yy, 'b')
    pplot.show()


if __name__ == "__main__":
    main()


