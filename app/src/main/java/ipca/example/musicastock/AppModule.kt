package ipca.example.musicastock

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ipca.example.musicastock.data.auth.AuthInterceptor
import ipca.example.musicastock.data.auth.TokenStore
import ipca.example.musicastock.data.auth.TokenStoreImpl
import ipca.example.musicastock.data.local.AppDatabase
import ipca.example.musicastock.data.local.dao.CollectionDao
import ipca.example.musicastock.data.local.dao.MusicDao
import ipca.example.musicastock.data.remote.api.AuthApi
import ipca.example.musicastock.data.remote.api.CollectionsApi
import ipca.example.musicastock.data.remote.api.EnvironmentsApi
import ipca.example.musicastock.data.remote.api.MusicApi
import ipca.example.musicastock.data.repository.CollectionRepositoryImpl
import ipca.example.musicastock.data.repository.CollectionsLocalRepository
import ipca.example.musicastock.data.repository.LoginRepository
import ipca.example.musicastock.data.repository.MusicRepository
import ipca.example.musicastock.data.repository.MusicsLocalRepository
import ipca.example.musicastock.domain.repository.ICollectionRepository
import ipca.example.musicastock.domain.repository.ILoginRepository
import ipca.example.musicastock.domain.repository.IMusicRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "http://10.0.2.2:5267/"

    // -----------------------------
    // TokenStore
    // -----------------------------
    @Provides
    @Singleton
    fun provideTokenStore(@ApplicationContext context: Context): TokenStore =
        TokenStoreImpl(context)

    // -----------------------------
    // Interceptor
    // -----------------------------
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenStore: TokenStore): AuthInterceptor =
        AuthInterceptor(tokenStore)

    // -----------------------------
    // OkHttp / Retrofit
    // -----------------------------
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // -----------------------------
    // APIs
    // -----------------------------
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideMusicApi(retrofit: Retrofit): MusicApi =
        retrofit.create(MusicApi::class.java)

    @Provides
    @Singleton
    fun provideCollectionsApi(retrofit: Retrofit): CollectionsApi =
        retrofit.create(CollectionsApi::class.java)

    // -----------------------------
    // Repositories (Domain interfaces)
    // -----------------------------
    @Provides
    @Singleton
    fun provideLoginRepository(
        authApi: AuthApi,
        tokenStore: TokenStore
    ): ILoginRepository = LoginRepository(authApi, tokenStore)

    @Provides
    @Singleton
    fun provideMusicRepository(
        musicApi: MusicApi,
        collectionsApi: CollectionsApi,
        local: MusicsLocalRepository
    ): IMusicRepository = MusicRepository(musicApi, collectionsApi, local)

    @Provides
    @Singleton
    fun provideCollectionRepository(
        api: CollectionsApi,
        local: CollectionsLocalRepository
    ): ICollectionRepository = CollectionRepositoryImpl(api, local)

    // -----------------------------
    // Room
    // -----------------------------
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "local_music_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideCollectionDao(db: AppDatabase): CollectionDao = db.collectionDao()

    @Provides
    @Singleton
    fun provideMusicDao(db: AppDatabase): MusicDao = db.musicDao()

    @Module
    @InstallIn(SingletonComponent::class)
    object AppModule {
    }

    @Provides
    @Singleton
    fun provideEnvironmentsApi(retrofit: Retrofit): EnvironmentsApi =
        retrofit.create(EnvironmentsApi::class.java)
}
