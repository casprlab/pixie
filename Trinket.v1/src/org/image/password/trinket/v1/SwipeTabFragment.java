package org.image.password.trinket.v1;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SwipeTabFragment extends Fragment {

    private String tab, description;
    private int color, animationId;
    AnimationDrawable animation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        tab = bundle.getString("tab");
        color = bundle.getInt("color");
        animationId = bundle.getInt("animationID");
        description = bundle.getString("description");
        
        
        	
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipe_tab, null);
        TextView tv = (TextView) view.findViewById(R.id.textView);
        tv.setText(tab);
        
        TextView desc = (TextView) view.findViewById(R.id.descriptionTextView);
        desc.setText(description);
        
        view.setBackgroundResource(color);
        
        ImageView animationImageView = (ImageView) view.findViewById(R.id.imageView1);
        animationImageView.setBackgroundResource(animationId);
        animation = (AnimationDrawable) animationImageView.getBackground();
        animation.start();
        
       
        return view;
    }
    
    
   /* @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
          ImageView animationImageView = (ImageView) view.findViewById(R.id.imageView1);
          animationImageView.setBackgroundResource(R.drawable.animationclick1);
          animation = (AnimationDrawable) animationImageView.getBackground();
          animation.start();
          super.onViewCreated(view, savedInstanceState);
    }*/

    
    @Override
    public void onPause() {
          animation.stop();
          super.onPause();
    }

    @Override
    public void onResume() {
          super.onResume();
          animation.start();
    }
}