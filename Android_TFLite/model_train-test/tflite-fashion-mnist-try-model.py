import tensorflow as tf
import numpy as np


interpreter = tf.lite.Interpreter(model_path="model.tflite")
interpreter.allocate_tensors()

# Get input and output tensors
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

# read the image and if it is rgb convert it to gray scaled image --> dim=1
import cv2 as cv
im2 = cv.imread('fashion687.png')   # take an image from dataset, copy the image in the file where this code is located
im2 = cv.resize(im2, (28, 28))
grayIm = cv.cvtColor(im2, cv.COLOR_BGR2GRAY)

# adding 1 dimension on 0 because of having 1 sample and on 3 because of grayscale (for rgb it is 3 dim.)
arr = np.expand_dims(grayIm, axis=0)
arr = np.expand_dims(arr, axis=3)


# Test our tflite model on input data (in this case the data is the image we read)
input_data = np.array(arr, dtype=np.float32)
interpreter.set_tensor(input_details[0]['index'], input_data)
interpreter.invoke()
output_data = interpreter.get_tensor(output_details[0]['index'])

# localize the predicted class' label on the list
c = 0
for q in output_data[0]:
    if q != 1.:
        c += 1
    else:
        coef = c

outfit_list = ['T-shirt/top', 'Trouser', 'Pullover', 'Dress', 'Coat', 'Sandal', 'Shirt', 'Sneaker', 'Bag', 'Ankle boot']
print(outfit_list[coef]) # print out the prediction
