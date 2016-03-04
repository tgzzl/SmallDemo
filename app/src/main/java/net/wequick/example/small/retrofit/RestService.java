package net.wequick.example.small.retrofit;

import net.wequick.example.small.Contributor;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by tg on 2015/10/17.
 */
public interface RestService {

    @GET("https://api.github.com/repos/{owner}/{repo}/contributors")
    Observable<List<Contributor>> contributors(
            @Path("owner") String owner,
            @Path("repo") String repo);

    @GET
    Observable<Response<ResponseBody>> download(@Url String url);
}
