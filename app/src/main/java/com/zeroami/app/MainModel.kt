package com.zeroami.app

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import java.net.URL
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import java.io.File


class MainModel : IMainModel {

    override fun login(account: String, password: String): Observable<String> = Observable.create<String> {
        it.onNext(URL("http://www.baidu.com").readText())
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


}