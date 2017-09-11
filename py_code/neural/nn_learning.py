#najjednoduchsie ucenie
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
			local_e.append(np.subtract(d,y))
		
		if debug:
			print('yy='+str(yy))
			
		xx = yy
	
	print('xx='+str(xx))
	
	if len(np.shape(xx)) == 1:
		xx = [xx]
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
	
def main():
	x = [[-1,-1],[-1,1],[1,-1],[1,1]]
	d = [-1,-1,-1,1]
	w = [[np.random.uniform(-1.,1.),np.random.uniform(-1.,1.)]]
	w0 = -0.8
	#w0 = -.3 #and
	
	alfa = 0.1
	iterations = 1000
	iter = 0
	error = []
	gradient_e = 1000.
	
	while iter < iterations and gradient_e > 0.1:
		print('w0='+str(w0))
		print('weights='+str(w))
		
		y,local_e = net(x,w,w0,d,step2)	
		
		print('d='+str(d))
		print('y='+str(y))
		
		gradient_e = 0.5*np.sum(np.power(np.subtract(d,y),2))
		error.append(gradient_e)
		print('global gradient='+str(gradient_e))
		print('------')
		
		#kazda vaha ma 4 vysledky?
		for i in range(len(x)):
			for layer in range(len(w)):
				for j in range(len(w[layer])):
					w[layer][j] = w[layer][j]+alfa*local_e[layer][i]*x[i][j]

		iter+=1
	
	print('----RESULTS----')
	for i in range(len(x)):
		y,_ = net(x[i],w,w0,d,step2,learning=False,debug=True)		
		print('x[i]='+str(x[i]))
		print('result=>'+str(d[i])+'|'+str(y))
		print('------')
	
	# plt.subplot(2,2,1)
	plt.plot(range(len(error)),error,'r')
	
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
	
	
	plt.show()
	
if __name__ == '__main__':
	main()