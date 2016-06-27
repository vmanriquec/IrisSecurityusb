package com.apolomultimedia.guardify.util;

import android.view.View;

public interface RecyclerViewOnItemClickListener {

    void onClick(View v, int position);

    void onItemLongCLick(View v, int position);

}
