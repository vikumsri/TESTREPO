package com.affix.pawsbyaffix;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MessageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessageFragment extends Fragment {


    String firebaseChild ="Users";
    String header = "";
    String Uid;

    RecyclerView recview;
    MessageAdapter adapter;
    MessageAdapter.RecyclerViewClickListener listener;
    FirebaseRecyclerOptions<MessageModel> options;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MessageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MessageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessageFragment newInstance(String param1, String param2) {
        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Uid = Paper.book().read("Uid");
        setOnClickListner();

        recview = (RecyclerView)getView().findViewById(R.id.recview_message);

        LinearLayoutManager linearLayoutManager = (new LinearLayoutManager(getActivity().getApplicationContext()));
        recview.setLayoutManager(linearLayoutManager);

        options =
                new FirebaseRecyclerOptions.Builder<MessageModel>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Chats").child(Uid), MessageModel.class)
                        .build();


        adapter = new MessageAdapter(options,listener);


        recview.setAdapter(adapter);

        new ItemTouchHelper(iteSimpleCallback).attachToRecyclerView(recview);
        recview.setAdapter(adapter);
    
        
    }

    private void setOnClickListner() {
        listener = new MessageAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {

                final String friendname = options.getSnapshots().get(position).getUsername();
                final String frienduid = options.getSnapshots().get(position).getParticipant2();
                final String friendimg = options.getSnapshots().get(position).getProfileImage();

                Paper.book().write("ParticipantName",friendname);
                Paper.book().write("ParticipantId",frienduid);
                Paper.book().write("ParticipantImg",friendimg);

                DatabaseReference dbref2 = FirebaseDatabase.getInstance().getReference().child("Chats").child(Uid).child(frienduid).child("unread");

                dbref2.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            startActivity(new Intent(getActivity().getApplicationContext(), MessageChatActivity.class));
                        }

                    }
                });



            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("start","fragment started");
        adapter.startListening();
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.i("stop","fragment stopped");
        adapter.stopListening();
    }

    ItemTouchHelper.SimpleCallback iteSimpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            //remove the notification
            String chatId = options.getSnapshots().get(viewHolder.getAdapterPosition()).getParticipant2();

            DatabaseReference rootref = FirebaseDatabase.getInstance().getReference().child("Chats").child(Uid).child(chatId);

            rootref.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });

        }
    };

}