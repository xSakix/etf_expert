#gradient descent for training a linear unit
import numpy as np
import matplotlib.pyplot as plt
import csv


def main():
	x = [[0.,0.],[0.,1.],[1.,0.],[1.,1.]]
	d = [0.,1.,1.,1.]
	w = [np.random.uniform(0.,1.),np.random.uniform(0.,1.)]
	alfa = 0.1
	w0 = -0.8
	
	for iter in range(1000):
		print(w)
		dw = [0., 0.]
		for i in range(len(x)):
			y = w0+np.array(x[i]).dot(w)
			error = d[i]-y
			print('[d[i],y,error]=['+str(d[i])+','+str(y)+','+str(error)+']')
			for k in range(len(dw)):
				dw[k] = dw[k] + alfa*error*x[i][k]
		
		for k in range(len(w)):
			w[k] = w[k]+dw[k]
	

if __name__ == '__main__':
	main()
	