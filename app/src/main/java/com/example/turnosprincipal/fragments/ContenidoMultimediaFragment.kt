package com.example.turnosprincipal.fragments

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.turnosprincipal.R
import com.example.turnosprincipal.adapter.ControlMultimediaAdapter
import com.example.turnosprincipal.adapter.VideoAdapter
import com.example.turnosprincipal.model.Multimedia
import com.example.turnosprincipal.network.ApiClient
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.delete
import io.ktor.client.request.forms.*
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.http.*
import kotlinx.coroutines.launch
import java.io.InputStream

class ContenidoMultimediaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_contenido_multimedia,
            container,
            false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)

        val adapter = ControlMultimediaAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = "Videos"
                1 -> tab.text = "Imagenes"
                2 -> tab.text = "Texto"
            }
        }.attach()
    }
}