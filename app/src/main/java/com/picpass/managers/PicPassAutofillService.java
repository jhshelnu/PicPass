package com.picpass.managers;

import android.app.PendingIntent;
import android.app.assist.AssistStructure;
import android.content.Intent;
import android.os.Build;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.text.InputType;
import android.util.Log;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.picpass.PINActivity;
import com.picpass.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PicPassAutoFillService allows for autofilling PicPass passwords directly into any other app
 * The service will check for password fields and suggest the autofill feature below the field.
 * Selecting this option launches the user into PicPass in the autofill flow.
 * @author John Shelnutt, Jackson Gregory
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class PicPassAutofillService extends AutofillService {
    private static final String TAG = "PicPassAutoFillService";

    public static final String EXTRA_AUTOFILL_MODE = "picpass_autofill_mode";
    public static final String EXTRA_AUTOFILL_FIELD_IDS = "picpass_autofill_fields";

    @Override
    public void onFillRequest(@NonNull FillRequest fillRequest, @NonNull CancellationSignal cancellationSignal, @NonNull FillCallback fillCallback) {
        List<FillContext> contexts = fillRequest.getFillContexts();
        AssistStructure structure = contexts.get(contexts.size() - 1).getStructure();

        // Populate a list with all the password fields
        ArrayList<AutofillId> passwordFieldAutofillIDs = new ArrayList<>();
        findPasswordFields(structure.getWindowNodeAt(0).getRootViewNode(), passwordFieldAutofillIDs);

        // If no password fields are found then return
        if (passwordFieldAutofillIDs.size() == 0) {
            return;
        }

        // Create intent to launch PicPass in autofill mode, with the id's of all password fields
        Intent intent = new Intent(this, PINActivity.class);
        intent.putExtra(EXTRA_AUTOFILL_MODE, true);
        intent.putExtra(EXTRA_AUTOFILL_FIELD_IDS, passwordFieldAutofillIDs);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Build a placeholder dataset to launch PicPass on selection
        // PicPass will make a new dataset autofilling all password fields with the generated password
        Dataset.Builder builder = new Dataset.Builder(new RemoteViews(getPackageName(), R.layout.autofill_message))
            .setAuthentication(pendingIntent.getIntentSender());

        // Cannot build an empty dataset, so associate each password field with the empty string (to be overwritten upon password generation)
        for (AutofillId id : passwordFieldAutofillIDs) {
            builder.setValue(id, AutofillValue.forText(""));
        }

        // return result into onSuccess callback
        FillResponse response = new FillResponse.Builder().addDataset(builder.build()).build();
        fillCallback.onSuccess(response);
    }

    @Override
    public void onSaveRequest(@NonNull SaveRequest saveRequest, @NonNull SaveCallback saveCallback) {

    }

    private void findPasswordFields(AssistStructure.ViewNode node, List<AutofillId> passwordFieldAutofillIDs) {
        if (isPasswordField(node)) {
            passwordFieldAutofillIDs.add(node.getAutofillId());
        }

        // Recursively search for password fields in children nodes
        for (int i = 0; i < node.getChildCount(); i++) {
            findPasswordFields(node.getChildAt(i), passwordFieldAutofillIDs);
        }
    }

    private boolean isPasswordField(AssistStructure.ViewNode node) {
        // Try to detect first through autofill hints directly
        String[] autofillHints = node.getAutofillHints();
        if (autofillHints != null && autofillHints.length > 0) {
            return Arrays.asList(autofillHints).contains("password");
        }

        // Then try through the traditional hint
        String hint = node.getHint();
        if (hint != null) {
            return node.getHint().toLowerCase().contains("password");
        }

        // If both of the above are not set,
        // a password field is an EditText with an explicit password input type
        return node.getClassName().contains("EditText") &&
                (node.getInputType() == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                node.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                node.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }
}