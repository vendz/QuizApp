package cf.vandit.quizapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private Toolbar toolbar;
    private CircleImageView profileImage;
    private FloatingActionButton floatingActionButton;
    private LinearLayout nameLayout;
    private ImageButton nameBtn;
    private TextView nameText;
    private TextView emailText;
    private SharedPreferences preferences;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        toolbar = view.findViewById(R.id.profile_toolbar);
        profileImage = view.findViewById(R.id.profile_image);
        floatingActionButton = view.findViewById(R.id.profile_fab);
        nameLayout = view.findViewById(R.id.ln_name);
        nameBtn = view.findViewById(R.id.edit_name_icon);
        nameText = view.findViewById(R.id.profile_name);
        emailText = view.findViewById(R.id.tv_email);

        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        emailText.setText(preferences.getString("email", "Your Email"));
        nameText.setText(preferences.getString("name", "Your Name"));

        nameBtn.setOnClickListener(view1 -> {
            AlertDialog.Builder nameDialog = new AlertDialog.Builder(getActivity());
            nameDialog.setTitle("Change Name");

            final EditText nameEditText = new EditText(getActivity());
            nameEditText.setHint("name");
            nameDialog.setView(nameEditText);

            nameDialog.setPositiveButton("ok", (dialogInterface, i) -> {
                String name = nameEditText.getText().toString();
                name = name.trim();
                if(!name.equals("")) {
                    editor.putString("name", name);
                    editor.apply();

                    HashMap<String, String> nameMap = new HashMap<>();
                    nameMap.put("name", name);
                    firebaseFirestore.collection("users").document(currentUser.getEmail()).set(nameMap, SetOptions.merge());
                    nameText.setText(name);
                }
            });
            nameDialog.setNegativeButton("Cancel", (dialogInterface, i) -> {});
            nameDialog.show();
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                galleryLauncher.launch(intent);
            }
        });
    }

    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri imageUri = data.getData();
                        profileImage.setImageURI(imageUri);
                    }
                }
            }
    );
}