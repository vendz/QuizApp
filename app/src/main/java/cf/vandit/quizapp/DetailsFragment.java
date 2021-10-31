package cf.vandit.quizapp;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DetailsFragment extends Fragment implements View.OnClickListener {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private QuizListViewModel quizListViewModel;
    private int position;
    private long totalQuestions = 0;
    private String quizID;
    private String quizName;

    private TextView title, difficulty, total_questions, description, score;
    private ImageView coverImage;
    private Button start_btn;

    private CollapsingToolbarLayout collapsingToolbarLayout;

    private NavController navController;

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        navController = Navigation.findNavController(view);

        position = DetailsFragmentArgs.fromBundle(getArguments()).getPosition();

        coverImage = view.findViewById(R.id.details_image);
//        title = view.findViewById(R.id.details_title);
        collapsingToolbarLayout = view.findViewById(R.id.toolbar_layout);
        description = view.findViewById(R.id.details_desc);
        difficulty = view.findViewById(R.id.details_difficulty_text);
        total_questions = view.findViewById(R.id.details_questions_text);
        score = view.findViewById(R.id.details_score_text);
        start_btn = view.findViewById(R.id.details_start_btn);

        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.parseColor("#E9E9E9"));

        start_btn.setOnClickListener(this);

        Window window = getActivity().getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(getActivity(),R.color.dark_grey));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
        quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), new Observer<List<QuizListModel>>() {
            @Override
            public void onChanged(List<QuizListModel> quizListModels) {
                quizID = quizListModels.get(position).getQuiz_id();
//                title.setText(quizListModels.get(position).getName());
                quizName = quizListModels.get(position).getName();
                collapsingToolbarLayout.setTitle(quizName);
                description.setText(quizListModels.get(position).getDescription());
                difficulty.setText(quizListModels.get(position).getLevel());
                total_questions.setText(quizListModels.get(position).getQuestions() + "");

                Glide.with(getContext())
                        .load(quizListModels.get(position).getImage())
                        .centerCrop()
                        .placeholder(R.drawable.placeholder_image)
                        .into(coverImage);

                totalQuestions = quizListModels.get(position).getQuestions();

                // Load results data
                loadResults();
            }
        });
    }

    private void loadResults() {
        // fetch score data
        firebaseFirestore.collection("QuizList")
                .document(quizID).collection("Results")
                .document(firebaseAuth.getCurrentUser().getEmail())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot != null && documentSnapshot.exists()) {
                        Long correct = documentSnapshot.getLong("correct");
                        Long incorrect = documentSnapshot.getLong("incorrect");
                        Long missed = documentSnapshot.getLong("unanswered");

                        // calculate progress
                        Long total = correct + incorrect + missed;
                        Long percent = (correct*100)/total;

                        score.setText(percent + "%");
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.details_start_btn) {
            DetailsFragmentDirections.ActionDetailsFragmentToQuizFragment action = DetailsFragmentDirections.actionDetailsFragmentToQuizFragment();
            action.setTotalQuestions(totalQuestions);
            action.setQuizID(quizID);
            action.setQuizName(quizName);
            navController.navigate((NavDirections) action);
        }
    }
}