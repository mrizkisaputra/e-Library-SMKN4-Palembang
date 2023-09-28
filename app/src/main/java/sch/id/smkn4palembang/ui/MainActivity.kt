package sch.id.smkn4palembang.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.GravityCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import sch.id.smkn4palembang.admin.ui.HomeAdminActivity
import sch.id.smkn4palembang.admin.ui.LoginActivity
import sch.id.smkn4palembang.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.homeMenuButton.setOnClickListener { goNavigate(HomeActivity()) }
        binding.adminLoginButton.setOnClickListener { goNavigate(LoginActivity()) }

        playAnimation()

        with(window) {
            enterTransition = Explode()
            exitTransition = Explode()
        }
    }

    private fun playAnimation() {
        binding.apply {
            val animatorLogoImageview =
                ObjectAnimator.ofFloat(logoImageView, View.TRANSLATION_Y, -1000f, 0f).apply {
                    duration = 1200
                    interpolator = DecelerateInterpolator()
                    start()
                }


            val animatorTitle = ObjectAnimator.ofFloat(titleTextview, View.ALPHA, 1f).setDuration(500)
            val animatorSubTitle1 = ObjectAnimator.ofFloat(subtitle1Textview, View.ALPHA, 1f).setDuration(500)
            val animatorSubTitle2 = ObjectAnimator.ofFloat(subTitle2Textview, View.ALPHA, 1f).setDuration(500)
            val animatorAdminLogin = ObjectAnimator.ofFloat(adminLoginButton, View.ALPHA, 1f).setDuration(600)
            val animatorMainMenu = ObjectAnimator.ofFloat(homeMenuButton, View.ALPHA, 1f).setDuration(600)

            val togetherButton = AnimatorSet().apply {
                playTogether(animatorAdminLogin, animatorMainMenu)
            }

            AnimatorSet().apply {
                playSequentially(animatorTitle, animatorSubTitle1, animatorSubTitle2, togetherButton)
                start()
            }


        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            Log.i(TAG, "Current User: ${currentUser.email} ${currentUser.displayName}")

            startActivity(Intent(this, HomeAdminActivity::class.java))
            finish()
        } else {
            Log.i(TAG, "currentUser: $currentUser")
        }
    }

    private fun goNavigate(destination: AppCompatActivity) {
        if (destination is HomeActivity)
            Intent(this, HomeActivity::class.java).apply {
                startActivity(this, ActivityOptionsCompat.makeSceneTransitionAnimation(this@MainActivity).toBundle())
            }

        if (destination is LoginActivity)
            Intent(this, LoginActivity::class.java).apply { startActivity(this) }
    }
}