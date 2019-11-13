package com.picpass.Managers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.picpass.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;

public class ResourceManager {
    private static final String TAG = "ResourceManager";

    private static final String FILENAME = "picpass_cfg.json";
    private static final String IMAGE_SET = "imageSet";

    public static int getDrawableIdFromString(String resName) throws NoSuchFieldException, IllegalAccessException {
        Field field = R.drawable.class.getDeclaredField(resName);
        return field.getInt(field);
    }

    //TODO: not writing to file properly
    public static void saveImageSet(Context ctx, String[] imageSet) {
        JSONObject config = loadConfigFileContents(ctx);
        if (config != null) {
            try {
                config.put(IMAGE_SET, new JSONArray(imageSet));
                saveConfigFileContents(ctx, config);
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }

    public static String[] loadImageSet(Context ctx) {
        JSONObject config = loadConfigFileContents(ctx);
        if (config == null) {
            return null;
        }

        JSONArray imageSetJSONArray = config.optJSONArray(IMAGE_SET);
        if (imageSetJSONArray == null) {
            return null;
        }

        String[] imageSet = new String[imageSetJSONArray.length()];
        for (int i = 0; i < imageSet.length; i++) {
            imageSet[i] = imageSetJSONArray.optString(i);
        }
        return imageSet;

    }

    // Read from config file and return JSONObject containing entire config
    private static JSONObject loadConfigFileContents(Context ctx) {
        try {
            File configFile = new File(ctx.getFilesDir(), FILENAME);
            if (!configFile.exists()) {
                saveConfigFileContents(ctx, new JSONObject());
            }

            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            StringBuilder buffer = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                buffer.append(line).append('\n');
            }
            reader.close();

            return new JSONObject(buffer.toString());

        } catch (IOException | JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        }
    }

    // Write JSONObject representing the entire config of picpass to the config file
    private static void saveConfigFileContents(Context ctx, JSONObject config) {
        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(ctx.getFilesDir(), FILENAME)));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(ctx.getFilesDir(), FILENAME))));
            writer.write(config.toString());
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }
}
