package com.example.customalarm.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.customalarm.common.Setting.Companion.TIMEZONE_DIFF_IN_MILLIS
import com.example.customalarm.data.converter.DateConverter
import com.example.customalarm.data.entity.*

@Database(entities = [AlarmSettingEntity::class, HolidayEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmSettingDao(): AlarmSettingDao
    abstract fun holidayDao(): HolidayDao

    // https://somachob.com/android-room/
    companion object {
        private const val DBNAME = "custom_alarm_db.sqlite"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DBNAME
                )
                    // TODO 祝日の取得APIが未作成のため、初期データの投入で代えている。
                    //  APIを作成次第、この処理は削除する。
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                            val sql = "INSERT INTO 'holiday' VALUES " +
                                    "(1667401200000 + ${TIMEZONE_DIFF_IN_MILLIS}, '文化の日')," +
                                    "(1669129200000 + ${TIMEZONE_DIFF_IN_MILLIS}, '勤労感謝の日')," +
                                    "(1672498800000 + ${TIMEZONE_DIFF_IN_MILLIS}, '元日')," +
                                    "(1672585200000 + ${TIMEZONE_DIFF_IN_MILLIS}, '休日')," +
                                    "(1673190000000 + ${TIMEZONE_DIFF_IN_MILLIS}, '成人の日')," +
                                    "(1676041200000 + ${TIMEZONE_DIFF_IN_MILLIS}, '建国記念の日')," +
                                    "(1677078000000 + ${TIMEZONE_DIFF_IN_MILLIS}, '天皇誕生日')," +
                                    "(1679324400000 + ${TIMEZONE_DIFF_IN_MILLIS}, '春分の日')," +
                                    "(1682694000000 + ${TIMEZONE_DIFF_IN_MILLIS}, '昭和の日')," +
                                    "(1683039600000 + ${TIMEZONE_DIFF_IN_MILLIS}, '憲法記念日')," +
                                    "(1683126000000 + ${TIMEZONE_DIFF_IN_MILLIS}, 'みどりの日')," +
                                    "(1683212400000 + ${TIMEZONE_DIFF_IN_MILLIS}, 'こどもの日')," +
                                    "(1689519600000 + ${TIMEZONE_DIFF_IN_MILLIS}, '海の日')," +
                                    "(1691679600000 + ${TIMEZONE_DIFF_IN_MILLIS}, '山の日')," +
                                    "(1694962800000 + ${TIMEZONE_DIFF_IN_MILLIS}, '敬老の日')," +
                                    "(1695394800000 + ${TIMEZONE_DIFF_IN_MILLIS}, '秋分の日')," +
                                    "(1696777200000 + ${TIMEZONE_DIFF_IN_MILLIS}, 'スポーツの日')," +
                                    "(1698937200000 + ${TIMEZONE_DIFF_IN_MILLIS}, '文化の日')," +
                                    "(1700665200000 + ${TIMEZONE_DIFF_IN_MILLIS}, '勤労感謝の日')"
                            db.execSQL(sql)
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}