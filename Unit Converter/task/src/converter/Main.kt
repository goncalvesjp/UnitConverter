package converter

import kotlin.system.exitProcess

enum class TypeUnit {
    Length, Weight, Temperature, None
}

enum class UniversalUnit(
    val shortName: String,
    var singularName: String,
    val pluralName: String,
    val multiplier: Double,
    val type: TypeUnit
) {
    Meter("m", "meter", "meters", 1.0, TypeUnit.Length),
    Kilometer("km", "kilometer", "kilometers", 1000.0, TypeUnit.Length),
    Centimeter("cm", "centimeter", "centimeters", 0.01, TypeUnit.Length),
    Millimeter("mm", "millimeter", "millimeters", 0.001, TypeUnit.Length),
    Mile("mi", "mile", "miles", 1609.35, TypeUnit.Length),
    Yard("yd", "yard", "yards", 0.9144, TypeUnit.Length),
    Foot("ft", "foot", "feet", 0.3048, TypeUnit.Length),
    Inch("in", "inch", "inches", 0.0254, TypeUnit.Length),

    Grams("g", "gram", "grams", 1.0, TypeUnit.Weight),
    Kilograms("kg", "kilogram", "kilograms", 1000.0, TypeUnit.Weight),
    Milligrams("mg", "milligram", "milligrams", 0.001, TypeUnit.Weight),
    Pounds("lb", "pound", "pounds", 453.592, TypeUnit.Weight),
    Ounces("oz", "ounce", "ounces", 28.3495, TypeUnit.Weight),

    Celsius("celsius,dc,c", "degree Celsius", "degrees Celsius", 0.0, TypeUnit.Temperature),
    Fahrenheit("fahrenheit,df,f", "degree Fahrenheit", "degrees Fahrenheit", 0.0, TypeUnit.Temperature),
    Kelvins("k", "kelvin", "kelvins", 0.0, TypeUnit.Temperature),


    None("", "", "", 0.0, TypeUnit.None)

}

fun main() {
    while (true) {
        print("Enter what you want to convert (or exit): ")
        val input = readln()

        when (input) {
            "exit" -> exitProcess(0)
            else -> {
                try {
                    val aValue = """(?<value>((-?)[0-9]+.[0-9]+)|(-?[0-9]+))"""

                    val left = """(?<inputUnit>([a-zA-Z]+)|(degree [a-zA-Z]+)|(degrees [a-zA-Z]+))"""
                    val center = """(to|in)"""
                    val right = """(?<outputUnit>([a-zA-Z]+)|(degree [a-zA-Z]+)|(degrees [a-zA-Z]+))"""

                    val regex = "($aValue $left $center $right)".toRegex()

                    val matchResult = regex.matches(input)

                    if (matchResult) {
                        val match = regex.find(input)!!
                        val value = (match.groups["value"]?.value)?.toDouble()
                        val inputUnit = match.groups["inputUnit"]?.value
                        val outputUnit = match.groups["outputUnit"]?.value


                        if (value != null && inputUnit != null && outputUnit != null) {

                            val inputUnitSelected = getUniversalUnitValue(inputUnit)
                            val outputUnitSelected = getUniversalUnitValue(outputUnit)

                            if (inputUnitSelected != UniversalUnit.None && outputUnitSelected != UniversalUnit.None && inputUnitSelected.type == outputUnitSelected.type) {
                                convert(value, inputUnit, inputUnitSelected, outputUnitSelected)
                                println()
                            } else {
                                var inputUnitStr: String
                                if (inputUnitSelected == UniversalUnit.None) {
                                    inputUnitStr = "???"
                                } else {
                                    inputUnitStr = inputUnit
                                }
                                var outputUnitStr: String
                                if (outputUnitSelected == UniversalUnit.None) {
                                    outputUnitStr = "???"
                                } else {
                                    outputUnitStr = outputUnit
                                }

                                println("Conversion from $inputUnitStr to $outputUnitStr is impossible")
                                println()
                            }
                        } else {
                            throw Exception("Parse error")
                        }
                    } else {
                        println("Parse error")
                    }
                } catch (e: Exception) {
                    println("Error : $e.cause")
                }
            }
        }
    }
}

private fun convertWeightAndLength(value: Double, inputUnit: String, outputUnit: UniversalUnit, toUnit: UniversalUnit) {
    val convertedValue = value / toUnit.multiplier * outputUnit.multiplier

    val inputUnitStr = getSingularOrPlural(value, outputUnit)
    val outputUnitStr = getSingularOrPlural(convertedValue, toUnit)

    println("$value $inputUnitStr is $convertedValue $outputUnitStr")
}

private fun getSingularOrPlural(value: Double, pUnit: UniversalUnit) = if (value == 1.0) {
    pUnit.singularName
} else {
    pUnit.pluralName
}

fun convertTemperatures(value: Double, inputUnit: String, fromUnit: UniversalUnit, toUnit: UniversalUnit) {
    var convertedValue: Double = 0.0
    when {
        fromUnit == UniversalUnit.Celsius && toUnit == UniversalUnit.Fahrenheit -> convertedValue = value * 9 / 5 + 32
        fromUnit == UniversalUnit.Fahrenheit && toUnit == UniversalUnit.Celsius -> convertedValue = (value - 32) * 5 / 9
        fromUnit == UniversalUnit.Kelvins && toUnit == UniversalUnit.Celsius -> convertedValue = value - 273.15
        fromUnit == UniversalUnit.Celsius && toUnit == UniversalUnit.Kelvins -> convertedValue = value + 273.15
        fromUnit == UniversalUnit.Fahrenheit && toUnit == UniversalUnit.Kelvins -> convertedValue =
            (value + 459.67) * 5 / 9


        fromUnit == UniversalUnit.Kelvins && toUnit == UniversalUnit.Fahrenheit -> convertedValue =
            value * 9 / 5 - 459.67

    }

    val inputUnitStr = getSingularOrPlural(value, fromUnit)
    val outputUnitStr = getSingularOrPlural(convertedValue, toUnit)

    println("$value $inputUnitStr is $convertedValue $outputUnitStr")
}

private fun convert(value: Double, inputUnit: String, fromUnit: UniversalUnit, toUnit: UniversalUnit) {
    if ((TypeUnit.Length == fromUnit.type) || (TypeUnit.Weight == fromUnit.type) &&
        (TypeUnit.Length == toUnit.type) || (TypeUnit.Weight == toUnit.type)
    ) {
        convertWeightAndLength(value, inputUnit, fromUnit, toUnit)
    } else {
        convertTemperatures(value, inputUnit, fromUnit, toUnit)
    }
}


fun getUniversalUnitValueFromShort(value: String, x: UniversalUnit): Boolean {

    val r = x.shortName.split(",").stream().filter { s -> s.lowercase().equals(value.lowercase(), true) }.findFirst()
    return r.isPresent
}

fun getUniversalUnitValue(value: String): UniversalUnit {

    val u1 = UniversalUnit.entries.stream().filter { x ->
        getUniversalUnitValueFromShort(value, x)
                || x.pluralName.equals(value.lowercase(), true)
                || x.singularName.equals(value.lowercase(), true)
    }
        .findFirst()


    return if (u1.isPresent) {
        u1.get()
    } else {
        UniversalUnit.None
    }
}


