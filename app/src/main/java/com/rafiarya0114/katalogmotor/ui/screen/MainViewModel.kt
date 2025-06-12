package com.rafiarya0114.katalogmotor.ui.screen

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafiarya0114.katalogmotor.model.Katalog
import com.rafiarya0114.katalogmotor.network.ApiStatus
import com.rafiarya0114.katalogmotor.network.KatalogApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class MainViewModel: ViewModel() {
    var data = mutableStateOf(emptyList<Katalog>())
        private set
    var status = MutableStateFlow(ApiStatus.LOADING)
        private set
    var errorMessage = mutableStateOf<String?>(null)
        private set
    var deleteStatus = mutableStateOf<String?>(null)
        private set

    fun retrieveData(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                data.value = KatalogApi.service.getKatalog(token)
                status.value = ApiStatus.SUCCESS
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                status.value = ApiStatus.FAILED
            }
        }
    }

    fun saveData(token: String, judul: String, manufacturer: String, harga: Double, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = KatalogApi.service.postKatalog(
                    token,
                    judul.toRequestBody("text/plain".toMediaTypeOrNull()),
                    manufacturer.toRequestBody("text/plain".toMediaTypeOrNull()),
                    harga,
                    bitmap.toMultipartBody()
                )

                if (result.status == "success")
                    retrieveData(token)
                else
                    throw Exception(result.message)
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun saveData(token: String, idKatalog: Long, judul: String, manufacturer: String, harga: Double, bitmap: Bitmap?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imagePart = bitmap?.toMultipartBody()
                val result = KatalogApi.service.updateKatalog(
                    token,
                    "PUT".toRequestBody("text/plain".toMediaTypeOrNull()),
                    idKatalog,
                    judul.toRequestBody("text/plain".toMediaTypeOrNull()),
                    manufacturer.toRequestBody("text/plain".toMediaTypeOrNull()),
                    harga,
                    imagePart
                )

                if (result.status == "success")
                    retrieveData(token)
                else
                    throw Exception(result.message)
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteData(token: String, katalogId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = KatalogApi.service.deleteKatalog(token, katalogId)
                if (result.status == "success") {
                    retrieveData(token)
                } else {
                    deleteStatus.value = result.message ?: "Gagal menghapus data"
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Delete failure: ${e.message}")
                deleteStatus.value = "Terjadi kesalahan: ${e.message}"
            }
        }
    }

    fun clearDeleteStatus() {
        deleteStatus.value = null
    }

    private fun Bitmap.toMultipartBody(): MultipartBody.Part {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody(
            "image/jpg".toMediaTypeOrNull(), 0, byteArray.size)
        return MultipartBody.Part.createFormData(
            "image", "image.jpg", requestBody)
    }

    fun clearMessage() {
        errorMessage.value = null
        }
}