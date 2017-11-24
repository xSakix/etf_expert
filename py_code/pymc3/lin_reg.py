import numpy as np
import matplotlib.pyplot as plt
import pymc3 as pm

np.random.seed(123)
alpha, sigma = 1, 1
beta = [1, 2.5]

size = 100

X1 = np.random.randn(size)
X2 = np.random.randn(size)*0.2

Y = alpha+beta[0]*X1 + beta[1]*X2+np.random.rand(size)*sigma

fig,axes = plt.subplots(2,2,sharex=True,figsize=(10,4))
axes[0][0].scatter(X1,Y)
axes[0][1].scatter(X2,Y)
axes[0][0].set_ylabel('Y')
axes[0][0].set_xlabel('X1')
axes[0][1].set_xlabel('X2')

basic_model = pm.Model()

with basic_model:
    alpha = pm.Normal('alpha',mu=0,sd=10)
    beta=pm.Normal('beta',mu=0,sd=10,shape=2)
    sigma=pm.Normal('sigma',sd=1)
    mu = alpha+beta[0]*X1+beta[1]*X2
    Y_obs = pm.Normal('Y_obs',mu=mu,sd=sigma,observed=Y)
    # axes[1][0].scatter(X1,Y_obs)
    # axes[1][1].scatter(X2,Y_obs)
    # axes[1][0].set_ylabel('Y_obs')
    # axes[1][0].set_xlabel('X1')
    # axes[1][1].set_xlabel('X2')

plt.show()

