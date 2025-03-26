package com.example.eventmanager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private final val nTabs:Int= 2;

    override fun getItemCount(): Int {

        return nTabs

    }

    override fun createFragment(position: Int): Fragment {

        when(position){
            0->return Events()
            1->return Profile()
            else->return Events()
        }

    }
}