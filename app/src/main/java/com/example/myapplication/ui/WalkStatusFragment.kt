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
import com.example.myapplication.data.WalkStatus
import com.example.myapplication.databinding.FragmentWalkStatusBinding
import com.example.myapplication.databinding.ItemTimelineBinding

class WalkStatusFragment : Fragment() {
    private var _binding: FragmentWalkStatusBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWalkStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val walkId = arguments?.getString("walkId") ?: return
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnLiveMap.setOnClickListener {
            val walk = DogWalkerApp.instance.repo.walkById(walkId)
            if (walk == null || walk.status == WalkStatus.PENDING || walk.status == WalkStatus.REJECTED) {
                Toast.makeText(requireContext(), "El paseo aún no inicia", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.liveMapFragment, bundleOf("walkId" to walkId))
            }
        }
        DogWalkerApp.instance.repo.walksLive.observe(viewLifecycleOwner) { render(walkId) }
        render(walkId)
    }

    private fun render(walkId: String) {
        val repo = DogWalkerApp.instance.repo
        val walk = repo.walkById(walkId) ?: return
        val first = walk.dogIds.firstOrNull()?.let { repo.dogById(it) }
        binding.avatar.circleAvatar(first?.name ?: "P", first?.avatarColor ?: "#22C55E")
        binding.txtDogs.text = repo.dogNames(walk) + (first?.let { " · ${it.breed}" } ?: "")
        binding.txtWhen.text = "${walk.date}  ${walk.time}  ·  ${walk.durationMin} min"
        binding.txtStatus.text = walk.status.label()
        val hasWalker = !walk.walkerName.isNullOrBlank()
        binding.walkerCard.visible(hasWalker)
        if (hasWalker) {
            binding.walkerAvatar.circleAvatar(walk.walkerName!!, "#16A34A")
            binding.txtWalker.text = walk.walkerName
        }
        bindStep(binding.stepPending, "Pendiente", true)
        bindStep(binding.stepAccepted, "Aceptado", walk.status.ordinal >= WalkStatus.ACCEPTED.ordinal && walk.status != WalkStatus.REJECTED)
        bindStep(binding.stepProgress, "En curso", walk.status == WalkStatus.IN_PROGRESS || walk.status == WalkStatus.COMPLETED)
        bindStep(binding.stepDone, "Finalizado", walk.status == WalkStatus.COMPLETED)
        if (walk.status == WalkStatus.REJECTED) {
            bindStep(binding.stepAccepted, "Rechazado", true)
        }
    }

    private fun bindStep(include: ItemTimelineBinding, label: String, on: Boolean) {
        include.label.text = label
        include.dot.setBackgroundResource(if (on) R.drawable.bg_timeline_dot else R.drawable.bg_timeline_dot_off)
        include.label.setTextColor(requireContext().getColor(if (on) R.color.dw_text else R.color.dw_hint))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
