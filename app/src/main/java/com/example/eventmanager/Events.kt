package com.example.eventmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.eventmanager.databinding.FragmentEventsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Events : Fragment() {
    private lateinit var database: EventDatabase
    private lateinit var binding: FragmentEventsBinding
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize database
        database = Room.databaseBuilder(
            requireContext(),
            EventDatabase::class.java,
            "events_database"
        ).build()

        // Setup RecyclerView
        eventAdapter = EventAdapter(emptyList())
        binding.notesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventAdapter
        }

        // Setup Add Button
        binding.addButton.setOnClickListener {
            addSampleEvent()
        }

        // Observe events
        observeEvents()
    }

    private fun observeEvents() {
        database.eventDAO().getAllEvents().observe(viewLifecycleOwner) { events ->
            eventAdapter.updateEvents(events)

            // Show/hide empty view based on events
            binding.emptyView.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun addSampleEvent() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val newEvent = Event(
                    id = 0, // Room will auto-generate
                    title = "New Event ${System.currentTimeMillis()}",
                    date = "23/05/2025",
                    time = "23:00",
                    location = "BLR"
                )
                database.eventDAO().insertEvent(newEvent)
            }
        }
    }
}