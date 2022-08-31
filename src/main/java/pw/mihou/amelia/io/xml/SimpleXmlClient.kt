package pw.mihou.amelia.io.xml

import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.concurrent.TimeUnit
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.NoSuchElementException

/**
 * An XML client intended for small file XMLs that uses the DOM parser together with
 * OkHttp to create a simple API that should be at least fast and resilient.
 */
object SimpleXmlClient {

    @Volatile var client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Volatile var userAgent = "Amelia/2.0.0-luminous (Language=Kotlin/1.7.10, Developer=Shindou Mihou)"

    private val documentBuilder = DocumentBuilderFactory.newInstance()
        .apply {
            setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        }
        .newDocumentBuilder()

    fun read(uri: String): Document {
        val stream = request(uri)
        return parse(stream)
    }

    private fun request(uri: String): String {
        val request = Request.Builder()
            .addHeader("User-Agent", userAgent)
            .url(uri)
            .get()
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to connect to $uri: The status code isn't within the range of 200 to 300.")
            }

            if (response.body == null) {
                throw NoSuchElementException("Failed to find the body response from $uri.")
            }

            return@use response.body!!.string()
        }
    }

    private fun parse(contents: String) = documentBuilder.parse(InputSource(StringReader(contents)))

}