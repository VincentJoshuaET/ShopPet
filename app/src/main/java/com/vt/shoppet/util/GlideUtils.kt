package com.vt.shoppet.util

import android.app.Activity
import android.net.Uri
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.google.firebase.storage.StorageReference
import com.vt.shoppet.R

fun Activity.loadProfileImage(imageView: ImageView, reference: StorageReference) =
    GlideApp.with(this)
        .load(reference)
        .transform(CircleCrop(), FitCenter())
        .placeholder(R.drawable.ic_person)
        .into(imageView)

fun Fragment.loadImage(imageView: ImageView, uri: Uri?) =
    GlideApp.with(this)
        .load(uri)
        .fitCenter()
        .into(imageView)

fun Fragment.loadFirebaseImage(imageView: ImageView, reference: StorageReference) =
    GlideApp.with(this)
        .load(reference)
        .fitCenter()
        .into(imageView)

fun Fragment.loadMessageImage(imageView: ImageView, reference: StorageReference) =
    GlideApp.with(this)
        .load(reference)
        .fitCenter()
        .placeholder(circularProgressLarge())
        .into(imageView)

fun Fragment.loadProfileImage(imageView: ImageView, reference: StorageReference) =
    GlideApp.with(this)
        .load(reference)
        .transform(CircleCrop(), FitCenter())
        .placeholder(R.drawable.ic_person)
        .into(imageView)

fun Fragment.setProfileImage(imageView: ImageView, uri: Uri) =
    GlideApp.with(this)
        .load(uri)
        .transform(CircleCrop(), FitCenter())
        .placeholder(R.drawable.ic_person)
        .into(imageView)