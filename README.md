# etf_expert
playing with ETFs, AI and artificial life

### ETFDataDatabase

- java project with domain objects to describe ETF data
- contains helper classes to find dead ETF and strange ETF. BY strange ETF i mean those with have questionable data...
- contians a loader from csv files, containing date and nav price

### ETFDataDownloader

- functionality to download data from yahoo and such


### ETF_CONFIG_PROVIDER

- returns configured directory

### ETF_EVOLUTION

- contains main classes to run evolution simulation for non-intelligent investors. Contains various scenarious. Also has GA optimizers for some scenarious for searching the "most" optimal ETF.

### ETF_EVOL_DATA_ANALYZER

- code for analyzing simualtion results from evolution

### ETF_EVOL_DATA_SERVER

- nothing for now. later should contain code to push data to server

### ETF_EVOL_MODEL

- model of evolution and GA simulation. Always some unit, which has few capabilities. Aplication of those capabilities is mostly done by individual preferences such as how much it likes to buy/sell/hold some ETF.
- tried various forms as how to discover the optimal searching of which ETF are "likeable" - from uninteligent methods to GA and chaos.

### ETF_EVOL_NEURAL_MODEL

- inteligent investor guided by NN...not yet implemented

### ETF_NAV_APPROX

- aproximation of NAV of ETF done by MLP using neuroph framewok

### ETF_NAV_APPROX_2

- aproximation using custom MLP 

### c_code

- c code for uninteligent and GA investors - for faster performance and less memory usage...but its much more hardcoded

### py_code

- py code for doing simulation with chaos investors. Preferences are grown by york equations for population growth. Using R values from 3. to 4., one can get periodic behaviore in nonperiodic systems. So one can simulate behaviore of non inteligent investors, which get swoon by emotions/irationality. Then the problem gets transformed to finding such R, that the preferences align with NAV of ETF, and investors buy when the price is low and sell when the price is high.
- uses GA to find optimal R parameters wich net the biggest amount of cash
- py code for neural networks...basic, but hard won
- py code for fractals - mandelbrot set - found somewhere on inet
- also contains varios graphs of results from variouse simulations







