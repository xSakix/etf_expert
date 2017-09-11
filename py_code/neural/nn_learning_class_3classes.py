import numpy as np
import matplotlib.pyplot as plt
import csv
from mpl_toolkits.mplot3d import Axes3D

def sigmoid(x):
	return 1./(1.+np.exp(-x))

def step(x):
	return 0.5*(np.sign(x) + np.sign(1 - x))

def step2(x):
	if x > 0:
		return 1.
	return -1.
	
def leaky_relu(x):
	if x > 0.:
		return x
	
	return 0.01*x

def may(x,r):
	return x*r*(1.-x)

def node(x,w,w0,activation_func):
	# print('---NODE-START---')
	# print('x='+str(x))
	# print('w='+str(w))
	# print('w0='+str(w0))
	multi = np.multiply(x,w)
	# print('multi='+str(multi))
	result = activation_func(w0+np.sum(multi))
	# print('---NODE-END---')
	return result

def net(x,w,w0,d,function,learning=True,debug=True):
	local_e = []
	y = []	
	n_layers = len(w)
	xx = x
	for layer in range(n_layers-1):
		
		yy = []
		if debug:
			print('layer='+str(layer))
			print('xx='+str(xx))
			print('w[layer]='+str(w[layer]))
			
		for x_i in range(len(xx)):
			yy.append(node(xx[x_i],w[layer],w0,function))
		
		if learning:
			local_e.append(np.subtract(d,yy))
		
		if debug:
			print('yy='+str(yy))
			
		xx = yy
	
	if debug:
		print('xx='+str(xx))
	
	if len(np.shape(xx)) == 1:
		xx = [xx]
		if debug:	
			print('[xx]='+str(xx))
	
	for x_i in xx:
		y.append(node(x_i,w[n_layers-1],w0, function))
	
	if debug:
		print('y[net]='+str(y))
	
	if learning:
		local_e.append(np.subtract(d,y))
	
	if debug:
		print('local_e='+str(local_e))
	
	return y,local_e


def load_data(etf_data_path, limit_lo=None, limit_hi=None):
	x = []
	d = []
	it = 0
	with open(etf_data_path,'r') as csvfile:
		spamreader = csv.reader(csvfile,delimiter=',')
		for row in spamreader:
			if limit_lo is not None and limit_lo < it:
				continue
			x.append([float(r) for r in row[:4]])
			d.append(float(row[4]))
			if limit_hi is not None and len(x) > limit_hi:
				break
			it+=1
	
	return x,d
	
def main():
	
	x,d = load_data('c:\\WORK folders\\etf_expert\\py_code\\SPY_training_data.csv')	
	
	fig = plt.figure()
	# ax = fig.add_subplot(111, projection='3d')
	# for i in range(len(x)):
		# c = 'k'
		# if int(d[i]) == -4:
			# c = 'r'
		# elif int(d[i]) == 4:
			# c = 'b'
		# ax.scatter(x[i][1],x[i][2],x[i][3],color=c)

	for i in range(len(x)):
		c = 'k'
		if d[i] == 0.5:
			c = 'r'
		elif d[i] == 1.:
			c = 'b'
		plt.scatter(d[i],x[i][3],color=c)
		
	plt.show()
	
	# w = [
		# [np.random.uniform(-5.,5.),np.random.uniform(-5.,5.),np.random.uniform(-5.,5.),np.random.uniform(-5.,5.)],
		# [np.random.uniform(-5.,5.),np.random.uniform(-5.,5.),np.random.uniform(-5.,5.),np.random.uniform(-5.,5.)],
		# [np.random.uniform(-5.,5.)]
		# ]
	# w0 = 0.0
	
	# alfa = 0.1
	# iterations = 100
	# iter = 0
	# error = []
	# gradient_e = 1000.
	
	
	
	# while iter < iterations and gradient_e > 0.1:
		# print('w0='+str(w0))
		# print('weights='+str(w))
		
		# y,local_e = net(x,w,w0,d,sigmoid,learning=True,debug=False)	
		
		
		# gradient_e = 0.5*np.sum(np.power(np.subtract(d,y),2))
		# if gradient_e is float('NaN') or gradient_e is float('Inf') :
			# exit()
		# error.append(gradient_e)
		# print('global gradient='+str(gradient_e))
		# print('------')
		
		# kazda vaha ma 4 vysledky?
		# for i in range(len(x)):
			# for layer in range(len(w)):
				# for j in range(len(w[layer])):
					# w[layer][j] = w[layer][j]-alfa*local_e[layer][i]*x[i][j]

		# iter+=1
	
	
	# x,d = load_data('c:\\WORK folders\\etf_expert\\py_code\\SPY_training_data.csv',limit_lo=6000,limit_hi=100)	

	# num = 0.
	# matches = 0
	# print('----RESULTS----')
	# for i in range(len(x)):
		# y,_ = net(x[i],w,w0,d,sigmoid,learning=False,debug=True)		
		# print('x[i]='+str(x[i]))
		# print('result=>'+str(d[i])+'|'+str(y))
		# print('------')
		# sub = np.subtract(d[i],y)
		# if sub == 0:
			# matches+=1
		# num += np.power(sub,2)
	
	# num = 0.5*num
	# print('result:'+str(num))
	
	# plt.subplot(2,2,1)
	# plt.plot(range(len(error)),error,'r')
	
	
	
	# plt.show()
	
if __name__ == '__main__':
	main()