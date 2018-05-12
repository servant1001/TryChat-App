package com.example.user.trychat;

import android.content.Context;
import android.graphics.Color;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{//Part 29

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    String current_user_id;
    String from_user;
    String message_type;

    public static int INCOMING = 1;
    public static int OUTGOING = 2;

    @Override
    public int getItemViewType(int position) {
        int viewType = -1;
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        Messages c = mMessageList.get(position);
        from_user = c.getFrom();

        if (from_user.equals(current_user_id)){
            viewType = INCOMING;
        }else {
            viewType = OUTGOING;
        }
        return viewType;
    }

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public ImageView  messageImage;

        public MessageViewHolder(View view) {
            super(view);

            messageText = view.findViewById(R.id.message_text_layout);
            profileImage = view.findViewById(R.id.message_profile_layout);
            displayName = view.findViewById(R.id.name_text_layout);
            messageImage = view.findViewById(R.id.message_image_layout);
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        Messages c = mMessageList.get(i);

        from_user = c.getFrom();
        message_type = c.getType();

        if (from_user.equals(current_user_id)){

            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background_white);
            viewHolder.messageText.setTextColor(Color.BLACK);
            //View v = layoutInflater.inflate(R.layout.message_single_layout_send,null);

        }else {

            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
            viewHolder.messageText.setTextColor(Color.WHITE);
        }

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                viewHolder.displayName.setText(name);

                Picasso.with(viewHolder.profileImage.getContext()).load(image)
                        .placeholder(R.drawable.default_avatar).into(viewHolder.profileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text")) {//判斷訊息是文字還是圖片

            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageImage.setVisibility(View.INVISIBLE);

        } else {

            viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.default_avatar).into(viewHolder.messageImage);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            //View v = LayoutInflater.from(parent.getContext())
                    //.inflate(R.layout.message_single_layout ,parent, false);
        Messages c = mMessageList.get(viewType-1);
        String sender = c.getFrom();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(sender);

        View v;
        if( viewType == MessageAdapter.OUTGOING ) {//接收訊息 靠左邊
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_layout, parent, false);
        }else {//發送訊息 靠右邊
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_layout_send, parent, false);
        }

        return new MessageViewHolder(v);
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}
