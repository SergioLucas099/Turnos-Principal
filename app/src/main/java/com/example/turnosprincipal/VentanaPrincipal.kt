package com.example.turnosprincipal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.turnosprincipal.fragments.ContenidoMultimediaFragment
import com.example.turnosprincipal.fragments.ControlTurnosFragment
import com.example.turnosprincipal.fragments.CrearAtraccionFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class VentanaPrincipal : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ventana_principal)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
           when(menuItem.itemId){
               R.id.firstFragment -> {
                   replaceFragment(CrearAtraccionFragment())
                   true
               }
               R.id.secondFragment -> {
                   replaceFragment(ControlTurnosFragment())
                   true
               }
               R.id.thirdFragment -> {
                   replaceFragment(ContenidoMultimediaFragment())
                   true
               }
               else -> false
           }
        }
        replaceFragment(CrearAtraccionFragment())
    }
    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
    }
}