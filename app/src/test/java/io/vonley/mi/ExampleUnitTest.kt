package io.vonley.mi

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun bitch(){
        val extractPlaystation = """\(([^()]*)\)""".toRegex()
        val extractVersion = "([0-9]+(?:\\.[0-9]+)?)".toRegex()
        val string = "Mozilla/5.0 (PlayStation 4 7.55) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.2 Safari/605.1.15"
        val matchEntire = extractPlaystation.findAll(string).flatMap { it.groupValues }.distinct().filter { it.contains("playstation", true) }.toList().firstOrNull();
        if(matchEntire != null){
            println(matchEntire)
            val version = extractVersion.findAll(matchEntire).flatMap { it.groupValues }.distinct().toList().last()
            println(version)
        }
    }
}