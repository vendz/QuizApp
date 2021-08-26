package cf.vandit.quizapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class StartFragment extends Fragment implements View.OnClickListener{

    // UI element
    private TextView feedbackText, register_now_btn, forgot_password;
    private TextInputEditText logEmail, logPassword;
    private Button login_btn;
    private ProgressBar progressBar;
    private ConstraintLayout constraintLayout;

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
        constraintLayout = view.findViewById(R.id.login_root_layout);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser == null) {
            loginUser();
        } else {
            if(currentUser.isEmailVerified()) {
                // navigate to home page
                navController.navigate(R.id.action_startFragment_to_listFragment);
            } else {
                showAlertDialog();
                firebaseAuth.signOut();
            }
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
                    feedbackText.setVisibility(View.VISIBLE);
                    logEmail.requestFocus();
                } else if(TextUtils.isEmpty(password)) {
                    feedbackText.setText("Password cannot be empty");
                    feedbackText.setVisibility(View.VISIBLE);
                    logPassword.requestFocus();
                } else if(!(email.contains("@"))) {
                    feedbackText.setText("Please enter a valid Email address");
                    feedbackText.setVisibility(View.VISIBLE);
                    logEmail.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    login_btn.setEnabled(false);
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if(user.isEmailVerified()) {
                                    navController.navigate(R.id.action_startFragment_to_listFragment);
                                } else if(!user.isEmailVerified()) {
                                    showAlertDialog();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    login_btn.setEnabled(true);
                                    firebaseAuth.signOut();
                                }
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                login_btn.setEnabled(true);

                                if(task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    feedbackText.setText("Incorrect Password");
                                    feedbackText.setVisibility(View.VISIBLE);
                                } else if(task.getException() instanceof FirebaseAuthInvalidUserException) {
                                    feedbackText.setText("User does not exist");
                                    feedbackText.setVisibility(View.VISIBLE);
                                } else if(task.getException() instanceof FirebaseTooManyRequestsException) {
                                    feedbackText.setText("We have blocked all the requests from this device due to unusual activity. \n\n try again later");
                                    feedbackText.setVisibility(View.VISIBLE);
                                }
                                else {
                                    feedbackText.setText(task.getException().toString());
                                    feedbackText.setVisibility(View.VISIBLE);
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
        if(view.getId() == R.id.register_now_btn) {
            navController.navigate(R.id.action_startFragment_to_registerFragment);
        } else if(view.getId() == R.id.forgot_pass_btn) {
            ResetPassDialog resetPassDialog = new ResetPassDialog();
            resetPassDialog.show(getFragmentManager(), "Password Reset");
        }
    }

    private void showAlertDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(getActivity());
        alertDialogBuilder.setTitle("Email Verification");
        alertDialogBuilder.setMessage("Please verify the Email sent to: \n " + user.getEmail());
        alertDialogBuilder.setPositiveButton("Verify Email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent emailIntent = new Intent(Intent.ACTION_MAIN);
                emailIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
                emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(emailIntent, "Email"));
            }
        });
        alertDialogBuilder.setNegativeButton("Resend Email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Snackbar snackbar = Snackbar.make(constraintLayout, "Verification Email Sent", Snackbar.LENGTH_LONG);
                            snackbar.setTextColor(Color.BLACK);
                            snackbar.setBackgroundTint(Color.WHITE);
                            snackbar.setAction("ok", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.setActionTextColor(Color.parseColor("#9875CB"));
                            snackbar.show();
                        } else {
                            feedbackText.setText(task.getException().toString());
                            feedbackText.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
        alertDialogBuilder.show();
    }
}