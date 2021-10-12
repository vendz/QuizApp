package cf.vandit.quizapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

public class ListFragment extends Fragment implements QuizListAdapter.OnQuizListItemClicked, View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton menu_btn;
    private PopupMenu popupMenu;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

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
        firebaseFirestore = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.list_progress);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        menu_btn = view.findViewById(R.id.menu_btn);
        menu_btn.setOnClickListener(this);

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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult();

                    DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseAuth.getCurrentUser().getEmail());
                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                if(documentSnapshot.exists()) {
                                    boolean is_admin = (boolean) documentSnapshot.get("is_admin");
                                    String name = null;
                                    try {
                                        name = documentSnapshot.get("name").toString();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    // adding user data to local database
                                    SharedPreferences preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();

                                    editor.putString("email", user.getEmail());
                                    editor.putString("token", token);
                                    editor.putBoolean("is_admin", is_admin);
                                    editor.putString("name", name);

                                    editor.apply();
                                }
                            }
                        }
                    });
                }
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
        if(view.getId() == R.id.menu_btn) {
            Context wrapper = new ContextThemeWrapper(getActivity(), R.style.MyPopupOtherStyle);
            popupMenu = new PopupMenu(wrapper, view);
            popupMenu.setOnMenuItemClickListener(this::onMenuItemClick);
            popupMenu.inflate(R.menu.popup_menu);

            SharedPreferences preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
            boolean is_admin = preferences.getBoolean("is_admin", false);

            if(!is_admin) {
                Menu popup = popupMenu.getMenu();
                popup.findItem(R.id.menu_new_quiz).setVisible(false);
            }

            popupMenu.show();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_profile:
                navController.navigate(R.id.action_listFragment_to_profileFragment);
                return true;
            case R.id.menu_new_quiz:
                Toast.makeText(getActivity(), "New Quiz", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_about:
                Toast.makeText(getActivity(), "About", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_logout:
                menuLogoutBtn();
                return true;
            default:
                return false;
        }
    }

    public void menuLogoutBtn() {
        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(getActivity());
        alertDialogBuilder.setMessage("are you sure you want to logout?");
        alertDialogBuilder.setPositiveButton("logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                firebaseAuth.signOut();
                navController.navigate(R.id.action_listFragment_to_startFragment);
            }
        });
        alertDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {}
        });
        alertDialogBuilder.show();
    }
}