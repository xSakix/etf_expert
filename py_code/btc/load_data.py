import json
from urllib import request
import csv

url = 'https://poloniex.com/public?command=returnChartData&currencyPair=USDT_BTC&start=1405699200&end=9999999999&period=86400'
r = request.urlopen(url)
print(r)
html = r.read().decode('utf-8')
result = json.loads(html)

with open('btc_data.csv','w',newline='') as csvfile:
	csvwriter = csv.writer(csvfile,delimiter=',')	
	for row in result:
		csvwriter.writerow([row['date'],row['close']])

		