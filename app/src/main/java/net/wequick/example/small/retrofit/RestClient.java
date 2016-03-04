package net.wequick.example.small.retrofit;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import timber.log.Timber;

/**
 * Created by tanner.tan on 2016/1/27.
 */
public class RestClient {
    static final String ENDPOINT = "http://dev-ldms-rws.optilink.com:8192";
    static RestClient sInstance;

    Context context;
    OkHttpClient client;
    Retrofit retrofit;
    RestService service;

    RestClient(Context context) {
        this.context = context;

        client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Response response = chain.proceed(request);

                        String contentType = response.headers().get("Content-Type");
                        Timber.d("[intercept] %s Content-Type:%s", request, contentType);
                        if (!TextUtils.isEmpty(contentType) && contentType.startsWith("application/json;")) {
                            // Response bodies can only be read once
                            String bodyString = response.body().string();
                            Timber.d("[intercept] Response:%s", bodyString);
                            // TODO add your code

                            return response.newBuilder()
                                    .body(ResponseBody.create(response.body().contentType(), bodyString))
                                    .build();
                        }

                        /**
                         * 文件下载时，ResponseBody.create(response.body().contentType(), bodyString)
                         *  构造的 body 的 contentLength 和实际 contentLength 不一致，导致文件下载错误
                         */
                        return response;
                    }
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();

        service = retrofit.create(RestService.class);
    }

    public static RestClient getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RestClient(context.getApplicationContext());
        }
        return sInstance;
    }

    public RestService getService() {
        return service;
    }

    public String getFileName(String url) {
        Timber.d("url:%s", url);
        int end = url.lastIndexOf('?');
        if (end < 0) {
            end = url.length();
        }
        return url.substring(url.lastIndexOf('/') + 1, end);
    }

    public boolean saveFile(String filePath, final retrofit2.Response<ResponseBody> response) {
        try {
            File file = new File(filePath);

            ResponseBody body = response.body();
            BufferedSource source = body.source();
            BufferedSink sink = Okio.buffer(Okio.sink(file));
            showDownloadProgress(source, sink, body.contentLength());
            sink.writeAll(source);
            sink.close();

            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    void showDownloadProgress(BufferedSource source, BufferedSink sink, long contentLength)
            throws IOException {
        int bufferSize = 1024;
        long count, total = 0;
        int progress, lastProgress = 0;
        while ((count = source.read(sink.buffer(), bufferSize)) != -1) {
            total += count;
            progress = (int) (100 * total / contentLength);
            if (lastProgress != progress) {
                lastProgress = progress;
                Timber.d("[DownloadProgress] %d", progress);
            }
        }
    }
}
