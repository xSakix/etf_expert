import numpy as np
import matplotlib.pyplot as plt
import sys


def odd_int(low,high):
	num = np.random.randint(low,high)
	while num % 2 == 0:
		num = np.random.randint(low,high)
	
	return num

def func(x,a,b,m):
	if b > 1. or b < 0.:
		return 0.
	if a % 2 == 0:
		return 0.
	
	sum = 0.
	for i in range(m):
		sum+= np.power(b,i)*np.cos(np.power(a,i)*x*np.pi)
	
	return sum

		
def main():
	#
	m = 10
	num_of_points = 10
	a = float(odd_int(3,21))
	print('a='+str(a))
	b = np.random.uniform(0.,1.)
	print('b='+str(b))

	for i in range(100):
		p1 = 2./3.
		p2 = np.pi/(a*b-1)
		if p1 > p2:
			break
		a = float(odd_int(3,21))
		print('a='+str(a))
		b = np.random.uniform(0.,1.)
		print('b='+str(b))
		
	
	x = 0.0	
	step = 1.0
	xx =[]
	yy = []
	for i in range(num_of_points):
		x = float(i*step)
		xx.append(x)
		y = func(x,a,b,m)
		yy.append(y)
	
	diffs = np.diff(yy)
	diffs2 = np.diff(yy,n=2)
	
	# plt.figure(1)
	# plt.subplot(1,3,1)
	# plt.plot(xx,yy,'k')
	# plt.subplot(1,3,2)
	# plt.plot(xx[:len(diffs)],diffs,'r')
	# plt.subplot(1,3,3)
	# plt.plot(xx[:len(diffs2)],diffs2,'b')
	# plt.show()
	
	#choose x_0
	#x_(m+1) = a**m * x0 - alfa
	mm = 5
	x0 = 5.
	a_m = np.power(a,mm)
	step = 0.1
	alfa = a_m*x0-5
	x_n = a_m*x0
	for i in range(100):
		x_m = x_n-alfa
		if x_m > -0.5 and x_m < 0.5:
			print('alfa('+str(i)+')='+str(alfa))
			break
		alfa += step
	
	print('1+x_m: '+str(1./2.)+' < ' +str(1+x_m)+' < '+str(3./2.))
	
	#x' < x0 < x''
	x_dot = (alfa - 1.)/a_m
	x_dot2 = (alfa+1.)/a_m
	print(str(x_dot)+'<'+str(x0)+'<'+str(x_dot2))
	#(f(x') - f(x0)) / (x'-x0) = sum(0,m) (b**n * (cos(a**n * x' * pi) - cos(a**n * x * pi ))/(x'-x0))
	#lim f(x) when x->x0
	f_x_dot = func(x_dot,a,b,m)
	print('f(x\')='+str(f_x_dot))
	f_x0 = func(x0,a,b,m)
	print('f(x0)='+str(f_x0))
	difference = (f_x_dot - f_x0)/(x_dot - x0)
	print('(f(x\') - f(x0)) / (x\'-x0) = '+str(difference))
	
	f_diff1 = 0.
	for i in range(m):
		f_diff1+= np.power(b,i)*((np.cos(np.power(a,i)*x_dot*np.pi) - np.cos(np.power(a,i)*x0*np.pi))/(x_dot-x0))
	
	print('f_diff1 = '+str(f_diff1))
	
	#f_diff2 = sum(0,m-1)((a*b)**n * (cos(a**n * x' * pi) - cos(a**n * x * pi ))/a**n(x'-x0)) +
	#sum(0,m)(b**(n+m) * (cos(a**(n+m) * x' * pi) - cos(a**(n+m) * x * pi ))/(x'-x0))
	f_diff2 = 0.
	first_term = 0.
	for i in range(mm):
		f_diff2+= np.power(b*a,i)*((np.cos(np.power(a,i)*x_dot*np.pi) - np.cos(np.power(a,i)*x0*np.pi))/(np.power(a,i)*(x_dot-x0)))
		first_term+= np.power(b*a,i)
	first_term *= np.pi
	
	print('f_diff2_partial = '+str(f_diff2))
	
	f_diff3 = 0.
	for i in range(m):
		f_diff3+= np.power(b,i+mm)*((np.cos(np.power(a,i+mm)*x_dot*np.pi) - np.cos(np.power(a,i+mm)*x0*np.pi))/(x_dot-x0))
	print('f_diff3 = '+str(f_diff3))
	f_diff4 = f_diff3 + f_diff2
	print('f_dif2+f_dif3 = '+str(f_diff4))
	
	x = []
	y = []
	
	for i in range(m):
		x.append(i)
		yy = np.sin(np.power(a,i)*((x_dot-x0)/2.)*np.pi) /(np.power(a,i)*((x_dot-x0)/2.)*np.pi)
		y.append(yy)
	
	# plt.plot(x,y,'r')
	# plt.show()
	print('max(sin)='+str(np.max(y)))
	print('min(sin)='+str(np.min(y)))

	print('f_diff2_partial < first_term == '+str(f_diff2)+' < '+str(first_term))
	
	smaller_two = (np.pi*(a*b-1))*np.power(a*b,mm)
	
	print('f_diff2_partial < smaller_two == '+str(f_diff2)+' < '+str(smaller_two))
	
	for i in range(m):
		c1 = np.cos(np.power(a,mm+i)*x_dot*np.pi)
		c2 = np.cos(np.power(a,i)*(alfa-1)*np.pi)
		#nie je pravda...v teorii je, v computingu nie je
		c3 = (-1.)*(-1.)**int(alfa)
		print(str(i)+' : '+str(c1)+' = '+str(c2)+' = '+str(c3))
		
	for i in range(m):
		c1 = np.cos(np.power(a,mm+i)*x0*np.pi)
		c2 = np.cos(np.power(a,i)*alfa*np.pi + np.power(a,i)*x_m*np.pi)
		#nie je pravda...v teorii je, v computingu nie je
		c3 = ((-1.)*(-1.)**int(alfa))*np.cos(np.power(a,i)*x_m*np.pi)
		print(str(i)+' : '+str(c1)+' = '+str(c2)+' = '+str(c3))
	
	f_diff5 = 0.	
	for i in range(m):
		f_diff5+=((1+np.cos(np.power(a,i)*x_m*np.pi))/(1+x_m))*np.power(b,i)
	f_diff5 *= ((-1.)**int(alfa)) * np.power(a*b,mm)
	
	print('f_diff5='+str(f_diff5))
	
	
if __name__ == "__main__":
    main()
