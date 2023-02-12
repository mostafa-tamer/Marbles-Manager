package com.example.barcodereader.fragments.loginFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodereader.network.Api
import com.example.barcodereader.network.properties.post.LoginRequest
import com.example.barcodereader.network.properties.post.LoginResponse
import com.example.barcodereader.utils.AESEncryption
import com.example.barcodereader.utils.GlobalKeys
import com.example.barcodereader.utils.Observable
import com.example.barcodereader.utils.TokenDecrypt
import com.udacity.asteroidradar.database.SavedUsers
import com.udacity.asteroidradar.database.SavedUsersDao
import com.udacity.asteroidradar.database.User
import com.udacity.asteroidradar.database.UserDao
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Response

class LoginFragmentViewModel(
    private val userDao: UserDao,
    private val savedUsersDao: SavedUsersDao
) : ViewModel() {

    private var savedUsers = savedUsersDao.retUsers()
    fun getSavedUsers() = savedUsers

    val connectionStatus = Observable(true)
    val saveUserDatabaseStatus = MutableLiveData(true)
    val saveRememberedUsersDatabaseStatus = MutableLiveData(true)

    val response = Observable<Response<LoginResponse>>(null)

    private lateinit var username: String
    private lateinit var password: String
    private lateinit var token: String

    fun login(
        username: String,
        password: String,
        token: String
    ) {
        this.username = username
        this.password = password
        this.token = token

        viewModelScope.launch {
            try {

                val pair = TokenDecrypt.decrypt(token)
                val subBaseURL = pair.first
                val schema = pair.second

                val api = Api(subBaseURL)
                val loginRequest =
                    LoginRequest(
                        AESEncryption.encrypt(username, GlobalKeys.KEY),
                        AESEncryption.encrypt(password, GlobalKeys.KEY),
                        AESEncryption.encrypt(schema, GlobalKeys.KEY)
                    )

                response.setValue(api.call.login(loginRequest))

                response.getValue()?.let {
                    if (it.code() == 200) {
                        it.body()?.let { loginResponse ->
                            runBlocking {
                                saveRememberedUsers(loginResponse.data.employeeNumber)
                            }
                            saveUserResponse(loginResponse, loginRequest, subBaseURL)
                        }
                    }
                }
                connectionStatus.setValue(true)
            } catch (e: Exception) {
                connectionStatus.setValue(false)
            }
        }
    }

    private suspend fun saveRememberedUsers(employeeNumber: String) {
        try {
            savedUsersDao.insertUser(
                SavedUsers(
                    employeeNumber,
                    username,
                    password,
                    token
                )
            )

            saveRememberedUsersDatabaseStatus.value = true
        } catch (e: Exception) {
            println(e.message)
            saveRememberedUsersDatabaseStatus.value = false
        }
    }

    private suspend fun saveUserResponse(
        response: LoginResponse,
        loginRequest: LoginRequest,
        subBaseURL: String
    ) {
        try {
            userDao.insertUser(
                User(
                    loginRequest.userName,
                    loginRequest.password,
                    loginRequest.schema,
                    response.data.loginCount,
                    response.data.loginLanguage,
                    response.data.employeeNumber,
                    subBaseURL
                )
            )
            saveUserDatabaseStatus.value = true
        } catch (e: Exception) {
            saveUserDatabaseStatus.value = false
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