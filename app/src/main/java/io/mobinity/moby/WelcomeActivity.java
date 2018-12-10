package io.mobinity.moby;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button mLoginButton = findViewById(R.id.btn_login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goLogin();
            }
        });

        Button mJoinButton = findViewById(R.id.btn_join);
        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goJoin();
            }
        });
    }

    public void goLogin() {
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        //finish();
    }

    public void goJoin() {
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        //finish();
    }
}
