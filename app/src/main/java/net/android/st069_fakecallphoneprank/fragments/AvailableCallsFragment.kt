package net.android.st069_fakecallphoneprank.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import net.android.st069_fakecallphoneprank.R
import net.android.st069_fakecallphoneprank.activity.IncomingCallActivity
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
        setupSwipeToDelete()
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

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition

                    // Remove item from adapter
                    val deletedCall = adapter.removeItem(position)

                    // Show Snackbar with Undo option
                    val snackbar = Snackbar.make(
                        binding.root,
                        getString(R.string.deleted_from_history, deletedCall.name),
                        Snackbar.LENGTH_LONG
                    )

                    snackbar.setAction(getString(R.string.undo)) {
                        // Restore item
                        adapter.restoreItem(deletedCall, position)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.restored, deletedCall.name),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    snackbar.addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            // If not undone, delete from database permanently
                            if (event != DISMISS_EVENT_ACTION) {
                                viewModel.delete(deletedCall)
                            }
                        }
                    })

                    snackbar.show()
                }
            }
        )

        itemTouchHelper.attachToRecyclerView(binding.rvAvailableCalls)
    }

    private fun setupObservers() {
        // Show PAST/TRIGGERED calls (already called) - This is the HISTORY
        viewModel.pastCalls.observe(viewLifecycleOwner) { fakeCalls ->
            if (fakeCalls.isEmpty()) {
                binding.ivEmpty.visibility = View.VISIBLE
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvAvailableCalls.visibility = View.GONE
            } else {
                binding.ivEmpty.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
                binding.rvAvailableCalls.visibility = View.VISIBLE
                adapter.submitList(fakeCalls)
            }
        }
    }

    private fun triggerFakeCall(fakeCall: FakeCall) {
        // Launch IncomingCallActivity directly for immediate call
        val intent = Intent(requireContext(), IncomingCallActivity::class.java).apply {
            putExtra("FAKE_CALL_ID", fakeCall.id)
            putExtra("NAME", fakeCall.name)
            putExtra("PHONE_NUMBER", fakeCall.phoneNumber)
            putExtra("AVATAR", fakeCall.avatar)
            putExtra("VOICE_TYPE", fakeCall.voiceType)
            putExtra("DEVICE_TYPE", fakeCall.deviceType)
            putExtra("TALK_TIME", fakeCall.talkTime)
        }
        startActivity(intent)

        Toast.makeText(
            requireContext(),
            "Re-triggering fake call from ${fakeCall.name}",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}