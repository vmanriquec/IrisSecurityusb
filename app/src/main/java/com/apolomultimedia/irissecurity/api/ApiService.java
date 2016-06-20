package com.apolomultimedia.irissecurity.api;

import com.apolomultimedia.irissecurity.api.model.UserModel;
import com.apolomultimedia.irissecurity.util.Constantes;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

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

}
