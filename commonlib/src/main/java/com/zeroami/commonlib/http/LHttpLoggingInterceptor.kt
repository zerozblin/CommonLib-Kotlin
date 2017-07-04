package com.zeroami.commonlib.http

import com.zeroami.commonlib.utils.LL

import org.json.JSONException
import org.json.JSONObject

import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset

import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource

/**
 * Http请求日志拦截器
 *
 * @author Zeroami
 */
class LHttpLoggingInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val t1 = System.nanoTime()
        var requestBody = ""
        try {
            val buffer = Buffer()
            val body = request.body()
            val isMultipartBody = body is MultipartBody
            if (!isMultipartBody) {
                body.writeTo(buffer)
            }
            if (!isMultipartBody && isPlaintext(buffer)) {
                requestBody = buffer.clone().readUtf8() + "\n\n" + body.contentLength() + " byte body"
            } else {
                requestBody = "binary " + body.contentLength() + " byte body"
            }
        } catch (e: Exception) {
        }

        LL.i(String.format("Sending request %s by %s%n%n%s%n%s",
                request.url(), request.method(), request.headers(), requestBody))
        val response = chain.proceed(request)
        val t2 = System.nanoTime()
        var responseBody = ""
        try {
            val body = response.body()
            val source = body.source()
            source.request(java.lang.Long.MAX_VALUE)
            val buffer = source.buffer().clone()
            if (isPlaintext(buffer)) {
                responseBody = buffer.readString(Charset.forName("UTF-8")) + "\n\n" + body.contentLength() + " byte body"
            } else {
                responseBody = "binary " + body.contentLength() + " byte body"
            }
        } catch (e: Exception) {
        }

        LL.i(String.format("Received response for %s in %.1fms%n%n%s %s %s%n%n%s%n%s",
                response.request().url(), (t2 - t1) / 1e6, response.protocol(), response.code(), response.message(), response.headers(), responseBody))
        if (isGoodJson(responseBody)) {
            LL.json(responseBody)
        }
        return response
    }


    /**
     * 判断返回数据时候为文本类型
     * @param buffer
     * @return
     */
    private fun isPlaintext(buffer: Buffer): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = if (buffer.size() < 64) buffer.size() else 64
            buffer.copyTo(prefix, 0, byteCount)
            for (i in 0..15) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (e: EOFException) {
            return false // Truncated UTF-8 sequence.
        }

    }

    /**
     * 判断字符串是否为json格式
     * @param json
     * @return
     */
    private fun isGoodJson(json: String): Boolean {

        try {
            JSONObject(json.trim())
            return true
        } catch (e: JSONException) {
            println("bad json: " + json)
            return false
        }

    }

}