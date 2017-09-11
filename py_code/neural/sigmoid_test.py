import numpy as np
import matplotlib.pyplot as plt
import csv
import math

def sigmoid(x):
	return  1./(1.+np.exp(-x))

def sigmoid2(x):
	print(x)
	return 1./(1.+math.exp(-x))
	
def main():
	x = np.random.uniform(-5.,5.,10000)
	x = np.sort(x)
	# y = []
	# for xx in x:
		# y.append(sigmoid(xx))
	y = sigmoid(x)
	plt.plot(x,y,'r')
	plt.show()
	
	
if __name__ == '__main__':
	main()