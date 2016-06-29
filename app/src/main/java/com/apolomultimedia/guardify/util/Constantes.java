package com.apolomultimedia.guardify.util;

public class Constantes {

    public static final String SERVER = "http://apolomultimedia-server1.info/android_j";
    public static final String SOCKET_SERVER = "http://64.37.54.112:9000";

    public static final String API_PATH = SERVER + "/api/";
    public static final String IMAGES_PATH = SERVER + "/images/";

    // api rest
    public static final String LOGIN = "userLogin.php";
    public static final String LOGIN_FACEBOOK = "userLoginFacebook.php";
    public static final String REGISTER = "userRegister.php";
    public static final String CHECK_STATUS = "userCheckStatus.php";
    public static final String UPDATE_DETAILS = "userUpdateDetails.php";
    public static final String UPDATE_PHOTO = "userUpdatePhoto.php";

    // contacts
    public static final String SAVE_CONTACT = "userSaveContact.php";
    public static final String EDIT_CONTACT = "userEditContact.php";
    public static final String DELETE_CONTACT = "userDeleteContact.php";

    // broadcast emiters
    public static final String BR_DEVICE_CONNECTED = "device_connected";
    public static final String BR_DEVICE_DISCONNECTED = "device_disconnected";
    public static final String BR_SINGLE_TAP = "single_tap";
    public static final String BR_DOUBLE_TAP = "double_tap";
    public static final String BR_LONG_TAP = "long_tap";

    // socket events
    public static final String EVENT_SOCKETID = "socket_id";

    // options
    public static final String OPT_TRACKGPS = "track_gps";

    // suboptions
    public static final String SUBOPT_FIRST = "first";
    public static final String SUBOPT_SECOND = "second";
    public static final String SUBOPT_THIRD = "third";

}
