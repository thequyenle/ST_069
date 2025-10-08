package net.android.st069_fakecallphoneprank.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import net.android.st069_fakecallphoneprank.activity.AddFakeCallActivity
import net.android.st069_fakecallphoneprank.adapters.HistoryCallAdapter
import net.android.st069_fakecallphoneprank.data.entity.FakeCall
import net.android.st069_fakecallphoneprank.databinding.FragmentCustomCallsBinding
import net.android.st069_fakecallphoneprank.services.FakeCallScheduler
import net.android.st069_fakecallphoneprank.viewmodel.FakeCallViewModel

class CustomCallsFragment : Fragment() {

    private var _binding: FragmentCustomCallsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FakeCallViewModel by viewModels()
    private lateinit var adapter: HistoryCallAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomCallsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = HistoryCallAdapter(
            showEditButton = true,
            onEditClick = { fakeCall ->
                editFakeCall(fakeCall)
            },
            onCallClick = { fakeCall ->
                triggerFakeCall(fakeCall)
            }
        )

        binding.rvCustomCalls.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCustomCalls.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.allFakeCalls.observe(viewLifecycleOwner) { fakeCalls ->
            if (fakeCalls.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvCustomCalls.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvCustomCalls.visibility = View.VISIBLE
                adapter.submitList(fakeCalls)
            }
        }
    }

    private fun editFakeCall(fakeCall: FakeCall) {
        val intent = Intent(requireContext(), AddFakeCallActivity::class.java).apply {
            putExtra("EDIT_MODE", true)
            putExtra("FAKE_CALL_ID", fakeCall.id)
            putExtra("NAME", fakeCall.name)
            putExtra("PHONE", fakeCall.phoneNumber)
            putExtra("AVATAR", fakeCall.avatar)
            putExtra("VOICE", fakeCall.voiceType)
            putExtra("DEVICE", fakeCall.deviceType)
            putExtra("SET_TIME", fakeCall.setTime)
            putExtra("TALK_TIME", fakeCall.talkTime)
        }
        startActivity(intent)
    }

    private fun triggerFakeCall(fakeCall: FakeCall) {
        val scheduler = FakeCallScheduler(requireContext())

        val updatedCall = fakeCall.copy(
            setTime = 0,
            scheduledTime = System.currentTimeMillis()
        )

        scheduler.scheduleFakeCall(updatedCall)

        Toast.makeText(
            requireContext(),
            "Fake call will trigger shortly",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}