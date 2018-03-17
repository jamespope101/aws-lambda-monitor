package com.jamespope101.monitoring

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync
import com.amazonaws.services.cloudwatch.model.Dimension
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest
import com.amazonaws.services.cloudwatch.model.Statistic
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 *    Created by jpope on 17/03/2018. Retrieves the invocation and error counts of Lambda functions via Cloudwatch
 */
class FunctionStatisticsRetriever(private val cloudwatch: AmazonCloudWatchAsync) {

    fun retrieveStatisticsForFunction(function: Function): FunctionStatistics {
        val invocationsInLastDay = getMetric(function, "Invocations")
        val errorsInLastDay = getMetric(function, "Errors")

        return FunctionStatistics(function.statsExpectation, invocationsInLastDay, errorsInLastDay)
    }

    private fun getMetric(function: Function, metricName: String): Int {
        val numSecondsToPoll = function.statsExpectation.statsPeriod.numSeconds

        val metricStatisticsRequest = GetMetricStatisticsRequest().withNamespace("AWS/Lambda")
                .withMetricName(metricName)
                .withStatistics(Statistic.Sum)
                .withDimensions(Dimension().withName("FunctionName").withValue(function.functionName))
                .withEndTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
                .withStartTime(Date.from(LocalDateTime.now().minusSeconds(numSecondsToPoll.toLong()).atZone(ZoneId.systemDefault()).toInstant()))
                .withPeriod(numSecondsToPoll)

        val metricStatisticsResult = cloudwatch.getMetricStatistics(metricStatisticsRequest)

        return if (!metricStatisticsResult.datapoints.isEmpty()) metricStatisticsResult.datapoints[0].sum.toInt() else 0
    }
}