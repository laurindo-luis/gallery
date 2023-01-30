package br.com.luislaurindo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

public class DialogComponent {

    public static AlertDialog createDeleteAlertDialog(Context context, String title, String message, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("Excluir", onClickListener);
        builder.setNegativeButton("Cancelar", null);
        return builder.create();
    }

    public static AlertDialog createInformationAlertDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", null);
        return builder.create();
    }

    public static AlertDialog createConfirmAlertDialog(Context context, String title, String message, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("Sim", onClickListener);
        builder.setNegativeButton("Não", null);
        return builder.create();
    }

    public static AlertDialog createProgressAlertDialog(Context context, View view, String title) {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setView(view);
        builder.setCancelable(false);
        return builder.create();
    }
}
