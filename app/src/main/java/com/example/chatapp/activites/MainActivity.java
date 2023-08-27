package com.example.chatapp.activites;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.adapters.RecentConversationAdapter;
import com.example.chatapp.databinding.ActivityMainBinding;
import com.example.chatapp.listeners.ConversationListener;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.User;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.Preferences;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConversationListener {
    private ActivityMainBinding binding;
    private Preferences preferences;

    private List<ChatMessage> conversations;

    private RecentConversationAdapter conversationAdapter;

    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferences=new Preferences(getApplicationContext());
        init();
        loadUserDetails();
        getToken();
        binding.iconSignOut.setOnClickListener(v->signOut());
        binding.iconNewChat.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(),UsersActivity.class)));
        listenConversation();
    }

    private void init()
    {
        conversations=new ArrayList<>();
        conversationAdapter=new RecentConversationAdapter(conversations, this);
        binding.conversationRecent.setAdapter(conversationAdapter);
        firebaseFirestore=FirebaseFirestore.getInstance();
    }



    private void loadUserDetails(){
        binding.textName.setText(preferences.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferences.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }
    
    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversation(){
        firebaseFirestore.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferences.getString(Constants.KEY_USER_ID))
                        .addSnapshotListener(eventListener);

        firebaseFirestore.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferences.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener=(value, error)->{
        if(error!=null){
            return;
        }
        if(value!=null){
            for(DocumentChange documentChange:value.getDocumentChanges()){
                if(documentChange.getType()== DocumentChange.Type.ADDED){
                    String senderId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage=new ChatMessage();
                    chatMessage.senderId=senderId;
                    chatMessage.receiverId=receiverId;
                    if(preferences.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImg=documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName=documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }else{
                        chatMessage.conversionImg=documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName=documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                     chatMessage.message=documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                     chatMessage.dateObj=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                     conversations.add(chatMessage);
                }else if(documentChange.getType()== DocumentChange.Type.MODIFIED){
                    for(int i=0;i<conversations.size();i++){
                        String senderId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message=documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObj=documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations,(obj1,obj2)->obj2.dateObj.compareTo(obj1.dateObj));
            conversationAdapter.notifyDataSetChanged();
            binding.conversationRecent.smoothScrollToPosition(0);
            binding.conversationRecent.setVisibility(View.VISIBLE);
            binding.favText.setVisibility(View.GONE);
        }

    };

    private void getToken()
    {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }
    private void updateToken(String token)
    {
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        DocumentReference documentReference=database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferences.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnFailureListener(e->showToast("Unable to update token"));
    }

    private void signOut(){
        showToast("Signing out...");
        FirebaseFirestore firebaseFirestore=FirebaseFirestore.getInstance();
        DocumentReference documentReference=firebaseFirestore.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferences.getString(Constants.KEY_USER_ID));
        HashMap<String,Object>updates=new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferences.clear();
                    startActivity(new Intent(getApplicationContext(),SignIn_Activity.class));
                    finish();
                }).addOnFailureListener(e->showToast("Unable to sign out"));
    }

    @Override
    public void onConversationClicked(User user) {
        Intent i=new Intent(getApplicationContext(),ChatActivity.class);
        i.putExtra(Constants.KEY_USER,user);
        startActivity(i);
    }
}