package com.example.mycalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.RuntimeException

fun eval(str: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0
        fun nextChar() {
            ch = if (++pos < str.length) str[pos].toInt() else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.toInt()) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        // Grammar:
        // expression = term | expression `+` term | expression `-` term
        // term = factor | term `*` factor | term `/` factor
        // factor = `+` factor | `-` factor | `(` expression `)`
        //        | number | functionName factor | factor `^` factor
        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.toInt())) x += parseTerm() // addition
                else if (eat('-'.toInt())) x -= parseTerm() // subtraction
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.toInt())) x *= parseFactor() // multiplication
                else if (eat('/'.toInt())) x /= parseFactor() // division
                else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.toInt())) return parseFactor() // unary plus
            if (eat('-'.toInt())) return -parseFactor() // unary minus
            var x: Double
            val startPos = pos
            if (eat('('.toInt())) { // parentheses
                x = parseExpression()
                eat(')'.toInt())
            } else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) { // numbers
                while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else if (ch >= 'a'.toInt() && ch <= 'z'.toInt()) { // functions
                while (ch >= 'a'.toInt() && ch <= 'z'.toInt()) nextChar()
                val func = str.substring(startPos, pos)
                x = parseFactor()
                x =
                    if (func == "sqrt") Math.sqrt(x) else if (func == "sin") Math.sin(
                        Math.toRadians(
                            x
                        )
                    ) else if (func == "cos") Math.cos(
                        Math.toRadians(x)
                    ) else if (func == "tan") Math.tan(Math.toRadians(x)) else throw RuntimeException(
                        "Unknown function: $func"
                    )
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }
            if (eat('^'.toInt())) x = Math.pow(x, parseFactor()) // exponentiation
            return x
        }
    }.parse()
}


class MainActivity : AppCompatActivity() {

    var lastDigit = false
    var lastOperand = false
    var lastDecimal = false
    var allowDecimal = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onDigit(view: View){

            tvInput.append((view as Button).text)
            lastOperand = false
            lastDigit = true
            lastDecimal = false

    }

    fun onDecimal(view: View){
        if(tvInput.text.isBlank() || lastOperand || lastDecimal || allowDecimal == false){
            //do nothing
        }
        else{
            tvInput.append(".")
            lastOperand = false
            lastDigit = false
            lastDecimal = true
            allowDecimal = false

        }
    }

    fun onOperand(view: View){
        if(tvInput.text.isNotBlank() && !lastOperand){
            tvInput.append((view as Button).text)
        }
        else if(tvInput.text.isBlank() && (view as Button).text.equals("-")) {
            tvInput.append("-")
        }

        lastOperand = true
        lastDigit = false
        lastDecimal = false
        allowDecimal = true

    }

    fun onClear(view: View){
        tvInput.text = ""
        lastDigit = false
        lastOperand = false
        lastDecimal = false
        allowDecimal = true
    }

    fun onDelete(view: View){
       if(tvInput.text.isNotBlank()) {
           if(tvInput.text.endsWith(".")) allowDecimal = true
           tvInput.text = tvInput.text.substring(0, tvInput.text.length - 1)
       }
    }

    fun evaluate(view: View){
        val value = eval(tvInput.text.toString())
        tvInput.text = value.toString()
    }
}