#mlp backprop
import numpy as np
import matplotlib.pyplot as plt
import csv

def sigmoid(x):
	return 1./(1.+np.exp(-x))

class Node:

	def __init__(self,isinput=False):
		self.out =0.
		self.error=0.
		self.x = None
		self.isinput = isinput

def forward(x,w,N):
	number_of_layers = len(N)
	for layer in range(number_of_layers):
		# print('-------Layer:'+str(layer)+'---------------')
		number_of_nodes_current_layer = len(N[layer])
		for output_node_index in range(number_of_nodes_current_layer):
			if layer == 0:
				N[layer][output_node_index].out = x[output_node_index]
			else:
				sum =0.
				number_of_nodes_prev_layer = len(N[layer-1])
				for input_node_index in range(number_of_nodes_prev_layer):
					weight = w[layer][output_node_index][input_node_index]
					input = N[layer-1][input_node_index].out
					sum += weight* input
					# print('Summing:w['+str(layer)+']['+str(output_node_index)+']['+str(input_node_index)+']*N['+str(layer-1)+']['+str(input_node_index)+'].out')
				N[layer][output_node_index].out = sigmoid(sum)
			# print('N['+str(layer)+']['+str(output_node_index)+'].out='+str(N[layer][output_node_index].out))		

def backward(N,t,w):
	number_of_layers = len(N)
	for layer in range(number_of_layers-1,0,-1):
		# print('-------Layer backtracking:'+str(layer)+'---------------')
		number_of_nodes_current_layer = len(N[layer])
		for output_node_index in range(number_of_nodes_current_layer):
			y = N[layer][output_node_index].out
			p1 = y*(1-y)
			# print('p1=N['+str(layer)+']['+str(output_node_index)+'].out*(1-N['+str(layer)+']['+str(output_node_index)+'].out)')
			if layer == len(N)-1:
				N[layer][output_node_index].error = p1*(t-y)
				# print('N['+str(layer)+']['+str(output_node_index)+'].error = p1*(t['+str(index_input)+']-N['+str(layer)+']['+str(output_node_index)+'].out)')
			else:
				sum=0.
				for input_node_index in range(len(N[layer+1])):
					for output_node_index in range(len(N[layer])):
						wkh = w[layer+1][input_node_index][output_node_index]
						deltah = N[layer+1][input_node_index].error
						sum += wkh*deltah
						# print('sum += w['+str(layer+1)+']['+str(input_node_index)+']['+str(output_node_index)+']*N['+str(layer+1)+']['+str(input_node_index)+'].error')
				N[layer][output_node_index].error = p1*sum
				# print('N['+str(layer)+']['+str(output_node_index)+'].error = p1*sum')


def recompute_weights(N,w,alfa):
	for layer in range(len(N)):
		# print('-------Layer recomputing weights:'+str(layer)+'---------------')
		if layer == 0:
			continue
		for output_node_index in range(len(N[layer])):
			for input_node_index in range(len(N[layer-1])):
				delta_w = alfa*N[layer][output_node_index].error*N[layer-1][input_node_index].out
				w[layer][output_node_index][input_node_index] += delta_w
				# print('delta_w = alfa*N['+str(layer)+']['+str(output_node_index)+'].error*N['+str(layer-1)+']['+str(input_node_index)+'].out')
				# print('w['+str(layer)+']['+str(output_node_index)+']['+str(input_node_index)+'] += delta_w')	
				
				
def init_weights(num_in, list_num_hid, num_out):
	w = []
	w.append([])
	
	w_min = -0.5
	w_max = 0.5
	
	num = num_in
	for k_hid in range(len(list_num_hid)):
		w_hid = []
		for k in range(list_num_hid[k_hid]):
			w_hid.append(np.random.uniform(w_min,w_max,num))
		w.append(w_hid)
		num = list_num_hid[k_hid]
	
	w_out=[]
	for k in range(num_out):
		w_out.append(np.random.uniform(w_min,w_max,list_num_hid[-1]))
	w.append(w_out)
	
	return w

def init_nodes(num_in, list_num_hid, num_out):
	N =[]
	N_in = []
	for i in range(num_in):
		N_in.append(Node(True))
	N.append(N_in)
	
	for k in range(len(list_num_hid)):
		N_hidden = []
		for i in range(list_num_hid[k]):
			N_hidden.append(Node())
		N.append(N_hidden)
	
	N_out = []
	for i in range(num_out):
		N_out.append(Node())	
	N.append(N_out)
	
	return N
	
def stop_condition(G_E,it):
	return (len(G_E) > 2 and G_E[len(G_E)-2] < G_E[len(G_E)-1]) or (G_E[len(G_E)-1] < 0.001) or (it > 100000)

def main():
	x = [[0.,0.],[0.,1.],[1.,0.],[1.,1.]]
	t = [0.,1.,1.,1.]
		
	w = init_weights(2,[2,4],1)		
	N = init_nodes(2,[2,4],1)
	
	alfa=0.1
		
	G_E = []
	
	it = 0
	while True:
		E = 0.
		#takze indexi su [layer][output_node_index][input_node_index]
		
		for index_input in range(len(x)):
			
			forward(x[index_input],w,N)
			backward(N,t[index_input],w)
			recompute_weights(N,w,alfa)
			
			E += np.power(t[index_input] - N[len(N)-1][0].out,2)	
		
		E = 0.5*E
		
		G_E.append(E)
			
		it = it +1
		
		if stop_condition(G_E,it):
			break
	
	print('G_E = '+str(G_E[-1]))
	# print(w)
	print('Iterations='+str(it))
	
	for i in range(len(x)):	
		
		forward(x[i],w,N)
		
		print('RESULTS:')
		print(str(t[i])+'->'+str(N[len(N)-1][0].out))
		
	plt.plot(range(len(G_E)),G_E,'r')
	plt.show()
	
if __name__ == '__main__':
	main()

	