package com.codinghub.apps.codinghubdemo.app

import com.codinghub.apps.codinghubdemo.BuildConfig
import com.codinghub.apps.codinghubdemo.model.objects.preferences.AppPrefs
import com.codinghub.apps.codinghubdemo.model.repository.*
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Injection {

    fun provideRepository(): Repository = RemoteRepository

    private fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppPrefs.getServiceURL().toString())
            .addConverterFactory(GsonConverterFactory.create())
            .client(provideOkHttpClient())
            .build()
    }

    private fun provideRetrofitALPR(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppPrefs.getOpenALPRServiceURL().toString())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun provideRetrofitFaceSass(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppPrefs.getFaceSassServiceURL().toString())
            .addConverterFactory(GsonConverterFactory.create())
            .client(provideOkHttpClientForFaceSass())
            .build()
    }


    private fun provideLoggingInterceptor(): HttpLoggingInterceptor {

        val logging = HttpLoggingInterceptor()
        logging.level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
        return logging
    }

    private fun provideOkHttpClient(): OkHttpClient {

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(provideLoggingInterceptor())
        val username = AppPrefs.getHeaderUserName().toString()
        val password = AppPrefs.getHeaderPassword().toString()

        httpClient.addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .addHeader("Authorization", Credentials.basic(username, password))
                .addHeader("apikey", AppPrefs.getApiKey().toString())
                .build()

            chain.proceed(request)
        }
        return httpClient.build()
    }

    private fun provideOkHttpClientForFaceSass(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(provideLoggingInterceptor())
        return httpClient.build()
    }

    fun provideCDHDemoApi(): CDHDemoApi {
        return provideRetrofit().create(CDHDemoApi::class.java)
    }

    fun provideALPRApi(): CDHALPRApi {
        return provideRetrofitALPR().create(CDHALPRApi::class.java)
    }

    fun provideFaceSassApi(): CDHFaceSassApi {
        return provideRetrofitFaceSass().create(CDHFaceSassApi::class.java)

    }

}