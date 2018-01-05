package com.example.jevil.autoclub.Views;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jevil.autoclub.Models.UserModel;
import com.example.jevil.autoclub.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AuthDialog extends Dialog implements View.OnClickListener{

    public AuthDialog(Context context) {
        super(context);
        this.context = context;
    }

    // вьюшки
    private Button btnCreate;
    private TextView tvChangeType;
    private EditText etNickname, etEmail,etPassword;
    private Context context;
    public ProgressDialog mProgressDialog;
    private TextInputLayout tilNickname;

    // переменные
    private boolean isCreate = true;
    private final String TAG = "myLog";

    // работа с БД
    // получаем нашу базу
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    // ссылка на логин пользователя
    DatabaseReference usersRef = database.getReference("Users");

    // получаем доступ к авторизации
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCreate:
                if (isCreate) // нажатие на кнопку "Создать аккаунт"
                    createAccount(etEmail.getText().toString(), etPassword.getText().toString(), etNickname.getText().toString());
                else // нажатие на кнопку "Вход"
                    signIn(etEmail.getText().toString(), etPassword.getText().toString());
                break;
            case R.id.tvChangeType: // меняем создание аккаунта / вход
                if (isCreate) { // текущее создать
                    tvChangeType.setText(R.string.tv_create);
                    btnCreate.setText(R.string.btn_sign_in);
                    tilNickname.setVisibility(View.GONE);
                } else { // текущее войти
                    tvChangeType.setText(R.string.tv_sign_in);
                    btnCreate.setText(R.string.btn_create);
                    tilNickname.setVisibility(View.VISIBLE);
                }
                isCreate = !isCreate;
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_layout);

        btnCreate = findViewById(R.id.btnCreate);
        btnCreate.setOnClickListener(this);

        tvChangeType = findViewById(R.id.tvChangeType);
        tvChangeType.setOnClickListener(this);

        etNickname = findViewById(R.id.etNickname);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tilNickname = findViewById(R.id.tilNickname);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void createAccount(String email, String password, String nickname) {
        if (!validateForm()) { // проверяем правильность заполненных форм
            return;
        }

        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { // успешная регистрация
                            usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(new UserModel(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                            FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                                            "online", // устанавливаем статус "online"
                                            etNickname.getText().toString(),
                                            "") // устанавливаем nickname
                                    );
                            dismiss(); // закрываем диалог

                        } else { // если регистрация прошла не успешно
                            Toast.makeText(context, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        hideProgressDialog();

                    }
                });
    }

    private void signIn(String email, String password) {
        if (!validateForm()) {// проверяем правильность заполненных форм
            return;
        }

        showProgressDialog();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            usersRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status")
                                    .setValue("online");
                            dismiss();
                        } else {
                            Toast.makeText(context, "Authentication failed. " + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        hideProgressDialog();
                    }
                });
    }

    private boolean validateForm() { // проверяем правильность введенных данных
        boolean valid = true;

        String nickname = etNickname.getText().toString();
        if ((isCreate) && (TextUtils.isEmpty(nickname)))
            etNickname.setError("Поле не должно быть пустым");
        else
            etNickname.setError(null);

        String email = etEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Поле не должно быть пустым");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Неккоректный формат почты");
            valid = false;
        } else {
            etEmail.setError(null);
        }

        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Поле не должно быть пустым");
            valid = false;
        } else if (password.length() < 6) {
            etPassword.setError("Не менее 6 символов");
            valid = false;
        } else {
            etPassword.setError(null);
        }

        return valid;
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(context.getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}