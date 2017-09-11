import matplotlib.pyplot as plt
import numpy as np
import csv

def compute_yorke(c, r_c,hist_c):
	c[0] = c[0]*r_c[0]*(1.-c[0])
	hist_c[0].append(c[0])
	c[1] = c[1]*r_c[1]*(1.-c[1])
	hist_c[1].append(c[1])
	c[2] = c[2]*r_c[2]*(1.-c[2])
	hist_c[2].append(c[2])

def compute_may_feigenbaum(c, r_c,hist_c):
	c_pow = np.power(c,2.)
	c[0] = r_c[0]*(c[0]-c_pow[0])
	hist_c[0].append(c[0])
	c[1] = r_c[1]*(c[1]-c_pow[1])
	hist_c[1].append(c[1])
	c[2] = r_c[2]*(c[2]-c_pow[2])
	hist_c[2].append(c[2])

def compute_feigenbaum(c, r_c,hist_c):
	c[0] = r_c[0]*np.sin(np.pi*c[0])
	hist_c[0].append(c[0])
	c[1] = r_c[1]*np.sin(np.pi*c[1])
	hist_c[1].append(c[1])
	c[2] = r_c[2]*np.sin(np.pi*c[2])
	hist_c[2].append(c[2])

	
def print_sim_report(sum, r_c,shares,cash,price):
	print('total investment:'+str(sum))
	print('r_hold:'+str(r_c[0]))
	print('r_buy:'+str(r_c[1]))
	print('r_sell:'+str(r_c[2]))
	print('num of shares:'+str(shares))
	print('available cash:'+str(cash))
	print('total:'+str((shares*price+cash)))

def plot(data,hist_c,r_c,hist_cash,hist_shares,num_hold,num_buy,num_sell):
	plt.figure(1)
	x = range(len(data))
	plt.subplot(3,3,1)
	line1, = plt.plot(x[0:40],hist_c[0][0:40],'r',label='hist hold')
	plt.legend([line1], ['hist hold, r='+str(r_c[0])])
	plt.subplot(3,3,2)
	line2, = plt.plot(x[0:40],hist_c[1][0:40],'g',label='hist buy')
	plt.legend([line2], [ 'hist buy, r='+str(r_c[1])])
	plt.subplot(3,3,3)
	line3, = plt.plot(x[0:40],hist_c[2][0:40],'b',label='hist sell')
	plt.legend([line3], ['hist sell, r='+str(r_c[2])])
	plt.subplot(3,3,4)
	line4, = plt.plot(x,hist_cash,'g',label='cash')
	plt.legend([line4], ['cash'])
	plt.subplot(3,3,5)
	line5, = plt.plot(x,hist_shares,'r',label='shares')
	plt.legend([line5], ['shares'])
	plt.subplot(3,3,6)
	line6, = plt.plot(x,data,'y',label='spy')
	plt.legend([line6], ['spy'])
	plt.subplot(3,3,7)
	line7 = plt.bar([1,2,3],[num_hold,num_buy,num_sell],label='hold,buy,sell operations')
	plt.legend([line7], ['hold,buy,sell operations'])
	plt.show()

def read_etf(etf_name):
	data = []

	#nacitame data
	with open('c:\\downloaded_data\\USD\\SPY.csv','r') as csvfile:
		spamreader = csv.reader(csvfile,delimiter=',')
		for row in spamreader:
			data.append(float(row[1]))
	
	return data
	
def compute_choice(choice_min=0.,choice_max=1.,isConstant=False):
	if(isConstant):
		return choice_max
	
	return np.random.uniform(choice_min,choice_max)
	
def main():
	data = read_etf('c:\\downloaded_data\\USD\\SPY.csv')

	max = 0.
			
	while(True):
			
		#zakladne vlastnosti hold, buy, sell	
		c = [0.01,0.01,0.01]		
		#grow factor pre hold, buy, sell
		#params pre may, yorke
		#r_c = [np.random.uniform(3.,4.),np.random.uniform(3.,4.),np.random.uniform(3.,4.)]
		#params pre feigenbaum
		r_c = [np.random.uniform(0.7,1.),np.random.uniform(0.7,1.),np.random.uniform(0.7,1.)]

		#zakladny cash
		cash = 300.
		#referecna suma
		sum = cash

		#pociatocny pocet share spy
		shares = 0

		#inicializacia historie vlastnosti, shares, cash a poctu operacii
		hist_c = [[] for i in range(len(data))]
		hist_shares = []
		hist_cash = []
		num_hold = 0
		num_buy=0
		num_sell=0

		for i in range(20):
			#inicializuj growth factor
			#compute_yorke(c,r_c,hist_c)
			#compute_may_feigenbaum(c,r_c,hist_c)
			compute_feigenbaum(c,r_c,hist_c)
		
		#zakladny loop pre jeden vyber parametrov
		for i in range(len(data)):
			price = data[i]
			#uloz do historie shares a hodnotu aktiv
			hist_shares.append(shares)
			hist_cash.append(cash+price*shares)

			#vypocitaj growth factor
			#compute_yorke(c,r_c,hist_c)
			#compute_may_feigenbaum(c,r_c,hist_c)
			compute_feigenbaum(c,r_c,hist_c)
			
			#kazdy mesiac (30dni) investujeme 300
			if(i % 30 == 0):
				cash+=300.
				sum+=300.
			
			#nahodny vyber
			choice = compute_choice(choice_max=0.9, isConstant=True)
			action_performed = False
			#hold akcia
			if(c[0] > choice):
				num_hold+=1
				action_performed = True
				continue
			#buy akcia
			if(c[1] > choice):
				num_buy+=1
				num_shares = np.rint(cash/price)
				cash -= price*num_shares
				shares += num_shares
				action_performed = True
			#sell akcia
			if(c[2] > choice):
				num_sell+=1
				cash += price*shares
				shares=0
				action_performed = True
			
			#no-action = hold action
			if(not action_performed):
				num_hold+=1
			
		#uloz max
		max = cash+price*shares
		
		#ak je max vacsi nez limit, skonci
		if(max > 320000.):
			print_sim_report(sum, r_c,shares,cash,data[len(data)-1])
			plot(data,hist_c,r_c,hist_cash,hist_shares,num_hold,num_buy,num_sell)
			
			break

if __name__ == "__main__":
    main()
