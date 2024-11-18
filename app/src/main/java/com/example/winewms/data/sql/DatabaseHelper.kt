package com.example.winewms.data.sql

//database file: data\data\com.examples\database\winewms

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.winewms.session.SessionModel
import com.example.winewms.ui.account.AccountAddressModel
import com.example.winewms.ui.account.AccountModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(
    context: Context?,
    dbName: String = "wine_wms.db",
    dbVersion: Int = 2
) : SQLiteOpenHelper(context, dbName, null, dbVersion) {

    //variables for Account Table
    var tableAccount: String = "account"
    var columnAccountId: String = "acc_account_id"
    var columnFirstName: String = "acc_first_name"
    var columnLastName: String = "acc_last_name"
    var columnEmail: String = "acc_email"
    var columnPassword: String = "acc_password"
    var columnPhone: String = "acc_phone"
    var columnAccountStatus: String = "acc_status"
    val columnAccountType: String = "acc_type"
    var columnAddress: String = "address"
    var columnCity: String = "city"
    var columnProvince: String = "province"
    var columnPostalCode: String = "postal_code"
    var columnAccountCountry: String = "country"

    //variables for Session Table
    var tableSession: String = "session"
    var columnSessionId: String = "session_id"
    var columnSessionStart: String = "session_start_date"
    var columnSessionEnd: String = "session_end_date"
    var columnSessionStatus: String = "session_status"

    override fun onCreate(db: SQLiteDatabase?) {

        //Create account table.
        val createAccountTable: String = ("CREATE TABLE " + tableAccount + "("
                + columnAccountId + " TEXT PRIMARY KEY,"
                + columnFirstName + " TEXT,"
                + columnLastName + " TEXT,"
                + columnEmail + " TEXT,"
                + columnPassword + " TEXT,"
                + columnPhone + " TEXT,"
                + columnAccountStatus + " INTEGER,"
                + columnAccountType + " INTEGER,"
                + columnAddress + " TEXT,"
                + columnCity + " TEXT,"
                + columnProvince + " TEXT,"
                + columnPostalCode + " TEXT,"
                + columnAccountCountry + " TEXT"
                + ");")
        db?.execSQL(createAccountTable)

        //Create session table
        val createSessionTable: String = ("CREATE TABLE " + tableSession + "("
                + columnSessionId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + columnSessionStart + " TEXT,"
                + columnSessionEnd + " TEXT,"
                + columnSessionStatus + " INTEGER,"
                + columnAccountId + " TEXT,"
                + "FOREIGN KEY (" + columnAccountId + ") REFERENCES "
                + tableAccount + "(" + columnAccountId + ")"
                + ");");
        db?.execSQL(createSessionTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

        //Drop and recreate tables
        db?.execSQL("DROP TABLE IF EXISTS " + tableAccount)
        db?.execSQL("DROP TABLE IF EXISTS " + tableSession)
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
    }

    //function to create new account
    fun createAccount(account: AccountModel): Boolean {
        return try {
            writableDatabase.use { db ->
                val contentValues = ContentValues().apply {
                    put(columnAccountId, account.accountId)
                    put(columnFirstName, account.firstName)
                    put(columnLastName, account.lastName)
                    put(columnEmail, account.email)
                    put(columnPassword, account.password)
                    put(columnPhone, account.phone)
                    put(columnAccountStatus, account.status)
                    put(columnAccountType, account.type)

                    // Just add the address if it is not null
                    if (account.address != null) {
                        put(columnAddress, account.address.address)
                        put(columnCity, account.address.city)
                        put(columnProvince, account.address.province)
                        put(columnPostalCode, account.address.postalCode)
                        put(columnAccountCountry, account.address.country)
                    }
                }
                //insert into account table
                db.insert(tableAccount, null, contentValues) != -1L
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
            false
        }
    }

    //function to return account by email
    fun getAccount(email: String): AccountModel? {
        return try {
            readableDatabase.use { db ->
                val queryString = "SELECT * FROM $tableAccount WHERE $columnEmail = ?"
                val cursor = db.rawQuery(queryString, arrayOf(email))

                val account = if (cursor.moveToFirst()) {

                    // Handle potential null values from the cursor
                    val addressModel = AccountAddressModel(
                        address = cursor.getString(8) ?: "",
                        city = cursor.getString(9) ?: "",
                        province = cursor.getString(10) ?: "",
                        postalCode = cursor.getString(11) ?: "",
                        country = cursor.getString(12) ?: ""
                    )

                    AccountModel(
                        accountId = cursor.getString(0),
                        firstName = cursor.getString(1) ?: "",
                        lastName = cursor.getString(2) ?: "",
                        email = cursor.getString(3) ?: "",
                        password = cursor.getString(4) ?: "",
                        confirmPassword = cursor.getString(4) ?: "",
                        phone = cursor.getString(5) ?: "",
                        status = cursor.getInt(6),
                        type = cursor.getInt(7),
                        address = addressModel
                    )
                } else {
                    null
                }
                cursor.close()
                account
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
            null
        }
    }

    //Function to check if account exists.
    fun checkAccountById(accountId: String): Boolean {
        return try {
            readableDatabase.use { db ->
                val queryString = "SELECT * FROM $tableAccount WHERE $columnAccountId = ?"
                val cursor = db.rawQuery(queryString, arrayOf(accountId))

                val exists = cursor.moveToFirst() // Check if any record exists
                cursor.close()
                exists
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
            false // Return false in case of an exception
        }
    }

    // Function to update account
    fun updateAccount(account: AccountModel): Boolean {
        return try {
            writableDatabase.use { db ->
                val cValues = ContentValues().apply {
                    put(columnFirstName, account.firstName)
                    put(columnLastName, account.lastName)
                    put(columnEmail, account.email)
                    put(columnPassword, account.password)
                    put(columnPhone, account.phone)
                    put(columnAccountStatus, true)
                    put(columnAccountType, account.type)

                    // Verificar se o endereço não é nulo antes de adicionar
                    if (account.address != null) {
                        put(columnAddress, account.address.address)
                        put(columnCity, account.address.city)
                        put(columnProvince, account.address.province)
                        put(columnPostalCode, account.address.postalCode)
                        put(columnAccountCountry, account.address.country)
                    }
                }

                val selection = "$columnAccountId = ?"
                val selectionArgs = arrayOf(account.accountId)

                // Execute and return result directly
                db.update(tableAccount, cValues, selection, selectionArgs) > 0
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
            false
        }
    }

    //function to start new session
    fun signin(accountModel: AccountModel): Boolean {

        if (checkAccountById(accountModel.accountId)) {
            updateAccount(accountModel)
        }
        else {
            createAccount(accountModel)
        }
        return startSession(accountModel.accountId)
    }

    //function to start new session
    fun startSession(accountId: String): Boolean {
        return try {
            writableDatabase.use { db ->
                val dateFormat = SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US)
                val cValues = ContentValues().apply {
                    put(columnSessionStart, dateFormat.format(Date()))
                    put(columnSessionStatus, 1)
                    put(columnAccountId, accountId)
                }
                db.insert(tableSession, null, cValues) != -1L
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
            false
        }
    }

    //function to return active session
    fun getActiveSession(): SessionModel? {
        return try {
            readableDatabase.use { db ->
                val queryString = "SELECT * FROM $tableSession WHERE $columnSessionStatus = ?"
                val cursor = db.rawQuery(queryString, arrayOf(true.toString()))
                val session = if (cursor.moveToFirst()) {
                    SessionModel(
                        sessionId = cursor.getInt(0),
                        sessionStart = cursor.getString(1),
                        sessionEnd = cursor.getString(2),
                        sessionStatus = cursor.getInt(3),
                        accountId = cursor.getInt(4),
                    )
                } else {
                    null
                }
                cursor.close()
                session
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
            null
        }
    }

    //function to logout (to end session)
    fun signout(accountId: String): Boolean {
        return try {
            writableDatabase.use { db ->
                val dateFormat = SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US)
                val cValues = ContentValues().apply {
                    put(columnSessionEnd, dateFormat.format(Date()))
                    put(columnSessionStatus, 0)
                }
                val selection = "$columnAccountId = ?"
                val selectionArgs = arrayOf(accountId)
                db.update(tableSession, cValues, selection, selectionArgs) > 0
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
            false
        }
    }
}