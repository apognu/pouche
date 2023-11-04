package com.github.apognu.push

import android.app.NotificationManager
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.github.apognu.push.databinding.ActivityMainBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
  companion object {
    const val appbarOffsetFactor = 1.7
  }

  private val repository by lazy { (applicationContext as Pouche).subscriptionRepository }

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)

    setContentView(binding.root)

    setSupportActionBar(binding.appbar)

    findNavController(R.id.nav_host_fragment_activity_main).let {
      binding.toolbarLayout.setupWithNavController(
          binding.appbar,
          it,
          AppBarConfiguration(it.graph)
      )
      binding.bottombar.setupWithNavController(it)
    }

    binding.appbarLayout.addOnOffsetChangedListener(
        AppBarLayout.OnOffsetChangedListener { _, offset ->
          if ((binding.toolbarLayout.height + offset) <
                  (appbarOffsetFactor * ViewCompat.getMinimumHeight(binding.toolbarLayout))
          ) {
            tintAppBarIcons(android.R.color.white)
          } else {
            tintAppBarIcons(R.color.colorOnSurface)
          }
        }
    )
  }

  override fun onResume() {
    super.onResume()

    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()

    FirebaseApp.initializeApp(this)

    lifecycleScope.launch(Dispatchers.IO) {
      repository.allOnce().forEach { Firebase.messaging.subscribeToTopic(it.slug) }
    }
  }

  private fun tintAppBarIcons(color: Int) {
    binding.appbar.navigationIcon?.colorFilter =
        PorterDuffColorFilter(ContextCompat.getColor(this, color), PorterDuff.Mode.SRC_ATOP)

    binding.appbar.children.forEach {
      (it as? ActionMenuView)?.children?.forEach { menuView ->
        (menuView as? ActionMenuItemView)?.compoundDrawables?.forEach { itemView ->
          itemView?.colorFilter =
              PorterDuffColorFilter(ContextCompat.getColor(this, color), PorterDuff.Mode.SRC_ATOP)
        }
      }
    }
  }
}
