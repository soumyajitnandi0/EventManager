package com.example.eventmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.room.Room
import com.example.eventmanager.databinding.FragmentEventsBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Events : Fragment() {


    private lateinit var database: EventDatabase
    private lateinit var binding: FragmentEventsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEventsBinding.inflate(inflater,container,false)



        database = Room.databaseBuilder(context,
            EventDatabase::class.java,
            "events_database").build()

        GlobalScope.launch {
            database.eventDAO().insertEvent(Event(0,"HOLI","23/05/2025","23:00","BLR"))
        }

        GlobalScope.launch {
            database.eventDAO().deleteEvent(Event(1,"King","961553294"))
        }

        GlobalScope.launch {
            database.eventDAO().updateEvent(Event(2,"Smith","9999999999"))
        }


    }


}