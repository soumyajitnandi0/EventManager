package com.example.eventmanager

import android.content.res.Resources
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {


    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLayout= findViewById(R.id.mytablayout)
        viewPager2= findViewById(R.id.myviewpager)



        viewPager2.adapter= MyAdapter(this)
        TabLayoutMediator(tabLayout,viewPager2){tab,index->

            tab.text= when(index){
                0->{"Events"}
                1->{"Profile"}
                else->{throw Resources.NotFoundException("Position Not Found")}
            }

        }.attach()

    }
}