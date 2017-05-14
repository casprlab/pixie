package org.image.password.trinket.v1;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabPagerAdapter extends FragmentPagerAdapter {

    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
    	
        Bundle bundle = new Bundle();
        String tab = "";
        String description = "";
        int colorResId = 0;
        int animation = 0;
        switch (index) {
            case 0:
                tab = "Set Your Trinket";
                colorResId = R.color.greenm;
                animation = R.drawable.animationsettrinket;
                description = "Put the trinket in the circle and take 3 images of it from the same angle";
                
                break;
            case 1:
                tab = "Confirm Trinket";
                description = "Confirm trinket by using it to login";
                colorResId = R.color.redm;
                animation = R.drawable.animationconfirmtrinket;
                break;
            case 2:
                tab = "Enter Your FIU Credentials";
                colorResId = R.color.purplem;
                description = "Set your MyFIU username and password";
                animation = R.drawable.animationfiu;
                break;
            case 3:
                tab = "Test Your Trinket";
                description = "Next time, log in with your trinket";
                colorResId = R.color.bluem;
                animation = R.drawable.animationtesttrinket;
                break;
            
        }
        bundle.putString("tab",tab);
        bundle.putInt("color", colorResId);
        bundle.putString("description", description);
        bundle.putInt("animationID", animation);
        SwipeTabFragment swipeTabFragment = new SwipeTabFragment();
        swipeTabFragment.setArguments(bundle);
        return swipeTabFragment;
    }

    @Override
    public int getCount() {
        return 4;
    }
}