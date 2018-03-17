package com.jamespope101.monitoring

import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.model.FunctionConfiguration
import com.amazonaws.services.lambda.model.ListFunctionsResult
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Test

/**
 *    Created by jpope on 17/03/2018.
 */
class FunctionRetrieverTest {

    private val expectationsPerCategory = ImmutableMap.of(
            "Drinks Prep", ImmutableList.of(
                FunctionStatsExpectation("CoffeeMaker", 25, StatsPeriod.DAY),
                FunctionStatsExpectation("CoffeeMakerClone", 25, StatsPeriod.DAY),
                FunctionStatsExpectation("TeaMaker", 30, StatsPeriod.WEEK)),
            "Morale Boost", ImmutableList.of(FunctionStatsExpectation("RoundOfApplause", 1, StatsPeriod.MONTH)))

    private val mockAwsLambda: AWSLambda = mock()
    private val functionNameRetriever = FunctionRetriever(mockAwsLambda, expectationsPerCategory)

    @Test
    fun filtersDownToTadProdFunctions() {
        whenever(mockAwsLambda.listFunctions(any())).thenReturn(ListFunctionsResult().withFunctions(
                FunctionConfiguration().withFunctionName("CoffeeMaker"),
                FunctionConfiguration().withFunctionName("TvFixer")
        ))

        val retrievedFunctions = functionNameRetriever.retrieveFunctionsPerCategory()
        assertThat(retrievedFunctions)
                .containsOnlyKeys("Drinks Prep", "Unexpected") // categorise found functions, put into 'Unexpected' if not matching existing category
                .doesNotContainKey("Morale Boost") // since no functions for that category were retrieved

        assertThat(retrievedFunctions["Drinks Prep"])
                .extracting("functionName", "statsExpectation.displayName", "statsExpectation.expectedInvocationCount", "statsExpectation.statsPeriod")
                .containsExactly(
                        tuple("CoffeeMaker", "CoffeeMaker", 25, StatsPeriod.DAY))

        assertThat(retrievedFunctions["Unexpected"]).extracting("functionName", "statsExpectation.displayName", "statsExpectation.expectedInvocationCount", "statsExpectation.statsPeriod")
                .containsExactly(tuple("TvFixer", "TvFixer", 1, StatsPeriod.MONTH))
    }
}