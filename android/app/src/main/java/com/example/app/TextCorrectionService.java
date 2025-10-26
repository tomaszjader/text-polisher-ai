package com.example.app;

import android.os.AsyncTask;
import android.util.Log;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TextCorrectionService {
    private static final String TAG = "TextCorrectionService";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    
    private final OkHttpClient client;
    private final PreferencesManager preferencesManager;
    
    public interface CorrectionCallback {
        void onSuccess(String correctedText);
        void onError(String error);
    }
    
    public TextCorrectionService(PreferencesManager preferencesManager) {
        this.preferencesManager = preferencesManager;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    
    public void correctText(String inputText, CorrectionCallback callback) {
        if (inputText == null || inputText.trim().isEmpty()) {
            callback.onError("Tekst nie może być pusty");
            return;
        }
        
        String apiKey = preferencesManager.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty() || "YOUR_OPENAI_API_KEY_HERE".equals(apiKey)) {
            Log.w(TAG, "API key not configured, using local fallback");
            String localResult = correctTextLocally(inputText);
            callback.onSuccess(localResult);
            return;
        }
        
        // Use OpenAI API
        new OpenAITask(apiKey, callback, inputText).execute(inputText);
    }
    
    private String correctTextLocally(String input) {
        if (input == null) return "";
        String s = input;
        
        // Normalize whitespace
        s = s.replaceAll("\r\n", "\n");
        s = s.replaceAll("\t", " ");
        s = s.replaceAll("\\s+", " ").trim();
        
        // Fix spaces before punctuation
        s = s.replaceAll(" \\.", ".");
        s = s.replaceAll(" ,", ",");
        s = s.replaceAll(" ;", ";");
        s = s.replaceAll(" !", "!");
        s = s.replaceAll(" \\?", "?");
        
        // Capitalize first letter
        if (!s.isEmpty()) {
            char first = s.charAt(0);
            char upper = Character.toUpperCase(first);
            if (first != upper) {
                s = upper + (s.length() > 1 ? s.substring(1) : "");
            }
        }
        
        // Ensure ending punctuation
        if (!s.isEmpty() && !s.matches(".*[\\.!\\?]$")) {
            s = s + ".";
        }
        
        return s;
    }
    
    private class OpenAITask extends AsyncTask<String, Void, String> {
        private final String apiKey;
        private final CorrectionCallback callback;
        private final String inputText;
        private String errorMessage;
        
        public OpenAITask(String apiKey, CorrectionCallback callback, String inputText) {
            this.apiKey = apiKey;
            this.callback = callback;
            this.inputText = inputText;
        }
        
        @Override
        protected String doInBackground(String... params) {
            String input = params[0];
            
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "gpt-4o-mini");
                requestBody.put("max_tokens", 2000);
                requestBody.put("temperature", 0.3);
                
                JSONArray messages = new JSONArray();
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                String systemContent =
                        "You are a proofreader and will correct my typos in the text. You will only return the corrected text, nothing else.\n" +
                        "Task and objective:\n" +
                        "* Correcting typos, spelling, punctuation, and grammatical errors in the text provided by the user.\n" +
                        "* Return only the corrected version of the text, without any additional comments, explanations, or questions.\n" +
                        "Behavior and rules:\n" +
                        "1) Receiving the text:\n" +
                        "a) Wait for the text from the user that needs proofreading.\n" +
                        "b) Do not initiate a conversation or ask questions.\n" +
                        "2) Correction and return:\n" +
                        "a) Carefully correct the text for typos, spelling, grammar, and punctuation.\n" +
                        "b) Return the entire text after correction.\n" +
                        "c) Make sure that the reply contains only the corrected text. Do not add any \"Please,\" \"Here is the corrected text,\" or similar phrases.\n" +
                        "3) Tone and style:\n" +
                        "a) Be neutral and impersonal.\n" +
                        "b) Your \"personality\" is to be a quiet but effective tool for proofreading text.\n" +
                        "Text to be corrected:";
                systemMessage.put("content", systemContent);
                
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", input);
                
                messages.put(systemMessage);
                messages.put(userMessage);
                requestBody.put("messages", messages);
                
                RequestBody body = RequestBody.create(
                        requestBody.toString(),
                        MediaType.get("application/json; charset=utf-8")
                );
                
                Request request = new Request.Builder()
                        .url(API_URL)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .addHeader("Content-Type", "application/json")
                        .build();
                
                Response response = client.newCall(request).execute();
                
                if (!response.isSuccessful()) {
                    errorMessage = "API error: " + response.code();
                    return null;
                }
                
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);
                
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    return message.getString("content").trim();
                }
                
                errorMessage = "No response from API";
                return null;
                
            } catch (Exception e) {
                Log.e(TAG, "OpenAI API call failed", e);
                errorMessage = "Network error: " + e.getMessage();
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Log.d(TAG, "OpenAI success, output length: " + result.length());
                callback.onSuccess(result);
            } else {
                Log.w(TAG, "OpenAI failed: " + errorMessage + ", using fallback");
                String localResult = correctTextLocally(inputText);
                callback.onSuccess(localResult);
            }
        }
    }
}