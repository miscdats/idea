package com.example.idea;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.idea.Controllers.DesignCardAdapter;
import com.example.idea.Types.Design;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mindorks.placeholderview.SwipeDecor;
import com.mindorks.placeholderview.SwipePlaceHolderView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener{

    private DesignCardAdapter designCardAdapter;
    private SwipePlaceHolderView mSwipeView;
    Context context = getContext();

    private static final String TAG = "HomeFragment";

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home, container, false);
        mSwipeView = v.findViewById(R.id.swipeView);
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        int bottomMargin = Utils.dpToPx(160);
        WindowManager windowManager = getActivity().getWindowManager();
        Point windowSize = Utils.getDisplaySize(windowManager);

        mSwipeView.getBuilder()
                .setDisplayViewCount(3)
                .setSwipeDecor(new SwipeDecor()
                        .setViewWidth(windowSize.x)
                        .setViewHeight(windowSize.y - bottomMargin)
                        .setViewGravity(Gravity.TOP)
                        .setPaddingTop(20)
                        .setRelativeScale(0.01f)
                        .setSwipeInMsgLayoutId(R.layout.swipe_right_view)
                        .setSwipeOutMsgLayoutId(R.layout.swipe_left_view));

        // Load from Firestore and add to mSwipeView
        final List<QueryDocumentSnapshot> results = new ArrayList<>();
        final List<Design> designs = new ArrayList<>();
        fetchAllDesignSnapshots(db, results, designs);
        Log.i(TAG, "FETCH COMPLETE");

        v.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwipeView.doSwipe(false);
            }
        });

        v.findViewById(R.id.addBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwipeView.doSwipe(true);
            }
        });

        v.findViewById(R.id.uploadBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), UploadActivity.class)) ;
            }
        });

        return v;
    }

    public interface OnNextClickListener {
        void OnHomeFragmentNextClick(HomeFragment fragment);
    }

    OnNextClickListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (OnNextClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement OnHomeFragmentNextClick");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onClick(View view) {
        if (listener != null) {
            listener.OnHomeFragmentNextClick(this);
        }
    }

    private void fetchAllDesignSnapshots(FirebaseFirestore db,
                                         final List<QueryDocumentSnapshot> results,
                                         final List<Design> designs) {
        Query query = db.collection("pictures");
        designCardAdapter = new DesignCardAdapter(query);

        query.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, document.getId() + " => " + document.getData());
                                results.add(document);
                            }
                            toDesigns(results, designs);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void toDesigns(List<QueryDocumentSnapshot> documentSnapshotList, List<Design> designs) {
        for (QueryDocumentSnapshot doc : documentSnapshotList) {
            String designId = doc.getString("id");
            String tag = doc.getString("tag_id");
            String picUrl = doc.getString("picture_url");
            String textDescription = doc.getString("description");
            Design newDesign = new Design(designId, tag, picUrl, textDescription);
            designs.add(newDesign);
            mSwipeView.addView(new DesignCard(getActivity(), newDesign, mSwipeView));
//            Log.i("How many designs now: ", String.valueOf(designs.size()));
        }
    }

}
