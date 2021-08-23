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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ResultFragment extends Fragment {

    private TextView correctAnswers, incorrectAnswers, missedAnswers, percentage_view;
    private Button home_btn;
    private ProgressBar progressBar;

    private String quizID;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentUserID;
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
            currentUserID = firebaseAuth.getCurrentUser().getUid();
        } else {
            navController.navigate(R.id.action_resultFragment_to_listFragment);
        }

        quizID = ResultFragmentArgs.fromBundle(getArguments()).getQuizID();

        firebaseFirestore.collection("QuizList")
                .document(quizID).collection("Results")
                .document(currentUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot result = task.getResult();
                    if(result != null) {
                        Long correct = result.getLong("correct");
                        Long incorrect = result.getLong("incorrect");
                        Long missed = result.getLong("unanswered");

                        correctAnswers.setText(correct.toString());
                        incorrectAnswers.setText(incorrect.toString());
                        missedAnswers.setText(missed.toString());

                        // calculate progress
                        Long total = correct + incorrect + missed;
                        Long percent = (correct * 100) / total;

                        percentage_view.setText(percent + "%");
                        progressBar.setProgress(percent.intValue());
                    }
                }
            }
        });
    }
}