package com.example.turapp.fragments

import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.Images.Media._ID
import android.provider.MediaStore.MediaColumns.DATE_ADDED
import android.provider.MediaStore.MediaColumns.RELATIVE_PATH
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.turapp.databinding.FragmentSavePictureBinding
import com.example.turapp.roomDb.TypeOfPoint
import com.example.turapp.viewmodels.PointOfInterestViewModel
import com.example.turapp.viewmodels.SavePictureViewModel

class SavePictureFragment : Fragment() {

    private lateinit var binding: FragmentSavePictureBinding

    private var picturePath: String? = null

    private lateinit var viewModel: SavePictureViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            picturePath = it.getString("picturePath")
        }
        if (picturePath != null) {
            val app = requireNotNull(activity).application
            viewModel = ViewModelProvider(this, SavePictureViewModel.Factory(app,
                picturePath!!))[SavePictureViewModel::class.java]
        } else
            findNavController().popBackStack()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSavePictureBinding.inflate(inflater)



        binding.imageSavePicture.setOnClickListener{
            viewModel.setHappyWithPicture()
        }

        viewModel.happyWithPicture.observe(viewLifecycleOwner,Observer{
            if(it == true) {
                binding.llShowTextInput.visibility = View.VISIBLE
            }
        })

        // Use the Kotlin extension in the fragment-ktx artifact
//        setFragmentResultListener("requestKey") { requestKey, bundle ->
//            // We use a String here, but any type that can be put in a Bundle is supported
//            binding.thePath.text = bundle.getString("bundleKey")
//        }

//        val projection = arrayOf(
//            //MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
//            MediaStore.Images.Media.DISPLAY_NAME,
//            MediaStore.MediaColumns.MIME_TYPE, "image/jpeg",
//            MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
//        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
//        val selectionArgs = arrayOf("DCIM/Test%") // Test was my folder name
//        val sortOrder = "$DATE_ADDED DESC"
//
//        context?.contentResolver?.query(
//            EXTERNAL_CONTENT_URI, //MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//            projection,
//            selection,
//            selectionArgs,
//            sortOrder
//        )?.use {
//            val id = it.getColumnIndexOrThrow(_ID)
//            //val bucket = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
//            val bucket = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
//            val date = it.getColumnIndexOrThrow(DATE_ADDED)
//            val path = it.getColumnIndexOrThrow(RELATIVE_PATH)
//            while (it.moveToNext()) {
//                // Iterate the cursor
//            }
//        }

        // se også: https://developer.android.com/training/data-storage/shared/photopicker#select-single-item

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}