package com.practice.facerecognition

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Pair
import android.util.Size
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.practice.facerecognition.databinding.ActivityMainBinding
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.lang.Float.MAX_VALUE
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.ReadOnlyBufferException
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.experimental.inv
import kotlin.math.sqrt


class MainActivity : AppCompatActivity() {
    private lateinit var vb: ActivityMainBinding

    // User data class to extract the data from JSON String
    data class User(val username: String, val password: String)

    private var detector: FaceDetector? = null
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var tfLite: Interpreter
    private lateinit var cameraSelector: CameraSelector
    private var developerMode = false
    private var distance = 1.0f
    private var start = true
    private var flipX = false
    private var context: Context = this@MainActivity
    private var camFace = CameraSelector.LENS_FACING_BACK //Default Back Camera
    private lateinit var intValues: IntArray
    private var inputSize = 112 //Input size for model
    private var isModelQuantized = false
    private lateinit var embeedings: Array<FloatArray>
    private var imageMean = 128.0f
    private var imageSTD = 128.0f
    private var outputSize = 192 //Output size of model
    private lateinit var cameraProvider: ProcessCameraProvider
    private var modelFile = "mobile_face_net.tflite" //model name
    private var registered = HashMap<String?, SimilarityClassifier.Recognition>() //saved Faces

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registered = readFromSP() //Load saved faces from memory when app starts
        setContentView(R.layout.activity_main)

        // Init View Binding
        vb = ActivityMainBinding.inflate(layoutInflater)
        val view = vb.root
        setContentView(view)

        val sharedPref = getSharedPreferences("Distance", MODE_PRIVATE)
        distance = sharedPref.getFloat("distance", 1.00f)
        vb.ivFRFacePreview.visibility = INVISIBLE

        // Getting all the registered faces from SP on Create activity
        registered.putAll(readFromSP())

