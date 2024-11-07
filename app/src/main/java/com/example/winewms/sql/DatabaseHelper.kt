package com.example.winewms.sql

//database file: data\data\com.examples,winewms

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
    dbVersion: Int = 1,
    openParams: SQLiteDatabase.OpenParams
) : SQLiteOpenHelper(context, dbName, dbVersion, openParams) {

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
    var columnAddressLineOne: String = "address_line_one"
    var columnAddressLineTwo: String = "address_line_two"
    var columnCity: String = "city"
    var columnState: String = "state"
    var columnPostalCode: String = "postal_code"
    var columnCountry: String = "country"

    //variables for Session Table
    var tableSession: String = "session"
    var columnSessionId: String = "session_id"
    var columnSessionStart: String = "session_start_date"
    var columnSessionEnd: String = "session_end_date"
    var columnSessionStatus: String = "session_status"

    override fun onCreate(db: SQLiteDatabase?) {

        //Create account table.
        val createAccountTable: String = ("CREATE TABLE " + tableAccount + "("
                + columnAccountId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + columnFirstName + " TEXT,"
                + columnLastName + " TEXT,"
                + columnEmail + " TEXT,"
                + columnPassword + " TEXT,"
                + columnPhone + " TEXT,"
                + columnAccountStatus + " INTEGER,"
                + columnAccountType + " INTEGER,"
                + columnAddressLineOne + " TEXT,"
                + columnAddressLineTwo + " TEXT,"
                + columnCity + " TEXT,"
                + columnState + " TEXT,"
                + columnPostalCode + " TEXT,"
                + columnCountry + " TEXT"
                + ");")
        db?.execSQL(createAccountTable)

        //Create session table
        val createSessionTable: String = ("CREATE TABLE " + tableSession + "("
                + columnSessionId + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + columnSessionStart + " TEXT,"
                + columnSessionEnd + " TEXT,"
                + columnSessionStatus + " INTEGER,"
                + columnAccountId + " INTEGER,"
                + "FOREIGN KEY (" + columnAccountId + ") REFERENCES "
                + tableAccount + "(" + columnAccountId + ")"
                + ");");
        db?.execSQL(createSessionTable)

        db?.close()
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
                val cValues = ContentValues().apply {
                    put(columnFirstName, account.firstName)
                    put(columnLastName, account.lastName)
                    put(columnEmail, account.email)
                    put(columnPassword, account.password)
                    put(columnPhone, account.phone)
                    put(columnAccountStatus, true)
                    put(columnAccountType, account.accountType)
                    put(columnAddressLineOne, account.address.addressLineOne)
                    put(columnAddressLineTwo, account.address.addressLineTwo)
                    put(columnCity, account.address.city)
                    put(columnState, account.address.state)
                    put(columnPostalCode, account.address.postalCode)
                    put(columnCountry, account.address.country)
                }
                db.insert(tableAccount, null, cValues) != -1L
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

                    val addressModel = AccountAddressModel(
                        addressLineOne = cursor.getString(8),
                        addressLineTwo = cursor.getString(9),
                        city = cursor.getString(10),
                        state = cursor.getString(11),
                        postalCode = cursor.getString(12),
                        country = cursor.getString(13)
                    )
                    AccountModel(
                        accountId = cursor.getInt(0),
                        firstName = cursor.getString(1),
                        lastName = cursor.getString(2),
                        email = cursor.getString(3),
                        password = cursor.getString(4),
                        confirmPassword = cursor.getString(4),
                        phone = cursor.getString(5),
                        accountStatus = cursor.getInt(6),
                        accountType = cursor.getInt(7),
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

    //function to return account by id
    fun getAccountById(accountId: Int): AccountModel? {
        return try {
            readableDatabase.use { db ->
                val queryString = "SELECT * FROM $tableAccount WHERE $columnAccountId = ?"
                val cursor = db.rawQuery(queryString, arrayOf(accountId.toString()))

                val account = if (cursor.moveToFirst()) {

                    val addressModel = AccountAddressModel(
                        addressLineOne = cursor.getString(8),
                        addressLineTwo = cursor.getString(9),
                        city = cursor.getString(10),
                        state = cursor.getString(11),
                        postalCode = cursor.getString(12),
                        country = cursor.getString(13)
                    )
                    AccountModel(
                        accountId = cursor.getInt(0),
                        firstName = cursor.getString(1),
                        lastName = cursor.getString(2),
                        email = cursor.getString(3),
                        password = cursor.getString(4),
                        confirmPassword = cursor.getString(4),
                        phone = cursor.getString(5),
                        accountStatus = cursor.getInt(6),
                        accountType = cursor.getInt(7),
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

    //function to update account
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
                    put(columnAccountType, account.accountType)
                    put(columnAddressLineOne, account.address.addressLineOne)
                    put(columnAddressLineTwo, account.address.addressLineTwo)
                    put(columnCity, account.address.city)
                    put(columnState, account.address.state)
                    put(columnPostalCode, account.address.postalCode)
                    put(columnCountry, account.address.country)
                }

                val selection = "$columnAccountId = ?"
                val selectionArgs = arrayOf(account.accountId.toString())

                // Execute and return result directly
                db.update(tableAccount, cValues, selection, selectionArgs) > 0
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
            false
        }
    }

    //function to start new session
    fun signOut(accountId: Int): Boolean {

        return try {
            writableDatabase.use { db ->
                val cValues = ContentValues().apply {
                    put(columnAccountStatus, false)
                }
                val selection = "$columnAccountId = ?"
                val selectionArgs = arrayOf(accountId.toString())
                // Execute and return result directly
                db.update(tableAccount, cValues, selection, selectionArgs) > 0
            }
        } catch (e: SQLiteException) {
            e.printStackTrace()
            false
        }
    }

    //function to start new session
    fun startSession(accountId: Int): Boolean {

        return try {
            writableDatabase.use { db ->
                val dateFormat = SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US)
                val cValues = ContentValues().apply {
                    put(columnSessionStart, dateFormat.format(Date()))
                    put(columnSessionStatus, true)
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

    //get latest session
    fun getLatestSession(): SessionModel? {

        return try {
            readableDatabase.use { db ->
                val queryString = "SELECT * FROM $tableSession ORDER BY $columnSessionStart DESC LIMIT 1"
                val cursor = db.rawQuery(queryString, null)

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

    //End all sessions.
    fun endSessions(): Boolean {

        return try {
            writableDatabase.use { db ->
                val dateFormat = SimpleDateFormat("HH:mm:ss MM/dd/yyyy", Locale.US)
                val cValues = ContentValues().apply {
                    put(columnSessionEnd, dateFormat.format(Date()))
                    put(columnSessionStatus, false)
                }

                val selection = "$columnSessionStatus = ?"
                val selectionArgs = arrayOf(true.toString())

                // Execute and return result directly
                db.update(tableSession, cValues, selection, selectionArgs) > 0

            }
        } catch (e: SQLiteException) {
                e.printStackTrace()
                false
            }
    }

}