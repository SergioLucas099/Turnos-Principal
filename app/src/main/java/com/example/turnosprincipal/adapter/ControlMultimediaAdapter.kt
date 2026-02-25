package com.example.turnosprincipal.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.turnosprincipal.fragments.CrearImagenesFragment
import com.example.turnosprincipal.fragments.CrearTextoFragment
import com.example.turnosprincipal.fragments.CrearVideosFragment

class ControlMultimediaAdapter (fragment: Fragment) :
    FragmentStateAdapter(fragment){

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> CrearVideosFragment()
            1 -> CrearImagenesFragment()
            2 -> CrearTextoFragment()
            else -> CrearVideosFragment()
        }
    }
}