package lionzxy.storage

import java.util.*

class ListWithLimit<T>(private val limit: Int) : LinkedList<T>() {
    override fun add(element: T): Boolean {
        val elementInsert = super.add(element)
        while (size >= limit) {
            removeFirst()
        }
        return elementInsert
    }
}