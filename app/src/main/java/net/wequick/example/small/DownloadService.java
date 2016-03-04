package net.wequick.example.small;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import net.wequick.example.small.retrofit.RestClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by tanner.tan on 2016/3/3.
 */
public class DownloadService extends IntentService {
    static final String[] urls = {"http://pic38.nipic.com/20140215/12359647_224250202132_2.jpg",
            "http://www.bz55.com/uploads/allimg/150309/139-150309101F2.jpg",
            "http://pic38.nipic.com/20140215/12359647_224249690129_2.jpg"
    };

    static final String download_dir = Environment.getExternalStorageDirectory() + "/small/download";
    static final String patch_dir = Environment.getExternalStorageDirectory() + "/small/patch";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        checkUpdate(urls);
    }

    boolean isUpdate() {
        File downloadDir = new File(download_dir);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        String[] array = downloadDir.list();
        return array == null || array.length == 0;
    }

    void checkUpdate(final String[] urls) {
        final RestClient client = RestClient.getInstance(this);
        final List<String> patchList = new ArrayList<>(urls.length);

        client.getService()
                .contributors("square", "retrofit")
                .flatMap(new Func1<List<Contributor>, Observable<String>>() {
                    @Override
                    public Observable<String> call(List<Contributor> contributors) {
                        if (isUpdate()) {
                            return Observable.from(urls);
                        }
                        return Observable.empty();
                    }
                })
                .flatMap(new Func1<String, Observable<Response<ResponseBody>>>() {
                    @Override
                    public Observable<Response<ResponseBody>> call(String url) {
                        return client.getService().download(url);
                    }
                })
                .map(new Func1<Response<ResponseBody>, String>() {
                    @Override
                    public String call(Response<ResponseBody> response) {
                        String utl = response.raw().request().url().toString();
                        utl = download_dir + "/" + client.getFileName(utl);
                        if (client.saveFile(utl, response)) {
                            return utl;
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {

                    @Override
                    public void onNext(String path) {
                        // 插件的更新视为一个事务
                        if (!TextUtils.isEmpty(path)) {
                            patchList.add(path);
                        }
                    }

                    @Override
                    public void onCompleted() {
                        // 所有插件下载完成
                        if (!patchList.isEmpty() && patchList.size() == urls.length) {
                            testUpgradeBundle();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    void testUpgradeBundle() {
        File patchDir = new File(patch_dir);
        if (!patchDir.exists()) {
            patchDir.mkdirs();
        }

        File[] patchArray = patchDir.listFiles();
        for (File file : patchArray) {
            upgradeBundle(file);
        }
    }

    boolean upgradeBundle(File patch) {
        String pluginName = patch.getName().replace('_', '.');
        pluginName = pluginName.substring(3, pluginName.lastIndexOf('.'));
        net.wequick.small.Bundle bundle = net.wequick.small.Bundle.findByName(pluginName);
        Timber.e("pluginName:%s", pluginName);

        try {
            InputStream is = new FileInputStream(patch);
            OutputStream os = new FileOutputStream(bundle.getPatchFile());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
            is.close();


            bundle.upgrade();
            Timber.e("pluginName:%s upgraded.", pluginName);
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
