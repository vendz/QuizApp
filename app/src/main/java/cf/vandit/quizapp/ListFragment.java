package cf.vandit.quizapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ListFragment extends Fragment implements QuizListAdapter.OnQuizListItemClicked, View.OnClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton logout_btn;

    FirebaseAuth firebaseAuth;

    private NavController navController;

    private Animation fade_in, fade_out;

    private QuizListViewModel quizListViewModel;
    private QuizListAdapter adapter;

    public ListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.list_progress);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        logout_btn = view.findViewById(R.id.logout_btn);
        logout_btn.setOnClickListener(this);

        navController = Navigation.findNavController(view);

        fade_in = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fade_out = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);

        adapter = new QuizListAdapter(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        //TODO: work on this, as this is made using MVVM architecture, the data is downloaded from another file
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
                progressBar.setVisibility(View.INVISIBLE);
                quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), new Observer<List<QuizListModel>>() {
                    @Override
                    public void onChanged(List<QuizListModel> quizListModels) {
                        recyclerView.startAnimation(fade_in);
                        progressBar.startAnimation(fade_out);

                        adapter.setQuizListModels(quizListModels);
                        adapter.notifyDataSetChanged();
                    }
                });
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        quizListViewModel = new ViewModelProvider(getActivity()).get(QuizListViewModel.class);
        quizListViewModel.getQuizListModelData().observe(getViewLifecycleOwner(), new Observer<List<QuizListModel>>() {
            @Override
            public void onChanged(List<QuizListModel> quizListModels) {
                recyclerView.startAnimation(fade_in);
                progressBar.startAnimation(fade_out);

                adapter.setQuizListModels(quizListModels);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onItemClicked(int position) {
        ListFragmentDirections.ActionListFragmentToDetailsFragment action = ListFragmentDirections.actionListFragmentToDetailsFragment();
        action.setPosition(position);
        navController.navigate(action);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.logout_btn) {
            firebaseAuth.signOut();
            navController.navigate(R.id.action_listFragment_to_startFragment);
        }
    }
}