package com.example.combobackup.test

import android.graphics.Rect
import android.hardware.Camera
import java.lang.IllegalArgumentException
import kotlin.concurrent.thread

abstract class ktTest {
    // Unit is Void type
    val greeting: () -> Unit = { println("Hello") }
    val greetingHasParamAndReturnValue =
        { name: String, age: String -> "Hello. My name is $name. I'm $age year old" }
    val greetingHasParamAndReturnValue2: (String, String) -> String =
        { name, age -> "Hello. My name is  $name. I'm $age year old" }
    val biggerThan5InList = listOf<Int>(5, 1, 3, 2, 7).sortedBy({ it }).filter({it > 5})

    class Address(val streetAddress: String, val zipCode: Int, val city: String, val country: String)
    class Company(val name: String, val address: Address?)
    class Person(val name: String, val company: Company?)

    fun printShippingLabel(person: Person)
    {
        val address = person.company?.address ?: throw IllegalArgumentException("No address")

        with(address)
        {
            println(streetAddress)
            println("$zipCode $city, $country")
        }

        val foo = "a" as? Int
    }

    private fun doTest()
    {
        val a = Address("why", 15, "molra", "korea")
        val c = Company("dabin", a)
        val p = Person("LWY", c)
        printShippingLabel(person = p)
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    val s = "one, two, three, four, five"
    val c = ','

    // 문자의 모든 인덱스 찾기
    fun findIndex()
    {
        // 문자열 내 "첫 번째" 문자가 나타나는 인덱스만 반환함
        var index = s.indexOf(c)
        println(index) // 3,
        
        // 문자열 내 "마지막" 발생 인덱스를 반환
        val lastIndex = s.lastIndexOf(c)
        println(lastIndex) // 16,
        
        // 문자열 내 모든 문자 인스턴스의 인덱스 검색
        while ( index != -1)
        {
            println(index)
            index = s.indexOf(c, index + 1)
        }

        // 위 while 문과 동일 코드
        while (s.indexOf(c, index + 1).also {index = it} != -1)
        {
            println(index)
        }
    }

    val pattern = "o"

    // 부분 문자열의 모든 인덱스 찾기
    fun findIndexInPart()
    {
        // "하위 문자열"의 모든 모양에 대한 인덱스 찾기
        var indices = Regex(pattern).findAll(s).map {it.range.first}.toList()
        println(indices) // [7, 18]

        // 대 소문자 구분 x
        indices = Regex(pattern, RegexOption.IGNORE_CASE).findAll(s).map {it.range.first}.toList()
        println(indices) // [0, 7, 18]
    }

    // lateinit Test를 위한 class
    // var에만 적용 가능한 lateinit, primitive type(Int, Boolean..)에는 적용 불가
    // custom getter/setter 생성 불가
    class Rectangle{
        lateinit var area : Area
        fun initArea(param: Area) : Unit{
            this.area = param
        }
    }

    
    class Area(val value:Int)
    {
        fun TestArea()
        {
            val rectangle = Rectangle()
            println(rectangle.area.value) // 초기와 안된 area를 사용하여 Exception 발생할 것임
            rectangle.initArea(Area(10))
        }
    }
    class Account(){
        val balance : Int by lazy {
            println("Setting balance!") // 처음 사용될 때 한번 호출되는 부분
            100
        }
        
        fun TestBalance()
        {
            val account = Account()
            println(account.balance) // Setting balance! 이 표시됨
            println(account.balance)
        }
    }

    class IteratorTest()
    {
        fun TestFor() {
            // for ( 요소 변수 in 컬렉션 혹은 범위 ) { 반복할 Code }

            for (x in 1..3) { print(x) } // 123
            for (i in 5 downTo 1) {print(i)} // 54321 (하행 반복)
            for (i in 1..5 step 2) {print(i)} // 135 (단계 증가)
        }

        fun TestForeach(){
            val numList = arrayListOf(1, 2, 3, 4, 5, 6)

            numList.forEach { i ->println("${i}") } // 명시적
            numList.forEach run@ { println(it) // 암시적
                if (it == 3) return@run
            }
            // foreach는 continue와 break 사용 불가, break 사용하고 싶을 떄 위와같이 return@run 사용할 것
            
            // foreach에서의 continue 사용 -> return@forEach
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////

    class ThreadTestMain()
    {
        fun test()
        {
            Thread.currentThread()

            val threadTest = ThreadTest()
            threadTest.start()

            threadTest.join()

            val runnableTest = Thread(RunnableTest())
            runnableTest.start()

            runnableTest.join()

            val lambdaRunnableTest = Thread{
                println("${Thread.currentThread()} : it`s running.")
            }

            // kotlin에서 제공하는 thread() 함수
            val threadKtTest = thread(start = false) {
                println("${Thread.currentThread()} : it`s running.")
            } // join 미 호출 시 val threadKtTest 정의부분 생략 가능
        }
    }

    // Thread 상속받아 run 함수를 구현하는 방법
    class ThreadTest : Thread(){
        public override  fun run(){
            println("${currentThread()} : it`s running.")
        }
    }

    class RunnableTest : Runnable{
        public override fun run() {
            println("${Thread.currentThread()} : it`s running.")
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    class ListTest{
        val fruits = listOf<String>("apple", "banana", "kiwi", "peach")

        fun test()
        {
            println("fruits.size:  ${fruits.size}")
            println("fruits.get(2): ${fruits.get(2)}")
            println("fruits[3]: ${fruits[3]}")
            println("fruits.indexOf(\"Peach\"): ${fruits.indexOf("peach")}")
        }
    }
}
