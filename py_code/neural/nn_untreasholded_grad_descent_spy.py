#gradient descent for training a linear unit
import numpy as np
import matplotlib.pyplot as plt
import csv

def load_data(etf_data_path):
	x = []
	d = []
	with open(etf_data_path,'r') as csvfile:
		spamreader = csv.reader(csvfile,delimiter=',')
		for row in spamreader:
			x.append([float(r) for r in row[:4]])
			d.append(float(row[4]))
	
	return x,d

def main():
	x,d = load_data
	w = [np.random.uniform(0.,1.),np.random.uniform(0.,1.)]
	alfa = 0.1
	
	for iter in range(100):
		print(w)
		dw = [0., 0.]
		for i in range(len(x)):
			y = np.array(x[i]).dot(w)
			error = d[i]-y
			print('[d[i],y,error]=['+str(d[i])+','+str(y)+','+str(error)+']')
			for k in range(len(dw)):
				dw[k] = dw[k] + alfa*error*x[i][k]
		
		for k in range(len(w)):
			w[k] = w[k]+dw[k]
	

if __name__ == '__main__':
	main()
	