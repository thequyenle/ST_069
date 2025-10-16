package net.android.st069_fakecallphoneprank.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import net.android.st069_fakecallphoneprank.adapters.HistoryCallAdapter
import net.android.st069_fakecallphoneprank.data.entity.FakeCall
import net.android.st069_fakecallphoneprank.databinding.FragmentAvailableCallsBinding
import net.android.st069_fakecallphoneprank.services.FakeCallScheduler
import net.android.st069_fakecallphoneprank.viewmodel.FakeCallViewModel

class AvailableCallsFragment : Fragment() {

    private var _binding: FragmentAvailableCallsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FakeCallViewModel by viewModels()
    private lateinit var adapter: HistoryCallAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAvailableCallsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = HistoryCallAdapter(
            showEditButton = false, // No edit for history/past calls
            onEditClick = {
                // No edit functionality for past calls
            },
            onCallClick = { fakeCall ->
                triggerFakeCall(fakeCall)
            }
        )

        binding.rvAvailableCalls.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAvailableCalls.adapter = adapter
    }

    private fun setupObservers() {
        // Show PAST/TRIGGERED calls (already called) - This is the HISTORY
        viewModel.pastCalls.observe(viewLifecycleOwner) { fakeCalls ->
            if (fakeCalls.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvAvailableCalls.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvAvailableCalls.visibility = View.VISIBLE
                adapter.submitList(fakeCalls)
            }
        }
    }

    private fun triggerFakeCall(fakeCall: FakeCall) {
        // Trigger the fake call again (re-trigger from history)
        val scheduler = FakeCallScheduler(requireContext())

        val updatedCall = fakeCall.copy(
            setTime = 0,
            scheduledTime = System.currentTimeMillis()
        )

        scheduler.scheduleFakeCall(updatedCall)

        Toast.makeText(
            requireContext(),
            "Triggering fake call from ${fakeCall.name}",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}