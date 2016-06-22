package com.apolomultimedia.guardify.util;

/**
 * Created by developer on 20/06/2016.
 */
public class Constantes {

    //public static final String SERVER = "http://apolomultimedia-server1.info/android/iris";
    public static final String SERVER = "http://192.168.1.117/iris-security";
    public static final String SOCKET_SERVER = "http://apolomultimedia-server1.info:XXXX";

    public static final String API_PATH = SERVER + "/api/";
    public static final String IMAGES_PATH = SERVER + "/images/";

    // api rest
    public static final String LOGIN = "userLogin.php";
    public static final String LOGIN_FACEBOOK = "userLoginFacebook.php";
    public static final String REGISTER = "userRegister.php";
    public static final String CHECK_STATUS = "userCheckStatus.php";
    public static final String UPDATE_PHOTO = "userUpdatePhoto.php";

    // broadcast emiters
    public static final String BR_DEVICE_CONNECTED = "device_connected";
    public static final String BR_DEVICE_DISCONNECTED = "device_disconnected";

}