        //Checking camera permission if not send request
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE)
        }

        // More more options button
        vb.btnFRSetting.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Select Action:")

            // List of options after clicking on more Options BTN
            val names = arrayOf(
                "View Recognition List",
                "Update Recognition List",
//                "Save Recognitions",
//                "Load Recognitions",
                "Clear All Recognitions",
                "Import Photo (Beta)",
//                "Hyper parameters",
//                "Developer Mode"
            )

            builder.setItems(names) { _, which ->
                when (which) {
                    0 -> displayUsersListView()
                    1 -> updateUsersList()
//                    2 -> insertToSP(registered, 0) //mode: 0:save all, 1:clear all, 2:update all
//                    3 -> registered.putAll(readFromSP())
                    2 -> clearUsersList()
                    3 -> loadPhoto()
//                    6 -> testHyperParameter()
//                    7 -> developerMode()
                }
            }
            builder.setPositiveButton("OK") { _, _ -> }
            builder.setNegativeButton("Cancel", null)

            // create and show the alert dialog
            val dialog = builder.create()
            dialog.show()
        }

        //On-screen switch to toggle between Cameras.
        vb.btnFRSwitchCamera.setOnClickListener {
            if (camFace == CameraSelector.LENS_FACING_BACK) {
                camFace = CameraSelector.LENS_FACING_FRONT
                flipX = true
            } else {
                camFace = CameraSelector.LENS_FACING_BACK
                flipX = false
            }
            cameraProvider.unbindAll()
            cameraBind()
        }

        vb.btnFRSaveFace.setOnClickListener {
            addFace()
        }

        vb.btnFRAddFace.setOnClickListener {
            if (vb.btnFRAddFace.contentDescription == "Recognize") {
                start = true
                vb.btnFRAddFace.contentDescription = "Add Face"
                vb.btnFRAddFace.setImageResource(R.drawable.add_face_icon)
                vb.ivFRFacePreview.visibility = GONE
                vb.tvFRRecognised.visibility = VISIBLE
            } else {
                vb.btnFRAddFace.contentDescription = "Recognize"
                vb.btnFRAddFace.setImageResource(R.drawable.face_id_icon)
                vb.ivFRFacePreview.visibility = VISIBLE
                vb.tvFRRecognised.visibility = GONE
            }
        }

        //Load model
        try {
            tfLite = Interpreter(loadModelFile(this@MainActivity, modelFile))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //Initialize Face Detector
        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()
        detector = FaceDetection.getClient(highAccuracyOpts)
        cameraBind()
    }

    private fun testHyperParameter() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Hyper parameter:")

        // add a checkbox list
        val names = arrayOf("Maximum Nearest Neighbour Distance")
        builder.setItems(names) { _, which ->
            when (which) {
                0 -> // Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show();
                    hyperParameters()
            }
        }
        builder.setPositiveButton("OK") { _, _ -> }
        builder.setNegativeButton("Cancel", null)

        // create and show the alert dialog
        val dialog = builder.create()
        dialog.show()
    }

    private fun developerMode() {
        if (developerMode) {
            developerMode = false
            Toast.makeText(context, "Developer Mode OFF", Toast.LENGTH_SHORT).show()
        } else {
            developerMode = true
            Toast.makeText(context, "Developer Mode ON", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addFace() {
        run {
            start = false
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Enter Details")

            // Setting up the inputs
            val username = EditText(context)
            username.hint = "Username"
            username.inputType = InputType.TYPE_CLASS_TEXT
            val password = EditText(context)
            password.hint = "Password"
            password.inputType = InputType.TYPE_CLASS_TEXT

            // Making the layout
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.addView(username)
            layout.addView(password)

            // Setting padding
            layout.setPadding(60, 60, 60, 60)

            // Setting the view
            builder.setView(layout)

            builder.setPositiveButton("ADD") { _, _ ->
                // Create and Initialize new object with Face embeddings, Username & Password.
                val result = SimilarityClassifier.Recognition(
                    "0", "", -1f
                )
                result.extra = embeedings
                val jsonObject = mapOf(
                    "username" to username.text.toString(),
                    "password" to password.text.toString()
                )

                // Converting JSON to String for storing in SP
                val jsonString = Gson().toJson(jsonObject)
                // Storing data in SP
                registered[jsonString] = result
                start = true
                insertToSP(registered, 0)
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                start = true
                dialog.cancel()
            }
            builder.show()
        }
    }

    private fun clearUsersList() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Do you want to delete all Recognitions?")
        builder.setPositiveButton("Delete All") { _, _ ->
            registered.clear()
            Toast.makeText(context, "Recognitions Cleared", Toast.LENGTH_SHORT).show()
        }
        insertToSP(registered, 1)
        builder.setNegativeButton("Cancel", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun updateUsersList() {
        val builder = AlertDialog.Builder(context)
        if (registered.isEmpty()) {
            builder.setTitle("No Faces Added!!")
            builder.setPositiveButton("OK", null)
        } else {
            builder.setTitle("Select Recognition to delete:")

            // Adding a checkbox list
            val names = arrayOfNulls<String>(registered.size)
            val users = arrayOfNulls<String>(registered.size)
            val checkedItems = BooleanArray(registered.size)
            var i = 0
            for ((key) in registered) {
                users[i] = key
                // Converting JSONString to User Object
                val user = Gson().fromJson(key, User::class.java)
                names[i] = user.username
                checkedItems[i] = false
                i += 1
            }
            builder.setMultiChoiceItems(
                names,
                checkedItems
            ) { _, which, isChecked -> // user checked or unchecked a box
                //Toast.makeText(MainActivity.this, names[which], Toast.LENGTH_SHORT).show();
                checkedItems[which] = isChecked
            }
            builder.setPositiveButton("OK") { _, _ -> // System.out.println("status:"+ Arrays.toString(checkedItems));
                for (i in checkedItems.indices) {
                    //System.out.println("status:"+checkedItems[i]);
                    if (checkedItems[i]) {
                        registered.remove(users[i])
                    }
                }
                insertToSP(registered, 2) //mode: 0:save all, 1:clear all, 2:update all
                Toast.makeText(context, "Recognitions Updated", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("Cancel", null)

            // create and show the alert dialog
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun hyperParameters() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Euclidean Distance")
        builder.setMessage("0.00 -> Perfect Match\n1.00 -> Default\nTurn On Developer Mode to find optimum value\n\nCurrent Value:")
        // Set up the input
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        builder.setView(input)
        val sharedPref = getSharedPreferences("Distance", MODE_PRIVATE)
        distance = sharedPref.getFloat("distance", 1.00f)
        input.setText(distance.toString())
        // Set up the buttons
        builder.setPositiveButton("Update") { _, _ ->
            distance = input.text.toString().toFloat()
            val sharedPref = getSharedPreferences("Distance", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putFloat("distance", distance)
            editor.apply()
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
        builder.show()
    }

    private fun displayUsersListView() {
        val builder = AlertDialog.Builder(context)
        if (registered.isEmpty()) builder.setTitle("No Users Added!!") else builder.setTitle("Recognitions:")

        // Adding a checkbox list
        val names = arrayOfNulls<String>(registered.size)
        val checkedItems = BooleanArray(registered.size)
        var i = 0
        for ((key) in registered) {
            // Converting JSONString to User Object
            val user = Gson().fromJson(key, User::class.java)
            names[i] = user.username
            checkedItems[i] = false
            i += 1
        }
        builder.setItems(names, null)
        builder.setPositiveButton("OK") { _, _ -> }

        // Creating the alert dialog
        val dialog = builder.create()
        dialog.show()
    }

    // Camera permission check
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(activity: Activity, MODEL_FILE: String): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    //Bind camera and Preview view
    private fun cameraBind() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            } catch (e: ExecutionException) {
                // No errors need to be handled for this in Future.
                // This should never be reached.
            } catch (_: InterruptedException) {
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError", "SetTextI18n")
    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(camFace)
            .build()
        preview.setSurfaceProvider(vb.cvFR.surfaceProvider)
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(640, 480))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) //Latest frame is shown
            .build()
        val executor: Executor = Executors.newSingleThreadExecutor()
        imageAnalysis.setAnalyzer(executor) { imageProxy ->
            try {
                Thread.sleep(0) //Camera preview refreshed every 10 millisecond(adjust as required)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            var image: InputImage? = null
            @SuppressLint("UnsafeExperimentalUsageError") val mediaImage// Camera Feed-->Analyzer-->ImageProxy-->mediaImage-->InputImage(needed for ML kit face detection)
                    = imageProxy.image
            if (mediaImage != null) {
                image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            }

            //Process acquired image to detect faces
            val result = detector!!.process(
                image!!
            )
                .addOnSuccessListener { faces ->
                    if (faces.size != 0) {
                        val face = faces[0] //Get first face from detected faces

                        //mediaImage to Bitmap
                        val frameBmp = toBitmap(mediaImage)
                        val rot = imageProxy.imageInfo.rotationDegrees

                        //Adjust orientation of Face
                        val frameBmp1 = rotateBitmap(frameBmp, rot, flipX = false, flipY = false)

                        //Get bounding box of face
                        val boundingBox = RectF(face.boundingBox)

                        //Crop out bounding box from whole Bitmap(image)
                        var croppedFace = getCropBitmapByCPU(frameBmp1, boundingBox)
                        if (flipX) croppedFace = rotateBitmap(croppedFace, 0, flipX, false)
                        //Scale the acquired Face to 112*112 which is required input for model
                        val scaled = getResizedBitmap(croppedFace, 112, 112)
                        if (start) recognizeImage(scaled) //Send scaled bitmap to create face embeddings.
                    } else {
                        vb.tvFRRecognised.text =
                            if (registered.isEmpty()) "Add Face" else "No Face Detected!"
                    }
                }
                .addOnFailureListener {
                    // Task failed with an exception
                    // ...
                }
                .addOnCompleteListener {
                    imageProxy.close() //v.important to acquire next frame for analysis
                }
        }
        cameraProvider.bindToLifecycle(
            (this as LifecycleOwner),
            cameraSelector,
            imageAnalysis,
            preview
        )
    }

    @SuppressLint("SetTextI18n")
    fun recognizeImage(bitmap: Bitmap) {
        // set Face to Preview
        vb.ivFRFacePreview.setImageBitmap(bitmap)

        //Create ByteBuffer to store normalized image
        val imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)
        imgData.order(ByteOrder.nativeOrder())
        intValues = IntArray(inputSize * inputSize)

        //get pixel values from Bitmap to normalize
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        imgData.rewind()
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val pixelValue = intValues[i * inputSize + j]
                if (isModelQuantized) {
                    // Quantized model
                    imgData.put((pixelValue shr 16 and 0xFF).toByte())
                    imgData.put((pixelValue shr 8 and 0xFF).toByte())
                    imgData.put((pixelValue and 0xFF).toByte())
                } else { // Float model
                    imgData.putFloat(((pixelValue shr 16 and 0xFF) - imageMean) / imageSTD)
                    imgData.putFloat(((pixelValue shr 8 and 0xFF) - imageMean) / imageSTD)
                    imgData.putFloat(((pixelValue and 0xFF) - imageMean) / imageSTD)
                }
            }
        }
        //imgData is input to our model
        val inputArray = arrayOf<Any>(imgData)
        val outputMap: MutableMap<Int, Any> = HashMap()
        embeedings =
            Array(1) { FloatArray(outputSize) } //output of model will be stored in this variable
        outputMap[0] = embeedings
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap) //Run model
        var distanceLocal = MAX_VALUE
        val id = "0"
        val label = "?"

        //Compare new face with saved Faces.
        if (registered.size > 0) {
            val nearest = findNearest(embeedings[0]) //Find 2 closest matching face
            if (nearest[0] != null) {
                val name = nearest[0]!!.first //get name and distance of closest matching face
                // label = name;
                distanceLocal = nearest[0]!!.second
                if (developerMode) {
                    if (distanceLocal < distance) //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                        vb.tvFRRecognised.text = "Nearest: $name\nDist: " + String.format(
                            "%.3f",
                            distanceLocal
                        ) + "\n2nd Nearest: " + nearest[1]!!.first + "\nDist: " + String.format(
                            "%.3f",
                            nearest[1]!!.second
                        ) else vb.tvFRRecognised.text =
                        """Unknown Dist:""".trimIndent() + String.format(
                            "%.3f",
                            distanceLocal
                        ) + "\nNearest: " + name + "\nDist: " + String.format(
                            "%.3f",
                            distanceLocal
                        ) + "\n2nd Nearest: " + nearest[1]!!.first + "\nDist: `" + String.format(
                            "%.3f",
                            nearest[1]!!.second
                        )

                } else {
                    //If distance between Closest found face is more than 1.000 ,then output UNKNOWN face.
                    // Converting JSONString to User Object
                    val user = Gson().fromJson(name, User::class.java)
                    if (distanceLocal < distance) {
                        vb.tvFRRecognised.text =
                            "Username: ${user.username}\nPassword: ${user.password}"
                    } else {
                        vb.tvFRRecognised.text = "Unknown User "
                    }
                }
            }
        }
    }

    //Compare Faces by distance between face embeddings
    private fun findNearest(emb: FloatArray): List<Pair<String?, Float>?> {
        val neighbourList: MutableList<Pair<String?, Float>?> = ArrayList()
        var ret: Pair<String?, Float>? = null //to get closest match
        var prevRet: Pair<String?, Float>? = null //to get second closest match
        for ((name, value) in registered) {
            val knownEmb = (value.extra as Array<FloatArray>)[0]
            var distance = 0f
            for (i in emb.indices) {
                val diff = emb[i] - knownEmb[i]
                distance += diff * diff
            }
            distance = sqrt(distance.toDouble()).toFloat()
            if (ret == null || distance < ret.second) {
                prevRet = ret
                ret = Pair(name, distance)
            }
        }
        if (prevRet == null) prevRet = ret
        neighbourList.add(ret)
        neighbourList.add(prevRet)
        return neighbourList
    }

    private fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }

    private fun toBitmap(image: Image?): Bitmap {
        val nv21 = YUV_420_888toNV21(image)
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image!!.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 75, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    //Save Faces to Shared Preferences.Conversion of Recognition objects to json string
    private fun insertToSP(jsonMap: HashMap<String?, SimilarityClassifier.Recognition>, mode: Int) {
        if (mode == 1) //mode: 0:save all, 1:clear all, 2:update all
            jsonMap.clear() else if (mode == 0) jsonMap.putAll(readFromSP())
        val jsonString = Gson().toJson(jsonMap)
        val sharedPreferences = getSharedPreferences("HashMap", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("map", jsonString)
        editor.apply()
        Toast.makeText(context, "Recognitions Saved", Toast.LENGTH_SHORT).show()
    }

    //Load Faces from Shared Preferences.Json String to Recognition object
    private fun readFromSP(): HashMap<String?, SimilarityClassifier.Recognition> {
        val sharedPreferences = getSharedPreferences("HashMap", MODE_PRIVATE)
        val defValue = Gson().toJson(HashMap<String, SimilarityClassifier.Recognition>())
        val json = sharedPreferences.getString("map", defValue)
        // System.out.println("Output json"+json.toString());
        val token: TypeToken<HashMap<String?, SimilarityClassifier.Recognition?>?> =
            object : TypeToken<HashMap<String?, SimilarityClassifier.Recognition?>?>() {}
        val retrievedMap =
            Gson().fromJson<HashMap<String?, SimilarityClassifier.Recognition>>(json, token.type)
        //During type conversion and save/load procedure,format changes(eg float converted to double).
        //So embeddings need to be extracted from it in required format(eg.double to float).
        for ((_, value) in retrievedMap) {
            val output = Array(1) { FloatArray(outputSize) }
            var arrayList = value.extra as ArrayList<*>
            arrayList = arrayList[0] as ArrayList<*>
            for (counter in arrayList.indices) {
                output[0][counter] = (arrayList[counter] as Double).toFloat()
            }
            value.extra = output
        }
        Toast.makeText(context, "Recognitions Loaded", Toast.LENGTH_SHORT).show()
        return retrievedMap
    }

    //Load Photo from phone storage
    private fun loadPhoto() {
        start = false
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)
    }

    //Similar Analyzing Procedure
    @Deprecated("Deprecated in Java")
    @SuppressLint("SetTextI18n")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                val selectedImageUri = data!!.data
                try {
                    val importPicture = InputImage.fromBitmap(getBitmapFromUri(selectedImageUri), 0)
                    detector!!.process(importPicture).addOnSuccessListener { faces ->
                        if (faces.size != 0) {
                            vb.btnFRAddFace.contentDescription = "Recognize"
                            vb.btnFRAddFace.setImageResource(R.drawable.face_id_icon)
                            vb.tvFRRecognised.visibility = GONE
                            vb.ivFRFacePreview.visibility = VISIBLE
                            vb.tvFRRecognised.text =
                                "1.Bring Face in view of Camera.\n\n2.Your Face preview will appear here.\n\n3.Click Add button to save face."
                            val face = faces[0]
                            //                                System.out.println(face);

                            //write code to recreate bitmap from source
                            //Write code to show bitmap to canvas
                            var frame_bmp: Bitmap? = null
                            try {
                                frame_bmp = getBitmapFromUri(selectedImageUri)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                            val frameBmp1 = rotateBitmap(frame_bmp, 0, flipX, false)

                            //ivFace.setImageBitmap(frame_bmp1);
                            val boundingBox = RectF(face.boundingBox)
                            val croppedFace = getCropBitmapByCPU(frameBmp1, boundingBox)
                            val scaled = getResizedBitmap(croppedFace, 112, 112)
                            // ivFace.setImageBitmap(scaled);
                            recognizeImage(scaled)
                            addFace()
                            //                                System.out.println(boundingBox);
                            try {
                                Thread.sleep(100)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                    }.addOnFailureListener {
                        start = true
                        Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show()
                    }
                    vb.ivFRFacePreview.setImageBitmap(getBitmapFromUri(selectedImageUri))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri?): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(
            uri!!, "r"
        )
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    companion object {
        private const val SELECT_PICTURE = 1
        private const val MY_CAMERA_REQUEST_CODE = 100
        private fun getCropBitmapByCPU(source: Bitmap?, cropRectF: RectF): Bitmap {
            val resultBitmap = Bitmap.createBitmap(
                cropRectF.width().toInt(),
                cropRectF.height().toInt(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(resultBitmap)

            // draw background
            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
            paint.color = Color.WHITE
            canvas.drawRect(
                RectF(0f, 0f, cropRectF.width(), cropRectF.height()),
                paint
            )
            val matrix = Matrix()
            matrix.postTranslate(-cropRectF.left, -cropRectF.top)
            canvas.drawBitmap(source!!, matrix, paint)
            if (!source.isRecycled) {
                source.recycle()
            }
            return resultBitmap
        }

        private fun rotateBitmap(
            bitmap: Bitmap?, rotationDegrees: Int, flipX: Boolean, flipY: Boolean
        ): Bitmap {
            val matrix = Matrix()

            // Rotate the image back to straight.
            matrix.postRotate(rotationDegrees.toFloat())

            // Mirror the image along the X or Y axis.
            matrix.postScale(if (flipX) -1.0f else 1.0f, if (flipY) -1.0f else 1.0f)
            val rotatedBitmap =
                Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)

            // Recycle the old bitmap if it has changed.
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }
            return rotatedBitmap
        }

        //IMPORTANT. If conversion not done ,the toBitmap conversion does not work on some devices.
        private fun YUV_420_888toNV21(image: Image?): ByteArray {
            val width = image!!.width
            val height = image.height
            val ySize = width * height
            val uvSize = width * height / 4
            val nv21 = ByteArray(ySize + uvSize * 2)
            val yBuffer = image.planes[0].buffer // Y
            val uBuffer = image.planes[1].buffer // U
            val vBuffer = image.planes[2].buffer // V
            var rowStride = image.planes[0].rowStride
            assert(image.planes[0].pixelStride == 1)
            var pos = 0
            if (rowStride == width) { // likely
                yBuffer[nv21, 0, ySize]
                pos += ySize
            } else {
                var yBufferPos = -rowStride.toLong() // not an actual position
                while (pos < ySize) {
                    yBufferPos += rowStride.toLong()
                    yBuffer.position(yBufferPos.toInt())
                    yBuffer[nv21, pos, width]
                    pos += width
                }
            }
            rowStride = image.planes[2].rowStride
            val pixelStride = image.planes[2].pixelStride
            assert(rowStride == image.planes[1].rowStride)
            assert(pixelStride == image.planes[1].pixelStride)
            if (pixelStride == 2 && rowStride == width && uBuffer[0] == vBuffer[1]) {
                // maybe V an U planes overlap as per NV21, which means vBuffer[1] is alias of uBuffer[0]
                val savePixel = vBuffer[1]
                try {
                    vBuffer.put(1, savePixel.inv())
                    if (uBuffer[0] == savePixel.inv()) {
                        vBuffer.put(1, savePixel)
                        vBuffer.position(0)
                        uBuffer.position(0)
                        vBuffer[nv21, ySize, 1]
                        uBuffer[nv21, ySize + 1, uBuffer.remaining()]
                        return nv21 // shortcut
                    }
                } catch (ex: ReadOnlyBufferException) {
                    // unfortunately, we cannot check if vBuffer and uBuffer overlap
                }

                // unfortunately, the check failed. We must save U and V pixel by pixel
                vBuffer.put(1, savePixel)
            }

            // other optimizations could check if (pixelStride == 1) or (pixelStride == 2),
            // but performance gain would be less significant
            for (row in 0 until height / 2) {
                for (col in 0 until width / 2) {
                    val vuPos = col * pixelStride + row * rowStride
                    nv21[pos++] = vBuffer[vuPos]
                    nv21[pos++] = uBuffer[vuPos]
                }
            }
            return nv21
        }
    }
}