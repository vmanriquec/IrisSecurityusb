package com.apolomultimedia.guardify.fragment.track.gps;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenu;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.apolomultimedia.guardify.R;
import com.apolomultimedia.guardify.adapter.ContactListAdapter;
import com.apolomultimedia.guardify.api.ApiSingleton;
import com.apolomultimedia.guardify.api.model.ContactItemModel;
import com.apolomultimedia.guardify.api.model.StatusModel;
import com.apolomultimedia.guardify.database.ContactDB;
import com.apolomultimedia.guardify.model.ContactModel;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.RecyclerViewOnItemClickListener;
import com.apolomultimedia.guardify.util.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsFragment extends Fragment {

    private String TAG = getClass().getSimpleName();
    View view;

    View positivePhone, positiveAcc;
    EditText phone_name, phone_phone, acc_name, acc_mail;

    @Bind(R.id.fab_speed)
    FabSpeedDial fab_speed;

    @Bind(R.id.ll_nohay)
    LinearLayout ll_nohay;

    private UserPrefs userPrefs;
    private ContactDB contactDB;
    private ProgressDialog progressDialog;
    private RecyclerView rv_contacts;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_track_gps_contacts, container, false);
        ButterKnife.bind(this, view);

        userPrefs = new UserPrefs(getActivity());
        contactDB = new ContactDB(getActivity());
        setupCircularFAB();
        setupRecycler();
        refreshRecyclerView();

        return view;
    }

    private void setupRecycler() {
        rv_contacts = (RecyclerView) view.findViewById(R.id.rv_contacts);
        layoutManager = new LinearLayoutManager(getActivity());
        rv_contacts.setLayoutManager(layoutManager);

    }

    private void setupCircularFAB() {
        fab_speed.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return super.onPrepareMenu(navigationMenu);
            }

            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.action_phone:
                        loadModalPhone(false, 0);
                        break;

                    case R.id.action_account:
                        loadModalEmail(false, 0);

                        break;
                }

                return false;
            }
        });
    }

    private void saveContactRetrofit(final String name, final String phone, final String mail, final String cod) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("nombre", name);
        hashMap.put("telefono", phone);
        hashMap.put("mail", mail);
        hashMap.put("cod", cod);
        hashMap.put("idusuario", userPrefs.getKeyIdUsuario());

        initLoading(getActivity().getString(R.string.saving));

        ApiSingleton.getApiService().doSaveContact(hashMap).enqueue(new Callback<com.apolomultimedia.guardify.api.model.ContactModel>() {
            @Override
            public void onResponse(Call<com.apolomultimedia.guardify.api.model.ContactModel> call, Response<com.apolomultimedia.guardify.api.model.ContactModel> response) {
                finishLoading();
                if (response.body().getSuccess()) {
                    contactDB.insertContact(Integer.valueOf(response.body().get_id()), name, phone, mail, cod);
                    refreshRecyclerView();

                } else {
                    ToastUtil.shortToast(getActivity(), response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<com.apolomultimedia.guardify.api.model.ContactModel> call, Throwable t) {
                finishLoading();
                ToastUtil.showToastConnection(getActivity());
            }
        });

    }

    private void editContactRetrofit(final String name, final String phone, final String mail, final String cod, final String _id) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("idcontacto", String.valueOf(_id));
        hashMap.put("nombre", name);
        hashMap.put("telefono", phone);
        hashMap.put("mail", mail);
        hashMap.put("cod", cod);

        initLoading(getActivity().getResources().getString(R.string.updating));

        ApiSingleton.getApiService().doEditContact(hashMap).enqueue(new Callback<com.apolomultimedia.guardify.api.model.ContactModel>() {
            @Override
            public void onResponse(Call<com.apolomultimedia.guardify.api.model.ContactModel> call, Response<com.apolomultimedia.guardify.api.model.ContactModel> response) {
                finishLoading();
                contactDB.updateContact(Integer.valueOf(_id), name, phone, mail, cod);
                refreshRecyclerView();
            }

            @Override
            public void onFailure(Call<com.apolomultimedia.guardify.api.model.ContactModel> call, Throwable t) {
                finishLoading();
                ToastUtil.showToastConnection(getActivity());
            }
        });

    }

    private void refreshRecyclerView() {
        Cursor contactsCursor = contactDB.getContacts();
        if (contactsCursor != null && contactsCursor.getCount() > 0) {
            ll_nohay.setVisibility(View.GONE);
            rv_contacts.setVisibility(View.VISIBLE);

            final ArrayList<ContactModel> list = new ArrayList<>();
            while (contactsCursor.moveToNext()) {
                final ContactModel model = new ContactModel();
                model.set_id(contactsCursor.getString(0));
                model.setName(contactsCursor.getString(1));
                model.setPhone(contactsCursor.getString(2));
                model.setEmail(contactsCursor.getString(3));
                model.setCod(contactsCursor.getString(4));
                model.setSelected(contactsCursor.getString(5));

                list.add(model);
            }

            rv_contacts.setAdapter(new ContactListAdapter(list, new RecyclerViewOnItemClickListener() {
                @Override
                public void onClick(View v, int position) {
                    final ContactModel model = list.get(position);

                    switch (v.getId()) {
                        case R.id.rl_main:
                            String cod = model.getCod();
                            if (cod.equals("1")) {
                                loadModalPhone(true, Integer.valueOf(model.get_id()));
                            } else {
                                loadModalEmail(true, Integer.valueOf(model.get_id()));
                            }
                            break;

                        case R.id.cb_selected:
                            String cambio = "1";
                            if (model.getSelected().equals("1")) {
                                cambio = "0";
                            }
                            contactDB.updateContactSelected(Integer.valueOf(model.get_id()), cambio);
                            refreshRecyclerView();
                            break;

                    }

                }

                @Override
                public void onItemLongCLick(View v, int position) {

                }
            }));

        } else {
            ll_nohay.setVisibility(View.VISIBLE);
            rv_contacts.setVisibility(View.GONE);
        }
    }

    private void initLoading(String msg) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void finishLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void loadModalPhone(Boolean edit, final int _id) {
        MaterialDialog dialogPhone = new MaterialDialog.Builder(getActivity())
                .title(getActivity().getResources().getString(R.string.add_new_contact))
                .customView(R.layout.custom_view_contact_add, true)
                .positiveText(getActivity().getResources().getString(R.string.add))
                .negativeText(getActivity().getResources().getString(R.string.cancel))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String name = phone_name.getText().toString();
                        String phone = phone_phone.getText().toString();

                        if (name.length() > 2 && phone.length() > 3) {
                            saveContactRetrofit(name, phone, "", "1");
                        } else {
                            return;
                        }

                    }
                }).build();

        if (edit) {
            dialogPhone = new MaterialDialog.Builder(getActivity())
                    .title(getActivity().getResources().getString(R.string.edit_contact))
                    .customView(R.layout.custom_view_contact_add, true)
                    .positiveText(getActivity().getResources().getString(R.string.edit))
                    .negativeText(getActivity().getResources().getString(R.string.cancel))
                    .neutralText(getActivity().getResources().getString(R.string.delete))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            String name = phone_name.getText().toString();
                            String phone = phone_phone.getText().toString();

                            if (name.length() > 2 && phone.length() > 3) {
                                editContactRetrofit(name, phone, "", "1", String.valueOf(_id));
                            } else {
                                return;
                            }

                        }
                    })
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            doDeleteContact(_id);
                        }
                    }).build();
        }

        positivePhone = dialogPhone.getActionButton(DialogAction.POSITIVE);

        phone_name = (EditText) dialogPhone.getCustomView().findViewById(R.id.name);
        phone_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (phone_phone.length() > 0 && phone_name.length() > 0) {
                    positivePhone.setEnabled(true);
                } else {
                    positivePhone.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        phone_phone = (EditText) dialogPhone.getCustomView().findViewById(R.id.phone);
        phone_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (phone_phone.length() > 0 && phone_name.length() > 0) {
                    positivePhone.setEnabled(true);
                } else {
                    positivePhone.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        positivePhone.setEnabled(false);

        if (edit) {
            Cursor contactCursor = contactDB.selectContact(_id);
            if (contactCursor != null && contactCursor.getCount() > 0) {
                contactCursor.moveToFirst();
                phone_name.setText(contactCursor.getString(1));
                phone_phone.setText(contactCursor.getString(2));
                positivePhone.setEnabled(true);

            }
        }

        dialogPhone.show();
    }

    private void doDeleteContact(final int id) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage(getActivity().getResources().getString(R.string.confirm_delete_contact))
                .setNegativeButton(getActivity().getResources().getString(R.string.cancel), null)
                .setPositiveButton(getActivity().getResources().getString(R.string.confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                initLoading(getActivity().getResources().getString(R.string.deleting));

                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("idcontacto", id + "");

                                ApiSingleton.getApiService().doDeleteContact(hashMap).enqueue(
                                        new Callback<StatusModel>() {
                                            @Override
                                            public void onResponse(Call<StatusModel> call, Response<StatusModel> response) {
                                                Log.i(TAG, "onResponse delete");
                                                finishLoading();
                                                contactDB.deleteContact(id);
                                                refreshRecyclerView();
                                            }

                                            @Override
                                            public void onFailure(Call<StatusModel> call, Throwable t) {
                                                Log.i(TAG, "onFailure delete");
                                                finishLoading();
                                                contactDB.deleteContact(id);
                                                refreshRecyclerView();
                                            }
                                        }
                                );

                            }
                        });
        alertDialog.show();
    }

    private void loadModalEmail(Boolean edit, final int _id) {
        MaterialDialog dialogEmail = new MaterialDialog.Builder(getActivity())
                .title(getActivity().getResources().getString(R.string.add_new_contact))
                .customView(R.layout.custom_view_contact_email_add, true)
                .positiveText(getActivity().getResources().getString(R.string.add))
                .negativeText(getActivity().getResources().getString(R.string.cancel))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String name = acc_name.getText().toString();
                        String mail = acc_mail.getText().toString();

                        if (name.length() > 2 && mail.length() > 3) {
                            saveContactRetrofit(name, "", mail, "2");
                        } else {
                            return;
                        }

                    }
                }).build();

        if (edit) {
            dialogEmail = new MaterialDialog.Builder(getActivity())
                    .title(getActivity().getResources().getString(R.string.edit_contact))
                    .customView(R.layout.custom_view_contact_email_add, true)
                    .positiveText(getActivity().getResources().getString(R.string.edit))
                    .negativeText(getActivity().getResources().getString(R.string.cancel))
                    .neutralText(getActivity().getResources().getString(R.string.delete))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            String name = acc_name.getText().toString();
                            String mail = acc_mail.getText().toString();

                            if (name.length() > 2 && mail.length() > 3) {
                                editContactRetrofit(name, "", mail, "2", String.valueOf(_id));
                            } else {
                                return;
                            }

                        }
                    })
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            doDeleteContact(_id);
                        }
                    }).build();
        }

        positiveAcc = dialogEmail.getActionButton(DialogAction.POSITIVE);

        acc_name = (EditText) dialogEmail.getCustomView().findViewById(R.id.name);
        acc_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (acc_mail.length() > 0 && acc_name.length() > 0) {
                    positiveAcc.setEnabled(true);
                } else {
                    positiveAcc.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        acc_mail = (EditText) dialogEmail.getCustomView().findViewById(R.id.email);
        acc_mail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (acc_mail.length() > 0 && acc_name.length() > 0) {
                    positiveAcc.setEnabled(true);
                } else {
                    positiveAcc.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        positiveAcc.setEnabled(false);

        if (edit) {
            Cursor contactCursor = contactDB.selectContact(_id);
            if (contactCursor != null && contactCursor.getCount() > 0) {
                contactCursor.moveToFirst();
                acc_name.setText(contactCursor.getString(1));
                acc_mail.setText(contactCursor.getString(3));
                positiveAcc.setEnabled(true);

            }
        }

        dialogEmail.show();
    }

}
