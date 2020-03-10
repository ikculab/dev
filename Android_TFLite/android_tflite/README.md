# Android - Tensorflow Lite Model

## Requirements

Add dependencies and option to use tflite model in Android in build.gradle.
```java
android {
    aaptOptions {
        noCompress "tflite"
    }
}

dependencies {
    implementation fileTree(dir:'tensorflow-lite', include: ['*.aar'])
    implementation 'org.tensorflow:tensorflow-lite:+'
    implementation 'org.tensorflow:tensorflow-android:1.13.1'
}
```

We need to create assets folder. Right click on app > New > Folder > Assets Folder. Make sure that you are placing that folder into the right location. ("app/src/main/assets/")
Also you can check [Creating Assets Folder](https://abhiandroid.com/androidstudio/create-assets-folder-android-studio-html-files.html)
Then move your tflite model into assets folder.


## Embedding the model

We need to define an interpreter to use .tflite model in android as we used .tflite model in python while testing.
```java
private static final String modelFile = "your_model_name.tflite";
public Interpreter tflite;
```

As input we are giving an array in the form of (x, w, h, x). Define the input array, later we will save rgb values of the image into that array and send this array to tflite model as an input.
```java
float[][][][] input;
```

To embed the tflite model, we will call it from assets folder with this code:
```java
private MappedByteBuffer loadModelFile() throws IOException {
	AssetFileDescriptor fileDescriptor = this.getAssets().openFd(modelFile);
	FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
	FileChannel fileChannel = inputStream.getChannel();
	long startOffset = fileDescriptor.getStartOffset();
	long declaredLength = fileDescriptor.getDeclaredLength();
	return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
}
```

Then try to embed the model as interpreter. (You can try this in 'onCreate')
```java
try {
	tflite = new Interpreter(loadModelFile());
}
catch (IOException e) {
	e.printStackTrace();
}
```

Until here, everything is OK. Now we need to convert our image (image is used as bitmap) into array.
In python, numpy makes it easier for us to convert it, but how does it do that?

Take the image from gallery. We will take the rgb values in each pixel of our image and save it as array (in our case the image is 28x28 and array is 1x28x28x1).

```java
private void bitmapToInputArray() {
	int w = bitmap.getWidth();
	int h = bitmap.getHeight();

	input = new float[1][w][h][1];
	for (int x = 0; x < w; x++) {
		for (int y = 0; y < h; y++) {
			int pixel = bitmap.getPixel(y, x);
			
			r = Color.red(pixel);;
			g = Color.green(pixel);
			b = Color.blue(pixel);
			
			input[0][x][y][0] = ((r * 0.3f) + (g * 0.59f) + (b * 0.11f)) ;
		}
	}
}
```

Almost done! We have embedded our tflite model, converted our image to an array and there is one more step left; define an output array, run the interpreter and read the output.

```java
public void btnClassifyClicked(View v){
	// since we have 10 classes in our model, our output will be [1,10] array
	float[][] out = new float[1][10];

	bitmapToInputArray(); // converting bitmap to array

	tflite.run(input, out);
}
```

Since we are expecting 1x10 vector as an output, let's add these codes in btnClassifyClicked;
```java
for(int i = 0; i < 10; i++) {
	Log.e("number", String.valueOf(out[0][i]));
	if (out[0][i] <= 0.5) {
		out[0][i] = 0;
	} else {
		out[0][i] = 1;
	}
}
```
*In the output we will have the nearest value 1 out of 10 elements of the output array, I added a threshold (0.5) to make the nearest one as 1 and the others as 0*

OK. Now we had 1x10 vector as output. Let's convert those zeros and ones into our language as the order of the classes in dataset and show it in textview. Add these codes in btnClassifyClicked (result_tv is the name of the textview, don't forget to define it);

```java
int c =0;
for(int s=0; s<10; s++){
	if(out[0][s]==0){
		c = c + 1;
	}
	else{
		break;
	}
}

List<String> labels = Arrays.asList("T-shirt/top", "Trouser!", "Pullover", "Dress", "Coat", "Sandal", "Shirt", "Sneaker", "Bag", "Ankle boot");

// writing the label onto textview
result_tv.setText(labels.get(c));
```


#### Attention!
**Don't forget to move the images from dataset to the phone you are using this.**

**Don't worry if you get a wrong prediction. Try the same image in python tflite model trying script and observe the result. If the result is also the same in python, then consider the accuracy of your model training.**

