package com.github.mrtwon.library

import android.util.Log
import okhttp3.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.StringReader
import java.lang.Exception
import java.lang.NumberFormatException
import java.util.*


class XmlParse {
    var baseUrl: String? = null
    var okHttpClient = OkHttpClient()
    var addExpansion: Boolean = false
    var ignoreList = arrayListOf<IgnoreCode>()
    fun <T: Any> request(path: String, classPojo: Class<T>): T{
        val request = expansionWithRequest(path)
        val response = okHttpClient.newCall(request).execute()
        if(isIgnoredCode(response.code)){
            return classPojo.newInstance()
        }
        val responseString = response.body?.string()
        if(responseString == null || responseString.isEmpty()){
            throw Exception("Body is null")
        }

        return parse(classPojo, responseString)
    }

    fun <T: Any> requestWithCallback(path: String, classPojo: Class<T>, callback: (T) -> Unit){
        val request = expansionWithRequest(path)
        okHttpClient.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) { throw e }
            override fun onResponse(call: Call, response: Response) {

                if (!isIgnoredCode(response.code)) {
                    val responseString = response.body?.string()
                    if (responseString == null || responseString.isEmpty()) {
                        throw Exception("Body is null")
                    }
                    val result = parse(classPojo, responseString)
                    callback(result as T)
                } else {
                    callback(classPojo.newInstance())
                }

            }

        })
    }

    private fun isIgnoredCode(code: Int): Boolean{
        for(element_ignore in ignoreList){
            if(element_ignore.code == code) return true
        }
        return false
    }

    private fun expansionWithRequest(request: String): Request{
        var result = baseUrl+request
        if(addExpansion){ result += ".xml" }
        return Request.Builder()
            .url(result)
            .build()
    }


    private fun <T : Any> parse(_class: Class<T>, xmlString: String): T{

        val instance = _class.newInstance()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val xpp = factory.newPullParser()
        xpp.setInput(StringReader(xmlString))

        var eventType = xpp.eventType
        var tag: String? = null
        while(eventType != XmlPullParser.END_DOCUMENT){
            when(eventType){
                XmlPullParser.START_TAG -> { tag = xpp.name }
                XmlPullParser.END_TAG -> { tag = null }
                XmlPullParser.TEXT -> {
                    tag?.let {
                        if(xpp.text.isNotEmpty()){
                            addValue(instance, it, xpp.text)
                        }
                    }
                }
            }
            eventType = xpp.next()
        }
        return instance
    }

    private fun <T: Any> addValue(instance: T, tag: String, value: String){

        val cl = instance.javaClass
        val fields = cl.declaredFields
        log("fields size = ${fields.size}")
        for(field in fields){

            field.isAccessible = true
            if(field.isAnnotationPresent(Field::class.java)){
                val anon: Field = field.getAnnotation(Field::class.java)
                if(anon.name == tag){
                    addByType(instance, field, value)
                }
            }
        }
    }
    private fun <T: Any> addByType(instance: T, field: java.lang.reflect.Field, value: String){
        try {
            when(field.type.kotlin) {
                Int::class.java.kotlin -> {
                    field.set(instance, Integer.parseInt(value))
                }
                Double::class.java.kotlin -> {
                    field.set(instance, value.toDouble())
                }
                Float::class.java.kotlin -> {
                    field.set(instance, value.toFloat())
                }
                else -> {
                    field.set(instance, value)
                }
            }
        }catch (e: NumberFormatException){
            e.printStackTrace()
            throw NumberFormatException("couldn't convert value '$value' to type  ${field.type}")
        }
    }
    private fun log(s: String){
        Log.i("self-xml-parser", s)
    }
}
enum class IgnoreCode(val code: Int){
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500),
    NOT_IMPLEMENTED(501),
    BAD_GATEWAY(502),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504)
}