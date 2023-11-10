package query.utils

fun IntArray.addValues(param: IntArray): IntArray {
    require(this.size == param.size)
    return IntArray(this.size) {i ->
        this[i] + param[i]
    }
}