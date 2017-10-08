#mlp backprop
import numpy as np
import matplotlib.pyplot as plt
import csv

def sigmoid(x):
	return 1./(1.+np.exp(-x))

def error_output(y,t):
	return y*(1-y)*(t-y)

def error_hidden(y_hidden,w_hidden,error_output):
	sum = np.sum()


def main():
	x = [[0.,0.],[0.,1.],[1.,0.],[1.,1.]]
	t = [0.,1.,1.,1.]
	w = [
		[np.random.uniform(0.,1.),np.random.uniform(0.,1.),np.random.uniform(0.,1.),np.random.uniform(0.,1.)],
		[np.random.uniform(0.,1.),np.random.uniform(0.,1.)]
		]
	
	alfa=0.1
		
	G_E = []
	
	it = 0
	while True:
		E = 0.
		for i in range(len(x)):
	
			x53 = 0.
			x54 = 0.
			x31 = x[i][0]*w[0][0] + x[i][1]*w[0][1]
			x42 = x[i][0]*w[0][2] + x[i][1]*w[0][3]
			y3 = x53 = sigmoid(x31)	
			y4 = x54 = sigmoid(x42)
			x5 = x53*w[1][0]+x54*w[1][1]
			y = sigmoid(x5)
			
			delta5 = y*(1-y)*(t[i]-y)
			delta3 = y3*(1-y3)*w[1][0]*delta5
			delta4 = y4*(1-y4)*w[1][1]*delta5
			
			w[1][1] = w[1][1] + alfa*delta5*x54
			w[1][0] = w[1][0] + alfa*delta5*x53
			w[0][0] = w[0][0] + alfa*delta3*x31
			w[0][1] = w[0][1] + alfa*delta3*x31
			w[0][2] = w[0][2] + alfa*delta4*x42
			w[0][3] = w[0][3] + alfa*delta4*x42
			
			E += np.power(np.subtract(t[i],y),2)	
		
		E = 0.5*E
		
		G_E.append(E)
		
		if len(G_E) > 2 and G_E[len(G_E)-2] < G_E[len(G_E)-1] or G_E[len(G_E)-1] < 0.001:
			break
		it = it +1
		if(it > 10000):
			break;
	
	print('G_E = '+str(G_E[-1]))
	#print(w)
	print('Iterations='+str(it))
	
	for i in range(len(x)):	
		x53 = 0.
		x54 = 0.
		x31 = x[i][0]*w[0][0] + x[i][1]*w[0][1]
		x42 = x[i][0]*w[0][2] + x[i][1]*w[0][3]
		y3 = x53 = sigmoid(x31)	
		y4 = x54 = sigmoid(x42)
		x5 = x53*w[1][0]+x54*w[1][1]
		y = sigmoid(x5)
		
		print('RESULTS:')
		print(str(t[i])+'->'+str(y))
		
	plt.plot(range(len(G_E)),G_E,'r')
	plt.show()
	
if __name__ == '__main__':
	main()

	