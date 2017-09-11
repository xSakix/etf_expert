#najjednoduchsie siet - 1 perceptron...w1,w2 su 0.5 a w0 reguluje ci to je and (-0.3) al or (+0.3)
#-1 = false, 1 = true
import numpy as np
import matplotlib.pyplot as plt


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
	return activation_func(w0+np.sum(np.multiply(x,w)))

def net(x,w,w0,d,function,learning=True,debug=True):
	local_e = []
	y = []	
	n_layers = len(w)
	xx = x
	for layer in range(n_layers-1):
		
		yy = []
		if debug:
			print('layer='+str(layer))
			print(xx)
			print(w[layer])
			
		for x_i in range(len(xx)):
			yy.append(node(xx[x_i],w[layer],w0,function))
		
		if learning:
			local_e.append(0.5*np.sum(np.power(np.subtract(d,yy),2)))
		
		if debug:
			print(yy)
			
		xx = yy
				
	y.append(node(xx,w[n_layers-1][0],w0, function))
	if debug:
		print('y='+str(y))
	
	if learning:
		local_e.append(0.5*np.sum(np.power(np.subtract(d,y),2)))
	
	if debug:
		print('local_e='+str(local_e))
	
	return y,local_e
	
def main():
	x = [[-1,-1],[-1,1],[1,-1],[1,1]]
	d = [-1,1,1,1]
	#w = [[0.,0.],[0.,0.],[0.,0.],[0.,0.],[0.,0.]]
	#w = [[0.,0.],[0.,0.],[0.]]
	# w = [[0.,0.],[0.,0.,0.,0.],[0.]]
	#w = [[0.,0.],[0.]]
	w = [[0.5,0.5]]
	#w0 = np.random.uniform(-1.,1.)
	# w0 = -0.8 #and
	w0 = .3 #and
	
	# w = [[0.,0.,0.],
		 # [0.,0.,0.,0.,0.,0.],
		 # [0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.],
		 # [0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.,0.],
		 # [0.]]
	alfa = 0.3
	iterations = 10
	iter = 0
	error = []
	gradient_e = 1000.
	
	# while iter < iterations:# or gradient_e > 0.1:
		# print('w0='+str(w0))
		# print('weights='+str(w))
		
		# y,local_e = net(x,w,w0,d,step2)	
		
		# gradient_e = 0.5*np.sum(np.power(np.subtract(d,y),2))
		# error.append(gradient_e)
		# print('global gradient='+str(gradient_e))
		# print('------')
		# for i in range(len(w)):
			# for j in range(len(w[i])):
				# w[i][j] = w[i][j]-alfa*local_e[i]
		
		# w0 = w0-alfa*gradient_e
		
		# iter+=1
		
		# l = len(error)
	
		# if l > 1 and error[l-2] < error[l-1]:
			# break;
	
	print('----RESULTS----')
	for i in range(len(x)):
		y,_ = net(x[i],w,w0,d,step2,learning=False,debug=True)		
		print(x[i])
		print('result=>'+str(d[i])+'|'+str(y))
		print('------')
	
	# plt.subplot(2,2,1)
	# plt.plot(range(len(error)),error,'r')
	
	# plt.subplot(2,2,2)
	# for i in range(100):
		# x = np.random.uniform(0.,1.)
		# y = np.random.uniform(0.,1.)
		
		# yy = []
		# yy.append(node([x,y],w[0],leaky_relu))
		# yy.append(node([x,y],w[1],leaky_relu))
		# result = node(yy,w[2], leaky_relu)
		# color = 'k'
		# if result  > 0.6:
			# color = 'r'
		
		# plt.plot(x,y,'o',color=color)
	
	
	# plt.show()
	
if __name__ == '__main__':
	main()