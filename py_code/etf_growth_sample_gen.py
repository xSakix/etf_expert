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
	dates = []
	num_rows = 0
	#nacitame data
	with open(etf_name,'r') as csvfile:
		spamreader = csv.reader(csvfile,delimiter=',')
		for row in spamreader:
			num_rows+=1
			#if int(row[0]) > 1483228800:
			data.append(float(row[1]))
			dates.append(str(row[0]))
	
	return data,num_rows, dates
	
def compute_choice(choice_min=0.,choice_max=1.,isConstant=False):
	if(isConstant):
		return choice_max
	
	return np.random.uniform(choice_min,choice_max)
	
def main():
	#ticket = 'btc_data'
	ticket = 'SPY'
	
	data,l,dates = read_etf('c:\\WORK folders\\etf_expert\\py_code\\etf_data\\'+ticket+'.csv')
			
	#zakladne vlastnosti hold, buy, sell	
	c = [0.01,0.01,0.01]		
	#grow factor pre hold, buy, sell
	#best spy
	#r_c = [3.7248023104723726, 3.7434059826819586, 3.6937761867181664]
	#best spy single investment
	r_c=[0.007496274346508,3.758124406721808,3.604305873682242]
	#best ewa
	#r_c=[3.673787568009235,3.6054382787045705,3.7991922674927654]
	#best ews
	#r_c=[3.8220189381860346,3.9584157485372193,3.622339608548732]
	#semi-best btc
	#r_c = [1044.0665024044329,3.8342336595366913,3.718616826123904]
	#semi-ga-full
	#r_c = [3.6881401636784386, 3.9533624620185392, 3.675141142971656]
	#last ga full goodd
	#r_c=[2.7132956491891904,3.7724358341968154, 4.050632771766222]	
	#r_c = [3.9615792089203103, 3.9814282553862337, 3.513512900295451]
	
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

	actions = []
	
	for i in range(20):
		#inicializuj growth factor
		compute_yorke(c,r_c,hist_c)
		#compute_may_feigenbaum(c,r_c,hist_c)
		#compute_feigenbaum(c,r_c,hist_c)
	
	print('total number of rows:'+str(l))
	print('num of data:'+str(len(data)))
	for preheat in range(l - len(data)):
		compute_yorke(c,r_c,hist_c)
	
	
	#zakladny loop pre jeden vyber parametrov
	for i in range(len(data)):
		price = data[i]
		#uloz do historie shares a hodnotu aktiv
		hist_shares.append(shares)
		hist_cash.append(cash+price*shares)

		#vypocitaj growth factor
		compute_yorke(c,r_c,hist_c)
		#compute_may_feigenbaum(c,r_c,hist_c)
		#compute_feigenbaum(c,r_c,hist_c)
		
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
			actions.append([dates[i],c[0],c[1],c[2],price/1000.,'H', price*shares+cash,shares])
			continue
		#buy akcia
		if(c[1] > choice):
			num_buy+=1
			num_shares = cash/price
			cash -= price*num_shares
			shares += num_shares
			action_performed = True
			if num_shares > 0:
				actions.append([dates[i],c[0],c[1],c[2],price/1000.,'B', price*shares+cash,shares])
			else:
				actions.append([dates[i],c[0],c[1],c[2],price/1000.,'H', price*shares+cash,shares])
		#sell akcia
		if(c[2] > choice):
			num_sell+=1
			cash += price*shares
			if shares > 0:
				actions.append([dates[i],c[0],c[1],c[2],price/1000.,'S', cash,0])
			else:
				actions.append([dates[i],c[0],c[1],c[2],price/1000.,'H', price*shares+cash,shares])
			shares=0
			action_performed = True
		
		#no-action = hold action
		if(not action_performed):
			num_hold+=1
			actions.append([dates[i],c[0],c[1],c[2],price/1000.,'H', price*shares+cash,shares])
			
	#ak je max vacsi nez limit, skonci
	print_sim_report(sum, r_c,shares,cash,data[len(data)-1])
	plot(data,hist_c,r_c,hist_cash,hist_shares,num_hold,num_buy,num_sell)
	
	with open(ticket+'_training_data.csv','w',newline='') as csvfile:
		writer = csv.writer(csvfile,delimiter=',')
		for action in actions:
			writer.writerow(action)
	
	

if __name__ == "__main__":
    main()
