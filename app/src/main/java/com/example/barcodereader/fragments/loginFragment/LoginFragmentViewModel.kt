package com.example.barcodereader.fragments.loginFragment


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodereader.databaes.SavedUsers
import com.example.barcodereader.databaes.SavedUsersDao
import com.example.barcodereader.databaes.User
import com.example.barcodereader.databaes.UserDao
import com.example.barcodereader.network.RetrofitClient
import com.example.barcodereader.network.properties.post.login.LoginRequest
import com.example.barcodereader.network.properties.post.login.LoginResponse
import com.example.barcodereader.userData
import com.example.barcodereader.utils.Observable
import com.example.barcodereader.utils.TokenDecrypt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Response

class LoginFragmentViewModel(
    private val userDao: UserDao,
    private val savedUsersDao: SavedUsersDao
) : ViewModel() {

    var isBusy = MutableLiveData(false)

    val connectionStatus = Observable(true)
    val saveUserDatabaseStatus = MutableLiveData(true)
    val saveRememberedUsersDatabaseStatus = MutableLiveData(true)

    val loginResponse = Observable<Response<LoginResponse>>(null)

    private lateinit var username: String
    private lateinit var password: String
    private lateinit var token: String


    suspend fun getSavedUsersSuspend() = savedUsersDao.retUsersSuspend()

    fun login(
        username: String,
        password: String,
        token: String
    ) {

        if (isBusy.value!!)
            return
        isBusy.value = true

        this.username = username
        this.password = password
        this.token = token

        viewModelScope.launch {
            try {
                val pair = TokenDecrypt.decrypt(token)
                val subBaseURL = pair.first
                val schema = pair.second
                println(subBaseURL)
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
                    loginResponse.setValue(response)
                    if (response.code() == 200) {
                        response.body()?.let { body ->
                            saveRememberedUsersDatabaseStatus.value =
                                saveRememberedUsers(body.data.employeeNumber)

                            saveUserDatabaseStatus.value =
                                saveUserResponse(
                                    body,
                                    username,
                                    password,
                                    schema,
                                    subBaseURL
                                )

                        }
                    }
                } else {
                    throw Exception("Login Error: " + response.code())
                }
                isBusy.value = false
                connectionStatus.setValue(true)
            } catch (e: CancellationException) {
                println(e.message)
            } catch (e: Exception) {
                connectionStatus.setValue(false)
                println(e.message)
            }
        }
    }

    private suspend fun saveRememberedUsers(employeeNumber: String): Boolean = runBlocking {
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
        response: LoginResponse,
        username: String,
        password: String,
        schema: String,
        subBaseURL: String
    ): Boolean = runBlocking {
        val user = User(
            response.data.employeeName,
            username,
            password,
            schema,
            response.data.loginCount,
            response.data.loginLanguage,
            response.data.employeeNumber,
            subBaseURL
        )

        userData = user

        println("Login Fragment $userData")

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
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginFragmentViewModel(userDao, savedUsersDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}