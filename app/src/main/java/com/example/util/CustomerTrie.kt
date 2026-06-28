package com.example.util

import com.example.data.Customer

class CustomerTrieNode {
    val children = mutableMapOf<Char, CustomerTrieNode>()
    val customers = mutableSetOf<Customer>()
}

class CustomerTrie {
    private val root = CustomerTrieNode()

    fun insert(customer: Customer) {
        val terms = mutableSetOf<String>()

        // 1. Full name
        val nameLower = customer.name.lowercase().trim()
        if (nameLower.isNotEmpty()) {
            terms.add(nameLower)
            
            // 2. Individual words of name
            val nameParts = nameLower.split("\\s+".toRegex())
            for (part in nameParts) {
                if (part.isNotEmpty()) {
                    terms.add(part)
                }
            }
        }

        // 3. Mobile number
        val mobileLower = customer.mobile.lowercase().trim()
        if (mobileLower.isNotEmpty()) {
            terms.add(mobileLower)
        }

        // For each searchable term, insert it into the Trie
        for (term in terms) {
            var current = root
            for (char in term) {
                current = current.children.getOrPut(char) { CustomerTrieNode() }
                current.customers.add(customer)
            }
        }
    }

    fun search(prefix: String): List<Customer> {
        val cleanPrefix = prefix.lowercase().trim()
        if (cleanPrefix.isEmpty()) {
            return emptyList()
        }
        var current = root
        for (char in cleanPrefix) {
            current = current.children[char] ?: return emptyList()
        }
        // Return matching customers sorted by name ASC to keep original behavior consistent
        return current.customers.sortedBy { it.name.lowercase() }
    }
}
