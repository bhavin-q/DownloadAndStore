package com.bhavinqf.downloadandstoreimage

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bhavinqf.downloadandstoreimage.databinding.ItemImageListBinding
import com.bhavinqf.downloadandstoreimage.fordelete.DeleteCallBack
import com.bhavinqf.downloadandstoreimage.fordelete.DeleteUtilsR
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.File

class ImageAdapter(
    var context: Activity,
    var mArrayList: ArrayList<File>,
    var deleteUtilsR: DeleteUtilsR,
    var callback: (position: Int, view: View, click: String) -> Unit
) : RecyclerView.Adapter<ImageAdapter.FileViewHolder>() {

    inner class FileViewHolder (private var binding: ItemImageListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setBinding(position: Int, item: File) {
            binding.apply {
                val fileObj = File(item.absolutePath)
                if (fileObj.exists()) {
                    Glide
                        .with(context.applicationContext)
                        .load(fileObj.absolutePath)
                        .skipMemoryCache(false)
                        .dontAnimate()
                        .dontTransform()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.baseline_error_24)
                        .into(imageView)
                } else {
                    Glide
                        .with(context.applicationContext)
                        .load(R.drawable.baseline_error_24)
                        .skipMemoryCache(false)
                        .dontAnimate()
                        .dontTransform()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView)
                }

                deleteImage.setOnClickListener {
                    deleteUtilsR.deleteImage(fileObj.path,
                        object : DeleteCallBack {
                            override fun onDeleted() {
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                mArrayList.removeAt(position)
                                notifyDataSetChanged()
                            }

                            override fun onDeleteFailed() {
                                Toast.makeText(context, "Delete Failed", Toast.LENGTH_SHORT).show()

                            }
                        }) }
                shareImage.setOnClickListener {  }

            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        FileViewHolder(ItemImageListBinding.inflate(LayoutInflater.from(context), parent, false))

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) =
        holder.setBinding(position, mArrayList[position])

    override fun onBindViewHolder(
        holder: FileViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) = holder.setBinding(position, mArrayList[position])

    override fun getItemCount() = mArrayList.size

    fun refreshData(arrayList: ArrayList<File>){
        mArrayList=arrayList
        notifyDataSetChanged() // don't use this untill you need this i'm adding because its just for example
    }

}