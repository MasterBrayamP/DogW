package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.DogWalkerApp
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentRequestWalkBinding
import com.example.myapplication.databinding.ItemDogSelectBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RequestWalkFragment : Fragment() {
    private var _binding: FragmentRequestWalkBinding? = null
    private val binding get() = _binding!!
    private val selected = mutableSetOf<String>()
    private var duration = 60

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRequestWalkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val app = DogWalkerApp.instance
        val ownerId = app.session.currentUserId() ?: return
        val preselect = arguments?.getString("dogId")
        val dogs = app.repo.dogsOf(ownerId)
        if (preselect != null) selected += preselect else dogs.firstOrNull()?.let { selected += it.id }

        val cal = Calendar.getInstance()
        binding.inputDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        binding.inputTime.text = "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

        dogs.forEach { dog ->
            val item = ItemDogSelectBinding.inflate(layoutInflater, binding.dogsRow, false)
            item.avatar.circleAvatar(dog.name, dog.avatarColor)
            item.name.text = dog.name
            fun refresh() { item.root.isSelected = dog.id in selected }
            refresh()
            item.root.setOnClickListener {
                if (dog.id in selected) selected.remove(dog.id) else selected.add(dog.id)
                refresh()
            }
            binding.dogsRow.addView(item.root)
        }

        fun durationUi() {
            listOf(binding.dur30 to 30, binding.dur45 to 45, binding.dur60 to 60).forEach { (tv, min) ->
                tv.isSelected = duration == min
                tv.setTextColor(requireContext().getColor(if (duration == min) R.color.white else R.color.dw_text))
            }
        }
        val pickDur: (Int) -> View.OnClickListener = { min ->
            View.OnClickListener { duration = min; durationUi() }
        }
        binding.dur30.setOnClickListener(pickDur(30))
        binding.dur45.setOnClickListener(pickDur(45))
        binding.dur60.setOnClickListener(pickDur(60))
        durationUi()

        binding.inputDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker().setTitleText("Fecha del paseo").build()
            picker.addOnPositiveButtonClickListener { millis ->
                binding.inputDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
            }
            picker.show(parentFragmentManager, "date")
        }
        binding.inputTime.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(cal.get(Calendar.HOUR_OF_DAY))
                .setMinute(cal.get(Calendar.MINUTE))
                .setTitleText("Hora")
                .build()
            picker.addOnPositiveButtonClickListener {
                binding.inputTime.text = "%02d:%02d".format(picker.hour, picker.minute)
            }
            picker.show(parentFragmentManager, "time")
        }
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnSubmit.setOnClickListener {
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), "Elige al menos un perro", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val walk = app.repo.createWalk(
                ownerId = ownerId,
                dogIds = selected.toList(),
                date = binding.inputDate.text.toString(),
                time = binding.inputTime.text.toString(),
                durationMin = duration,
                notes = binding.inputNotes.text.toString()
            )
            Toast.makeText(requireContext(), "Solicitud enviada", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.walkStatusFragment, bundleOf("walkId" to walk.id))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
