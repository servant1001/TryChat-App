package com.example.user.trychat;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment{

    private RecyclerView mConvList;

    private DatabaseReference mRootRef;
    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mConvkey;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    public FirebaseRecyclerAdapter firebaseConvAdapter;

    NotificationManager notificationManager;

    public ChatsFragment() {
        // Required empty public constructor
    }

    /*
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle results = RemoteInput.getResultsFromIntent(intent);
                if (results != null) {
                    CharSequence result = results.getCharSequence("quick_notification_reply");
                    if (TextUtils.isEmpty(result)) {
                        //((TextView) findViewById(R.id.txt_inline_reply)).setText("no content");
                    } else {
                        //((TextView) findViewById(R.id.txt_inline_reply)).setText(result);

                        updateNotification(context, 1);
                    }
                }
                getActivity().unregisterReceiver(this);
            }
        };

        //注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addCategory(getActivity().getPackageName());
        filter.addAction("quick.reply.input");
        getActivity().registerReceiver(br, filter);
    }
    */
    private void updateNotification(Context context, int notifyId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Notification.Builder builder = new Notification.Builder(getContext())
                .setSmallIcon(R.drawable.trychat_smallicon)
                .setColor(Color.GREEN)
                .setContentTitle("TryChat")
                .setContentText("Sent success...")
                .setChannelId("1");


        notificationManager.notify(notifyId,builder.build());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);//三秒钟后移除Notification
                    cancalNotification();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    //移除Notification
    private void cancalNotification(){
        notificationManager.cancel(1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mConvList = mMainView.findViewById(R.id.conv_list);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();//目前使用者id

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat");//改下面這3行
        mConvkey = mConvDatabase.child(mCurrent_user_id);//顯示除了自己以外的好友

        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("message").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerOptions<Conv> options =
                new FirebaseRecyclerOptions.Builder<Conv>()
                        .setQuery(mConvkey, Conv.class)
                        .build();

        firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder convViewHolder, final int position, @NonNull final Conv conv) {
                final String list_user_id = getRef(position).getKey();

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        final String data = dataSnapshot.child("message").getValue().toString();
                        convViewHolder.setMessage(data, conv.isSeen());
                        sendNotification(data, list_user_id);


                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();


                        if(dataSnapshot.hasChild("online")) {

                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            convViewHolder.setUserOnline(userOnline);
                        }

                        convViewHolder.setName(userName);
                        convViewHolder.setUserImage(userThumb, getContext());

                        convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {//選擇聊天室

                                //所點擊對象的資料傳到ChatActivity
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("from_user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                startActivity(chatIntent);
                            }
                        });

                        convViewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                final CharSequence options[] = new CharSequence[]{"Chat", "Delete"};

                                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                                //builder.setTitle("Select Options");
                                alert.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        //Click Event for each item.
                                        if(which == 0){//Go to Chat room
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("from_user_id", list_user_id);
                                            chatIntent.putExtra("user_name", userName);
                                            startActivity(chatIntent);
                                        }

                                        if(which == 1) {//Delete chat
                                            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                                            alert.setTitle("Deletion confirmation");//警告提醒
                                            alert.setMessage("Deleted chatsy can't be recovered. Are you sure want to continue?");
                                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    DatabaseReference mDelete_User = FirebaseDatabase.getInstance().getReference().child("message").child(mCurrent_user_id).child(list_user_id);
                                                    mDelete_User.removeValue();//刪除聊天內容
                                                    DatabaseReference mDelete_Chat = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id).child(list_user_id);
                                                    mDelete_Chat.removeValue();
                                                    firebaseConvAdapter.notifyItemRemoved(position);
                                                    firebaseConvAdapter.getRef(position).removeValue();//從adapter移除 (列表上消失)
                                                }
                                            });
                                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                }
                                            });
                                            alert.show();
                                        }
                                    }
                                });
                                alert.show();
                                return false;
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mConvDatabase.child(mCurrent_user_id).child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild("unRead")) {
                            String unReadCount = dataSnapshot.child("unRead").getValue().toString();
                            convViewHolder.setUserUnReadMessage(unReadCount);

                        }else{
                            String unReadCount = "";
                            convViewHolder.setUserUnReadMessage(unReadCount);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);

                return new ChatsFragment.ConvViewHolder(view);
            }
        };

        mConvList.setAdapter(firebaseConvAdapter);
        firebaseConvAdapter.startListening();
    }

    private void sendNotification(final String message, final String list_user_id) {

        mRootRef.child("Users").child(list_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String userName = dataSnapshot.child("name").getValue().toString();
                mConvDatabase.child(mCurrent_user_id).child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("unRead") && !dataSnapshot.child("unRead").getValue().equals("")) {
                            //Toast.makeText(getContext(), message,Toast.LENGTH_SHORT).show();
                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

                                mConvDatabase.child(mCurrent_user_id).child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("seen")){
                                            String seen = dataSnapshot.child("seen").getValue().toString();
                                            if (seen.equals("false") && !dataSnapshot.child("unRead").getValue().equals("")){
                                                notification(userName,message,list_user_id);
                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }else{
                                mConvDatabase.child(mCurrent_user_id).child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("seen")){
                                            String seen = dataSnapshot.child("seen").getValue().toString();
                                            if (seen.equals("false") && !dataSnapshot.child("unRead").getValue().equals("")){
                                                notification2(userName,message,list_user_id);
                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
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


    }

    public void notification(String userName, String message,String list_user_id){

        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
        chatIntent.putExtra("from_user_id", list_user_id);
        chatIntent.putExtra("user_name", userName);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(),0,chatIntent,0);

        NotificationCompat.Builder notivicationBuilder =  new NotificationCompat.Builder(getContext())
                .setTicker("New messages")
                .setSmallIcon(R.drawable.trychat_smallicon)
                .setColor(Color.GREEN)
                .setShowWhen(true)
                .setContentTitle(userName)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);//點擊後自動清除通知

        notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,notivicationBuilder.build());
    }

    public void notification2(String userName, String message,String list_user_id){
        notificationManager = (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel("1","name",NotificationManager.IMPORTANCE_HIGH);

        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
        chatIntent.putExtra("from_user_id", list_user_id);
        chatIntent.putExtra("user_name", userName);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(),0,chatIntent,0);

        //创建一个启动广播的Intent
        Intent quickIntent = new Intent();
        quickIntent.setAction("quick.reply.input");

        Notification.Builder builder = new Notification.Builder(getContext())
                .setSmallIcon(R.drawable.trychat_smallicon)
                .setColor(Color.GREEN)
                .setShowWhen(true)
                .setContentTitle(userName)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)//點擊後自動清除通知
                .setChannelId("1");
                /*.setFullScreenIntent(PendingIntent.getActivity(getActivity().getBaseContext(), 1,
                        quickIntent,PendingIntent.FLAG_ONE_SHOT), true)
                .addAction(
                        new Notification.Action.Builder(
                                null,
                                "Reply",
                                PendingIntent.getBroadcast(getActivity().getBaseContext(), 1, quickIntent,
                                        PendingIntent.FLAG_ONE_SHOT))
                                //直接回复输入框，quick_notification_reply是key
                                .addRemoteInput(new RemoteInput.Builder("quick_notification_reply")
                                        .setLabel("Please input here!").build())
                                .setAllowGeneratedReplies(true)
                                .build());*/


        notificationManager.createNotificationChannel(notificationChannel);
        notificationManager.notify(1,builder.build());
    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen){

            TextView userStatusView = mView.findViewById(R.id.user_single_status);
            userStatusView.setText(message);

            if(!isSeen){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

        }

        public void setName(String name){

            TextView userNameView = mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setUserImage(String thumb_image, Context ctx){
            CircleImageView userImageView = mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
        }

        public void setUserOnline(String online_status) {
            ImageView userOnlineView = mView.findViewById(R.id.user_single_online_icon);
            if(online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }

        public void setUserUnReadMessage(String unread_count){
            TextView unReadCount = mView.findViewById(R.id.user_single_unread_count);
            if (unread_count.equals("")){
                unReadCount.setVisibility(View.GONE);
            }else{
                unReadCount.setVisibility(View.VISIBLE);
                unReadCount.setText(unread_count);
            }
        }
    }

}
