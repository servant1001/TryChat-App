package com.example.user.trychat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.widget.Toolbar;
import android.support.v7.widget.Toolbar;

import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class Login_Activity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;
    private EditText mEtPassword;
    private Button mLogin_btn,mBtnPassword;
    private TextView mCreateAccount;
    private boolean mbDisplayFlg = false;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private ProgressBar mProgressbar_cycle;

    private LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");

        mProgressbar_cycle = findViewById(R.id.progressBar_cyclic);
        mProgressbar_cycle.setVisibility(View.GONE);

        animationView = findViewById(R.id.animation_view);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mLoginEmail = findViewById(R.id.login_email);
        mLoginPassword = findViewById(R.id.login_password);
        mEtPassword = findViewById(R.id.et_login_password);
        mCreateAccount = findViewById(R.id.create_account);
        mLogin_btn = findViewById(R.id.login_btn);

        //到創建帳號頁面
        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reg_intent = new Intent(Login_Activity.this,RegisterActivity.class);
                startActivity(reg_intent);
            }
        });
        //登入按鈕
        mLogin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = mLoginEmail.getEditText().getText().toString();
                //String password = mLoginPassword.getEditText().getText().toString();
                String password = mEtPassword.getText().toString();//為了加eye button做了修改

                //EditText判斷有沒有輸入字元
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    loginUser(email, password);
                }else{
                    Toast.makeText(Login_Activity.this, "Please check your format again...", Toast.LENGTH_LONG).show();
                }
            }
        });

        //密碼顯示按鈕
        mBtnPassword = findViewById(R.id.login_button_eye);
        mBtnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mbDisplayFlg) {
                    mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                mbDisplayFlg = !mbDisplayFlg;
                mEtPassword.postInvalidate();
            }
        });
    }

    //上傳Firebase 登入
    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    mProgressbar_cycle.setVisibility(View.VISIBLE);

                    String current_user_id = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent mainIntent = new Intent(Login_Activity.this, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();
                        }
                    });

                } else {

                    mProgressbar_cycle.setVisibility(View.GONE);

                    String task_result = task.getException().getMessage().toString();

                    Toast.makeText(Login_Activity.this, "Error : " + task_result, Toast.LENGTH_LONG).show();

                }
            }
        });
    }
}