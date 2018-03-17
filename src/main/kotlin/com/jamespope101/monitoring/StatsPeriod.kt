package com.jamespope101.monitoring

/**
 *    Created by jpope on 17/03/2018.
 */
enum class StatsPeriod(val numSeconds: Int) {
    HOUR(60 * 60),
    DAY(24 * 60 * 60),
    WEEK(7 * 24 * 60 * 60),
    MONTH(31 * 24 * 60 * 60); // assuming we need to be precise about days in calendar month
}