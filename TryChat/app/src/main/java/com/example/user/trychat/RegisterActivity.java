package com.example.user.trychat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
//import android.widget.Toolbar;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private EditText mEtPassword,mEtCheckPassword;
    private Button mCreateBtn;
    private Button mBtnPassword,mBtnCheckPassword;
    private boolean mbDisplayFlg = false;
    private boolean mbDisplayFlg2 = false;

    //Firebase Auth
    private FirebaseAuth mAuth;

    private Toolbar mToolbar;

    private DatabaseReference mDatabase;

    private ProgressBar mProgressbar_cycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolbar Set
        mToolbar = (Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressbar_cycle = (ProgressBar) findViewById(R.id.progressBar_cyclic);
        mProgressbar_cycle.setVisibility(View.GONE);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Android Field
        mDisplayName = (TextInputLayout)findViewById(R.id.reg_displayname);
        mEmail = (TextInputLayout)findViewById(R.id.reg_email);
        mPassword = (TextInputLayout)findViewById(R.id.reg_password);
        mEtPassword = findViewById(R.id.et_register_password);
        mEtCheckPassword = findViewById(R.id.et_register_checkpassword);
        mCreateBtn = (Button)findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                //String password = mPassword.getEditText().getText().toString();
                String password = mEtPassword.getText().toString();//為了加eye button做了修改
                String checkpassword = mEtCheckPassword.getText().toString();

                //判斷Edit 是否空白
                if (!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {
                    if (password.equals(checkpassword)){
                        mProgressbar_cycle.setVisibility(View.VISIBLE);
                        register_user(display_name, email, password);//用來放firebase create new user account (assistant Step 4.)
                    }else {
                        Toast.makeText(RegisterActivity.this,"[Error] Please check password.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(RegisterActivity.this,"[Error] Please check the form and try again.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //密碼顯示按鈕
        mBtnPassword = findViewById(R.id.register_button_eye);
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


        mBtnCheckPassword = findViewById(R.id.register_button_eye2);
        mBtnCheckPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mbDisplayFlg2) {
                    mEtCheckPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    mEtCheckPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                mbDisplayFlg2 = !mbDisplayFlg2;
                mEtCheckPassword.postInvalidate();
            }
        });
    }

    private void register_user(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    //Part 9 Database
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", display_name);
                    userMap.put("status", "Hi there I'm using Try Chat App.");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");
                    //mDatabase.setValue(userMap);//設定完成 啟動 (這行跟下面這段差別在判斷是否完成，和可命令完成後要做甚麼)
                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);//成功就回MainActivity
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });

                }else{
                    mProgressbar_cycle.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this,"You got some error. Please check the form and try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
