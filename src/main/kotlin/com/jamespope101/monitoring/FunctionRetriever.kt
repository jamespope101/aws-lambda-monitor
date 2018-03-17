package com.jamespope101.monitoring

import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.model.FunctionConfiguration
import com.amazonaws.services.lambda.model.ListFunctionsRequest
import com.google.common.collect.Lists
import com.google.common.collect.Lists.newArrayList
import com.google.common.collect.Maps.newHashMap

/**
 *    Created by jpope on 17/03/2018. Retrieves the function names of all Lambdas.
 */
class FunctionRetriever(private val lambda: AWSLambda, private val knownFunctionsPerCategory: Map<String, List<FunctionStatsExpectation>>) {

    private val unexpectedCategoryName = "Unexpected"

    fun retrieveFunctionsPerCategory(): Map<String, List<Function>> {

        val listFunctionsRequest = ListFunctionsRequest().withMaxItems(10000)

        val foundFunctionNames = Lists.newArrayList<String>()

        var done = false

        while(!done) {
            val pageOfFunctionNames = Lists.newArrayList<String>()

            val response = lambda.listFunctions(listFunctionsRequest)

            response.functions
                    .map{ function: FunctionConfiguration -> function.functionName }
                    .distinct()
                    .toCollection(pageOfFunctionNames)

            foundFunctionNames.addAll(pageOfFunctionNames)

            listFunctionsRequest.marker = response.nextMarker

            if (response.nextMarker == null) {
                done = true
            }
        }

        val functionsPerCategory = newHashMap<String, ArrayList<Function>>()

        foundFunctionNames.forEach { foundFunctionName ->
            val matchingCategory = knownFunctionsPerCategory.entries.stream()
                    .filter { e -> e.value.stream().anyMatch { expectation -> foundFunctionName.contains(expectation.displayName) } }
                    .map { it.key }
                    .findFirst().orElse(unexpectedCategoryName)

            val functions = functionsPerCategory.getOrDefault(matchingCategory, newArrayList())
                if (matchingCategory == unexpectedCategoryName) {
                    val noStatsExpectation = FunctionStatsExpectation(foundFunctionName, 1, StatsPeriod.MONTH)
                    functions.add(Function(foundFunctionName, noStatsExpectation))
                } else {
                    val matchingExpectation = knownFunctionsPerCategory[matchingCategory]!!.stream()
                            .filter { expectation -> foundFunctionName.contains(expectation.displayName) }.findAny().get()
                    functions.add(Function(foundFunctionName, matchingExpectation))
                }

            functionsPerCategory[matchingCategory] = functions
            }

        return functionsPerCategory

    }
}