import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.base.Stopwatch
import com.jamespope101.monitoring.AwsLambdaMonitor
import java.util.concurrent.TimeUnit

/**
 *    Created by jpope on 17/03/2018.
 */
fun main(args: Array<String>) {
    val mapper = jacksonObjectMapper()

    val monitor = AwsLambdaMonitor()

    val stopwatch = Stopwatch.createStarted()
    println(mapper.writeValueAsString(monitor.retrieveStatistics()))
    stopwatch.stop()
    println(String.format("Retrieved all statistics in %dms", stopwatch.elapsed(TimeUnit.MILLISECONDS)))
}