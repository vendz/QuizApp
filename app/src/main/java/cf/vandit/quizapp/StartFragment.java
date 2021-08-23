package cf.vandit.quizapp;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.TextUtils;
import android.util.Log;
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

public class StartFragment extends Fragment implements View.OnClickListener{

    // UI element
    private TextView feedbackText, register_now_btn, forgot_password;
    private TextInputEditText logEmail, logPassword;
    private Button login_btn;
    private ProgressBar progressBar;

    // firebase authentication
    private FirebaseAuth firebaseAuth;

    // initialize NavController
    private NavController navController;

    public StartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        navController = Navigation.findNavController(view);

        feedbackText = view.findViewById(R.id.start_feedback_text);
        logEmail = view.findViewById(R.id.logEmailInputField);
        logPassword = view.findViewById(R.id.logPasswordInputField);
        login_btn = view.findViewById(R.id.login_Button);
        register_now_btn = view.findViewById(R.id.register_now_btn);
        register_now_btn.setOnClickListener(this);
        forgot_password = view.findViewById(R.id.forgot_pass_btn);
        forgot_password.setOnClickListener(this);

        progressBar = view.findViewById(R.id.start_progressBar);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser == null) {
            loginUser();
        } else {
            // navigate to home page
            navController.navigate(R.id.action_startFragment_to_listFragment);
        }
    }

    private void loginUser() {
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedbackText.setVisibility(View.INVISIBLE);
                String email = logEmail.getText().toString();
                String password = logPassword.getText().toString();

                if(TextUtils.isEmpty(email)) {
                    feedbackText.setText("Email cannot be empty");
                    feedbackText.setTextColor(Color.RED);
                    feedbackText.setVisibility(View.VISIBLE);
                    logEmail.requestFocus();
                } else if(TextUtils.isEmpty(password)) {
                    feedbackText.setText("Password cannot be empty");
                    feedbackText.setTextColor(Color.RED);
                    feedbackText.setVisibility(View.VISIBLE);
                    logPassword.requestFocus();
                } else if(!(email.contains("@"))) {
                    feedbackText.setText("Please enter a valid Email address");
                    feedbackText.setTextColor(Color.RED);
                    feedbackText.setVisibility(View.VISIBLE);
                    logEmail.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    login_btn.setEnabled(false);
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                navController.navigate(R.id.action_startFragment_to_listFragment);
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                login_btn.setEnabled(true);

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
        if(view.getId() == R.id.register_now_btn) {
            navController.navigate(R.id.action_startFragment_to_registerFragment);
        } else if(view.getId() == R.id.forgot_pass_btn) {
            ResetPassDialog resetPassDialog = new ResetPassDialog();
            resetPassDialog.show(getFragmentManager(), "Password Reset");
        }
    }
}