from PIL import Image

print('start')
x_center = -2.5
y_center = -1.0
step = 0.01
max_iteration = 1000
size_x = 3360
size_y = 2100

im = Image.new("RGB",(size_x,size_y))

c1 = float(3.5/size_x)
c2 = float(2.0/size_y)

for i in range(size_x):
	for j in range(size_y):
		
		x,y = ( x_center + c1*i,
                y_center + c2*j
                )
		
		a,b = (0.,0.)
		
		color_val1 = 255		
		color_val2 = 153	
		color_val3 = 255		
		
		for iteration in range(1000):
			a,b = (a**2-b**2+x,2*a*b+y)
			if(a**2+b**2 > 4.0):
				color_val1 = iteration%255
				color_val2 = iteration%153
				color_val3 = iteration%255
				break
		im.putpixel((i,j),(color_val1,color_val2,color_val3))

im.save('result.png','PNG')


