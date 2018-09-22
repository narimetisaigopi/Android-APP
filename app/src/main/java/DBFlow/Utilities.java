package DBFlow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.util.Log;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

public class Utilities {

    Context context;

    String TAG = "Utilities";

    public String URL = "https://mock-api-mobile.dev.lalamove.com/deliveries";

    public Utilities(Context context) {
        this.context = context;
    }

    public List<LocalModel> getAllData(){
        List<LocalModel> localModels = SQLite.select().from(LocalModel.class).queryList();
        Log.d(TAG,"getAllData : "+localModels.size());
        return localModels;
    }

    public AlertDialog.Builder alertDialog(String message, String buttonName, DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(buttonName, clickListener);
        builder.create().show();

        return builder;
    }


    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}
