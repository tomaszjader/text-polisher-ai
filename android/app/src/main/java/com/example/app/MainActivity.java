package com.example.app;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private EditText inputText;
    private TextView outputText;
    private Button correctButton;
    private Button clearButton;
    private Button copyButton;
    private ImageButton settingsButton;
    private ProgressBar progressBar;
    
    private PreferencesManager preferencesManager;
    private TextCorrectionService correctionService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize preferences and services
        preferencesManager = new PreferencesManager(this);
        correctionService = new TextCorrectionService(preferencesManager);
        
        // Initialize UI components
        initializeViews();
        setupClickListeners();
        
        // Handle shared text from other apps
        handleSharedText();
        
        // Check if API key is configured
        if (!preferencesManager.hasApiKey()) {
            showApiKeyDialog();
        }
    }
    
    private void initializeViews() {
        inputText = findViewById(R.id.inputText);
        outputText = findViewById(R.id.outputText);
        correctButton = findViewById(R.id.correctButton);
        clearButton = findViewById(R.id.clearButton);
        copyButton = findViewById(R.id.copyButton);
        settingsButton = findViewById(R.id.settingsButton);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupClickListeners() {
        correctButton.setOnClickListener(v -> correctText());
        clearButton.setOnClickListener(v -> clearText());
        copyButton.setOnClickListener(v -> copyToClipboard());
        settingsButton.setOnClickListener(v -> showApiKeyDialog());
    }
    
    private void handleSharedText() {
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
            CharSequence sharedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
            if (sharedText != null) {
                inputText.setText(sharedText.toString());
            }
        }
        
        // Handle text from URL parameter (if any)
        Uri data = intent.getData();
        if (data != null) {
            String text = data.getQueryParameter("text");
            if (text != null) {
                inputText.setText(Uri.decode(text));
            }
        }
    }
    
    private void correctText() {
        String input = inputText.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Wprowadź tekst do poprawienia", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showProgress(true);
        correctionService.correctText(input, new TextCorrectionService.CorrectionCallback() {
            @Override
            public void onSuccess(String correctedText) {
                runOnUiThread(() -> {
                    showProgress(false);
                    outputText.setText(correctedText);
                    copyButton.setVisibility(View.VISIBLE);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(MainActivity.this, "Błąd: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void clearText() {
        inputText.setText("");
        outputText.setText("");
        copyButton.setVisibility(View.GONE);
    }
    
    private void copyToClipboard() {
        String text = outputText.getText().toString();
        if (!text.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Poprawiony tekst", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Skopiowano do schowka", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        correctButton.setEnabled(!show);
    }
    
    private void showApiKeyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ustawienia API");
        builder.setMessage("Wprowadź swój klucz API OpenAI. Zostanie zapisany lokalnie na urządzeniu.");
        
        final EditText input = new EditText(this);
        input.setHint("sk-...");
        input.setText(preferencesManager.getApiKey());
        builder.setView(input);
        
        builder.setPositiveButton("Zapisz", (dialog, which) -> {
            String apiKey = input.getText().toString().trim();
            if (!apiKey.isEmpty()) {
                preferencesManager.saveApiKey(apiKey);
                Toast.makeText(this, "Klucz API zapisany", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Podaj klucz API", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.cancel());
        
        if (!preferencesManager.hasApiKey()) {
            builder.setCancelable(false);
        }
        
        builder.show();
    }
}
