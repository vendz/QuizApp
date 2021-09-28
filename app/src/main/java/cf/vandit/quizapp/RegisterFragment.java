package cf.vandit.quizapp;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment implements View.OnClickListener {

    // UI element
    private TextView feedbackText, login_now_btn;
    private TextInputEditText regEmail, regPassword, regConfirmPass;
    private TextInputLayout regEmailLayout, regPasswordLayout, regConfirmPassLayout;
    private Button register_btn;
    private ProgressBar progressBar;
    private ConstraintLayout rootLayout;

    // firebase authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    // initialize NavController
    private NavController navController;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        navController = Navigation.findNavController(view);

        feedbackText = view.findViewById(R.id.registration_feedback_text);
        regEmail = view.findViewById(R.id.regEmailInputField);
        regPassword = view.findViewById(R.id.regPassInputField);
        regConfirmPass = view.findViewById(R.id.regConfirmPassInputField);
        regEmailLayout = view.findViewById(R.id.regEmailInputLayout);
        regPasswordLayout = view.findViewById(R.id.regPassInputLayout);
        regConfirmPassLayout = view.findViewById(R.id.regConfirmPassInputLayout);
        register_btn = view.findViewById(R.id.register_button);
        login_now_btn = view.findViewById(R.id.login_now_btn);
        login_now_btn.setOnClickListener(this);
        progressBar = view.findViewById(R.id.register_progressBar);
        rootLayout = view.findViewById(R.id.register_root_layout);

        regEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if(!TextUtils.isEmpty(regEmail.getText().toString().trim())) {
                    if(regEmailLayout.getError() == getString(R.string.empty_email_error)) {
                        regEmailLayout.setError(null);
                    }
                }

                if(regEmail.getText().toString().trim().contains("@")) {
                    if(regEmailLayout.getError() == getString(R.string.invalid_email_error)) {
                        regEmailLayout.setError(null);
                    }
                }
            }
        });

        regPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if(!TextUtils.isEmpty(regPassword.getText())) {
                    if(regPasswordLayout.getError() == getString(R.string.empty_password_error)) {
                        regPasswordLayout.setError(null);
                    }
                }
            }
        });

        regConfirmPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if(regPassword.getText().toString().trim().equals(regConfirmPass.getText().toString().trim())) {
                    if(regConfirmPassLayout.getError() == getString(R.string.passwords_unmatch_error)) {
                        regConfirmPassLayout.setError(null);
                    }
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser == null) {
            registerUser();
        } else {
            if(currentUser.isEmailVerified()) {
                // navigate to home page
                navController.navigate(R.id.action_registerFragment_to_listFragment);
            } else {
                Snackbar snackbar = Snackbar.make(rootLayout, "Email is not verified", Snackbar.LENGTH_LONG);
                snackbar.setBackgroundTint(Color.WHITE);
                snackbar.setTextColor(Color.BLACK);
                snackbar.show();
                firebaseAuth.signOut();
            }
        }
    }

    private void registerUser() {
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedbackText.setVisibility(View.INVISIBLE);
                String email = regEmail.getText().toString().trim();
                String password = regPassword.getText().toString().trim();
                String confirm_password = regConfirmPass.getText().toString().trim();

                if(TextUtils.isEmpty(email)) {
                    regEmailLayout.setError(getString(R.string.empty_email_error));
                } else if(!(email.contains("@"))) {
                    regEmailLayout.setError(getString(R.string.invalid_email_error));
                } else if(TextUtils.isEmpty(password)) {
                    regPasswordLayout.setError(getString(R.string.empty_password_error));
                } else if(!(password.equals(confirm_password))) {
                    regConfirmPassLayout.setError(getString(R.string.passwords_unmatch_error));
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    register_btn.setEnabled(false);
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                assert user != null;
                                String user_email = user.getEmail();

                                // fetch user FCM token
                                final String[] token = new String[1];
                                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull Task<String> task) {
                                        if(task.isSuccessful()) {
                                            token[0] = task.getResult();

                                            // add user to database
                                            Map<String, Object> new_user = new HashMap<>();
                                            new_user.put("email", user_email);
                                            new_user.put("token", token[0]);
                                            new_user.put("is_admin", false);

                                            firebaseFirestore.collection("users").document(user.getEmail()).set(new_user);
                                        }
                                    }
                                });

                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Snackbar snackbar = Snackbar.make(rootLayout, "Verification Email Sent", Snackbar.LENGTH_LONG);
                                            snackbar.setBackgroundTint(Color.WHITE);
                                            snackbar.setTextColor(Color.BLACK);
                                            snackbar.setAction("ok", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    snackbar.dismiss();
                                                }
                                            });
                                            snackbar.setActionTextColor(Color.parseColor("#9875CB"));
                                            snackbar.show();

                                            progressBar.setVisibility(View.INVISIBLE);
                                            register_btn.setEnabled(true);
                                        } else {
                                            feedbackText.setText(task.getException().toString());
                                            feedbackText.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                register_btn.setEnabled(true);

                                if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    regEmailLayout.setError("Email already in use");
                                    regPasswordLayout.setError(null);
                                    regConfirmPassLayout.setError(null);
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.login_now_btn) {
            navController.navigate(R.id.action_registerFragment_to_startFragment);
        }
    }
}