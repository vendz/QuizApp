package cf.vandit.quizapp;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterFragment extends Fragment implements View.OnClickListener {

    // UI element
    private TextView feedbackText, login_now_btn;
    private TextInputEditText regEmail, regPassword, regConfirmPass;
    private Button register_btn;
    private ProgressBar progressBar;

    // firebase authentication
    private FirebaseAuth firebaseAuth;

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

        navController = Navigation.findNavController(view);

        feedbackText = view.findViewById(R.id.registration_feedback_text);
        regEmail = view.findViewById(R.id.regEmailInputField);
        regPassword = view.findViewById(R.id.regPassInputField);
        regConfirmPass = view.findViewById(R.id.regConfirmPassInputField);
        register_btn = view.findViewById(R.id.register_button);
        login_now_btn = view.findViewById(R.id.login_now_btn);
        login_now_btn.setOnClickListener(this);
        progressBar = view.findViewById(R.id.register_progressBar);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser == null) {
            registerUser();
        } else {
            // navigate to home page
            navController.navigate(R.id.action_registerFragment_to_listFragment);
        }
    }

    private void registerUser() {
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedbackText.setVisibility(View.INVISIBLE);
                String email = regEmail.getText().toString();
                String password = regPassword.getText().toString();
                String confirm_password = regConfirmPass.getText().toString();

                if(TextUtils.isEmpty(email)) {
                    feedbackText.setText("Email cannot be empty");
                    feedbackText.setTextColor(Color.RED);
                    feedbackText.setVisibility(View.VISIBLE);
                    regEmail.requestFocus();
                } else if(TextUtils.isEmpty(password)) {
                    feedbackText.setText("Password cannot be empty");
                    feedbackText.setTextColor(Color.RED);
                    feedbackText.setVisibility(View.VISIBLE);
                    regPassword.requestFocus();
                } else if(!(password.equals(confirm_password))) {
                    feedbackText.setText("Passwords do not match");
                    feedbackText.setTextColor(Color.RED);
                    feedbackText.setVisibility(View.VISIBLE);
                    regConfirmPass.requestFocus();
                } else if(!(email.contains("@"))) {
                    feedbackText.setText("Please enter a valid Email address");
                    feedbackText.setTextColor(Color.RED);
                    feedbackText.setVisibility(View.VISIBLE);
                    regEmail.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    register_btn.setEnabled(false);
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                navController.navigate(R.id.action_registerFragment_to_listFragment);
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                register_btn.setEnabled(true);

                                feedbackText.setText(task.getException().toString());
                                feedbackText.setTextColor(Color.RED);
                                feedbackText.setVisibility(View.VISIBLE);
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