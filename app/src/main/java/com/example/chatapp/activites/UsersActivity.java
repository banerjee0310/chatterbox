package com.example.chatapp.activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatapp.adapters.UserAdapter;
import com.example.chatapp.databinding.ActivityUsersBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.model.User;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.Preferences;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity implements UserListener {
    private ActivityUsersBinding binding;
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferences=new Preferences(getApplicationContext());
        binding.iconBack.setOnClickListener(v->onBackPressed());
        getUsers();

    }

    private void getUsers(){
        loading(true);
        FirebaseFirestore firebaseFirestore= FirebaseFirestore.getInstance();
        firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId=preferences.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() !=null)
                    {
                        List<User> userList=new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot :task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user=new User();
                            user.name=queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.image=queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token=queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id=queryDocumentSnapshot.getId();
                            userList.add(user);
                        }
                        if(userList.size()>0){
                            UserAdapter userAdapter=new UserAdapter(userList, this);
                            binding.userList.setAdapter(userAdapter);
                            binding.userList.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }
    private void loading(Boolean isLoading)
    {
        if(isLoading)
        {
            binding.progress.setVisibility(View.VISIBLE);
        }
        else {
            binding.progress.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent=new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}