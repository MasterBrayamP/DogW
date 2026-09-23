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
import com.example.myapplication.databinding.FragmentOwnerHomeBinding
import com.example.myapplication.databinding.ItemDogCardBinding
import com.example.myapplication.databinding.ItemWalkBinding

class OwnerHomeFragment : Fragment() {
    private var _binding: FragmentOwnerHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOwnerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val app = DogWalkerApp.instance
        val user = app.repo.userById(app.session.currentUserId().orEmpty()) ?: return
        binding.txtHello.text = "¡Hola, ${user.name}!"
        binding.btnAddDog.setOnClickListener { findNavController().navigate(R.id.registerDogFragment) }
        binding.navDogs.setOnClickListener { findNavController().navigate(R.id.registerDogFragment) }
        binding.navProfile.setOnClickListener { findNavController().navigate(R.id.profileFragment) }
        binding.btnRequestWalk.setOnClickListener {
            if (app.repo.dogsOf(user.id).isEmpty()) {
                Toast.makeText(requireContext(), "Primero registra un perro", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.requestWalkFragment)
            }
        }
        binding.navHome.setColorFilter(requireContext().getColor(R.color.dw_green))
        binding.navDogs.setColorFilter(requireContext().getColor(R.color.dw_muted))
        app.repo.dogsLive.observe(viewLifecycleOwner) { render() }
        app.repo.walksLive.observe(viewLifecycleOwner) { render() }
        render()
    }

    private fun render() {
        val app = DogWalkerApp.instance
        val ownerId = app.session.currentUserId() ?: return
        val inflater = layoutInflater
        binding.dogsRow.removeAllViews()
        val dogs = app.repo.dogsOf(ownerId)
        if (dogs.isEmpty()) {
            val empty = android.widget.TextView(requireContext()).apply {
                text = "Aún no tienes perros. Toca + Agregar"
                setTextColor(requireContext().getColor(R.color.dw_muted))
            }
            binding.dogsRow.addView(empty)
        } else {
            dogs.forEach { dog ->
                val item = ItemDogCardBinding.inflate(inflater, binding.dogsRow, false)
                item.avatar.circleAvatar(dog.name, dog.avatarColor)
                item.name.text = dog.name
                item.breed.text = dog.breed
                item.rootCard.setOnClickListener {
                    findNavController().navigate(R.id.dogProfileFragment, bundleOf("dogId" to dog.id))
                }
                binding.dogsRow.addView(item.root)
            }
        }

        val walks = app.repo.walksOfOwner(ownerId)
        binding.recentWalks.removeAllViews()
        binding.emptyWalks.visible(walks.isEmpty())
        walks.take(8).forEach { walk -> bindWalk(walk) }
    }

    private fun bindWalk(walk: Walk) {
        val app = DogWalkerApp.instance
        val item = ItemWalkBinding.inflate(layoutInflater, binding.recentWalks, false)
        val first = walk.dogIds.firstOrNull()?.let { app.repo.dogById(it) }
        item.avatar.circleAvatar(first?.name ?: "P", first?.avatarColor ?: "#22C55E")
        item.title.text = app.repo.dogNames(walk)
        item.subtitle.text = "${walk.date} · ${walk.time} · ${walk.durationMin} min"
        item.status.text = walk.status.label()
        item.root.setOnClickListener {
            findNavController().navigate(R.id.walkStatusFragment, bundleOf("walkId" to walk.id))
        }
        binding.recentWalks.addView(item.root)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
