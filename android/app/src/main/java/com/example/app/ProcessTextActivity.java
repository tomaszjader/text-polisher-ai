package com.example.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ProcessTextActivity extends Activity {

    private static final String TAG = "ProcessTextActivity";
    private PreferencesManager preferencesManager;
    private TextCorrectionService correctionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Log.d(TAG, "ProcessTextActivity started");

            // Initialize preferences and services using the same classes as MainActivity
            preferencesManager = new PreferencesManager(this);
            correctionService = new TextCorrectionService(preferencesManager);

            Intent intent = getIntent();
            if (intent == null) {
                Log.e(TAG, "Intent is null");
                setResult(Activity.RESULT_CANCELED);
                finish();
                return;
            }

            String action = intent.getAction();
            Log.d(TAG, "Intent action: " + action);

            if (!Intent.ACTION_PROCESS_TEXT.equals(action)) {
                Log.e(TAG, "Wrong action: " + action);
                setResult(Activity.RESULT_CANCELED);
                finish();
                return;
            }

            CharSequence inputText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            boolean readonly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);
            String input = inputText != null ? inputText.toString() : "";

            Log.d(TAG, "Input text length: " + input.length());
            Log.d(TAG, "Readonly mode: " + readonly);
            Log.d(TAG, "Has API key: " + preferencesManager.hasApiKey());

            // If empty input, just return immediately
            if (input.isEmpty()) {
                Log.w(TAG, "Empty input, returning without changes");
                deliverResult(input);
                return;
            }

            // If source is read-only, open our main UI with the text instead of replacing
            if (readonly) {
                Log.i(TAG, "Source is read-only; launching MainActivity to display text");
                Intent viewer = new Intent(this, MainActivity.class);
                viewer.setAction(Intent.ACTION_PROCESS_TEXT);
                viewer.putExtra(Intent.EXTRA_PROCESS_TEXT, input);
                viewer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(viewer);
                setResult(Activity.RESULT_CANCELED);
                finish();
                return;
            }

            // Correct the text using the same service as MainActivity
            correctionService.correctText(input, new TextCorrectionService.CorrectionCallback() {
                @Override
                public void onSuccess(String correctedText) {
                    Log.d(TAG, "Text correction successful, output length: " + correctedText.length());
                    deliverResult(correctedText);
                }

                @Override
                public void onError(String error) {
                    Log.w(TAG, "Text correction failed: " + error);
                    // On error, return the original text
                    deliverResult(input);
                }
            });

        } catch (Throwable t) {
            Log.e(TAG, "Unexpected error in onCreate", t);
            safeFinishWithOriginal();
        }
    }

    private void deliverResult(String output) {
        try {
            Intent result = new Intent();
            result.putExtra(Intent.EXTRA_PROCESS_TEXT, output);
            setResult(Activity.RESULT_OK, result);
            Log.d(TAG, "Result set, finishing activity");
            finish();
        } catch (Throwable t) {
            Log.e(TAG, "Error delivering result", t);
            safeFinishWithOriginal();
        }
    }

    private void safeFinishWithOriginal() {
        try {
            Intent intent = getIntent();
            String input = intent != null ? String.valueOf(intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)) : "";
            Intent result = new Intent();
            result.putExtra(Intent.EXTRA_PROCESS_TEXT, input);
            setResult(Activity.RESULT_OK, result);
        } catch (Throwable t) {
            Log.e(TAG, "Error setting original result", t);
            setResult(Activity.RESULT_CANCELED);
        } finally {
            finish();
        }
    }
}