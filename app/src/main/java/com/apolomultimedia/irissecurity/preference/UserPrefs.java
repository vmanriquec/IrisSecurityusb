package com.apolomultimedia.irissecurity.preference;

import android.content.Context;

/**
 * Created by developer on 20/06/2016.
 */

public class UserPrefs {

    private SingletonPrefs prefs;

    private static final String KEY_LOGGED = "logged";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IDUSUARIO = "IdUsuario";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_APELLIDO = "apellido";
    private static final String KEY_LENGUAJE = "lenguaje";
    private static final String KEY_GENERO = "genero";
    private static final String KEY_ONOMASTICO = "onomastico";
    private static final String KEY_FOTO = "foto";
    private static final String KEY_CIUDAD = "ciudad";
    private static final String KEY_IDFACEBOOK = "fbId";

    public UserPrefs(Context context) {
        prefs = SingletonPrefs.getInstance(context);
    }

    public void reset() {
        setKeyLogged(false);
    }

    public void setKeyEmail(String value) {
        prefs.put(KEY_EMAIL, value);
    }

    public String getKeyEmail() {
        return prefs.getString(KEY_EMAIL);
    }

    public void setKeyIdusuario(String value) {
        prefs.put(KEY_IDUSUARIO, value);
    }

    public String getKeyIdUsuario() {
        return prefs.getString(KEY_IDUSUARIO);
    }

    public void setKeyNombre(String value) {
        prefs.put(KEY_NOMBRE, value);
    }

    public String getKeyNombre() {
        return prefs.getString(KEY_NOMBRE);
    }

    public void setKeyApellido(String value) {
        prefs.put(KEY_APELLIDO, value);
    }

    public String getKeyApellido() {
        return prefs.getString(KEY_APELLIDO);
    }

    public void setKeyLenguaje(String value) {
        prefs.put(KEY_LENGUAJE, value);
    }

    public String getKeyLenguaje() {
        return prefs.getString(KEY_LENGUAJE);
    }

    public void setKeyGenero(String value) {
        prefs.put(KEY_GENERO, value);
    }

    public String getKeyGenero() {
        return prefs.getString(KEY_GENERO);
    }

    public void setKeyOnomastico(String value) {
        prefs.put(KEY_ONOMASTICO, value);
    }

    public String getKeyOnomastico() {
        return prefs.getString(KEY_ONOMASTICO);
    }

    public void setKeyFoto(String value) {
        prefs.put(KEY_FOTO, value);
    }

    public String getKeyFoto() {
        return prefs.getString(KEY_FOTO);
    }

    public void setKeyCiudad(String value) {
        prefs.put(KEY_CIUDAD, value);
    }

    public String getKeyCiudad() {
        return prefs.getString(KEY_CIUDAD);
    }

    public void setKeyIdFacebook(String value) {
        prefs.put(KEY_IDFACEBOOK, value);
    }

    public String getKeyIdFacebook() {
        return prefs.getString(KEY_IDFACEBOOK);
    }

    public void setKeyLogged(Boolean val) {
        prefs.put(KEY_LOGGED, val);
    }

    public Boolean getKeyLogged() {
        return prefs.getBoolean(KEY_LOGGED);
    }

}
