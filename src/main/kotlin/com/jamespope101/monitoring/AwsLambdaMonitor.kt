package com.jamespope101.monitoring

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder
import com.amazonaws.services.lambda.AWSLambdaClient
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Maps.newHashMap
import java.io.InputStream
import java.io.OutputStream
import java.util.stream.Collectors.toList

data class HandlerOutput(val statusCode: String, val body: String, val headers: Map<String, String>)

data class FunctionStatsExpectation(val displayName: String, val expectedInvocationCount: Int, val statsPeriod: StatsPeriod)
data class Function(val functionName: String, val statsExpectation: FunctionStatsExpectation)
data class FunctionStatistics(val function: FunctionStatsExpectation, val numInvocations: Int, val numErrors: Int)
/**
 *    Created by jpope on 17/03/2018.
 */
class AwsLambdaMonitor {

    private val functionStatsExpectationsPerCategory = ImmutableMap.builder<String, List<FunctionStatsExpectation>>()
            // fill in with your own Lambda expectations
            .put("Checks", ImmutableList.of(
                    FunctionStatsExpectation("Database-Check", 12, StatsPeriod.HOUR),
                    FunctionStatsExpectation("Service-Health-Check", 12, StatsPeriod.HOUR)))
            .put("Reports", ImmutableList.of(
                    FunctionStatsExpectation("Finance-Reports", 1, StatsPeriod.MONTH)))
            .put("Essential", ImmutableList.of(
                    FunctionStatsExpectation("Put-The-Kettle-On", 1, StatsPeriod.HOUR)))
            .build()

    fun handler(input: InputStream, output: OutputStream) {
        val mapper = jacksonObjectMapper()

        mapper.writeValue(output, HandlerOutput("200", mapper.writeValueAsString(retrieveStatistics()), ImmutableMap.of("Access-Control-Allow-Origin", "*")))
    }

    fun retrieveStatistics(): Map<String, List<FunctionStatistics>> {
        val lambdaClient = AWSLambdaClient.builder().build()
        val cloudwatch = AmazonCloudWatchAsyncClientBuilder.defaultClient()

        val functionRetriever = FunctionRetriever(lambdaClient, functionStatsExpectationsPerCategory)
        val functionStatisticsRetriever = FunctionStatisticsRetriever(cloudwatch)
        val functionsPerCategory = functionRetriever.retrieveFunctionsPerCategory()

        val functionStatsPerCategory = newHashMap<String, List<FunctionStatistics>>()
        functionsPerCategory.entries.stream()
                .forEach{e -> functionStatsPerCategory[e.key] =
                        e.value.stream().map(functionStatisticsRetriever::retrieveStatisticsForFunction).collect(toList()) }

        return functionStatsPerCategory
    }
}