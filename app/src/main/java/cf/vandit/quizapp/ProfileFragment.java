package cf.vandit.quizapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Toolbar toolbar;
    private CircleImageView profileImage;
    private FloatingActionButton floatingActionButton;
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
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        toolbar = view.findViewById(R.id.profile_toolbar);
        profileImage = view.findViewById(R.id.profile_image);
        floatingActionButton = view.findViewById(R.id.profile_fab);
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

        Window window = getActivity().getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(getActivity(),R.color.app_bar_bg));

        emailText.setText(preferences.getString("email", "Your Email"));
        nameText.setText(preferences.getString("name", "Your Name"));

        boolean image_exists = preferences.getBoolean("image_exists", false);
        if(image_exists){

            Picasso.get()
                    .load(preferences.getString("imageURL", ""))
                    .centerCrop()
                    .resize(1000, 1000)
                    .placeholder(R.drawable.ic_account_circle)
                    .into(profileImage);
        }

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
                        uploadPicture(imageUri);
                    }
                }
            }
    );

    private void uploadPicture(Uri imageUri){
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.show();

        final String randomName = UUID.randomUUID().toString();

        // Create a reference to profile image
        StorageReference storageRef = storageReference.child("profile_images/" + randomName);

        storageRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        HashMap<String, Object> imageMap = new HashMap<>();
                        imageMap.put("profile_pic", uri.toString());
                        imageMap.put("image_exists", true);
                        firebaseFirestore.collection("users").document(firebaseAuth.getCurrentUser().getEmail()).set(imageMap, SetOptions.merge());
                        SharedPreferences preferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();

                        editor.putString("imageURL", uri.toString());
                        editor.putBoolean("image_exists", true);
                        editor.apply();
                        Picasso.get()
                                .load(uri.toString())
                                .centerCrop()
                                .resize(200, 200)
                                .placeholder(R.drawable.ic_account_circle)
                                .into(profileImage);
                        progressDialog.dismiss();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setMessage("Uploading " + (int)progressPercent + "%");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Upload unsuccessful. Please try later...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}