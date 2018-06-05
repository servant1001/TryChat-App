package com.example.user.trychat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private RecyclerView mRequestsList;
    private View mMainView;

    private DatabaseReference mFriendsRequests;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendDatabase;//Part 49
    private DatabaseReference mFriendReqDatabase;
    private FirebaseUser mCurrent_user;

    private FirebaseAuth mAuth;
    private String mCurrent_user_id;

    private DatabaseReference mRootRef;

    String userName,userStatus;

    Button mAcceptBtn,mDeclineBtn;
    private String mCurrent_state;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mRequestsList = mMainView.findViewById(R.id.requests_list);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        //mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);//Part 25 5:00左右打完會有錯
        mFriendsRequests = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);//改下面這3行
        //mFriendskey = mFriendsDatabase.child(mCurrent_user_id);//顯示除了自己以外的好友

        //mUsersDatabase.keepSynced(true);//保持同步
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mRequestsList.setHasFixedSize(true);
        mRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mCurrent_state = "not_friends";//初始狀態 not_friends


        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Requests> options =
                new FirebaseRecyclerOptions.Builder<Requests>()
                        .setQuery(mFriendsRequests, Requests.class)
                        .build();

        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Requests, RequestsFragment.RequestsViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final RequestsFragment.RequestsViewHolder RequestsViewHolder, int i, @NonNull final Requests requests) {

                RequestsViewHolder.setStatus(requests.getDate());

                final String list_user_id = getRef(i).getKey();

                //判斷是發送邀請者還是接受邀請者
                DatabaseReference get_type_ref = getRef(i).child("request_type").getRef();

                mAcceptBtn = RequestsViewHolder.mView.findViewById(R.id.request_accept_btn);
                mDeclineBtn = RequestsViewHolder.mView.findViewById(R.id.request_cancel_btn);

                mDeclineBtn.setVisibility(View.INVISIBLE);
                mDeclineBtn.setEnabled(false);

                mCurrent_state = "not_friends";//初始狀態 not_friends

                mDeclineBtn.setVisibility(View.INVISIBLE);
                mDeclineBtn.setEnabled(false);
                mRootRef = FirebaseDatabase.getInstance().getReference();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        userName = dataSnapshot.child("name").getValue().toString();
                        userStatus = dataSnapshot.child("status").getValue().toString();
                        String image = dataSnapshot.child("image").getValue().toString();

                        RequestsViewHolder.setName(userName);
                        RequestsViewHolder.setStatus(userStatus);
                        RequestsViewHolder.setUserImage(image, getContext());

                        if(mCurrent_user.getUid().equals(list_user_id)){

                            mDeclineBtn.setEnabled(false);
                            mDeclineBtn.setVisibility(View.INVISIBLE);

                            mAcceptBtn.setEnabled(false);
                            mAcceptBtn.setVisibility(View.INVISIBLE);

                        }


                        //--------------- FRIENDS LIST / REQUEST FEATURE -----

                        mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if(dataSnapshot.hasChild(list_user_id)){

                                    String req_type = dataSnapshot.child(list_user_id).child("request_type").getValue().toString();

                                    if(req_type.equals("received")){//接收方
                                        mCurrent_state = "req_received";
                                        mAcceptBtn.setText("Accept Friend Request");
                                        mDeclineBtn.setVisibility(View.VISIBLE);//顯示拒絕button
                                        mDeclineBtn.setEnabled(true);
                                    } else if(req_type.equals("sent")) {//送出方
                                        mCurrent_state = "req_sent";
                                        mAcceptBtn.setText("Cancel Friend Request");//取消邀請
                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }
                                } else {
                                    mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(list_user_id)){
                                                mCurrent_state = "friends";
                                                mAcceptBtn.setText("Unfriend this Person");

                                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                                mDeclineBtn.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                mAcceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mAcceptBtn.setEnabled(false);//按過邀請後停用邀請按鈕

                        // ----------------------- NOT FRIENDS STATE -----------------------*可以在Database看到資料的變化 Part 17、18
                        if(mCurrent_state.equals("not_friends")){//後來有修改 Part25

                            DatabaseReference newNotificationref = mRootRef.child("notifications").child(list_user_id).push();
                            String newNotificationId = newNotificationref.getKey();

                            HashMap<String, String> notificationData = new HashMap<>();
                            notificationData.put("from", mCurrent_user.getUid());
                            notificationData.put("type", "request");

                            Map requestMap = new HashMap();
                            requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + list_user_id + "/request_type", "sent");
                            requestMap.put("Friend_req/" + list_user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                            requestMap.put("notifications/" + list_user_id + "/" + newNotificationId, notificationData);

                            mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if(databaseError != null){

                                    } else {

                                        mCurrent_state = "req_sent";
                                        mAcceptBtn.setText("Cancel Friend Request");//原本邀請按鈕改成取消邀請 (按過邀請後變化)

                                    }
                                    mAcceptBtn.setEnabled(true);
                                }
                            });

                        }


                        // ----------------------- CANCEL REQUEST STATE -----------------------*可以在Database看到資料的變化 Part 18
                        if(mCurrent_state.equals("req_sent")){

                            mFriendReqDatabase.child(mCurrent_user.getUid()).child(list_user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendReqDatabase.child(list_user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mAcceptBtn.setEnabled(true);
                                            mCurrent_state = "not_friends";
                                            mAcceptBtn.setText("Send Friend Request");

                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                            mDeclineBtn.setEnabled(false);

                                        }
                                    });
                                }
                            });
                        }


                        // ------------ REQ RECEIVED STATE ----------
                        if(mCurrent_state.equals("req_received")){

                            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                            Map friendsMap = new HashMap();
                            friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + list_user_id + "/date", currentDate);
                            friendsMap.put("Friends/" + list_user_id + "/"  + mCurrent_user.getUid() + "/date", currentDate);


                            friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + list_user_id, null);
                            friendsMap.put("Friend_req/" + list_user_id + "/" + mCurrent_user.getUid(), null);


                            mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                                    if(databaseError == null){

                                        mAcceptBtn.setEnabled(true);
                                        mCurrent_state = "friends";
                                        mAcceptBtn.setText("Unfriend this Person");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    } else {
                                        String error = databaseError.getMessage();

                                    }
                                }
                            });
                        }

                        // ------------ UNFRIENDS ---------

                        if(mCurrent_state.equals("friends")){

                            Map unfriendMap = new HashMap();
                            unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + list_user_id, null);
                            unfriendMap.put("Friends/" + list_user_id + "/" + mCurrent_user.getUid(), null);

                            mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                                    if(databaseError == null){

                                        mCurrent_state = "not_friends";
                                        mAcceptBtn.setText("Send Friend Request");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);

                                    } else {

                                    }
                                    mAcceptBtn.setEnabled(true);
                                }
                            });
                        }
                    }
                });

                mDeclineBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mFriendReqDatabase.child(mCurrent_user_id).child(list_user_id).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            mFriendReqDatabase.child(list_user_id).child(mCurrent_user_id).removeValue()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Toast.makeText(getContext(),"Friends request Canceled Successfully",Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });}
                });
            }

            @Override
            public RequestsFragment.RequestsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.friend_request_all_users_layout, parent, false);

                return new RequestsFragment.RequestsViewHolder(view);
            }
        };

        mRequestsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RequestsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setStatus(String date) {
            TextView userStatusView = mView.findViewById(R.id.req_user_status);
            userStatusView.setText(date);
        }

        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.req_user_name);
            userNameView.setText(name);
        }

        public void setUserImage(String thumb_image, Context ctx){
            CircleImageView userImageView = mView.findViewById(R.id.request_user_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
        }
    }
}