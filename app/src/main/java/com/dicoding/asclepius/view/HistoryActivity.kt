package com.dicoding.asclepius.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.asclepius.R
import com.dicoding.asclepius.HistoryAdapter
import com.dicoding.asclepius.history.AppDatabase
import com.dicoding.asclepius.history.PredictionHistory
import com.dicoding.asclepius.view.ResultActivity.Companion.REQUEST_HISTORY_UPDATE
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity(), HistoryAdapter.OnDeleteClickListener {

    private lateinit var predictionRecyclerView: RecyclerView
    private lateinit var predictionAdapter: HistoryAdapter
    private var predictionList: MutableList<PredictionHistory> = mutableListOf()
    private lateinit var tvNotFound: TextView

    companion object{
        const val TAG = "historydata"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        predictionRecyclerView = findViewById(R.id.rvHistory)
        tvNotFound = findViewById(R.id.tvNotFound)

        predictionRecyclerView = findViewById(R.id.rvHistory)
        tvNotFound = findViewById(R.id.tvNotFound)

        predictionAdapter = HistoryAdapter(predictionList)
        predictionAdapter.setOnDeleteClickListener(this)
        predictionRecyclerView.adapter = predictionAdapter
        predictionRecyclerView.layoutManager = LinearLayoutManager(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        GlobalScope.launch(Dispatchers.Main) {
            loadPredictionHistoryFromDatabase()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_HISTORY_UPDATE && resultCode == RESULT_OK) {
            GlobalScope.launch(Dispatchers.Main) {
                loadPredictionHistoryFromDatabase()
            }
        }
    }

    private fun loadPredictionHistoryFromDatabase() {
        GlobalScope.launch(Dispatchers.Main) {
            val predictions = AppDatabase.getDatabase(this@HistoryActivity).predictionHistoryDao().getAllPredictions()
            Log.d(TAG, "Number of predictions: ${predictions.size}")
            predictionList.clear()
            predictionList.addAll(predictions)
            predictionAdapter.notifyDataSetChanged()
            showOrHideNoHistoryText()
        }
    }

    private fun showOrHideNoHistoryText() {
        if (predictionList.isEmpty()) {
            tvNotFound.visibility = View.VISIBLE
            predictionRecyclerView.visibility = View.GONE
        } else {
            tvNotFound.visibility = View.GONE
            predictionRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDeleteClick(position: Int) {
        val prediction = predictionList[position]
        if (prediction.result.isNotEmpty()) {
            GlobalScope.launch(Dispatchers.IO) {
                AppDatabase.getDatabase(this@HistoryActivity).predictionHistoryDao().deletePrediction(prediction)
            }
            predictionList.removeAt(position)
            predictionAdapter.notifyDataSetChanged()
            showOrHideNoHistoryText()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
