package com.github.apognu.push

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.github.apognu.push.databinding.ActivityMainBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
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
  private lateinit var notifications: Snackbar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)

    setContentView(binding.root)

    setSupportActionBar(binding.appbar)

    binding.appbarLayout.addOnOffsetChangedListener(
      AppBarLayout.OnOffsetChangedListener { _, offset ->
        if ((binding.toolbarLayout.height + offset) < (appbarOffsetFactor * binding.toolbarLayout.minimumHeight)) {
          tintAppBarIcons(android.R.color.white)
        } else {
          tintAppBarIcons(R.color.colorOnSurface)
        }
      }
    )

    notifications = Snackbar.make(binding.container, resources.getString(R.string.permission_prompt), Snackbar.LENGTH_INDEFINITE).also {
      it.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.notification, 0, 0, 0)

      it.setAction(R.string.permission_button) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
      }
    }
  }

  override fun onResume() {
    super.onResume()

    (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancelAll()

    FirebaseApp.initializeApp(this)

    lifecycleScope.launch(Dispatchers.IO) {
      repository.allOnce().forEach {
        Firebase.messaging.subscribeToTopic(it.slug)
      }
    }

    (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment).let { nav ->
      nav.navController.let {
        binding.toolbarLayout.setupWithNavController(
          binding.appbar,
          it,
          AppBarConfiguration(it.graph)
        )
        binding.bottombar.setupWithNavController(it)
      }
    }

    if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
      notifications.dismiss()
    } else {
      notifications.show()
    }
  }

  @SuppressLint("RestrictedApi")
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
