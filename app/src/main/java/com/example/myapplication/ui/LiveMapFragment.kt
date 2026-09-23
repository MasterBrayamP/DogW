package com.example.myapplication.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.DogWalkerApp
import com.example.myapplication.data.WalkStatus
import com.example.myapplication.databinding.FragmentLiveMapBinding

class LiveMapFragment : Fragment() {
    private var _binding: FragmentLiveMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLiveMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val walkId = arguments?.getString("walkId") ?: return
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        DogWalkerApp.instance.repo.walksLive.observe(viewLifecycleOwner) { render(walkId) }
        render(walkId)
    }

    private fun render(walkId: String) {
        val repo = DogWalkerApp.instance.repo
        val walk = repo.walkById(walkId) ?: return
        binding.txtTitle.text = repo.dogNames(walk)
        binding.mapView.progress = walk.progress
        binding.txtTime.text = formatClock(walk.elapsedSec)
        binding.txtDistance.text = "%.2f km".format(walk.distanceKm)
        binding.txtSpeed.text = "%.1f km/h".format(walk.speedKmh)
        val walker = walk.walkerName ?: "Paseador"
        binding.txtWalker.text = walker
        binding.walkerAvatar.circleAvatar(walker, "#16A34A")
        binding.txtLive.text = when (walk.status) {
            WalkStatus.COMPLETED -> "Paseo finalizado"
            WalkStatus.IN_PROGRESS -> "En movimiento"
            else -> walk.status.label()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
