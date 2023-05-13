package edu.cmu.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//
//
/*
Author: YU GU
Andrew ID: ygu3
This android app is used for getting user input and fetching f1 driver standings from the web service.
 */
public class MainActivity extends AppCompatActivity {
    EditText yearText, positionText;
    TextView result;
    Button submitButton, resetButton;
    String year, position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        yearText = findViewById(R.id.yearText);
        positionText = findViewById(R.id.positionText);
        submitButton = findViewById(R.id.submitButton);
        resetButton = findViewById(R.id.resetButton);
        result = findViewById(R.id.result);
        setupListeners();
    }

    void setupListeners() {
        // set submit button listener
        submitButton.setOnClickListener(v -> {
            year = yearText.getText().toString();
            position = positionText.getText().toString();
            // handle null input
            if (year.isEmpty()) {
                yearText.setHint(getString(R.string.yearHint));
            } else {
                ExecutorService service = Executors.newSingleThreadExecutor();
                service.execute(new Runnable() {
                    String response = "";
                    String resString = "";

                    @Override
                    public void run() {
                        // fetch response from the url
                        try {
                            URL url = new URL("https://yugu-yg-silver-dollop-746wq7476grhr675-8080.preview.app.github.dev/getDriverStandings?year=" + year + "&position=" + position);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                            String str;
                            while ((str = in.readLine()) != null) {
                                response += str;
                            }
                            resString = parse(response);
                            in.close();
                        } catch (Exception e) {
                            System.out.println("Fetching from web service unavailable");
                            v.invalidate();
                        }
                        // update the result textview
                        runOnUiThread(() -> {
                            try {
                                // Set the output.
                                result.setHint(resString);
                            } catch (Exception e) {
                                v.invalidate();
                            }
                        });
                    }
                });
            }
        });
        // set reset button listener
        resetButton.setOnClickListener(v -> {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(() -> runOnUiThread(() -> {
                try {
                    result.setText(null);
                    result.setHint(getString(R.string.resultHint));
                    yearText.setText(null);
                    yearText.setHint(getString(R.string.yearHint));
                    positionText.setText(null);
                    positionText.setHint(getString(R.string.positionHint));
                } catch (Exception e) {
                    v.invalidate();
                }
            }));
        });
    }

    // This method is used to parse json response into string that can be shown in the result textview.
    public String parse(String response) {
        String result = "";
        try {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = (JSONObject) array.get(i);
                String name = object.getString("name");
                String constructor = object.getString("constructor");
                String position = object.getString("position");
                String points = object.getString("points");
                String s = "Rank " + position + ": " + name + " from " + constructor + " get " + points + " points." + "\n";
                result += s;
            }
        } catch (Exception e) {
            System.out.println("result exception");
        }
        return result;
    }
}