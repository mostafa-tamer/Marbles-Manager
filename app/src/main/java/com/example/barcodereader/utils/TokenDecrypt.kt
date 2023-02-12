package com.example.barcodereader.utils

object TokenDecrypt {
    private val map = mutableMapOf(
        'j' to '1',
        'd' to '2',
        'g' to '3',
        'h' to '4',
        'c' to '5',
        'i' to '6',
        'e' to '7',
        'f' to '8',
        'b' to '9',
        'a' to '0',
        'z' to '.'
    )

    //jbdzjifzjbjzjhdxfafcxjc
    fun decrypt(token: String): Pair<String, String> {
        val (ip, port, schema) = token.split("x")
        var decryptedSchema = String()

        var decryptedSubBaseURL = String()

        for (i in ip)
            decryptedSubBaseURL += map[i]

        decryptedSubBaseURL += ":"

        for (i in port)
            decryptedSubBaseURL += map[i]
        for (i in schema)
            decryptedSchema += map[i]
        return decryptedSubBaseURL to "exp20$decryptedSchema"
    }
}