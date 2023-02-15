package com.example.barcodereader.fragments.loginFragment

import AESEncryption
import AESEncryption.decrypt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodereader.databaes.SavedUsers
import com.example.barcodereader.databaes.SavedUsersDao
import com.example.barcodereader.databaes.User
import com.example.barcodereader.databaes.UserDao
import com.example.barcodereader.network.Api
import com.example.barcodereader.network.properties.post.login.LoginRequest
import com.example.barcodereader.network.properties.post.login.LoginResponse
import com.example.barcodereader.userData
import com.example.barcodereader.utils.GlobalKeys
import com.example.barcodereader.utils.Observable
import com.example.barcodereader.utils.TokenDecrypt
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
                                saveRememberedUsersDatabaseStatus.value =
                                    saveRememberedUsers(loginResponse.data.employeeNumber)
                            }
                            saveUserDatabaseStatus.value =
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

    private suspend fun saveRememberedUsers(employeeNumber: String): Boolean {
        return try {
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
            println(e.message)
            false
        }
    }

    private suspend fun saveUserResponse(
        response: LoginResponse,
        loginRequest: LoginRequest,
        subBaseURL: String
    ): Boolean {
        val user = User(
            loginRequest.userName,
            loginRequest.password,
            loginRequest.schema,
            response.data.loginCount,
            response.data.loginLanguage,
            response.data.employeeNumber,
            subBaseURL
        )

        userData = user

        return try {
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