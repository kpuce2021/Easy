package com.example.easydashcam;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;




public interface ServiceApi {

    @POST("/user/login")
    Call<LoginResponse> userLogin(@Body LoginData data);
    //Call<서버의 응답데이터정의 클래스>

    @POST("/user/join")
    Call<JoinResponse> userJoin(@Body JoinData data);

    @Multipart
    @POST("/upload")
    Call<ResponseBody> postVideo(@Part MultipartBody.Part video, @Part("upload") RequestBody name );

    //added
    @Streaming
    @GET
    Call<ResponseBody> downloadFileByUrl(@Url String fileUrl);

    @POST("/accessDB")
    Call<ArrayList<TableResponse>> accessSeverDB();











}
