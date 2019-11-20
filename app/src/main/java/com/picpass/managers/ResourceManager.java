package com.picpass.managers;

import android.content.Context;
import android.util.Log;

import com.picpass.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Resource Manager is a helper class that allows PicPass to call various helper methods from any activity.
 * Methods include getters and setters for persistent configurations like the user's custom image set.
 *
 * @author John Shelnutt, Jackson Gregory
 */
public class ResourceManager {
    private static final String TAG = "ResourceManager";
    private static final String FILENAME = "picpass_cfg.json";
    private static final String IMAGE_SET = "imageSet";

    // Complete set of PicPass Images. To add more, drag .png files to res/drawable and add the filename here.
    // Can be public because the list is unmodifiable and declared as final.
    public static final List<String> galleryImages = Collections.unmodifiableList(Arrays.asList(
            "beach", "bridge", "cape", "castle", "cityscape", "desert",
            "desert2", "fields", "fields2", "forest", "hills", "home",
            "home2", "iceberg", "island", "mill", "mountains", "mountains2",
            "nuclearplant", "river", "ruins", "sea", "spruce", "trees",
            "village", "waterfall", "waterfall2", "windmills"));

    /**
     * Determines if PicPass should display tutorial dialogs, by determining if this is the first time the app is being run
     * If the config file does not exist, tutorial dialogs will show and 9 images will be chosen at random for the user
     * @param ctx The context of the invoking Activity, used to locate the config file.
     * @return true if the invoking activity should run in tutorial mode (display pop-ups, walkthroughs, etc)
     */
    public static boolean shouldDoTutorial(Context ctx) {
        File configFile = new File(ctx.getFilesDir(), FILENAME);
        return !configFile.exists();
//        return true;
    }

    /**
     * On attempting to load or save data, but the config file does not yet exist,
     * this helper function will be called to populate the config file with a random set of 9 images
     * @param ctx The context of the invoking Activity, used to locate the config file.
     */
    private static void initConfig(Context ctx) {
        try {
            File configFile = new File(ctx.getFilesDir(), FILENAME);
            configFile.createNewFile();

            List<String> randomImageSet = new ArrayList<>(galleryImages);
            Collections.shuffle(randomImageSet);
            randomImageSet = randomImageSet.subList(0, 9);

            JSONObject contents = new JSONObject();
            contents.put(IMAGE_SET, new JSONArray(randomImageSet));
            saveConfigFileContents(ctx, contents);
        } catch (IOException | JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    /**
     * saveImageSet stores the selected imageSet in persistent storage (the PicPass config file) for future use.
     * @param ctx The context (needed to retrieve storage directory)
     * @param imageSet An array of Strings, corresponding to image names chosen by the user.
     */
    public static void saveImageSet(Context ctx, List<String> imageSet) {
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

    /**
     * loadImageSet retrieves from persistent storage (the PicPass config file) the array of image names
     * @param ctx The context of the Activity requesting the image set, used to access the file.
     * @return a list of image names, representing the saved imageSet chosen by the user, or null if the file or json array does not exist
     */
    public static List<String> loadImageSet(Context ctx) {
        JSONObject config = loadConfigFileContents(ctx);
        if (config == null) {
            return null;
        }

        JSONArray imageSetJSONArray = config.optJSONArray(IMAGE_SET);
        if (imageSetJSONArray == null) {
            return null;
        }

        ArrayList<String> imageSet = new ArrayList<>();
        for (int i = 0; i < imageSetJSONArray.length(); i++) {
            imageSet.add(imageSetJSONArray.optString(i));
        }
        return imageSet;
    }

    /**
     * Internal helper function to retrieve the entire PicPass configuration as a JSONObject.
     * If the config file does not exist, it will be created and pre-populated with an empty JSONObject
     * @param ctx The context of the calling Activity, used to retrieve the config file
     * @see JSONObject
     * @return a JSONObject representing the configuration, or null if a problem was encountered (invalid permissions or corrupted file state)
     */
    private static JSONObject loadConfigFileContents(Context ctx) {
        try {
            File configFile = new File(ctx.getFilesDir(), FILENAME);
            if (!configFile.exists()) {
                initConfig(ctx);
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

    /**
     * Internal helper function to save the current configuration to persistent storage (the PicPass config file).
     * @param ctx The context of the calling Activity, used to retrieve the config file
     * @param config THe JSONObject representing the configuration
     */
    private static void saveConfigFileContents(Context ctx, JSONObject config) {



        File configFile = new File(ctx.getFilesDir(), FILENAME);
        if (!configFile.exists()) {
            initConfig(ctx);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(ctx.getFilesDir(), FILENAME)));
            writer.write(config.toString());
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

//    /**
//     * Gets the android auto-generated resource id given the string name of the resource. Ex. "boat" returns R.id.boat or throws.
//     * @param resName The string resource name for which the generated ID needs to be retrieved.
//     * @return The associated ID for the resource specified by resName
//     * @throws NoSuchFieldException
//     * @throws IllegalAccessException
//     */
//    public static int getDrawableIdFromString(String resName) throws NoSuchFieldException, IllegalAccessException {
//        Field field = R.drawable.class.getDeclaredField(resName);
//        return field.getInt(field);
//    }
}
