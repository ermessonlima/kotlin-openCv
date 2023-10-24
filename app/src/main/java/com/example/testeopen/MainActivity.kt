package com.example.testeopen

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import android.Manifest
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Scalar


class MainActivity : AppCompatActivity() {

    private lateinit var btnSelect: Button
    private lateinit var btnCamera: Button
    private  lateinit var imgView: ImageView
    private lateinit var bitmap: Bitmap

      lateinit var mat: Mat

    private var SELECT_CODE  = 100
    private var CAMERA_CODE  = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSelect = findViewById(R.id.selectId)
        btnCamera = findViewById(R.id.cameraId)
        imgView = findViewById(R.id.imageId)

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV! ${OpenCVLoader.initDebug()}")
        } else {
            Log.d("OpenCV", "OpenCV loaded Successfully!")
        }

        getPermission()


        btnSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            startActivityForResult(intent, SELECT_CODE)
        }

        btnCamera.setOnClickListener {
          val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
         startActivityForResult(intent, CAMERA_CODE)
        }

    }


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == SELECT_CODE && data != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData())
                    imgView.setImageBitmap(bitmap)

                    mat = Mat()
                    Utils.bitmapToMat(bitmap, mat)

                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
                    Imgproc.adaptiveThreshold(mat, mat, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2.0)


                    val contours: MutableList<MatOfPoint> = ArrayList()
                    val hierarchy = Mat()
                    Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

                    var maxArea = -1.0
                    var maxContour: MatOfPoint? = null
                    for (contour in contours) {
                        val contour2f = MatOfPoint2f(*contour.toArray())
                        val approx = MatOfPoint2f()
                        Imgproc.approxPolyDP(contour2f, approx, Imgproc.arcLength(contour2f, true) * 0.02, true)

                        if (approx.total() == 4L && Imgproc.contourArea(contour) > maxArea) {
                            maxArea = Imgproc.contourArea(contour)
                            maxContour = MatOfPoint(*approx.toArray())
                        }
                    }

                    if (maxContour != null) {
                        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2BGR)

                        val rect = Imgproc.boundingRect(maxContour)

                        Imgproc.rectangle(mat, rect.tl(), rect.br(), Scalar(255.0, 0.0, 0.0), 4)
                    }

//                    for (contour in contours) {
//                        val color = Scalar(Math.random() * 255, Math.random() * 255, Math.random() * 255)
//                        Imgproc.drawContours(mat, listOf(contour), -1, color, 2)
//                    }


                    Utils.matToBitmap(mat, bitmap)
                    imgView.setImageBitmap(bitmap)

                } catch (e: Exception) {
                    print(e)
                }
            }


            if (requestCode == CAMERA_CODE && data != null && data.extras != null) {
                bitmap = data.extras?.get("data") as Bitmap
                imgView.setImageBitmap(bitmap)

                mat =  Mat()
                Utils.bitmapToMat(bitmap,mat)

                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)

                Imgproc.adaptiveThreshold(
                    mat, mat, 255.0,
                    Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                    Imgproc.THRESH_BINARY, 11, 2.0
                )

                Utils.matToBitmap(mat, bitmap)
                imgView.setImageBitmap(bitmap)
            }

        }

    fun getPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 102)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            102 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    getPermission()
                }
                return
            }

        }
    }





}