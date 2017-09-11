#gradient descent for training a linear unit
import numpy as np
import matplotlib.pyplot as plt
import csv


def main():
	x = [[0.,0.],[0.,1.],[1.,0.],[1.,1.]]
	d = [0.,1.,1.,1.]
	w = [np.random.uniform(0.,1.),np.random.uniform(0.,1.)]
	alfa = 0.1	
	w_old = []
	
	G_E = []
	
	while True:
		E = 0.
		print('start'+str(w))
		w_old = w[:]
		for i in range(len(x)):
			y = np.array(x[i]).dot(w)
			error = d[i]-y
			print('[d[i],y,error]=['+str(d[i])+','+str(y)+','+str(error)+']')
		
			for k in range(len(w)):
				w[k] = w[k]+alfa*error*x[i][k]
			
			E += np.power(error,2)
			
		E = 0.5*E
		G_E.append(E)
		print('LSM='+str(E))
		print('end'+str(w))
		
		if E < 0.1 or (len(G_E) > 2 and G_E[len(G_E)-2] - G_E[len(G_E)-1] == 0.):
			print('not changing')
			break
	

	plt.plot(range(len(G_E)),G_E,'r')
	plt.show()
	
	
if __name__ == '__main__':
	main()
	