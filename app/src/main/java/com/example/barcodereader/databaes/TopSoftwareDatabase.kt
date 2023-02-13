package com.example.barcodereader.databaes

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.barcodereader.network.properties.get.groups.Group


@Entity
data class User(
    val userName: String,
    val password: String,
    val schema: String,
    val loginCount: Int,
    val loginLanguage: String,
    @PrimaryKey
    val employeeNumber: String,
    val subBaseURL: String
)

@Entity
data class SavedUsers(
    @PrimaryKey
    val employeeNumber: String,
    val userName: String,
    val password: String,
    val token: String
)

@Entity
data class InventoryItem(
    @PrimaryKey
    val itemCode: String,
    val itemName: String,
    val blockNumber: String,
    val amount: String,
    val number: String,
    val frz: String,
    val unit: String,
    val height: String,
    val length: String,
    val width: String
)


@Dao
interface SavedUsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(savedUsers: SavedUsers)

    @Query("select * from SavedUsers")
    fun retUsers(): LiveData<List<SavedUsers>>

    @Query("select * from SavedUsers")
    suspend fun retUsersSuspend(): List<SavedUsers>

    @Query("delete from SavedUsers")
    suspend fun deleteUsersData()
}


@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("select * from User")
    fun retUser(): LiveData<List<User>>

    @Query("select * from User")
    suspend fun retUserSuspend(): List<User>

    @Query("delete from User")
    suspend fun logout()
}

@Database(
    entities = [User::class, SavedUsers::class, InventoryItem::class, Group::class],
    version = 1,
    exportSchema = false
)
abstract class TopSoftwareDatabase : RoomDatabase() {

    abstract val userDao: UserDao
    abstract val savedUsersDao: SavedUsersDao

    companion object {

        @Volatile
        private var instance: TopSoftwareDatabase? = null

        fun getInstance(context: Context): TopSoftwareDatabase {
            synchronized(this) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TopSoftwareDatabase::class.java,
                        "top_software_database"
                    ).build()
                }
            }

            return instance!!
        }
    }
}


