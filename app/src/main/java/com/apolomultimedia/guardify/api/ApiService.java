package com.apolomultimedia.guardify.api;

import com.apolomultimedia.guardify.api.model.CheckStatusModel;
import com.apolomultimedia.guardify.api.model.ContactModel;
import com.apolomultimedia.guardify.api.model.StatusModel;
import com.apolomultimedia.guardify.api.model.UploadPhotoModel;
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

public interface ApiService {

    /*retrofit codifica las peticiones post get
    *
    *  public static final String LOGIN = "userLogin.php";
    * en la linea de abajo simplifica el envio post
    * */
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
    @POST(Constantes.UPDATE_DETAILS)
    Call<UserModel> doUpdateProfile(@FieldMap Map<String, String> params);

    @Multipart
    @POST(Constantes.UPDATE_PHOTO)
    Call<UploadPhotoModel> doUpdatePhoto(@Part("idusuario") RequestBody idusuario,
                                         @Part MultipartBody.Part file);

    @FormUrlEncoded
    @POST(Constantes.CHECK_STATUS)
    Call<CheckStatusModel> doCheckStatus(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(Constantes.SAVE_CONTACT)
    Call<ContactModel> doSaveContact(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(Constantes.EDIT_CONTACT)
    Call<ContactModel> doEditContact(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(Constantes.DELETE_CONTACT)
    Call<StatusModel> doDeleteContact(@FieldMap Map<String, String> params);

}
