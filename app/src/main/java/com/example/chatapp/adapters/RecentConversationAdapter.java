package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.ItemContainerUserRecentBinding;
import com.example.chatapp.listeners.ConversationListener;
import com.example.chatapp.model.ChatMessage;
import com.example.chatapp.model.User;

import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessages;

    private final ConversationListener conversionListener;



    public RecentConversationAdapter(List<ChatMessage> chatMessages, ConversationListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener = conversionListener;
    }


    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerUserRecentBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,false
                )
        );


    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerUserRecentBinding binding;

        ConversionViewHolder(ItemContainerUserRecentBinding itemContainerUserRecentBinding){
            super(itemContainerUserRecentBinding.getRoot());
            binding=itemContainerUserRecentBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.profileImg.setImageBitmap(getConversationImage(chatMessage.conversionImg));
            binding.textName.setText(chatMessage.conversionName);
            binding.recentConversation.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v->{
                User user=new User();
                user.id=chatMessage.conversionId;
                user.name=chatMessage.conversionName;
                user.image=chatMessage.conversionImg;
                conversionListener.onConversationClicked(user);
            });
        }
    }
    private Bitmap getConversationImage(String encodedImg){
        byte[] bytes= Base64.decode(encodedImg,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);

    }
}
