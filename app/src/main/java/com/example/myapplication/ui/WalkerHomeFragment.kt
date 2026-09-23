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
import com.example.myapplication.data.Walk
import com.example.myapplication.data.WalkStatus
import com.example.myapplication.databinding.FragmentWalkerHomeBinding
import com.example.myapplication.databinding.ItemRequestBinding
import com.example.myapplication.databinding.ItemWalkBinding

class WalkerHomeFragment : Fragment() {
    private var _binding: FragmentWalkerHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWalkerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val app = DogWalkerApp.instance
        val user = app.repo.userById(app.session.currentUserId().orEmpty()) ?: return
        binding.txtHello.text = "¡Hola, ${user.name}!"
        binding.navHome.setColorFilter(requireContext().getColor(R.color.dw_green))
        binding.navProfile.setOnClickListener { findNavController().navigate(R.id.profileFragment) }
        app.repo.walksLive.observe(viewLifecycleOwner) { render() }
        render()
    }

    private fun render() {
        val app = DogWalkerApp.instance
        val walkerId = app.session.currentUserId() ?: return
        val pending = app.repo.pendingWalks()
        binding.txtCount.text = "SOLICITUDES DISPONIBLES (${pending.size})"
        binding.requests.removeAllViews()
        binding.empty.visible(pending.isEmpty())
        pending.forEach { walk ->
            val item = ItemRequestBinding.inflate(layoutInflater, binding.requests, false)
            val first = walk.dogIds.firstOrNull()?.let { app.repo.dogById(it) }
            item.avatar.circleAvatar(first?.name ?: "P", first?.avatarColor ?: "#22C55E")
            item.name.text = app.repo.dogNames(walk)
            item.duration.text = "${walk.durationMin} min"
            item.whenText.text = "${walk.date}  ${walk.time}"
            val disability = app.repo.hasDisability(walk)
            item.disabilityBox.visible(disability)
            item.disabilityText.text = app.repo.disabilityText(walk)
            item.btnAccept.setOnClickListener {
                val walker = app.repo.userById(walkerId) ?: return@setOnClickListener
                app.repo.acceptWalk(walk.id, walker)
                Toast.makeText(requireContext(), "Paseo aceptado", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.liveMapFragment, bundleOf("walkId" to walk.id))
            }
            item.btnReject.setOnClickListener {
                app.repo.rejectWalk(walk.id)
                Toast.makeText(requireContext(), "Solicitud rechazada", Toast.LENGTH_SHORT).show()
            }
            binding.requests.addView(item.root)
        }

        binding.myWalks.removeAllViews()
        val mine = app.repo.walks().filter { it.walkerId == walkerId }
        mine.sortedByDescending { it.id }.forEach { walk -> bindMine(walk) }
    }

    private fun bindMine(walk: Walk) {
        val app = DogWalkerApp.instance
        val item = ItemWalkBinding.inflate(layoutInflater, binding.myWalks, false)
        val first = walk.dogIds.firstOrNull()?.let { app.repo.dogById(it) }
        item.avatar.circleAvatar(first?.name ?: "P", first?.avatarColor ?: "#22C55E")
        item.title.text = app.repo.dogNames(walk)
        item.subtitle.text = "${walk.date} · ${walk.durationMin} min"
        item.status.text = walk.status.label()
        item.root.setOnClickListener {
            val dest = if (walk.status == WalkStatus.IN_PROGRESS) R.id.liveMapFragment else R.id.walkStatusFragment
            findNavController().navigate(dest, bundleOf("walkId" to walk.id))
        }
        binding.myWalks.addView(item.root)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
