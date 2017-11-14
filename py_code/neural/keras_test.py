import numpy as np
import csv
from keras.models import Sequential
from keras.layers import LSTM, Dense, SimpleRNN
import matplotlib.pyplot as plt

norm = 1000.


def read_etf():
    data = []
    # nacitame data
    with open('c:\\downloaded_data\\USD\\SPY.csv', 'r') as csvfile:
        for row in csv.reader(csvfile, delimiter=','):
            data.append(float(row[1]) / norm)

    return data


def main():
    data = read_etf();

    x = np.array(data[0:len(data) - 1])
    y = np.array(data[1:len(data)])

    print(x)

    model = Sequential()
    #keras.layers.SimpleRNN(units, activation='tanh', use_bias=True, kernel_initializer='glorot_uniform', recurrent_initializer='orthogonal', bias_initializer='zeros', kernel_regularizer=None, recurrent_regularizer=None, bias_regularizer=None, activity_regularizer=None, kernel_constraint=None, recurrent_constraint=None, bias_constraint=None, dropout=0.0, recurrent_dropout=0.0, return_sequences=False, return_state=False, go_backwards=False, stateful=False, unroll=False)
    #model.add(LSTM(4, activation='sigmoid', input_shape=(1, 1)))
    #model.add(SimpleRNN(4,activation='sigmoid', input_shape=(1, 1)))
    model.add(SimpleRNN(units=4, stateful=True, batch_input_shape=(1, 1, 1)))
    #model.add(Dense(4, activation='sigmoid', input_dim=1))

    model.add(Dense(input_dim=4, output_dim=1, activation='sigmoid'))

    model.compile(loss='mean_squared_error',
                  optimizer='rmsprop',
                  metrics=['accuracy'])

    model.fit(x, y, epochs=5)
    yp = model.predict(x)
    plt.plot(y, 'r')
    plt.plot(yp, 'b')
    plt.show()




if __name__ == '__main__':
    main()
