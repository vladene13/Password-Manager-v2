package com.example.passwordmanagerv2;

import android.app.assist.AssistStructure;
import android.os.Build;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveInfo;
import android.service.autofill.SaveRequest;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;
import androidx.annotation.RequiresApi;

import com.example.passwordmanagerv2.data.entity.SavedPassword;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PasswordAutofillService extends AutofillService {

    private static final String TAG = "PasswordAutofill";

    @Override
    public void onFillRequest(FillRequest request, CancellationSignal cancellationSignal, FillCallback callback) {
        List<FillContext> context = request.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();

        ParsedStructure parsedStructure = parseStructure(structure);
        if (parsedStructure.webDomain != null) {
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            List<SavedPassword> savedPasswords = dbHelper.getActivePasswords();

            FillResponse.Builder responseBuilder = new FillResponse.Builder();

            for (SavedPassword password : savedPasswords) {
                if (parsedStructure.webDomain.contains(password.getSiteName().toLowerCase()) ||
                        password.getSiteName().toLowerCase().contains(parsedStructure.webDomain)) {

                    Dataset.Builder dataset = new Dataset.Builder();

                    if (parsedStructure.usernameId != null) {
                        dataset.setValue(parsedStructure.usernameId,
                                AutofillValue.forText(password.getUsername()),
                                createPresentation(password.getUsername()));
                    }

                    if (parsedStructure.passwordId != null) {
                        String decryptedPassword = dbHelper.decryptPassword(password.getEncryptedPassword());
                        dataset.setValue(parsedStructure.passwordId,
                                AutofillValue.forText(decryptedPassword),
                                createPresentation("Password"));
                    }

                    responseBuilder.addDataset(dataset.build());
                }
            }
            callback.onSuccess(responseBuilder.build());
        }
    }
    @Override
    public void onSaveRequest(SaveRequest request, SaveCallback callback) {
        List<FillContext> context = request.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();

        ParsedStructure parsedStructure = parseStructure(structure);

        if (parsedStructure.usernameId == null || parsedStructure.passwordId == null ||
                parsedStructure.usernameValue == null || parsedStructure.passwordValue == null) {
            callback.onSuccess();
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.savePassword(parsedStructure.webDomain,
                parsedStructure.usernameValue,
                parsedStructure.passwordValue);

        callback.onSuccess();
    }

    private RemoteViews createPresentation(String text) {
        RemoteViews presentation = new RemoteViews(getPackageName(),
                android.R.layout.simple_list_item_1);
        presentation.setTextViewText(android.R.id.text1, text);
        return presentation;
    }

    private static class ParsedStructure {
        AutofillId usernameId;
        AutofillId passwordId;
        String usernameValue;
        String passwordValue;
        String webDomain;
    }

    private ParsedStructure parseStructure(AssistStructure structure) {
        ParsedStructure result = new ParsedStructure();

        for (int i = 0; i < structure.getWindowNodeCount(); i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            AssistStructure.ViewNode viewNode = windowNode.getRootViewNode();

            // Get domain from web browser
            result.webDomain = viewNode.getWebDomain();
            if (result.webDomain != null) {
                Log.d(TAG, "Found web domain: " + result.webDomain);
                traverseNode(viewNode, result);
                break;
            }
        }
        return result;
    }


    private void traverseNode(AssistStructure.ViewNode viewNode, ParsedStructure result) {
        if (isUsernameField(viewNode)) {
            result.usernameId = viewNode.getAutofillId();
            if (viewNode.getAutofillValue() != null) {
                result.usernameValue = viewNode.getAutofillValue().getTextValue().toString();
            }
            Log.d(TAG, "Found username field");
        }

        if (isPasswordField(viewNode)) {
            result.passwordId = viewNode.getAutofillId();
            if (viewNode.getAutofillValue() != null) {
                result.passwordValue = viewNode.getAutofillValue().getTextValue().toString();
            }
            Log.d(TAG, "Found password field");
        }

        for (int i = 0; i < viewNode.getChildCount(); i++) {
            traverseNode(viewNode.getChildAt(i), result);
        }
    }

    private boolean isUsernameField(AssistStructure.ViewNode node) {
        String[] hints = node.getAutofillHints();
        if (hints != null) {
            for (String hint : hints) {
                if (View.AUTOFILL_HINT_USERNAME.equals(hint) ||
                        View.AUTOFILL_HINT_EMAIL_ADDRESS.equals(hint)) {
                    return true;
                }
            }
        }

        String idEntry = node.getIdEntry();
        if (idEntry != null) {
            idEntry = idEntry.toLowerCase();
            return idEntry.contains("email") || idEntry.contains("username") ||
                    idEntry.contains("user") || idEntry.contains("login");
        }

        return false;
    }

    private boolean isPasswordField(AssistStructure.ViewNode node) {
        Log.d(TAG, "Checking field - InputType: " + node.getInputType());
        Log.d(TAG, "Hints: " + (node.getAutofillHints() != null ? String.join(",", node.getAutofillHints()) : "null"));
        Log.d(TAG, "IdEntry: " + node.getIdEntry());
        Log.d(TAG, "Hint Text: " + node.getHint());
        Log.d(TAG, "Class Name: " + node.getClassName());

        // Input type check
        int inputType = node.getInputType();
        boolean isPasswordInputType = (inputType & InputType.TYPE_CLASS_TEXT) != 0 &&
                ((inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) != 0 ||
                        (inputType & InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD) != 0 ||
                        (inputType & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) != 0);

        if (isPasswordInputType) {
            Log.d(TAG, "Identified as password field by input type");
            return true;
        }

        // Autofill hints check
        if (node.getAutofillHints() != null) {
            for (String hint : node.getAutofillHints()) {
                if (View.AUTOFILL_HINT_PASSWORD.equals(hint)) {
                    Log.d(TAG, "Identified as password field by autofill hint");
                    return true;
                }
            }
        }

        return false;
    }
}