package com.example.whatdoisee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TutorialActivity extends AppCompatActivity {

    private int tutorialStep = 0 ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial);
        tutorial();
    }

    protected void tutorial() {
        TextView circleExplain = findViewById(R.id.circleExplain);
        TextView buttonExplain = findViewById(R.id.buttonExplain);
        TextView moveToMapExplain = findViewById(R.id.moveToMapExplain);
        TextView backToCameraExplain = findViewById(R.id.backToCameraExplain);
        Button nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this::nextStepInTutorial);
        switch (tutorialStep) {
            case 0:
                circleExplain.setVisibility(View.VISIBLE);
                buttonExplain.setVisibility(View.INVISIBLE);
                moveToMapExplain.setVisibility(View.INVISIBLE);
                backToCameraExplain.setVisibility(View.INVISIBLE);
                break;
            case 1:
                circleExplain.setVisibility(View.INVISIBLE);
                buttonExplain.setVisibility(View.VISIBLE);
                moveToMapExplain.setVisibility(View.INVISIBLE);
                backToCameraExplain.setVisibility(View.INVISIBLE);
                break;
            case 2:
                circleExplain.setVisibility(View.INVISIBLE);
                buttonExplain.setVisibility(View.INVISIBLE);
                moveToMapExplain.setVisibility(View.VISIBLE);
                backToCameraExplain.setVisibility(View.INVISIBLE);
                break;
            case 3:
                circleExplain.setVisibility(View.INVISIBLE);
                buttonExplain.setVisibility(View.INVISIBLE);
                moveToMapExplain.setVisibility(View.INVISIBLE);
                backToCameraExplain.setVisibility(View.VISIBLE);
                break;
            case 4:
                startActivity(new Intent(TutorialActivity.this, MainActivity.class));
                break;
        }
    }

    public void nextStepInTutorial(View view) {
        tutorialStep++;
        tutorial();
    }
}
