package com.apolomultimedia.guardify.api;

import com.apolomultimedia.guardify.api.model.CheckStatusModel;
import com.apolomultimedia.guardify.api.model.UpdatePhotoModel;
import com.apolomultimedia.guardify.api.model.UserModel;
import com.apolomultimedia.guardify.util.Constantes;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by developer on 20/06/2016.
 */

public interface ApiService {

    @FormUrlEncoded
    @POST(Constantes.LOGIN)
    Call<UserModel> getLogin(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(Constantes.LOGIN_FACEBOOK)
    Call<UserModel> getLoginFacebook(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(Constantes.REGISTER)
    Call<UserModel> doRegister(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(Constantes.CHECK_STATUS)
    Call<CheckStatusModel> doCheckStatus(@FieldMap Map<String, String> params);

    @Multipart
    @POST(Constantes.UPDATE_PHOTO)
    Call<UpdatePhotoModel> doUpdatePhoto(@Part("photo") RequestBody description,
                                         @Part MultipartBody.Part file);

}
