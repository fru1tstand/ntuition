package me.fru1t.ntuition.controller

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.fxml.FXML
import javafx.scene.chart.BarChart
import javafx.scene.chart.XYChart
import javafx.scene.control.Tooltip
import javafx.util.Duration

class StatsController {
  private data class Data(
      val key: String,
      private val times: MutableList<Long>,
      val graphDatum: XYChart.Data<String, Long>,
      val tooltip: Tooltip) {
    fun addTime(time: Long) {
      times.add(time)
      val total = times.sum()
      graphDatum.yValue = total / times.size
      tooltip.text = "$key - ${graphDatum.yValue}"
    }
  }

  private companion object {
    fun hackTooltipTimer(tooltip: Tooltip) {
      val behaviorField = Tooltip::class.java.getDeclaredField("BEHAVIOR")
      behaviorField.isAccessible = true
      val behaviorInstance = behaviorField.get(tooltip)

      val fieldTimer = behaviorInstance.javaClass.getDeclaredField("activationTimer")
      fieldTimer.isAccessible = true
      val timeline = fieldTimer.get(behaviorInstance) as Timeline

      timeline.keyFrames.clear()
      timeline.keyFrames.add(KeyFrame(Duration.ZERO))
    }
  }

  @FXML private lateinit var graph: BarChart<String, Long>
  private val series = XYChart.Series<String, Long>()
  private val data: MutableMap<String, Data> = HashMap()

  fun onShow() {
    graph.xAxis.isAutoRanging = false
    graph.data.add(series)
  }

  fun setWords(keySet: Set<String>) {
    keySet.forEach {
      val tooltip = Tooltip()
      tooltip.text = it
      hackTooltipTimer(tooltip)
      data[it] = Data(it, arrayListOf(), XYChart.Data(it, 0L), tooltip)
    }
    series.data.setAll(data.map { it.value.graphDatum })
    data.forEach { (_, datum) -> Tooltip.install(datum.graphDatum.node, datum.tooltip) }
  }

  fun onSuccessfulInput(key: String, time: Long) {
    data[key]!!.addTime(time)
  }
}