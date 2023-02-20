package com.example.barcodeReader.fragments.loginFragment


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodeReader.EnPack.error_occurred
import com.example.barcodeReader.EnPack.server_is_unreachable
import com.example.barcodeReader.databaes.SavedUsers
import com.example.barcodeReader.databaes.SavedUsersDao
import com.example.barcodeReader.databaes.User
import com.example.barcodeReader.databaes.UserDao
import com.example.barcodeReader.network.RetrofitClient
import com.example.barcodeReader.network.properties.post.login.LoginRequest
import com.example.barcodeReader.network.properties.post.login.LoginResponse
import com.example.barcodeReader.userData
import com.example.barcodeReader.utils.AlertDialogErrorMessage
import com.example.barcodeReader.utils.Observable
import com.example.barcodeReader.utils.TokenDecrypt
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException


class LoginFragmentViewModel(
    private val userDao: UserDao,
    private val savedUsersDao: SavedUsersDao
) : ViewModel() {

    var isLoginBusyLiveData = MutableLiveData(false)
    val alertDialogErrorMessageLiveData = MutableLiveData(AlertDialogErrorMessage())

    val loginBody = Observable<LoginResponse>()

    suspend fun getSavedUsersSuspend() = savedUsersDao.retUsersSuspend()

    fun login(
        username: String,
        password: String,
        token: String
    ) {
        if (isLoginBusyLiveData.value!!)
            return
        isLoginBusyLiveData.value = true

        viewModelScope.launch {
            try {
                val pair = TokenDecrypt.decrypt(token)
                val subBaseURL = pair.first
                val schema = pair.second

                val response = RetrofitClient
                    .getApiInstance(subBaseURL)
                    .login(
                        LoginRequest(
                            username,
                            password,
                            schema
                        )
                    )

                if (response.isSuccessful) {
                    loginBody.setValue(response.body())
                } else {
                    loginBody.setValue(
                        Gson().fromJson(
                            response.errorBody()!!.string(),
                            LoginResponse::class.java
                        )
                    )
                }

                if (response.code() == 200) {
                    val responseBody = response.body()!!

                    userData = User(
                        responseBody.data.metaData.employeeName,
                        username,
                        password,
                        schema,
                        responseBody.data.metaData.loginCount,
                        responseBody.data.metaData.loginLanguage,
                        responseBody.data.metaData.employeeNumber,
                        subBaseURL
                    )

                    saveRememberedUsers(
                        responseBody.data.metaData.employeeNumber,
                        username,
                        password,
                        token
                    )

                    saveUserResponse(
                        userData
                    )
                }

                alertDialogErrorMessageLiveData.value = AlertDialogErrorMessage()
            } catch (e: IOException) {
                alertDialogErrorMessageLiveData.value =
                    AlertDialogErrorMessage(true, "Login Failed", server_is_unreachable)
                println("LoginFragmentViewModel => login() => IOException: " + e.message)
            } catch (e: Exception) {
                alertDialogErrorMessageLiveData.value =
                    AlertDialogErrorMessage(
                        true,
                        "Login Failed",
                        "$error_occurred, check the token"
                    )
                println("LoginFragmentViewModel => login() => Exception: " + e.message)
            }

            isLoginBusyLiveData.value = false
        }
    }

    private suspend fun saveRememberedUsers(
        employeeNumber: String,
        username: String,
        password: String,
        token: String
    ): Boolean = runBlocking {
        try {
            savedUsersDao.insertUser(
                SavedUsers(
                    employeeNumber,
                    username,
                    password,
                    token
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun saveUserResponse(
        user: User
    ): Boolean = runBlocking {
        try {
            userDao.insertUser(user)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun clearSavedUsersData() {
        viewModelScope.launch {
            savedUsersDao.deleteUsersData()
        }
    }

    class LoginFragmentViewModelFactory(
        private val userDao: UserDao,
        private val savedUsersDao: SavedUsersDao
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginFragmentViewModel(userDao, savedUsersDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}