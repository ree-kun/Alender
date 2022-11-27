package com.example.customalarm

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customalarm.common.EditMode
import com.example.customalarm.data.db.AlarmSettingDao
import com.example.customalarm.data.db.AppDatabase
import com.example.customalarm.data.db.HolidayDao
import com.example.customalarm.data.entity.HolidayEntity
import com.example.customalarm.listcomponent.ListAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.Default)

    private lateinit var alarmSettingDao: AlarmSettingDao
    private lateinit var alarmSettingList: RecyclerView
    private lateinit var holidayDao: HolidayDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarmSettingDao = AppDatabase.getDatabase(applicationContext).alarmSettingDao()
        holidayDao = AppDatabase.getDatabase(applicationContext).holidayDao()
        alarmSettingList = generateAlarmSettingList()

        // フローティングアクションボタンの設定
        val addBtn = findViewById<FloatingActionButton>(R.id.addBtn)
        addBtn.setOnClickListener {
            val i = Intent(this, InputActivity::class.java)
            i.putExtra("editMode", EditMode.CREATE_MODE)
            this.startForResult.launch(i)
        }
    }

    override fun onStart() {
        super.onStart()

        val today = LocalDate.now()
        scope.launch {

            val apiAsyncList = listOf(
                async { getHolidaysFromApi() },
                async { holidayDao.selectAll() },
            )
            val holidaysList = apiAsyncList.awaitAll()

            val apiHolidays = holidaysList[0]
            val dbHolidays = holidaysList[1]
            val dbLastHoliday = dbHolidays.lastOrNull()

            println(dbHolidays.filter { it.date.isBefore(today) })
            holidayDao.removeHolidays(dbHolidays.filter { it.date.isBefore(today) })
            println(
                if (dbLastHoliday != null)
                    apiHolidays.filter { it.date.isAfter(today) && it.date.isAfter(dbLastHoliday.date) }
                else apiHolidays.filter { it.date.isAfter(today) })
            holidayDao.saveHolidays(
                if (dbLastHoliday != null)
                    apiHolidays.filter { it.date.isAfter(today) && it.date.isAfter(dbLastHoliday.date) }
                else apiHolidays.filter { it.date.isAfter(today) })
        }
    }

    private fun getHolidaysFromApi(): List<HolidayEntity> {
        // URLの設定
        val url = URL("https://holidays-jp.github.io/api/v1/datetime.csv")
        val connection = url.openConnection() as HttpURLConnection

        // タイムアウトを設定(ミリ秒で記述)
        connection.connectTimeout = 20_000
        connection.readTimeout = 20_000

        // リクエストメソッドを設定
        connection.requestMethod = "GET" // GETの場合は省略可能
        connection.connect() // 接続の確立

        // レスポンスを取得
        val responseStream = InputStreamReader(connection.inputStream)
        val br = BufferedReader(responseStream)

        return br.readLines()
            .map { it.split(",") }
            .map { HolidayEntity(
                LocalDateTime.ofEpochSecond(
                    it[0].toLong(), 0,
                    ZoneOffset.ofHours(9)).toLocalDate(), it[1]) }
    }

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result?.resultCode == RESULT_OK) {
            scope.launch {
                updateAlarmSettingList()
            }
        }
    }

    private fun generateAlarmSettingList(): RecyclerView {
        val alarmSettingList: RecyclerView = findViewById(R.id.alarmSetting)
        alarmSettingList.setHasFixedSize(true)
        val manager = LinearLayoutManager(this)
        manager.orientation = LinearLayoutManager.VERTICAL
        alarmSettingList.layoutManager = manager

        return alarmSettingList
    }

    override fun onResume() {
        super.onResume()
        scope.launch {
            updateAlarmSettingList()
        }
    }

    override fun onPause() {
        super.onPause()
        scope.coroutineContext.cancelChildren()
    }

    private suspend fun updateAlarmSettingList() {
        try {
            val alarmSettingEntities = alarmSettingDao.selectAll()

            withContext(Dispatchers.Main) {
                alarmSettingList.adapter = ListAdapter(alarmSettingEntities)
            }
        } catch (e: Exception) {
            // Do nothing
        }
    }
}
