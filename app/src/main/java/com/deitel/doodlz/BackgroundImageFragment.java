package com.deitel.doodlz;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BackgroundImageFragment extends DialogFragment implements View.OnClickListener {

    private final static int SELECT_FILE = 11;
    private Button uploadButton;

    public BackgroundImageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        View uploadImage = getActivity().getLayoutInflater().inflate(
                R.layout.fragment_upload, null);

        uploadButton = (Button) uploadImage.findViewById(R.id.button_upload);
        uploadButton.setOnClickListener(this);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        builder.setView(uploadImage);

        final DoodleView doodleView = getDoodleFragment().getDoodleView();
        // set the AlertDialog's message
        builder.setMessage(R.string.message_upload_image);
        // add button to AlertDialog
        builder.setPositiveButton(R.string.button_set_image,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        (new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final Bitmap upImage = getBitmapBackground(uploadButton.getText().toString());
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    public void run() {
                                        doodleView.setUploadImage(upImage);
                                    }
                                });
                            }
                        })).start();
                    }
                }
        );

        return builder.create();
    }

    private MainActivityFragment getDoodleFragment() {
        return (MainActivityFragment) getFragmentManager().findFragmentById(
                R.id.doodleFragment);
    }

    private Bitmap getBitmapBackground(String urlText) {
        Bitmap bitmap = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlText);
            connection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception x) {
            Log.e("UPLOAD", "Failed to upload image" + urlText + "" + x.getMessage(), x);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return bitmap;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        MainActivityFragment fragment = getDoodleFragment();

        if (fragment != null)
            fragment.setDialogOnScreen(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivityFragment fragment = getDoodleFragment();

        if (fragment != null)
            fragment.setDialogOnScreen(false);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == uploadButton.getId()) {
            showFileChooser();
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/xml");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select an image to upload"), SELECT_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "Please install File Manager to upload an image.", Toast.LENGTH_SHORT).show();
        }
    }
}