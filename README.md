<h2>SimpleXmlParser</h2>
<p>Простая бибилотека для парсинга XML файла, результатом выдаёт заполненный POJO класс</p>
<p>Есть возможность игнорировать ошибки (404, 500, 501, 502 и т.д), в таком случае не будет вызванно исключение, результатом будет пустой POJO класс</p>

Пример POJO класса
```
class ExamplePOJO {

    @Field("kp_rating")
    var kp_rating: Float? = null
    
    @Field("imdb_rating")
    var imdb_rating: Float? = null
    
}
```

Пример #1
```
val xmlParse = XmlParseBuilder()
            .addOkHttpClient(OkHttpClient())  // объект okHttpClient
            .addBaseUrl("https://rating.kinopoisk.ru/")   // базовый адрес для всех запросов, всегда должно быть '/' в конце 
            .addExpansion(true)   // добавлять ли в конец запроса '.xml' автоматически
            .build()
            
            
xmlParse.requestWithCallback("723959", ExamplePOJO::class.java){  println(it.toString())  }   // запрос с callback
val result = xmlParse.request("723959", ExamplePOJO::class.java)                             // запрос без callback, выполняется в основном потоке
```

Пример #2
```
val xmlParse = XmlParseBuilder()
            .addOkHttpClient(OkHttpClient())
            .addBaseUrl("https://rating.kinopoisk.ru/")
            .addExpansion(true)
            .addIgnoreList(arrayListOf(IgnoreCode.NOT_FOUND))  // игнорирует ошибку 404
            .build()
            
            
xmlParse.requestWithCallback("723959", ExamplePOJO::class.java){  println(it.toString())  }   // запрос с callback
val result = xmlParse.request("723959", ExamplePOJO::class.java)                             // запрос без callback, выполняется в основном потоке
```

