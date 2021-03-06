package cf.vandit.quizapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.plattysoft.leonids.ParticleSystem;

public class ResultFragment extends Fragment {

    private TextView correctAnswers, incorrectAnswers, missedAnswers, percentage_view;
    private Button home_btn;
    private CircularProgressBar progressBar;

    private String quizID;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String userEmailID;
    private NavController navController;

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        navController = Navigation.findNavController(view);

        // Initialize UI
        correctAnswers = view.findViewById(R.id.results_correct_text);
        incorrectAnswers = view.findViewById(R.id.results_wrong_text);
        missedAnswers = view.findViewById(R.id.results_missed_text);
        percentage_view = view.findViewById(R.id.results_percentage);
        progressBar = view.findViewById(R.id.results_progress);
        home_btn = view.findViewById(R.id.results_home_btn);
        home_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_resultFragment_to_listFragment);
            }
        });

        // get UserID
        if(firebaseAuth.getCurrentUser() != null) {
            userEmailID = firebaseAuth.getCurrentUser().getEmail();
        } else {
            navController.navigate(R.id.action_resultFragment_to_listFragment);
        }

        quizID = ResultFragmentArgs.fromBundle(getArguments()).getQuizID();

        firebaseFirestore.collection("QuizList")
                .document(quizID).collection("Results")
                .document(userEmailID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot result = task.getResult();
                    if(result != null) {
                        Long correct = result.getLong("correct");
                        Long incorrect = result.getLong("incorrect");
                        Long missed = result.getLong("unanswered");

                        correctAnswers.setText(String.valueOf(correct));
                        incorrectAnswers.setText(String.valueOf(incorrect));
                        missedAnswers.setText(String.valueOf(missed));

                        // calculate progress
                        Long total = correct + incorrect + missed;
                        Long percent = (correct * 100) / total;

                        percentage_view.setText(percent + "%");
                        progressBar.setProgressWithAnimation(percent.intValue(), 700);

                        if(percent == 100) {
                            new ParticleSystem(getActivity(), 130, R.drawable.confeti2, 1000)
                                    .setSpeedRange(0.2f, 0.5f)
                                    .setScaleRange(0.7f, 1.3f)
                                    .setRotationSpeedRange(90, 180)
                                    .setAcceleration(0.00013f, 90)
                                    .setFadeOut(200, new AccelerateInterpolator())
                                    .emit(getActivity().findViewById(R.id.bottom_left_emitter), 130, 1000);

                            new ParticleSystem(getActivity(), 130, R.drawable.confeti3, 1000)
                                    .setSpeedRange(0.2f, 0.5f)
                                    .setScaleRange(0.7f, 1.3f)
                                    .setRotationSpeedRange(90, 180)
                                    .setAcceleration(0.00013f, 90)
                                    .setFadeOut(200, new AccelerateInterpolator())
                                    .emit(getActivity().findViewById(R.id.bottom_right_emitter), 130, 1000);
                        }
                    }
                }
            }
        });
    }
}