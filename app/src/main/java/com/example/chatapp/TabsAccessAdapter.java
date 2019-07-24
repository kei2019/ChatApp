package com.example.chatapp;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

public class TabsAccessAdapter extends FragmentPagerAdapter {

    public TabsAccessAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {

            case 0:
                NewsFeedFragment newsFeedFragment = new NewsFeedFragment();
                return newsFeedFragment;
            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;


            case 2:
                ContactsFragments contactsFragments = new ContactsFragments();
                return contactsFragments;

            case 3:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;


            default:
                return null;
        }

    }


    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {

            case 0:
                return "Home";

            case 1:
                return "Chats";


            case 2:
                return "Contacts";

            case 3:
                return "Requests";

            default:
                return null;
        }
    }

}
