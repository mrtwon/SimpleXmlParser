package com.github.mrtwon.library

import okhttp3.OkHttpClient

class XmlParseBuilder {
    private val xmlParse = XmlParse()

    fun addBaseUrl(url: String): XmlParseBuilder {
        xmlParse.baseUrl = url
        return this
    }

    fun addOkHttpClient(client: OkHttpClient): XmlParseBuilder {
        xmlParse.okHttpClient = client
        return this
    }

    fun addExpansion(bool: Boolean): XmlParseBuilder {
        xmlParse.addExpansion = bool
        return this
    }

    fun addIgnoreList(list: List<IgnoreCode>): XmlParseBuilder {
        xmlParse.ignoreList.clear()
        xmlParse.ignoreList.addAll(list)
        return this
    }

    fun build(): XmlParse {
        return xmlParse
    }

}