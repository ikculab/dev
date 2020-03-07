# Tensorflow Lite Model

## Model Training and Testing .tflite Model

### Model training

Load fashion mnist dataset from tensorflow.keras.datasets

```python
fashion_mnist = tf.keras.datasets.fashion_mnist
(x_train, y_train), (x_test, y_test) = fashion_mnist.load_data()
```

Train your model using neural network architectures (In this case the model is trained with CNN architecture).

Then save your model as .h5 file.

```python
model.save('cnn-fashion.h5')
```

If any error occurs while converting .h5 file to .tflite file, in order to not waste time by training the model again, after your model is saved as .h5 comment that part;

```python
i = Input(shape=x_train[0].shape)
x = Conv2D(32, (3, 3), strides=2, activation='relu')(i)
x = Conv2D(64, (3, 3), strides=2, activation='relu')(x)
x = Conv2D(128, (3, 3), strides=2, activation='relu')(x)
x = Flatten()(x)
x = Dropout(0.2)(x)
x = Dense(512, activation='relu')(x)
x = Dropout(0.2)(x)
x = Dense(K, activation='softmax')(x)


model = Model(i, x)
model.compile(optimizer='adam', loss='sparse_categorical_crossentropy',
              metrics=['accuracy'])
r = model.fit(x_train, y_train, validation_data=(x_test, y_test), epochs=EPOCH)

model.save('cnn-fashion.h5')        # saving trained model
```

and call your model by uncommenting;
```python
# model = tf.keras.models.load_model('your_model.h5')
```

### Model converting from .h5 to .tflite

Create a converter by calling your model with TFLite Converter.
```python
converter = tf.lite.TFLiteConverter.from_keras_model_file('cnn-fashion.h5')
```

Then convert your model to .tflite model by;
```python
tflite_model = converter.convert()
tflite_model_file = pathlib.Path('./your_model_name.tflite')
tflite_model_file.write_bytes(tflite_model)
```

**.tflite model requires the weights of the model trained. Make sure that your are saving the weights in the right format, otherwise you will get an error while converting it to .tflite model.**

### Testing your .tflite model
Create another python script and import required libraries (tensorflow, numpy and opencv).

Since we are dealing with .tflite model, we need an interpreter to read the weights.
```python
interpreter = tf.lite.Interpreter(model_path="your_model_name.tflite")
interpreter.allocate_tensors()
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()
```

Take an image from dataset folder and make sure that the image is in the same folder with that python script.
We need to resize the image to the same size of the images in dataset that the model trained by.
Then we will make our image as gray scaled and conver to an array in the dimension of (1, w, h, 1) (since we have 1 sample image and 1 dimension in gray, we add rescale the array dimension).

```python
im2 = cv.imread('image_name.png')
im2 = cv.resize(im2, (28, 28))
grayIm = cv.cvtColor(im2, cv.COLOR_BGR2GRAY)
arr = np.expand_dims(grayIm, axis=0)
arr = np.expand_dims(arr, axis=3)
```

In tensorflow lite, we need to read the input as float32 type. Now, we will set tensors and get output data.
```python
input_data = np.array(arr, dtype=np.float32)
interpreter.set_tensor(input_details[0]['index'], input_data)
interpreter.invoke()
output_data = interpreter.get_tensor(output_details[0]['index'])
```
In fashion mnist dataset, there are 10 classes (['T-shirt/top', 'Trouser', 'Pullover', 'Dress', 'Coat', 'Sandal', 'Shirt', 'Sneaker', 'Bag', 'Ankle boot']). Therefore the output data will be a vector and will contain 9 zeros and 1 one (e.g. [1 0 0 0 0 0 0 0 0 0] means T-shirt/top).

Let's turn those zeros and ones into our language with just a simple code.
```python
c = 0
for q in output_data[0]:
    if q != 1.:
        c += 1
    else:
        coef = c

outfit_list = ['T-shirt/top', 'Trouser', 'Pullover', 'Dress', 'Coat', 'Sandal', 'Shirt', 'Sneaker', 'Bag', 'Ankle boot']
print(outfit_list[coef])
```

*If you have a wrong prediction at output, then check your accuracy result while training the model.*