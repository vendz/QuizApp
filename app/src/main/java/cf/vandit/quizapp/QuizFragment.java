package cf.vandit.quizapp;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuizFragment extends Fragment implements View.OnClickListener{

    private FirebaseFirestore firebaseFirestore;
    private String QuizID;

    private String currentUserID;
    private FirebaseAuth firebaseAuth;

    private List<QuestionsModel> allQuestionsList = new ArrayList<>();
    private long totalQuestions = 0L;
    private List<QuestionsModel> questionToAnswer = new ArrayList<>();

    private String quizName;
    private String quizID;

    private TextView quiz_title;
    private TextView quiz_current_question;
    private TextView quiz_time_elapsed;
    private TextView quiz_question_feedback;
    private TextView quiz_question_text;
    private Button option_a;
    private Button option_b;
    private Button option_c;
    private Button option_d;
    private ImageButton close_btn;
    private Button next_btn;
    private ProgressBar quiz_time_progressBar;
    private CountDownTimer countDownTimer;

    private NavController navController;

    private Boolean canAnswer = false;
    private int currentQuestion = 0;

    private int correctAnswers = 0;
    private int incorrectAnswers = 0;
    private int unanswered = 0;

    public QuizFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        navController = Navigation.findNavController(view);

        // get UserID
        if(firebaseAuth.getCurrentUser() != null) {
            currentUserID = firebaseAuth.getCurrentUser().getUid();
        } else {
            // go back to homePage
            navController.navigate(R.id.action_quizFragment_to_startFragment);
        }

        quiz_title = view.findViewById(R.id.quiz_title);
        quiz_current_question = view.findViewById(R.id.quiz_question_number);
        quiz_time_elapsed = view.findViewById(R.id.quiz_question_time);
        quiz_question_feedback = view.findViewById(R.id.quiz_question_feedback);
        quiz_question_text = view.findViewById(R.id.quiz_question);
        quiz_time_progressBar = view.findViewById(R.id.quiz_question_progress);
        close_btn = view.findViewById(R.id.quiz_close_btn);
        next_btn = view.findViewById(R.id.quiz_next_btn);
        option_a = view.findViewById(R.id.quiz_option_one);
        option_b = view.findViewById(R.id.quiz_option_two);
        option_c = view.findViewById(R.id.quiz_option_three);
        option_d = view.findViewById(R.id.quiz_option_four);

        totalQuestions = QuizFragmentArgs.fromBundle(getArguments()).getTotalQuestions();
        quizName = QuizFragmentArgs.fromBundle(getArguments()).getQuizName();
        quizID = QuizFragmentArgs.fromBundle(getArguments()).getQuizID();

        // Initialize
        firebaseFirestore = FirebaseFirestore.getInstance();

        QuizID = QuizFragmentArgs.fromBundle(getArguments()).getQuizID();

        // get all questions from database
        firebaseFirestore.collection("QuizList")
                .document(QuizID).collection("Questions")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    allQuestionsList = task.getResult().toObjects(QuestionsModel.class);
                    pickQuestions();
                    loadUI();
                } else {
                    quiz_title.setText("Error loading data...");
                }
            }
        });

        // set button onClickListeners
        option_a.setOnClickListener(this);
        option_b.setOnClickListener(this);
        option_c.setOnClickListener(this);
        option_d.setOnClickListener(this);

        next_btn.setOnClickListener(this);
        close_btn.setOnClickListener(this);
    }

    private void loadUI() {
        quiz_title.setText(quizName);

        enableOptions();
        loadQuestions(1);
    }

    private void enableOptions() {
        option_a.setVisibility(View.VISIBLE);
        option_b.setVisibility(View.VISIBLE);
        option_c.setVisibility(View.VISIBLE);
        option_d.setVisibility(View.VISIBLE);

        next_btn.setEnabled(false);
    }

    private void loadQuestions(int questNum) {
        // set question number
        quiz_current_question.setText(String.valueOf(questNum));

        // Load Question
        quiz_question_text.setText(questionToAnswer.get(questNum-1).getQuestion());

        // Loading options
        option_a.setText(questionToAnswer.get(questNum-1).getOption_a());
        option_b.setText(questionToAnswer.get(questNum-1).getOption_b());
        option_c.setText(questionToAnswer.get(questNum-1).getOption_c());
        option_d.setText(questionToAnswer.get(questNum-1).getOption_d());

        canAnswer = true;
        currentQuestion = questNum;

        // starting timer
        startTimer(questNum);
    }

    private void startTimer(int questionNumber) {

        // set Timer text
        long timeToAnswer = questionToAnswer.get(questionNumber-1).getTimer();
        quiz_time_elapsed.setText(String.valueOf(timeToAnswer));
        quiz_time_progressBar.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(timeToAnswer*1000, 10) {

            @Override
            public void onTick(long l) {
                // update time
                quiz_time_elapsed.setText(l/1000 + "");

                // progress in percent
                Long percent = l/(timeToAnswer*10);
                quiz_time_progressBar.setProgress(percent.intValue());
            }

            @Override
            public void onFinish() {
                canAnswer = false;

                unanswered++;
                quiz_question_feedback.setVisibility(View.VISIBLE);
                quiz_question_feedback.setText("Time's Up!");
                quiz_question_feedback.setTextColor(getResources().getColor(R.color.yellow));
                if(currentQuestion == totalQuestions) {
                    next_btn.setText("Submit Results");
                }
                next_btn.setEnabled(true);
                next_btn.setVisibility(View.VISIBLE);
            }
        };

        countDownTimer.start();
    }

    private void pickQuestions() {
        for (int i=0; i<totalQuestions; i++) {
            int randomNumber = getRandomInteger(allQuestionsList.size(), 0);
            questionToAnswer.add(allQuestionsList.get(randomNumber));
            allQuestionsList.remove(randomNumber);
        }
    }

    public static int getRandomInteger(int max, int min) {
        return ((int) (Math.random() * (max - min))) + min;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.quiz_option_one:
                verifyAnswer(option_a);
                break;

            case R.id.quiz_option_two:
                verifyAnswer(option_b);
                break;

            case R.id.quiz_option_three:
                verifyAnswer(option_c);
                break;

            case R.id.quiz_option_four:
                verifyAnswer(option_d);
                break;
            case R.id.quiz_next_btn:
                if(currentQuestion == totalQuestions) {
                    submitResults();
                } else {
                    currentQuestion++;
                    loadQuestions(currentQuestion);
                    resetOptions();
                }
                break;
            case R.id.quiz_close_btn:
                navController.popBackStack();
        }
    }

    private void submitResults() {
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("correct", correctAnswers);
        resultMap.put("incorrect", incorrectAnswers);
        resultMap.put("unanswered", unanswered);

        firebaseFirestore.collection("QuizList")
                .document(QuizID).collection("Results")
                .document(currentUserID).set(resultMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    // go to results page
                    QuizFragmentDirections.ActionQuizFragmentToResultFragment action = QuizFragmentDirections.actionQuizFragmentToResultFragment();
                    action.setQuizID(quizID);
                    navController.navigate(action);
                } else {
                    quiz_title.setText(task.getException().getMessage());
                }
            }
        });
    }

    private void verifyAnswer(Button selectedAnswerButton) {
        if(canAnswer) {
            selectedAnswerButton.setTextColor(Color.WHITE);
            if(questionToAnswer.get(currentQuestion-1).getAnswer().contentEquals(selectedAnswerButton.getText())) {
                // correct answer
                selectedAnswerButton.setBackgroundColor(Color.GREEN);
                correctAnswers++;
                quiz_question_feedback.setText("Correct Answer");
                quiz_question_feedback.setTextColor(Color.GREEN);
            } else {
                // incorrect answer
                selectedAnswerButton.setBackgroundColor(Color.RED);
                incorrectAnswers++;
                quiz_question_feedback.setText("Wrong Answer \n Correct Answer: " + questionToAnswer.get(currentQuestion-1).getAnswer());
                quiz_question_feedback.setTextColor(Color.RED);
            }
            showNextBtn();

            // set canAnswer to false to avoid multiple answers
            canAnswer = false;

            // stop the timer
            countDownTimer.cancel();
        }
    }

    private void showNextBtn() {
        if(currentQuestion == totalQuestions) {
            next_btn.setText("Submit Results");
        }
        quiz_question_feedback.setVisibility(View.VISIBLE);
        next_btn.setVisibility(View.VISIBLE);
        next_btn.setEnabled(true);
    }

    private void resetOptions() {
        option_a.setBackgroundColor(Color.TRANSPARENT);
        option_b.setBackgroundColor(Color.TRANSPARENT);
        option_c.setBackgroundColor(Color.TRANSPARENT);
        option_d.setBackgroundColor(Color.TRANSPARENT);

        option_a.setTextColor(getResources().getColor(R.color.light_grey));
        option_b.setTextColor(getResources().getColor(R.color.light_grey));
        option_c.setTextColor(getResources().getColor(R.color.light_grey));
        option_d.setTextColor(getResources().getColor(R.color.light_grey));

        next_btn.setEnabled(false);
        next_btn.setVisibility(View.INVISIBLE);

        quiz_question_feedback.setVisibility(View.INVISIBLE);
    }
}