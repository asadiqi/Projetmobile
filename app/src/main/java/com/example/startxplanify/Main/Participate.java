package com.example.startxplanify.Main;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
public class Participate {

    private Context context;
    private Button participateButton;

    public Participate(Context context, Button participateButton) {
        this.context = context;
        this.participateButton = participateButton;
    }

    public void setupParticipateButton() {
        participateButton.setVisibility(View.GONE);
        participateButton.setOnClickListener(v -> {
            Toast.makeText(context, "You have successfully participated in this task!", Toast.LENGTH_SHORT).show();
        });
    }
}
